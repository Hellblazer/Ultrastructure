/**
 * Copyright (c) 2016 Chiral Behaviors, LLC, all rights reserved.
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

package com.chiralbehaviors.CoRE.phantasm.graphql;

import static com.chiralbehaviors.CoRE.phantasm.graphql.PhantasmProcessing.object;
import static com.chiralbehaviors.CoRE.phantasm.graphql.PhantasmProcessing.objectBuilder;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLString;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chiralbehaviors.CoRE.domain.Product;
import com.chiralbehaviors.CoRE.jooq.enums.Cardinality;
import com.chiralbehaviors.CoRE.jooq.enums.ReferenceType;
import com.chiralbehaviors.CoRE.jooq.enums.ValueType;
import com.chiralbehaviors.CoRE.jooq.tables.records.FacetRecord;
import com.chiralbehaviors.CoRE.kernel.phantasm.product.Plugin;
import com.chiralbehaviors.CoRE.kernel.phantasm.product.Workspace;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.workspace.WorkspaceAccessor;
import com.chiralbehaviors.CoRE.meta.workspace.dsl.WorkspacePresentation;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.AttributeAuthorizationMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.ChildSequencingMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.ExistentialMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.FacetMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.JobMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.MetaProtocolMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.NetworkAttributeAuthorizationMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.NetworkAuthorizationMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.ParentSequencingMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.ProtocolMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.SelfSequencingMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.SiblingSequencingMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.mutations.StatusCodeSequencingMutations;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.AttributeAuthorizationQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.ChildSequencingQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.ExistentialQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.FacetQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.JobChronologyQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.JobQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.MetaProtocolQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.NetworkAttributeAuthorizationQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.NetworkAuthorizationQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.ParentSequencingQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.ProtocolQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.SelfSequencingQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.SiblingSequencingQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.queries.StatusCodeSequencingQueries;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.AttributeAuthorization;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.ChildSequencing;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Agency;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Attribute;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Interval;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Location;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Relationship;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.StatusCode;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Unit;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Facet;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Job;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.JobChronology;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.MetaProtocol;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.NetworkAttributeAuthorization;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.NetworkAuthorization;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.ParentSequencing;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Protocol;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.SelfSequencing;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.SiblingSequencing;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.StatusCodeSequencing;
import com.chiralbehaviors.CoRE.phantasm.model.PhantasmCRUD;
import com.chiralbehaviors.CoRE.phantasm.model.PhantasmTraversal.Aspect;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.TypeResolver;

/**
 * @author hhildebrand
 *
 */
public class WorkspaceSchema {

    public static class InterfaceProxy extends GraphQLInterfaceType {
        private GraphQLInterfaceType target;

        public InterfaceProxy() {
            super("", "", Collections.emptyList(), o -> null);
        }

        @Override
        public boolean equals(Object obj) {
            return target.equals(obj);
        }

        @Override
        public String getDescription() {
            return target.getDescription();
        }

        @Override
        public GraphQLFieldDefinition getFieldDefinition(String name) {
            return target.getFieldDefinition(name);
        }

        @Override
        public List<GraphQLFieldDefinition> getFieldDefinitions() {
            return target.getFieldDefinitions();
        }

        @Override
        public String getName() {
            return target.getName();
        }

        @Override
        public TypeResolver getTypeResolver() {
            return target.getTypeResolver();
        }

        @Override
        public int hashCode() {
            return target.hashCode();
        }

        public void setTarget(GraphQLInterfaceType target) {
            this.target = target;
        }

        @Override
        public String toString() {
            return target.toString();
        }
    }

    public interface MetaMutations extends ExistentialMutations, FacetMutations,
            AttributeAuthorizationMutations, NetworkAuthorizationMutations,
            ChildSequencingMutations, ParentSequencingMutations,
            SelfSequencingMutations, SiblingSequencingMutations,
            ProtocolMutations, MetaProtocolMutations,
            StatusCodeSequencingMutations,
            NetworkAttributeAuthorizationMutations, JobMutations {
    }

    public interface MetaQueries extends ExistentialQueries, FacetQueries,
            AttributeAuthorizationQueries, NetworkAuthorizationQueries,
            ChildSequencingQueries, ParentSequencingQueries,
            SelfSequencingQueries, SiblingSequencingQueries, ProtocolQueries,
            MetaProtocolQueries, StatusCodeSequencingQueries,
            NetworkAttributeAuthorizationQueries, JobQueries,
            JobChronologyQueries {
    }

    public interface Mutations extends ExistentialMutations, JobMutations {
    }

    public interface Queries
            extends ExistentialQueries, JobQueries, JobChronologyQueries {
    }

    public static class TypeProxy extends GraphQLObjectType {
        private GraphQLObjectType target;

        public TypeProxy() {
            super("", "", Collections.emptyList(), Collections.emptyList());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof GraphQLObjectType)) {
                return false;
            }
            if (obj instanceof TypeProxy) {
                return getTarget().equals(((TypeProxy) obj).getTarget());
            }
            if (!getTarget().equals(obj)) {
                return false;
            }
            return true;
        }

        @Override
        public String getDescription() {
            return getTarget().getDescription();
        }

        @Override
        public GraphQLFieldDefinition getFieldDefinition(String name) {
            return getTarget().getFieldDefinition(name);
        }

        @Override
        public List<GraphQLFieldDefinition> getFieldDefinitions() {
            return getTarget().getFieldDefinitions();
        }

        @Override
        public List<GraphQLInterfaceType> getInterfaces() {
            return getTarget().getInterfaces();
        }

        @Override
        public String getName() {
            return getTarget().getName();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                     + ((getTarget() == null) ? 0 : getTarget().hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "GraphQLObjectType{" + "name='" + getName() + '\''
                   + ", description='" + getDescription() + '\''
                   + ", fieldDefinitions=" + getFieldDefinitions()
                   + ", interfaces=" + getInterfaces() + '}';
        }

        protected void setTarget(GraphQLObjectType target) {
            this.target = target;
        }

        protected GraphQLObjectType getTarget() {
            return target;
        }
    }

    public class CachingTypeProxy extends TypeProxy {
        public CachingTypeProxy(Producer<GraphQLObjectType> fill) {
            this.fill = fill;
        }

        private final Producer<GraphQLObjectType> fill;

        @Override
        protected GraphQLObjectType getTarget() {
            GraphQLObjectType resolved = super.getTarget();
            if (resolved == null) {
                resolved = fill.call();
                setTarget(resolved);
            }
            return resolved;
        }
    }

    public static Model ctx(DataFetchingEnvironment env) {
        return ((PhantasmCRUD) env.getContext()).getModel();
    }

    private final WorkspaceTypeFunction typeFunction = new WorkspaceTypeFunction();

    public WorkspaceSchema() {
    }

    public GraphQLSchema build(WorkspaceAccessor accessor, Model model,
                               ClassLoader executionScope) throws NoSuchMethodException,
                                                           InstantiationException,
                                                           IllegalAccessException {
        Deque<FacetRecord> unresolved = FacetFields.initialState(accessor,
                                                                 model);
        Map<FacetRecord, FacetFields> resolved = new HashMap<>();
        Product definingProduct = accessor.getDefiningProduct();
        Workspace workspace = model.wrap(Workspace.class, definingProduct);
        List<Plugin> plugins = workspace.getPlugins();
        while (!unresolved.isEmpty()) {
            FacetRecord facet = unresolved.pop();
            if (resolved.containsKey(facet)) {
                continue;
            }
            FacetFields type = new FacetFields(facet);
            resolved.put(facet, type);
            List<Plugin> facetPlugins = plugins.stream()
                                               .filter(plugin -> facet.getName()
                                                                      .equals(plugin.getFacetName()))
                                               .collect(Collectors.toList());
            type.resolve(facet, facetPlugins, model, executionScope)
                .stream()
                .filter(auth -> !resolved.containsKey(auth))
                .forEach(auth -> unresolved.add(auth));
        }
        registerTypes(resolved);
        Builder topLevelQuery = objectBuilder(Queries.class, typeFunction,
                                              typeFunction);
        Builder topLevelMutation = objectBuilder(Mutations.class, typeFunction,
                                                 typeFunction);
        GraphQLSchema schema;
        resolved.entrySet()
                .stream()
                .forEach(e -> e.getValue()
                               .build(new Aspect(model.create(), e.getKey()),
                                      topLevelQuery, topLevelMutation));
        schema = GraphQLSchema.newSchema()
                              .query(topLevelQuery.build())
                              .mutation(topLevelMutation.build())
                              .build();
        return schema;
    }

    public GraphQLSchema buildMeta() throws Exception {
        registerTypes(Collections.emptyMap());
        return GraphQLSchema.newSchema()
                            .query(object(MetaQueries.class, typeFunction,
                                          typeFunction))
                            .mutation(object(MetaMutations.class, typeFunction,
                                             typeFunction))
                            .build();
    }

    private void addPhantasmCast(Builder typeBuilder,
                                 Entry<FacetRecord, FacetFields> entry) {
        typeBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                                                .name(String.format("as%s",
                                                                    WorkspacePresentation.toTypeName(entry.getKey()
                                                                                                          .getName())))
                                                .description(String.format("Cast to the %s facet",
                                                                           entry.getKey()
                                                                                .getName()))
                                                .type(new GraphQLTypeReference(entry.getValue()
                                                                                    .getName()))
                                                .dataFetcher(env -> {
                                                    Existential existential = (Existential) env.getSource();
                                                    PhantasmCRUD crud = FacetFields.ctx(env);
                                                    crud.cast(existential.getRecord(),
                                                              new Aspect(crud.getModel()
                                                                             .create(),
                                                                         entry.getKey()));
                                                    return existential;
                                                })
                                                .build());
    }

    private void addPhantasmCast(graphql.schema.GraphQLInterfaceType.Builder builder,
                                 Entry<FacetRecord, FacetFields> entry) {
        builder.field(GraphQLFieldDefinition.newFieldDefinition()
                                            .name(String.format("as%s",
                                                                WorkspacePresentation.toTypeName(entry.getKey()
                                                                                                      .getName())))
                                            .description(String.format("Cast to the %s facet",
                                                                       entry.getKey()
                                                                            .getName()))
                                            .type(new GraphQLTypeReference(entry.getValue()
                                                                                .getName()))
                                            .build());
    }

    private void registerTypes(Map<FacetRecord, FacetFields> resolved) throws NoSuchMethodException,
                                                                       InstantiationException,
                                                                       IllegalAccessException {

        GraphQLInterfaceType et = existentialType(resolved);
        typeFunction.register(Existential.class, (u, t) -> et);

        typeFunction.register(Double.class, (u, t) -> GraphQLFloat);
        typeFunction.register(UUID.class, (u, t) -> GraphQLString);
        typeFunction.register(ValueType.class, (u, t) -> GraphQLString);
        typeFunction.register(Cardinality.class, (u, t) -> GraphQLString);
        typeFunction.register(ReferenceType.class, (u, t) -> GraphQLString);

        GraphQLObjectType agencyType = phantasm(resolved,
                                                objectBuilder(Agency.class,
                                                              typeFunction,
                                                              typeFunction));
        typeFunction.register(Agency.class, (u, t) -> agencyType);

        GraphQLObjectType attrType = phantasm(resolved,
                                              objectBuilder(Attribute.class,
                                                            typeFunction,
                                                            typeFunction));
        typeFunction.register(Attribute.class, (u, t) -> attrType);

        GraphQLObjectType intervalType = phantasm(resolved,
                                                  objectBuilder(Interval.class,
                                                                typeFunction,
                                                                typeFunction));
        typeFunction.register(Interval.class, (u, t) -> intervalType);

        GraphQLObjectType locationType = phantasm(resolved,
                                                  objectBuilder(Location.class,
                                                                typeFunction,
                                                                typeFunction));
        typeFunction.register(Location.class, (u, t) -> locationType);

        GraphQLObjectType productType = phantasm(resolved,
                                                 objectBuilder(com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Product.class,
                                                               typeFunction,
                                                               typeFunction));
        typeFunction.register(com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Product.class,
                              (u, t) -> productType);

        GraphQLObjectType relationshipType = phantasm(resolved,
                                                      objectBuilder(Relationship.class,
                                                                    typeFunction,
                                                                    typeFunction));
        typeFunction.register(Relationship.class, (u, t) -> relationshipType);

        GraphQLObjectType statusCodeType = phantasm(resolved,
                                                    objectBuilder(StatusCode.class,
                                                                  typeFunction,
                                                                  typeFunction));
        typeFunction.register(StatusCode.class, (u, t) -> statusCodeType);

        GraphQLObjectType unitType = phantasm(resolved,
                                              objectBuilder(Unit.class,
                                                            typeFunction,
                                                            typeFunction));
        typeFunction.register(Unit.class, (u, t) -> unitType);

        GraphQLObjectType facteType = objectTypeOf(Facet.class);
        typeFunction.register(Facet.class, (u, t) -> {
            return facteType;
        });

        GraphQLObjectType attrAuthType = objectTypeOf(AttributeAuthorization.class);
        typeFunction.register(AttributeAuthorization.class, (u, t) -> {
            return attrAuthType;
        });

        GraphQLObjectType csType = objectTypeOf(ChildSequencing.class);
        typeFunction.register(ChildSequencing.class, (u, t) -> {
            return csType;
        });

        GraphQLObjectType job = objectTypeOf(Job.class);
        typeFunction.register(Job.class, (u, t) -> job);

        GraphQLObjectType metaType = objectTypeOf(MetaProtocol.class);
        typeFunction.register(MetaProtocol.class, (u, t) -> {
            return metaType;
        });

        GraphQLObjectType netAuthType = objectTypeOf(NetworkAuthorization.class);
        typeFunction.register(NetworkAuthorization.class, (u, t) -> {
            return netAuthType;
        });

        GraphQLObjectType psType = objectTypeOf(ParentSequencing.class);
        typeFunction.register(ParentSequencing.class, (u, t) -> {
            return psType;
        });

        GraphQLObjectType protocolType = objectTypeOf(Protocol.class);
        typeFunction.register(Protocol.class, (u, t) -> {
            return protocolType;
        });

        GraphQLObjectType ssType = objectTypeOf(SelfSequencing.class);
        typeFunction.register(SelfSequencing.class, (u, t) -> {
            return ssType;
        });

        GraphQLObjectType sibSeqType = objectTypeOf(SiblingSequencing.class);
        typeFunction.register(SiblingSequencing.class, (u, t) -> {
            return sibSeqType;
        });

        GraphQLObjectType scsType = objectTypeOf(StatusCodeSequencing.class);
        typeFunction.register(StatusCodeSequencing.class, (u, t) -> {
            return scsType;
        });

        GraphQLObjectType netAttAuthType = objectTypeOf(NetworkAttributeAuthorization.class);
        typeFunction.register(NetworkAttributeAuthorization.class, (u, t) -> {
            return netAttAuthType;
        });

        GraphQLObjectType chronType = objectTypeOf(JobChronology.class);
        typeFunction.register(JobChronology.class, (u, t) -> {
            return chronType;
        });
    }

    private GraphQLObjectType objectTypeOf(Class<?> clazz) {
        GraphQLType type = typeFunction.getType(clazz);
        if (type != null) {
            return (GraphQLObjectType) type;
        }
        TypeProxy proxy = new TypeProxy();
        GraphQLObjectType object = object(clazz, typeFunction, typeFunction);
        proxy.setTarget(object);
        return object;
    }

    private GraphQLObjectType phantasm(Map<FacetRecord, FacetFields> resolved,
                                       Builder objectBuilder) {
        resolved.entrySet()
                .forEach(e -> addPhantasmCast(objectBuilder, e));
        return objectBuilder.build();
    }

    private GraphQLInterfaceType existentialType(Map<FacetRecord, FacetFields> resolved) {
        graphql.schema.GraphQLInterfaceType.Builder builder = graphql.schema.GraphQLInterfaceType.newInterface();
        builder.name("Existential");
        builder.description("The Existential interface type");
        builder.field(GraphQLFieldDefinition.newFieldDefinition()
                                            .name("id")
                                            .description("Existential id")
                                            .type(GraphQLString)
                                            .build());
        builder.field(GraphQLFieldDefinition.newFieldDefinition()
                                            .name("name")
                                            .description("Existential name")
                                            .type(GraphQLString)
                                            .build());
        builder.field(GraphQLFieldDefinition.newFieldDefinition()
                                            .name("description")
                                            .description("Existential description")
                                            .type(GraphQLString)
                                            .build());
        builder.field(GraphQLFieldDefinition.newFieldDefinition()
                                            .name("updatedBy")
                                            .description("Agency that updated the Existential")
                                            .type(new GraphQLTypeReference("Agency"))
                                            .build());
        builder.typeResolver(typeFunction);

        resolved.entrySet()
                .forEach(e -> addPhantasmCast(builder, e));
        return builder.build();
    }
}
