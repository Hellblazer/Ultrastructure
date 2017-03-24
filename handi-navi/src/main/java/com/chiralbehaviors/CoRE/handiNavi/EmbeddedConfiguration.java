package com.chiralbehaviors.CoRE.handiNavi;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.PRODUCTION;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;

import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chiralbehaviors.CoRE.loader.Loader;
import com.chiralbehaviors.CoRE.phantasm.service.config.PhantasmConfiguration;
import com.chiralbehaviors.CoRE.utils.DbaConfiguration;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.store.ArtifactStoreBuilder;
import io.dropwizard.db.DataSourceFactory;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.PackagePaths;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Credentials;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Net;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Storage;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Timeout;
import ru.yandex.qatools.embed.postgresql.config.DownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;
import ru.yandex.qatools.embed.postgresql.ext.CachedArtifactStoreBuilder;

public class EmbeddedConfiguration extends PhantasmConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedConfiguration.class);

    static {
        try {
            Driver.register();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } catch (Throwable e) {

        }
    }

    private static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException ignored) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException ignored) {
            // Ignore IOException on open
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // Ignore IOException on close()
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Postgresql Server on");
    }

    @Override
    public DataSourceFactory getDatabase() {
        DataSourceFactory database = super.getDatabase();
        try {
            database.setUrl(initializePostgresql());
        } catch (SQLException | IOException | URISyntaxException e) {
            throw new IllegalStateException("Cannot initialize postgres", e);
        }
        return database;
    }

    String initializePostgresql() throws SQLException, IOException,
                                  URISyntaxException {

        String username = System.getenv("UAAS_USERNAME");
        String password = System.getenv("UAAS_PASSWORD");
        if (username == null) {
            username = "core";
        }
        if (password == null) {
            password = "core";
        }
        final Command cmd = Command.Postgres;
        // the cached directory should contain pgsql folder
        final FixedPath cachedDir = new FixedPath(".uaas/postgres");
        ArtifactStoreBuilder download = new CachedArtifactStoreBuilder().defaults(cmd)
                                                                        .tempDir(cachedDir)
                                                                        .download(new DownloadConfigBuilder().defaultsForCommand(cmd)
                                                                                                             .packageResolver(new PackagePaths(cmd,
                                                                                                                                               cachedDir))
                                                                                                             .build());
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(cmd)
                                                                 .artifactStore(download)
                                                                 .build();

        PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getInstance(runtimeConfig);

        log.info("Starting Postgres");
        final PostgresConfig config = new PostgresConfig(PRODUCTION,
                                                         new Net("localhost",
                                                                 findFreePort()),
                                                         new Storage("core"),
                                                         new Timeout(),
                                                         new Credentials(username,
                                                                         password));
        // pass info regarding encoding, locale, collate, ctype, instead of setting global environment settings
        config.getAdditionalInitDbParams()
              .addAll(Arrays.asList("-E", "UTF-8", "--locale=en_US.UTF-8",
                                    "--lc-collate=en_US.UTF-8",
                                    "--lc-ctype=en_US.UTF-8"));
        PostgresExecutable exec = runtime.prepare(config);
        PostgresProcess process = exec.start();
        Runtime.getRuntime()
               .addShutdownHook(new Thread(() -> process.stop(), username));

        String uri = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s",
                                   config.net()
                                         .host(),
                                   config.net()
                                         .port(),
                                   config.storage()
                                         .dbName(),
                                   config.credentials()
                                         .username(),
                                   config.credentials()
                                         .password());

        DbaConfiguration dbaConfig = new DbaConfiguration();
        dbaConfig.coreDb = config.storage()
                                 .dbName();
        dbaConfig.corePort = config.net()
                                   .port();
        dbaConfig.coreServer = config.net()
                                     .host();
        dbaConfig.coreUsername = config.credentials()
                                       .username();
        dbaConfig.corePassword = config.credentials()
                                       .password();
        try {
            new Loader(dbaConfig).bootstrap();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot bootstrap CORE", e);
        }
        return uri;
    }
}
