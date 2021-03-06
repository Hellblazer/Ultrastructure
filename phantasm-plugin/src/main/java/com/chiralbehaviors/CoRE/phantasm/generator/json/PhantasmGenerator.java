/**
 * Copyright (c) 2018 Chiral Behaviors, LLC, all rights reserved.
 *

 *  This file is part of Ultrastructure.
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

package com.chiralbehaviors.CoRE.phantasm.generator.json;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import com.chiralbehaviors.CoRE.meta.workspace.json.Constraint;
import com.chiralbehaviors.CoRE.meta.workspace.json.Facet;
import com.chiralbehaviors.CoRE.meta.workspace.json.JsonWorkspace;
import com.chiralbehaviors.CoRE.utils.English;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellblazer.utils.Utils;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

/**
 * @author halhildebrand
 *
 */
public class PhantasmGenerator {
    private static final String ADD_S               = "add%s";
    private static final String ANNOTATIONS         = "com.chiralbehaviors.CoRE.phantasm.java.annotations.";
    private static final String CHILD               = "child";
    private static final String EDGE_ANNOTATION;
    private static final String EDGE_PROPERTIES_ANNOTATION;
    private static final String FACET_ANNOTATION;
    private static final String FIELD_NAME          = "fieldName";
    private static final String GET_EDGE_PROPERTIES = "get_PropertiesOf%s";
    private static final String GET_IMMEDIATE_S     = "getImmediate%s";
    private static final String GET_PROPERTIES      = "get_Properties";
    private static final String GET_S               = "get%s";
    private static final String INFERRED_ANNOTATION;
    private static final String PROPERTIES_ANNOTATION;
    private static final String REMOVE_S            = "remove%s";
    private static final String SCOPED_PHANTASM;
    private static final String SET_EDGE_PROPERTIES = "set_PropertiesOf%s";
    private static final String SET_IMMEDIATE_S     = "setImmediate%s";
    private static final String SET_PROPERTIES      = "set_Properties";
    private static final String SET_S               = "set%s";
    private static final String WRAPPED_CHILD_TYPE  = "wrappedChildType";

    static {
        INFERRED_ANNOTATION = ANNOTATIONS + "Inferred";
        PROPERTIES_ANNOTATION = ANNOTATIONS + "Properties";
        EDGE_ANNOTATION = ANNOTATIONS + "Edge";
        EDGE_PROPERTIES_ANNOTATION = ANNOTATIONS + "EdgeProperties";
        FACET_ANNOTATION = ANNOTATIONS + "Facet";
        SCOPED_PHANTASM = "com.chiralbehaviors.CoRE.phantasm.ScopedPhantasm";
    }

    public static String capitalized(String baseName) {
        return Character.toUpperCase(baseName.charAt(0))
               + (baseName.length() == 1 ? "" : baseName.substring(1));
    }

    public static String toFieldName(String name) {
        return Introspector.decapitalize(name.replaceAll("\\s", ""));
    }

    public static String toTypeName(String name) {
        char chars[] = toValidName(name).toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String toValidName(String name) {
        name = name.replaceAll("\\s", "");
        StringBuilder sb = new StringBuilder();
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            sb.append("_");
        }
        for (char c : name.toCharArray()) {
            if (!Character.isJavaIdentifierPart(c)) {
                sb.append("_");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private final Configuration config;

    private final JsonWorkspace workspace;

    public PhantasmGenerator(Configuration config) {
        InputStream input;
        try {
            input = Utils.resolveResource(getClass(), config.resource);
        } catch (Exception  e) {
            throw new IllegalArgumentException("Cannot resolve resource: "
                                               + config.resource, e);
        }
        if (input == null) {
            throw new IllegalArgumentException("Cannot resolve resource: "
                                               + config.resource);
        }
        try {
            this.workspace = new ObjectMapper().readerFor(JsonWorkspace.class)
                                               .readValue(input);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot deserialize json resource: "
                                               + config.resource, e);
        }
        this.config = config;
    }

    public void generate() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        generate(codeModel);
        config.outputDirectory.mkdirs();
        codeModel.build(config.outputDirectory);
    }

    public void generate(JCodeModel codeModel) {
        JPackage jpackage = codeModel._package(config.packageName);
        workspace.facets.forEach((name, facet) -> generate(name, facet,
                                                           jpackage,
                                                           codeModel));
    }

    private void generate(String name, Constraint constraint, JPackage jpackage,
                          JDefinedClass jClass, JCodeModel codeModel) {
        generateAttributes(name, constraint, jClass, codeModel);
        switch (constraint.card) {
            case MANY:
                generateMany(name, constraint, jClass, codeModel);
                break;
            case ONE:
                generateOne(name, constraint, jClass, codeModel);
                break;
            default:

        }
    }

    private void generate(String name, Facet facet, JPackage jpackage,
                          JCodeModel codeModel) {
        JDefinedClass jClass;
        try {
            jClass = jpackage._class(JMod.PUBLIC, toValidName(name),
                                     ClassType.INTERFACE);
        } catch (JClassAlreadyExistsException e) {
            throw new IllegalStateException(String.format("Facet %s has already been defined",
                                                          name));
        }
        JDefinedClass scopedPhantasm;
        try {
            scopedPhantasm = codeModel._class(SCOPED_PHANTASM,
                                              ClassType.INTERFACE);
            scopedPhantasm.hide();
        } catch (JClassAlreadyExistsException e) {
            scopedPhantasm = codeModel._getClass(SCOPED_PHANTASM);
        }
        jClass._extends(scopedPhantasm);
        JAnnotationUse facetAnnotation = jClass.annotate(jClass.owner()
                                                               .ref(FACET_ANNOTATION));
        facetAnnotation.param("key", name);
        facetAnnotation.param("workspace", workspace.uri);
        facet.constraints.forEach((n, constraint) -> generate(n, constraint,
                                                              jpackage, jClass,
                                                              codeModel));
        generateAttributes(name, facet, jClass, jpackage, codeModel);
    }

    private void generateAttributes(String name, Constraint constraint,
                                    JDefinedClass jClass,
                                    JCodeModel codeModel) {
        JPackage propPackage = jClass.getPackage()
                                     .subPackage(Introspector.decapitalize(jClass.name()
                                                                           + "EdgeProperties"))
                                     .subPackage(toFieldName(name) + "_");
        JClass propType;
        if (constraint.schema == null) {
            propType = codeModel.ref(JsonNode.class.getCanonicalName());
        } else {
            GenerationConfig config = new DefaultGenerationConfig() {
                @Override
                public AnnotationStyle getAnnotationStyle() {
                    return AnnotationStyle.JACKSON2;
                }

                @Override
                public boolean isGenerateBuilders() { // set config option by overriding method
                    return true;
                }

                @Override
                public boolean isIncludeHashcodeAndEquals() {
                    return false;
                }

                @Override
                public boolean isIncludeToString() {
                    return false;
                }
            };
            String propName = capitalized(toValidName(name + "Properties"));
            SchemaMapper mapper = new SchemaMapper(new RuleFactory(config,
                                                                   new Jackson2Annotator(config),
                                                                   new SchemaStore()),
                                                   new SchemaGenerator());
            try {
                mapper.generate(codeModel, propName, propPackage.name(),
                                constraint.schema.toString());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            propType = jClass.owner()
                             .ref(propPackage.name() + "." + propName);
        }

        String fieldName = toFieldName(name);
        String capitalized = capitalized(fieldName);
        JClass childType = jClass.owner()
                                 .ref(resolveChild(constraint.child));
        JMethod get = jClass.method(JMod.PUBLIC, propType,
                                    String.format(GET_EDGE_PROPERTIES,
                                                  capitalized));
        get.annotate(jClass.owner()
                           .ref(EDGE_PROPERTIES_ANNOTATION))
           .param(FIELD_NAME, name);
        get.param(childType, CHILD);

        JMethod set = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                    String.format(SET_EDGE_PROPERTIES,
                                                  capitalized));
        set.annotate(jClass.owner()
                           .ref(EDGE_PROPERTIES_ANNOTATION))
           .param(FIELD_NAME, name);
        set.param(childType, CHILD);
        set.param(propType, "properties");
    }

    private void generateAttributes(String name, Facet facet,
                                    JDefinedClass jClass, JPackage jpackage,
                                    JCodeModel codeModel) {
        JPackage propPackage = jpackage.subPackage(toFieldName(name
                                                               + "Properties"));
        JClass propType;
        if (facet.schema == null) {
            propType = codeModel.ref(JsonNode.class.getCanonicalName());
        } else {
            GenerationConfig config = new DefaultGenerationConfig() {
                @Override
                public AnnotationStyle getAnnotationStyle() {
                    return AnnotationStyle.JACKSON2;
                }

                @Override
                public boolean isGenerateBuilders() { // set config option by overriding method
                    return true;
                }

                @Override
                public boolean isIncludeHashcodeAndEquals() {
                    return false;
                }

                @Override
                public boolean isIncludeToString() {
                    return false;
                }
            };
            String propName = capitalized(toValidName(name + "Properties"));
            SchemaMapper mapper = new SchemaMapper(new RuleFactory(config,
                                                                   new Jackson2Annotator(config),
                                                                   new SchemaStore()),
                                                   new SchemaGenerator());
            try {
                mapper.generate(codeModel, propName, propPackage.name(),
                                facet.schema.toString());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            propType = jClass.owner()
                             .ref(propPackage.name() + "." + propName);
        }
        JMethod get = jClass.method(JMod.PUBLIC, propType, GET_PROPERTIES);
        get.annotate(jClass.owner()
                           .ref(PROPERTIES_ANNOTATION));

        JMethod set = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                    SET_PROPERTIES);
        set.annotate(jClass.owner()
                           .ref(PROPERTIES_ANNOTATION));
        set.param(propType, "properties");
    }

    private void generateInferedList(String fieldName, JClass listType,
                                     JDefinedClass jClass, String plural,
                                     String pluralParameter, JClass childType,
                                     JCodeModel codeModel) {
        JMethod getInfered = jClass.method(JMod.PUBLIC, listType,
                                           String.format(GET_S, plural));
        getInfered.annotate(jClass.owner()
                                  .ref(EDGE_ANNOTATION))
                  .param(FIELD_NAME, fieldName)
                  .param(WRAPPED_CHILD_TYPE, childType);
        getInfered.annotate(jClass.owner()
                                  .ref(INFERRED_ANNOTATION));

        jClass.method(JMod.PUBLIC, listType,
                      String.format(GET_IMMEDIATE_S, plural))
              .annotate(jClass.owner()
                              .ref(EDGE_ANNOTATION))
              .param(FIELD_NAME, fieldName)
              .param(WRAPPED_CHILD_TYPE, childType);

        JMethod setter = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                       String.format(SET_IMMEDIATE_S, plural));
        setter.annotate(jClass.owner()
                              .ref(EDGE_ANNOTATION))
              .param(FIELD_NAME, fieldName)
              .param(WRAPPED_CHILD_TYPE, childType);
        setter.param(listType, "x");
    }

    private void generateList(String name, JDefinedClass jClass,
                              JClass listType, String plural,
                              String pluralParameter, JClass childType,
                              JCodeModel codeModel) {
        jClass.method(JMod.PUBLIC, listType, String.format(GET_S, plural))
              .annotate(jClass.owner()
                              .ref(EDGE_ANNOTATION))
              .param(FIELD_NAME, name)
              .param(WRAPPED_CHILD_TYPE, childType);

        JMethod setter = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                       String.format(SET_S, plural));
        setter.annotate(jClass.owner()
                              .ref(EDGE_ANNOTATION))
              .param(FIELD_NAME, name)
              .param(WRAPPED_CHILD_TYPE, childType);
        setter.param(listType, "x");
    }

    private void generateListCommon(String fieldName, JClass listType,
                                    JClass childType, String plural,
                                    String pluralParameter, String normalized,
                                    JDefinedClass jClass, JCodeModel codeModel,
                                    String name) {
        JMethod add = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                    String.format(ADD_S, normalized));
        add.annotate(jClass.owner()
                           .ref(EDGE_ANNOTATION))
           .param(FIELD_NAME, name)
           .param(WRAPPED_CHILD_TYPE, childType);
        add.param(childType, "x");

        JMethod remove = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                       String.format(REMOVE_S, normalized));
        remove.annotate(jClass.owner()
                              .ref(EDGE_ANNOTATION))
              .param(FIELD_NAME, name)
              .param(WRAPPED_CHILD_TYPE, childType);
        remove.param(childType, "x");

        JMethod addList = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                        String.format(ADD_S, plural));
        addList.annotate(jClass.owner()
                               .ref(EDGE_ANNOTATION))
               .param(FIELD_NAME, name)
               .param(WRAPPED_CHILD_TYPE, childType);
        addList.param(listType, "x");

        JMethod removeList = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                           String.format(REMOVE_S, plural));
        removeList.annotate(jClass.owner()
                                  .ref(EDGE_ANNOTATION))
                  .param(FIELD_NAME, name)
                  .param(WRAPPED_CHILD_TYPE, childType);
        removeList.param(listType, "x");
    }

    private void generateMany(String name, Constraint constraint,
                              JDefinedClass jClass, JCodeModel codeModel) {
        String fieldName = toFieldName(name);
        String normalized = capitalized(fieldName);
        String plural = English.plural(normalized);
        String pluralParameter = English.plural(fieldName);
        JClass childType = jClass.owner()
                                 .ref(resolveChild(constraint.child));
        JClass listType = jClass.owner()
                                .ref(List.class)
                                .narrow(childType);

        if (constraint.infered) {
            generateInferedList(fieldName, listType, jClass, plural,
                                pluralParameter, childType, codeModel);
        } else {
            generateList(name, jClass, listType, plural, pluralParameter,
                         childType, codeModel);
        }
        generateListCommon(fieldName, listType, childType, plural,
                           pluralParameter, normalized, jClass, codeModel,
                           name);
    }

    private void generateOne(String name, Constraint constraint,
                             JDefinedClass jClass, JCodeModel codeModel) {
        String fieldName = toFieldName(name);
        String capitalized = capitalized(fieldName);
        JClass childType = jClass.owner()
                                 .ref(resolveChild(constraint.child));
        jClass.method(JMod.PUBLIC, childType, String.format(GET_S, capitalized))
              .annotate(jClass.owner()
                              .ref(EDGE_ANNOTATION))
              .param(FIELD_NAME, name)
              .param(WRAPPED_CHILD_TYPE, childType);

        JMethod set = jClass.method(JMod.PUBLIC, jClass.owner().VOID,
                                    String.format(SET_S, capitalized));
        set.annotate(jClass.owner()
                           .ref(EDGE_ANNOTATION))
           .param(FIELD_NAME, name)
           .param(WRAPPED_CHILD_TYPE, childType);
        set.param(childType, "x");
    }

    private String resolveChild(String child) {
        String[] split = child.split("::");
        String namespace = split.length == 2 ? split[0] : null;
        String name = split.length == 2 ? split[1] : split[0];

        if (namespace != null) {
            String pkg = config.namespacePackages.get(split[0]);
            if (pkg != null) {
                return pkg + "." + toTypeName(name);
            }
            throw new IllegalArgumentException("Cannot resolve facet: " + child
                                               + " in workspace");
        }
        return config.packageName + "." + toTypeName(name);
    }
}
