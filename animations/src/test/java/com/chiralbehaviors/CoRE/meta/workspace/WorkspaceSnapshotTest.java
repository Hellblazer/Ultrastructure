/**
 * (C) Copyright 2014 Chiral Behaviors, LLC. All Rights Reserved
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

package com.chiralbehaviors.CoRE.meta.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Arrays;

import org.jooq.util.postgres.PostgresDSL;
import org.junit.AfterClass;
import org.junit.Test;

import com.chiralbehaviors.CoRE.RecordsFactory;
import com.chiralbehaviors.CoRE.domain.Agency;
import com.chiralbehaviors.CoRE.domain.Product;
import com.chiralbehaviors.CoRE.jooq.enums.ExistentialDomain;
import com.chiralbehaviors.CoRE.jooq.enums.ReferenceType;
import com.chiralbehaviors.CoRE.json.CoREModule;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.models.AbstractModelTest;
import com.chiralbehaviors.CoRE.meta.models.ModelImpl;
import com.chiralbehaviors.CoRE.phantasm.test.Thing1;
import com.chiralbehaviors.CoRE.workspace.WorkspaceSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author hhildebrand
 *
 */
public class WorkspaceSnapshotTest extends AbstractModelTest {

    @AfterClass
    public static void cleanup() {
        try {
            Connection connection = newConnection();
            RecordsFactory.clear(PostgresDSL.using(connection));
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeltaGeneration() throws Exception {
        // This is a test where we have to have the kernel state committed, 
        // due to the use of multiple contexts to cleanly test delta generation
        model.create()
             .configuration()
             .connectionProvider()
             .acquire()
             .commit();
        model.close();

        File version1File = new File(TARGET_TEST_CLASSES, THING_1_JSON);
        File version2File = new File(TARGET_TEST_CLASSES, THING_2_JSON);
        File version2_1File = new File(TARGET_TEST_CLASSES, THING_1_2_JSON);

        try (Model myModel = new ModelImpl(newConnection())) {
            // load version 1
            JsonImporter importer = JsonImporter.manifest(getClass().getResourceAsStream("/thing.json"),
                                                          myModel);
            Product definingProduct = importer.getWorkspace()
                                              .getDefiningProduct();
            WorkspaceSnapshot snapshot = new WorkspaceSnapshot(definingProduct,
                                                               myModel.create());

            try (FileOutputStream os = new FileOutputStream(version1File)) {
                snapshot.serializeTo(os);
            }

        }

        try (Model myModel = new ModelImpl(newConnection())) {
            WorkspaceSnapshot.load(myModel.create(),
                                   Arrays.asList(version1File.toURI()
                                                             .toURL()));

            // load version 2

            JsonImporter importer = JsonImporter.manifest(getClass().getResourceAsStream("/thing.2.def.json"),
                                                          myModel);
            Product definingProduct = importer.getWorkspace()
                                              .getDefiningProduct();
            WorkspaceSnapshot snapshot = new WorkspaceSnapshot(definingProduct,
                                                               myModel.create());
            try (FileOutputStream os = new FileOutputStream(version2File)) {
                snapshot.serializeTo(os);
            }
        }

        try (Model myModel = new ModelImpl(newConnection())) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new CoREModule());
            WorkspaceSnapshot version1;
            WorkspaceSnapshot version2;
            try (InputStream is = new FileInputStream(version1File);) {
                version1 = mapper.readValue(is, WorkspaceSnapshot.class);
            }

            try (InputStream is = new FileInputStream(version2File);) {
                version2 = mapper.readValue(is, WorkspaceSnapshot.class);
            }

            WorkspaceSnapshot delta = version2.deltaFrom(myModel.create(),
                                                         version1);
            try (FileOutputStream os = new FileOutputStream(version2_1File)) {
                delta.serializeTo(os);
            }
            assertEquals(7, delta.getInserts()
                                 .size());
            assertEquals(1, delta.getUpdates()
                                 .size());

            assertNull(myModel.getWorkspaceModel()
                              .getScoped(WorkspaceAccessor.uuidOf(THING_URI)));
            version1.load(myModel.create());
            delta.load(myModel.create());
            WorkspaceScope scope = myModel.getWorkspaceModel()
                                          .getScoped(WorkspaceAccessor.uuidOf(THING_URI));
            assertNotNull(scope);
            Agency theDude = (Agency) scope.lookup(ReferenceType.Existential,
                                                   "TheDude");
            assertNotNull(theDude);
        }

        try (Model myModel = new ModelImpl(newConnection())) {
            model.create()
                 .configuration()
                 .connectionProvider()
                 .acquire();
            assertNull(myModel.getWorkspaceModel()
                              .getScoped(WorkspaceAccessor.uuidOf(THING_URI)));
            WorkspaceSnapshot.load(myModel.create(), version1File.toURI()
                                                                 .toURL());
            WorkspaceScope scope = myModel.getWorkspaceModel()
                                          .getScoped(WorkspaceAccessor.uuidOf(THING_URI));
            assertNotNull(scope);

            WorkspaceSnapshot.load(myModel.create(), version2_1File.toURI()
                                                                   .toURL());
            scope = myModel.getWorkspaceModel()
                           .getScoped(WorkspaceAccessor.uuidOf(THING_URI));
            assertNotNull(scope);
            Agency theDude = (Agency) scope.lookup(ReferenceType.Existential,
                                                   "TheDude");
            assertNotNull(theDude);
        }
    }

    @Test
    public void testUnload() throws Exception {
        JsonImporter importer = JsonImporter.manifest(getClass().getResourceAsStream("/thing.json"),
                                                      model);
        Product definingProduct = importer.getWorkspace()
                                          .getDefiningProduct();

        Thing1 thing1 = model.construct(Thing1.class, ExistentialDomain.Product,
                                        "Freddy", "He always comes back");
        model.getWorkspaceModel()
             .unload(definingProduct);
        try {
            assertNull(model.wrap(Thing1.class, thing1.getRuleform()));
            fail("Thing ontology not unloaded");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
