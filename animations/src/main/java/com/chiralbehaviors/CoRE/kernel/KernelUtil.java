/**
 * (C) Copyright 2012 Chiral Behaviors, LLC. All Rights Reserved
 *
 
 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.kernel;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.DSLContext;

import com.chiralbehaviors.CoRE.kernel.phantasm.agency.CoreInstance;
import com.chiralbehaviors.CoRE.kernel.phantasm.agency.ThisCoreInstance;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.workspace.WorkspaceSnapshot;

/**
 * Utilities for the Kernel
 *
 * @author hhildebrand
 *
 */
public class KernelUtil {

    public static final List<URL> KERNEL_LOADS;
    public static final String    SELECT_TABLE    = "SELECT table_schema || '.' || table_name AS name FROM information_schema.tables WHERE table_schema='ruleform' AND table_type='BASE TABLE' ORDER BY table_name";

    private static final String[] KERNEL_VERSIONS = { "/kernel.2.json" };

    static {
        KERNEL_LOADS = Collections.unmodifiableList(Arrays.asList(KERNEL_VERSIONS)
                                                          .stream()
                                                          .map(s -> KernelUtil.class.getResource(s))
                                                          .collect(Collectors.toList()));
    }

    public static void clear(DSLContext em) throws SQLException {
        boolean committed = false;
        Connection connection = em.configuration()
                                  .connectionProvider()
                                  .acquire();
        try {
            connection.setAutoCommit(false);
            ResultSet r = connection.createStatement()
                                    .executeQuery(KernelUtil.SELECT_TABLE);
            while (r.next()) {
                String table = r.getString("name");
                String query = String.format("TRUNCATE TABLE %s CASCADE",
                                             table);
                connection.createStatement()
                          .execute(query);
            }
            r.close();
            connection.commit();
            committed = true;
        } finally {
            if (!committed) {
                connection.rollback();
            }
        }
    }

    public static void clearAndLoadKernel(DSLContext em) throws SQLException,
                                                         IOException {
        clear(em);
        loadKernel(em);
    }

    public static void initializeInstance(Model model, String name,
                                          String description) throws InstantiationException {
        ThisCoreInstance core = model.construct(ThisCoreInstance.class, name,
                                                description);
        model.apply(CoreInstance.class, core);
    }

    public static void loadKernel(DSLContext em) throws IOException {
        em.transaction(config -> WorkspaceSnapshot.load(em, KERNEL_LOADS));
    }
}
