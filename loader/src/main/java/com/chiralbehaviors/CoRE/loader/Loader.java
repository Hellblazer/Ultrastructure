/** 
 * (C) Copyright 2012 Chiral Behaviors, LLC. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.chiralbehaviors.CoRE.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chiralbehaviors.CoRE.WellKnownObject;
import com.chiralbehaviors.CoRE.kernel.KernelUtil;
import com.hellblazer.utils.Utils;

/**
 * @author hhildebrand
 * 
 */
public class Loader {

    private static final String CREATE_DATABASE_XML                             = "create-database.xml";
    private static final String DROP_DATABASE_SQL                               = "/drop-database.sql";
    private static final String DROP_LIQUIBASE_SQL                              = "/drop-liquibase.sql";
    private static final String DROP_ROLES_SQL                                  = "/drop-roles.sql";
    private static final Logger log                                             = LoggerFactory.getLogger(Loader.class);
    private static final String MODEL_COM_CHIRALBEHAVIORS_CO_RE_SCHEMA_CORE_XML = "model/com/chiralbehaviors/CoRE/schema/core.xml";

    public static void main(String[] argv) throws Exception {
        Loader loader = new Loader(
                                   Configuration.fromYaml(Utils.resolveResource(Loader.class,
                                                                                argv[0])));
        loader.bootstrap();
    }

    private final Configuration configuration;

    public Loader(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    public void bootstrap() throws Exception {
        if (configuration.dropDatabase) {
            dropDatabase();
        }
        createDatabase();
        loadModel();
        bootstrapCoRE();
    }

    private void dropDatabase() throws Exception {
        Connection connection = configuration.getDbaConnection();
        connection.setAutoCommit(true);
        log.info(String.format("Dropping db %s", configuration.coreDb));
        executeWithError(connection,
                         Utils.getDocument(getClass().getResourceAsStream(DROP_DATABASE_SQL)));
        log.info(String.format("Dropping liquibase metadata in db %s",
                               configuration.dbaDb));
        execute(connection,
                Utils.getDocument(getClass().getResourceAsStream(DROP_LIQUIBASE_SQL)));
        log.info(String.format("Dropping roles in db %s", configuration.coreDb));
        execute(connection,
                Utils.getDocument(getClass().getResourceAsStream(DROP_ROLES_SQL)));
    }

    private void executeWithError(Connection connection, String sqlFile)
                                                                        throws SQLException {
        StringTokenizer tokes = new StringTokenizer(sqlFile, ";");
        while (tokes.hasMoreTokens()) {
            String line = tokes.nextToken();
            PreparedStatement exec = connection.prepareStatement(line);
            try {
                exec.execute();
            } finally {
                exec.close();
            }
        }
    }

    private void execute(Connection connection, String sqlFile)
                                                               throws Exception {
        StringTokenizer tokes = new StringTokenizer(sqlFile, ";");
        while (tokes.hasMoreTokens()) {
            String line = tokes.nextToken();
            PreparedStatement exec = connection.prepareStatement(line);
            try {
                exec.execute();
            } catch (SQLException e) {
            } finally {
                exec.close();
            }
        }
    }

    private void load(String changeLog, Connection connection) throws Exception {
        Liquibase liquibase = null;
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(
                                                                                                                   connection));
            liquibase = new Liquibase(
                                      changeLog,
                                      new ClassLoaderResourceAccessor(
                                                                      getClass().getClassLoader()),
                                      database);
            liquibase.update(Integer.MAX_VALUE, "");

        } finally {
            if (liquibase != null) {
                liquibase.forceReleaseLocks();
            }
            try {
                connection.rollback();
                connection.close();
            } catch (SQLException e) {
                //nothing to do
            }
        }
    }

    protected void bootstrapCoRE() throws SQLException, IOException {
        log.info(String.format("Bootstrapping core in db %s",
                               configuration.coreDb));
        String txfmd;
        try (InputStream is = getClass().getResourceAsStream("/jpa.properties")) {
            if (is == null) {
                throw new IllegalStateException("jpa properties missing");
            }
            Map<String, String> props = new HashMap<>();
            props.put("init.db.login", configuration.coreUsername);
            props.put("init.db.password", configuration.corePassword);
            props.put("init.db.server", configuration.coreServer);
            props.put("init.db.port", Integer.toString(configuration.corePort));
            props.put("init.db.database", configuration.coreDb);
            txfmd = Utils.getDocument(is, props);
        }
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(txfmd.getBytes()));
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(WellKnownObject.CORE,
                                                                          properties);
        EntityManager em = emf.createEntityManager();
        KernelUtil.loadKernel(em);
    }

    protected void createDatabase() throws Exception, SQLException {
        log.info(String.format("Creating core db %s", configuration.coreDb));
        load(CREATE_DATABASE_XML, configuration.getDbaConnection());
    }

    protected void loadModel() throws Exception, SQLException {
        log.info(String.format("loading model sql in core db %s",
                               configuration.coreDb));
        load(MODEL_COM_CHIRALBEHAVIORS_CO_RE_SCHEMA_CORE_XML,
             configuration.getCoreConnection());
    }
}
