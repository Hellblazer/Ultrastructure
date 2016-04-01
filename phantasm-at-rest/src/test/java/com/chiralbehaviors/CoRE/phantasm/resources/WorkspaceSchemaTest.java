/**
 * Copyright (c) 2015 Chiral Behaviors, LLC, all rights reserved.
 * 
 
 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.phantasm.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.WebApplicationException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.chiralbehaviors.CoRE.domain.Product;
import com.chiralbehaviors.CoRE.jooq.enums.ExistentialDomain;
import com.chiralbehaviors.CoRE.kernel.phantasm.product.Argument;
import com.chiralbehaviors.CoRE.kernel.phantasm.product.Constructor;
import com.chiralbehaviors.CoRE.kernel.phantasm.product.InstanceMethod;
import com.chiralbehaviors.CoRE.kernel.phantasm.product.Plugin;
import com.chiralbehaviors.CoRE.kernel.phantasm.product.Workspace;
import com.chiralbehaviors.CoRE.phantasm.graphql.FacetType;
import com.chiralbehaviors.CoRE.phantasm.model.PhantasmCRUD;
import com.chiralbehaviors.CoRE.phantasm.service.PhantasmBundle;
import com.chiralbehaviors.CoRE.phantasm.test.location.MavenArtifact;
import com.chiralbehaviors.CoRE.phantasm.test.product.Thing1;
import com.chiralbehaviors.CoRE.phantasm.test.product.Thing2;
import com.chiralbehaviors.CoRE.phantasm.test.product.Thing3;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

/**
 * @author hhildebrand
 *
 */
public class WorkspaceSchemaTest extends ThingWorkspaceTest {

    private static final String COM_CHIRALBEHAVIORS_CO_RE_PHANTASM_PLUGIN_TEST = "com.chiralbehaviors.CoRE.phantasm.plugin.test";

    private static ClassLoader  executionScope;

    @BeforeClass
    public static void buildExecutionScope() {
        executionScope = PhantasmBundle.configureExecutionScope(Collections.singletonList("target/test-plugin.jar"));
    }

    @Test
    public void testCasting() throws Exception {
        Thing1 thing1 = model.construct(Thing1.class, ExistentialDomain.Product,
                                        "test", "testy");
        Thing2 thing2 = model.construct(Thing2.class, ExistentialDomain.Product,
                                        "tester", "testier");
        Thing3 thing3 = model.construct(Thing3.class, ExistentialDomain.Product,
                                        "Thingy", "a favorite thing");
        thing1.setThing2(thing2);
        thing2.addThing3(thing3);

        GraphQlResource resource = new GraphQlResource(getClass().getClassLoader());
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", thing1.getRuleform()
                                  .getId()
                                  .toString());
        variables.put("thing3", thing3.getRuleform()
                                      .getId()
                                      .toString());
        QueryRequest request = new QueryRequest("mutation m($id: String!, $thing3: String!) { UpdateThing1(state: { id: $id, setThing2: $thing3}) { name } }",
                                                variables);
        ExecutionResult result = resource.query(null, THING_URI, request);
        assertNotNull(result);

        assertEquals(result.getErrors()
                           .toString(),
                     1, result.getErrors()
                              .size());
        assertTrue(result.getErrors()
                         .get(0)
                         .getMessage()
                         .contains("ClassCastException"));
    }

    @Test
    public void testGraphQlResource() throws Exception {
        Thing1 thing1 = model.construct(Thing1.class, ExistentialDomain.Product,
                                        "test", "testy");
        Thing2 thing2 = model.construct(Thing2.class, ExistentialDomain.Product,
                                        "tester", "testier");
        Thing3 thing3 = model.construct(Thing3.class, ExistentialDomain.Product,
                                        "Thingy", "a favorite thing");
        MavenArtifact artifact = model.construct(MavenArtifact.class,
                                                 ExistentialDomain.Location,
                                                 "model", "model artifact");
        artifact.setArtifactID("com.chiralbehaviors.CoRE");
        artifact.setArtifactID("model");
        artifact.setVersion("0.0.2-SNAPSHOT");
        artifact.setType("jar");

        MavenArtifact artifact2 = model.construct(MavenArtifact.class,
                                                  ExistentialDomain.Location,
                                                  "animations",
                                                  "animations artifact");
        artifact2.setArtifactID("com.chiralbehaviors.CoRE");
        artifact2.setArtifactID("animations");
        artifact2.setVersion("0.0.2-SNAPSHOT");
        artifact2.setType("jar");

        thing1.setAliases(new String[] { "smith", "jones" });
        String uri = "http://example.com";
        thing1.setURI(uri);
        thing1.setDerivedFrom(artifact);
        thing1.setThing2(thing2);
        thing2.addThing3(thing3);

        thing3.addDerivedFrom(artifact);
        thing3.addDerivedFrom(artifact2);

        GraphQlResource resource = new GraphQlResource(getClass().getClassLoader());
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", thing1.getRuleform()
                                  .getId()
                                  .toString());
        QueryRequest request = new QueryRequest("query it($id: String!) { Thing1(id: $id) {id name thing2 {id name thing3s {id name  derivedFroms {id name}}} derivedFrom {id name}}}",
                                                variables);
        ExecutionResult result;
        try {
            result = resource.query(null, THING_URI, request);
        } catch (WebApplicationException e) {
            fail(e.getResponse()
                  .getEntity()
                  .toString());
            return;
        }
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        Map<String, Object> thing1Result = (Map<String, Object>) ((Map<String, Object>) result.getData()).get("Thing1");
        assertNotNull(thing1Result);
        assertEquals(thing1.getName(), thing1Result.get("name"));
        assertEquals(thing1.getRuleform()
                           .getId()
                           .toString(),
                     thing1Result.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> thing2Result = (Map<String, Object>) thing1Result.get("thing2");
        assertNotNull(thing2Result);
        assertEquals(thing2.getName(), thing2Result.get("name"));
        assertEquals(thing2.getRuleform()
                           .getId()
                           .toString(),
                     thing2Result.get("id"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> thing3s = (List<Map<String, Object>>) thing2Result.get("thing3s");
        assertNotNull(thing3s);
        assertEquals(1, thing3s.size());
        Map<String, Object> thing3Result = thing3s.get(0);
        assertEquals(thing3.getName(), thing3Result.get("name"));
        assertEquals(thing3.getRuleform()
                           .getId()
                           .toString(),
                     thing3Result.get("id"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlugin() throws Exception {

        Workspace workspace = model.wrap(Workspace.class, scope.getWorkspace()
                                                               .getDefiningProduct());
        workspace.addPlugin(constructPlugin());

        GraphQlResource resource = new GraphQlResource(executionScope);
        Class<?> thing1Plugin = executionScope.loadClass(String.format("%s.Thing1_Plugin",
                                                                       COM_CHIRALBEHAVIORS_CO_RE_PHANTASM_PLUGIN_TEST));
        AtomicReference<String> passThrough = (AtomicReference<String>) thing1Plugin.getField("passThrough")
                                                                                    .get(null);
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "hello");
        String hello = "goodbye";
        variables.put("description", hello);
        QueryRequest request = new QueryRequest("mutation m ($name: String!, $description: String) { "
                                                + "CreateThing1("
                                                + "  state: { "
                                                + "     setName: $name, "
                                                + "     setDescription: $description"
                                                + "   }) { id name description } }",
                                                variables);
        String bob = "Give me food or give me slack or kill me";
        passThrough.set(bob);
        ExecutionResult result = resource.query(null, THING_URI, request);

        assertEquals(result.getErrors()
                           .toString(),
                     0, result.getErrors()
                              .size());

        Map<String, Object> thing1Result = (Map<String, Object>) ((Map<String, Object>) result.getData()).get("CreateThing1");
        assertNotNull(thing1Result);
        assertEquals(bob, thing1Result.get("description"));
        String thing1ID = (String) thing1Result.get("id");
        assertNotNull(thing1ID);
        Thing1 thing1 = model.wrap(Thing1.class, model.create()
                                                      .find(Product.class,
                                                            UUID.fromString(thing1ID)));
        assertEquals(bob, thing1.getDescription());

        String apple = "Connie";
        Thing2 thing2 = model.construct(Thing2.class, ExistentialDomain.Product,
                                        apple, "Her Dobbsness");
        thing1.setThing2(thing2);
        variables = new HashMap<>();
        variables.put("id", thing1ID);
        variables.put("test", "me");
        request = new QueryRequest("query it($id: String!, $test: String) { Thing1(id: $id) {id name instanceMethod instanceMethodWithArgument(arg1: $test) } }",
                                   variables);
        result = resource.query(null, THING_URI, request);

        assertEquals(result.getErrors()
                           .toString(),
                     0, result.getErrors()
                              .size());

        thing1Result = (Map<String, Object>) ((Map<String, Object>) result.getData()).get("Thing1");
        assertNotNull(thing1Result);
        assertEquals(apple, thing1Result.get("instanceMethod"));
        assertEquals("me", passThrough.get());
        assertEquals(apple, thing1Result.get("instanceMethodWithArgument"));
    }

    private Plugin constructPlugin() throws InstantiationException {
        Plugin testPlugin = model.construct(Plugin.class,
                                            ExistentialDomain.Product,
                                            "Test Plugin",
                                            "My super green test plugin");
        testPlugin.setFacetName("Thing1");
        testPlugin.setPackageName(COM_CHIRALBEHAVIORS_CO_RE_PHANTASM_PLUGIN_TEST);
        testPlugin.setConstructor(model.construct(Constructor.class,
                                                  ExistentialDomain.Product,
                                                  "constructor",
                                                  "For all your construction needs"));
        testPlugin.addInstanceMethod(model.construct(InstanceMethod.class,
                                                     ExistentialDomain.Product,
                                                     "instanceMethod",
                                                     "For instance"));
        InstanceMethod methodWithArg = model.construct(InstanceMethod.class,
                                                       ExistentialDomain.Product,
                                                       "instanceMethodWithArgument",
                                                       "For all your argument needs");
        Argument argument = model.construct(Argument.class,
                                            ExistentialDomain.Product, "arg1",
                                            "Who needs an argument?");
        methodWithArg.addArgument(argument);
        argument.setInputType("String");
        testPlugin.addInstanceMethod(methodWithArg);
        return testPlugin;
    }
}
