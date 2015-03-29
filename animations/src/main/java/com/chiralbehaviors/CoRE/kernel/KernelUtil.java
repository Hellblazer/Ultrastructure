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

package com.chiralbehaviors.CoRE.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.chiralbehaviors.CoRE.UuidGenerator;
import com.chiralbehaviors.CoRE.json.CoREModule;
import com.chiralbehaviors.CoRE.workspace.RehydratedWorkspace;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Repository of immutable kernal rules
 *
 * This used to be the standard. Now we use workspaces. However, kernel is a
 * fundamental workspace, and it's needed a lot. Consequently, because of the
 * way we do Java stored procedures, reentrancy requires a new image of the
 * kernel workspace in the context of the entity manager. Sucks to be us.
 *
 * Utilities for the Kerenl
 *
 * @author hhildebrand
 *
 */
public class KernelUtil {

    public static Kernel getKernel(EntityManager em) {
        Kernel kernel;
        try (InputStream is = KernelUtil.class.getResourceAsStream(KernelUtil.KERNEL_WORKSPACE_RESOURCE)) {
            RehydratedWorkspace kernelSnapshot = readKernel(is);
            kernelSnapshot.replaceFrom(em);
            kernel = kernelSnapshot.getAccessor(Kernel.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to rehydrate kernel", e);
        }
        return kernel;
    }

    public static void clear(EntityManager em) throws SQLException {
        em.getTransaction().begin();
        Connection connection = em.unwrap(Connection.class);
        connection.setAutoCommit(false);
        alterTriggers(connection, false);
        ResultSet r = connection.createStatement().executeQuery(KernelUtil.SELECT_TABLE);
        while (r.next()) {
            String table = r.getString("name");
            String query = String.format("DELETE FROM %s", table);
            connection.createStatement().execute(query);
        }
        r.close();
        alterTriggers(connection, true);
        connection.commit();
        em.getTransaction().commit();
    }

    public static Kernel clearAndLoadKernel(EntityManager em)
                                                             throws SQLException,
                                                             IOException {
        clear(em);
        em.getEntityManagerFactory().getCache().evictAll();
        return loadKernel(em);
    }

    public static Kernel loadKernel(EntityManager em) throws IOException {
        return loadKernel(em,
                          KernelUtil.class.getResourceAsStream(KernelUtil.KERNEL_WORKSPACE_RESOURCE));
    }

    public static Kernel loadKernel(EntityManager em, InputStream is)
                                                                     throws IOException {
        em.getTransaction().begin();
        RehydratedWorkspace workspace = rehydrateKernel(is);
        workspace.retarget(em);
        Kernel kernel = workspace.getAccessor(Kernel.class);
        em.getTransaction().commit();
        return kernel;
    }

    private static RehydratedWorkspace readKernel(InputStream is)
                                                                 throws IOException,
                                                                 JsonParseException,
                                                                 JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new CoREModule());
        RehydratedWorkspace workspace = mapper.readValue(is,
                                                         RehydratedWorkspace.class);
        return workspace;
    }

    private static RehydratedWorkspace rehydrateKernel(InputStream is)
                                                                      throws IOException,
                                                                      JsonParseException,
                                                                      JsonMappingException {
        RehydratedWorkspace workspace = readKernel(is);
        workspace.cache();
        return workspace;
    }

    static void alterTriggers(Connection connection, boolean enable)
                                                                    throws SQLException {
        for (String table : new String[] { "ruleform.agency",
                "ruleform.product", "ruleform.location" }) {
            String query = String.format("ALTER TABLE %s %s TRIGGER ALL",
                                         table, enable ? "ENABLE" : "DISABLE");
            connection.createStatement().execute(query);
        }
        ResultSet r = connection.createStatement().executeQuery(KernelUtil.SELECT_TABLE);
        while (r.next()) {
            String table = r.getString("name");
            String query = String.format("ALTER TABLE %s %s TRIGGER ALL",
                                         table, enable ? "ENABLE" : "DISABLE");
            connection.createStatement().execute(query);
        }
        r.close();
    }

    public static final String SELECT_TABLE              = "SELECT table_schema || '.' || table_name AS name FROM information_schema.tables WHERE table_schema='ruleform' AND table_type='BASE TABLE' ORDER BY table_name";

    public static final String ZERO                      = UuidGenerator.toBase64(new UUID(
                                                                                           0,
                                                                                           0));

    static final String        KERNEL_WORKSPACE_RESOURCE = "/kernel-workspace.json";
}
