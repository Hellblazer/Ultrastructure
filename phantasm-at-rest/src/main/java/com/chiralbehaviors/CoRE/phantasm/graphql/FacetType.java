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

package com.chiralbehaviors.CoRE.phantasm.graphql;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.chiralbehaviors.CoRE.ExistentialRuleform;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.attribute.AttributeAuthorization;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.network.NetworkAuthorization;
import com.chiralbehaviors.CoRE.network.NetworkRuleform;
import com.chiralbehaviors.CoRE.network.XDomainNetworkAuthorization;
import com.chiralbehaviors.CoRE.phantasm.model.PhantasmCRUD;
import com.chiralbehaviors.CoRE.phantasm.model.PhantasmTraversal;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

/**
 * Cannonical tranform of Phantasm metadata into GraphQL metadata.
 * 
 * @author hhildebrand
 *
 */
public class FacetType<RuleForm extends ExistentialRuleform<RuleForm, Network>, Network extends NetworkRuleform<RuleForm>>
        implements PhantasmTraversal.PhantasmVisitor<RuleForm, Network> {

    private static final String APPLY_TEMPLATE        = "Apply%s";
    private static final String CREATE_TEMPLATE       = "Create%s";
    private static final String AT_RULEFORM           = "@ruleform";
    private static final String DESCRIPTION           = "description";
    private static final String ID                    = "id";
    private static final String INSTANCES_OF_TEMPLATE = "InstancesOf%s";
    private static final String NAME                  = "name";
    private static final String REMOVE_TEMPLATE       = "Remove%s";
    private static final String STATE                 = "state";
    private static final String UPDATE_TEMPLATE       = "Update%s";
    private static final String UPDATE_TYPE_TEMPLATE  = "%sUpdate";

    private final NetworkAuthorization<RuleForm>                                                          facet;
    private final Model                                                                                   model;
    private graphql.schema.GraphQLInputObjectType.Builder                                                 updateTypeBuilder;
    private final Set<NetworkAuthorization<?>>                                                            references     = new HashSet<>();
    private Builder                                                                                       typeBuilder;
    private final Map<String, BiFunction<PhantasmCRUD<RuleForm, Network>, Map<String, Object>, RuleForm>> updateTemplate = new HashMap<>();

    public FacetType(NetworkAuthorization<RuleForm> facet, Model model) {
        this.model = model;
        this.facet = facet;
        typeBuilder = newObject().name(facet.getName())
                                 .description(facet.getNotes());
        updateTypeBuilder = newInputObject().name(String.format(UPDATE_TYPE_TEMPLATE,
                                                                facet.getName()))
                                            .description(facet.getNotes());
    }

    /**
     * Build the top level queries and mutations
     * 
     * @param query
     *            - top level query
     * @param mutation
     *            - top level mutation
     * @return the references this facet has to other facets.
     */
    public Set<NetworkAuthorization<?>> build(Builder query, Builder mutation) {
        buildRuleformAttributes();
        new PhantasmTraversal<RuleForm, Network>(model).traverse(facet, this);
        GraphQLObjectType type = typeBuilder.build();

        query.field(instance(type));
        query.field(instances());

        mutation.field(createInstance());
        mutation.field(apply());
        mutation.field(update());
        mutation.field(remove());
        return references;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public PhantasmCRUD<RuleForm, Network> ctx(DataFetchingEnvironment env) {
        return (PhantasmCRUD) env.getContext();
    }

    public NetworkAuthorization<RuleForm> getFacet() {
        return facet;
    }

    public String getName() {
        return facet.getName();
    }

    @Override
    public String toString() {
        return String.format("FacetType [name=%s]", getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(AttributeAuthorization<RuleForm, Network> auth,
                      String fieldName) {
        Attribute attribute = auth.getAuthorizedAttribute();
        GraphQLOutputType type = typeOf(attribute);
        typeBuilder.field(newFieldDefinition().type(type)
                                              .name(fieldName)
                                              .description(attribute.getDescription())
                                              .dataFetcher(env -> ctx(env).getAttributeValue((RuleForm) env.getSource(),
                                                                                             auth))
                                              .build());

        String setter = String.format("set%s", capitalized(fieldName));
        GraphQLInputType inputType;
        if (auth.getAuthorizedAttribute()
                .getIndexed()) {
            updateTemplate.put(setter,
                               (crud,
                                update) -> crud.setAttributeValue((RuleForm) update.get(AT_RULEFORM),
                                                                  auth,
                                                                  (List<Object>) update.get(setter)));
            inputType = new GraphQLList(GraphQLString);
        } else if (auth.getAuthorizedAttribute()
                       .getKeyed()) {
            updateTemplate.put(setter,
                               (crud,
                                update) -> crud.setAttributeValue((RuleForm) update.get(AT_RULEFORM),
                                                                  auth,
                                                                  (Map<String, Object>) update.get(setter)));
            inputType = GraphQLString;
        } else {
            updateTemplate.put(setter,
                               (crud,
                                update) -> crud.setAttributeValue((RuleForm) update.get(AT_RULEFORM),
                                                                  auth,
                                                                  (Object) update.get(setter)));
            inputType = new GraphQLList(GraphQLString);
        }
        updateTypeBuilder.field(newInputObjectField().type(inputType)
                                                     .name(setter)
                                                     .description(auth.getNotes())
                                                     .build());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visitChildren(NetworkAuthorization<RuleForm> auth,
                              String fieldName,
                              NetworkAuthorization<RuleForm> child) {
        GraphQLOutputType type = new GraphQLTypeReference(child.getName());
        type = new GraphQLList(type);
        typeBuilder.field(newFieldDefinition().type(type)
                                              .name(fieldName)
                                              .dataFetcher(env -> ctx(env).getChildren((RuleForm) env.getSource(),
                                                                                       auth))
                                              .description(auth.getNotes())
                                              .build());
        String setter = String.format("set%s", capitalized(fieldName));
        updateTypeBuilder.field(newInputObjectField().type(new GraphQLList(GraphQLString))
                                                     .name(setter)
                                                     .description(auth.getNotes())
                                                     .build());
        updateTemplate.put(setter,
                           (crud,
                            update) -> crud.setChildren((RuleForm) update.get(AT_RULEFORM),
                                                        auth,
                                                        (List<RuleForm>) crud.lookup(auth,
                                                                                     (List<String>) update.get(setter))));
        references.add(child);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visitChildren(XDomainNetworkAuthorization<?, ?> auth,
                              String fieldName, NetworkAuthorization<?> child) {
        GraphQLList type = new GraphQLList(new GraphQLTypeReference(child.getName()));
        typeBuilder.field(newFieldDefinition().type(type)
                                              .name(fieldName)
                                              .description(auth.getNotes())
                                              .dataFetcher(env -> ctx(env).getChildren((RuleForm) env.getSource(),
                                                                                       facet,
                                                                                       auth))
                                              .build());
        String setter = String.format("set%s", capitalized(fieldName));
        updateTypeBuilder.field(newInputObjectField().type(new GraphQLList(GraphQLString))
                                                     .name(setter)
                                                     .description(auth.getNotes())
                                                     .build());
        updateTemplate.put(setter,
                           (crud,
                            update) -> crud.setChildren((RuleForm) update.get(AT_RULEFORM),
                                                        facet, auth,
                                                        crud.lookup(child,
                                                                    (List<String>) update.get(setter))));
        references.add(child);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visitSingular(NetworkAuthorization<RuleForm> auth,
                              String fieldName,
                              NetworkAuthorization<RuleForm> child) {
        GraphQLOutputType type = new GraphQLTypeReference(child.getName());
        typeBuilder.field(newFieldDefinition().type(type)
                                              .name(fieldName)
                                              .dataFetcher(env -> ctx(env).getSingularChild((RuleForm) env.getSource(),
                                                                                            auth))
                                              .description(auth.getNotes())
                                              .build());
        String setter = String.format("set%s", capitalized(fieldName));
        updateTypeBuilder.field(newInputObjectField().type(GraphQLString)
                                                     .name(setter)
                                                     .description(auth.getNotes())
                                                     .build());
        updateTemplate.put(setter,
                           (crud,
                            update) -> crud.setSingularChild((RuleForm) update.get(AT_RULEFORM),
                                                             auth,
                                                             (RuleForm) crud.lookup(auth,
                                                                                    (String) update.get(setter))));
        references.add(child);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visitSingular(XDomainNetworkAuthorization<?, ?> auth,
                              String fieldName, NetworkAuthorization<?> child) {
        GraphQLTypeReference type = new GraphQLTypeReference(child.getName());
        typeBuilder.field(newFieldDefinition().type(type)
                                              .name(fieldName)
                                              .description(auth.getNotes())
                                              .dataFetcher(env -> ctx(env).getSingularChild((RuleForm) env.getSource(),
                                                                                            facet,
                                                                                            auth))
                                              .build());
        String setter = String.format("set%s", capitalized(fieldName));
        updateTypeBuilder.field(newInputObjectField().type(GraphQLString)
                                                     .name(setter)
                                                     .description(auth.getNotes())
                                                     .build());
        updateTemplate.put(setter,
                           (crud,
                            update) -> crud.setSingularChild((RuleForm) update.get(AT_RULEFORM),
                                                             facet, auth,
                                                             crud.lookup(child,
                                                                         (String) update.get(setter))));
        references.add(child);
    }

    @SuppressWarnings("unchecked")
    private GraphQLFieldDefinition apply() {
        return newFieldDefinition().name(String.format(APPLY_TEMPLATE,
                                                       facet.getName()))
                                   .type(new GraphQLTypeReference(facet.getName()))
                                   .argument(newArgument().name(ID)
                                                          .description("the id of the instance")
                                                          .type(GraphQLString)
                                                          .build())
                                   .dataFetcher(env -> ctx(env).apply(facet,
                                                                      (RuleForm) ctx(env).lookup(facet,
                                                                                                 (String) env.getArgument(ID))))
                                   .build();
    }

    private String capitalized(String field) {
        char[] chars = field.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    @SuppressWarnings("unchecked")
    private void buildRuleformAttributes() {
        typeBuilder.field(newFieldDefinition().type(GraphQLString)
                                              .name(ID)
                                              .description("The id of the facet instance")
                                              .build());
        typeBuilder.field(newFieldDefinition().type(GraphQLString)
                                              .name(NAME)
                                              .description("The name of the facet instance")
                                              .build());
        typeBuilder.field(newFieldDefinition().type(GraphQLString)
                                              .name(DESCRIPTION)
                                              .description("The description of the facet instance")
                                              .build());

        updateTypeBuilder.field(newInputObjectField().type(GraphQLString)
                                                     .name(ID)
                                                     .description(String.format("the id of the updated %s",
                                                                                facet.getName()))
                                                     .build());

        updateTypeBuilder.field(newInputObjectField().type(GraphQLString)
                                                     .name(NAME)
                                                     .description(String.format("the name to update on %s",
                                                                                facet.getName()))
                                                     .build());
        updateTemplate.put(NAME,
                           (crud,
                            update) -> crud.setName((RuleForm) update.get(AT_RULEFORM),
                                                    (String) update.get(NAME)));

        updateTypeBuilder.field(newInputObjectField().type(GraphQLString)
                                                     .name(DESCRIPTION)
                                                     .description(String.format("the description to update on %s",
                                                                                facet.getName()))
                                                     .build());
        updateTemplate.put(DESCRIPTION,
                           (crud,
                            update) -> crud.setName((RuleForm) update.get(AT_RULEFORM),
                                                    (String) update.get(DESCRIPTION)));
    }

    private GraphQLFieldDefinition createInstance() {
        return newFieldDefinition().name(String.format(CREATE_TEMPLATE,
                                                       facet.getName()))
                                   .type(new GraphQLTypeReference(facet.getName()))
                                   .argument(newArgument().name(NAME)
                                                          .description("the name of the created facet instance")
                                                          .type(GraphQLString)
                                                          .build())
                                   .argument(newArgument().name(DESCRIPTION)
                                                          .description("the optional description of the created facet instance")
                                                          .type(GraphQLString)
                                                          .build())
                                   .dataFetcher(env -> ctx(env).createInstance(facet,
                                                                               env.getArgument(NAME),
                                                                               env.getArgument(DESCRIPTION)))
                                   .build();
    }

    private GraphQLFieldDefinition instance(GraphQLObjectType type) {
        return newFieldDefinition().name(facet.getName())
                                   .type(type)
                                   .argument(newArgument().name(ID)
                                                          .description("id of the facet")
                                                          .type(GraphQLString)
                                                          .build())
                                   .dataFetcher(env -> ctx(env).lookup(facet,
                                                                       (String) env.getArgument(ID)))
                                   .build();
    }

    private GraphQLFieldDefinition instances() {
        return newFieldDefinition().name(String.format(INSTANCES_OF_TEMPLATE,
                                                       facet.getName()))
                                   .type(new GraphQLList(new GraphQLTypeReference(facet.getName())))
                                   .dataFetcher(context -> ctx(context).getInstances(facet))
                                   .build();

    }

    @SuppressWarnings("unchecked")
    private GraphQLFieldDefinition remove() {
        return newFieldDefinition().name(String.format(REMOVE_TEMPLATE,
                                                       facet.getName()))
                                   .type(new GraphQLTypeReference(facet.getName()))
                                   .argument(newArgument().name(ID)
                                                          .description("the id of the instance")
                                                          .type(GraphQLString)
                                                          .build())
                                   .dataFetcher(env -> ctx(env).remove(facet,
                                                                       (RuleForm) ctx(env).lookup(facet,
                                                                                                  (String) env.getArgument(ID)),
                                                                       true))
                                   .build();
    }

    private GraphQLOutputType typeOf(Attribute attribute) {
        GraphQLOutputType type;
        switch (attribute.getValueType()) {
            case BINARY:
                type = GraphQLString; // encoded binary
                break;
            case BOOLEAN:
                type = GraphQLBoolean;
                break;
            case INTEGER:
                type = GraphQLInt;
                break;
            case NUMERIC:
                type = GraphQLFloat;
                break;
            case TEXT:
                type = GraphQLString;
                break;
            case TIMESTAMP:
                type = GraphQLString;
                break;
            default:
                throw new IllegalStateException(String.format("Cannot resolved the value type: %s for %s",
                                                              attribute.getValueType(),
                                                              attribute));
        }
        return attribute.getIndexed() ? new GraphQLList(type) : type;
    }

    @SuppressWarnings("unchecked")
    private GraphQLFieldDefinition update() {
        return newFieldDefinition().name(String.format(UPDATE_TEMPLATE,
                                                       facet.getName()))
                                   .type(new GraphQLTypeReference(facet.getName()))
                                   .argument(newArgument().name(STATE)
                                                          .description("the update state to apply")
                                                          .type(new GraphQLNonNull(updateTypeBuilder.build()))
                                                          .build())
                                   .dataFetcher(env -> {
                                       Map<String, Object> updateState = (Map<String, Object>) env.getArgument(STATE);
                                       RuleForm ruleform = (RuleForm) ctx(env).lookup(facet,
                                                                                      (String) updateState.get(ID));
                                       updateState.put(AT_RULEFORM, ruleform);
                                       for (String field : updateState.keySet()) {
                                           if (!field.equals(ID)
                                               && !field.equals(AT_RULEFORM)
                                               && updateState.containsKey(field)) {
                                               updateTemplate.get(field)
                                                             .apply(ctx(env),
                                                                    updateState);
                                           }
                                       }
                                       return ruleform;
                                   })
                                   .build();
    }
}
