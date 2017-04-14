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

package com.chiralbehaviors.CoRE.phantasm.graphql.queries;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.chiralbehaviors.CoRE.domain.Product;
import com.chiralbehaviors.CoRE.jooq.Tables;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialAttributeRecord;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.phantasm.graphql.GraphQLInterface;
import com.chiralbehaviors.CoRE.phantasm.graphql.WorkspaceContext;
import com.chiralbehaviors.CoRE.phantasm.graphql.WorkspaceSchema;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.AttributeValue;

import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;
import graphql.schema.DataFetchingEnvironment;

/**
 * @author hhildebrand
 *
 */
@GraphQLInterface
public interface AttributeValueQueries {

    @GraphQLField
    default AttributeValue attributeValue(@NotNull @GraphQLName("id") String id,
                                          DataFetchingEnvironment env) {
        ExistentialAttributeRecord auth = AttributeValue.fetch(env,
                                                               UUID.fromString(id));
        return WorkspaceSchema.ctx(env)
                              .checkRead(auth) ? new AttributeValue(auth)
                                               : null;
    }

    @GraphQLField
    default List<AttributeValue> attributeValues(@GraphQLName("ids") List<String> ids,
                                                 DataFetchingEnvironment env) {
        if (ids == null) {
            Product workspace = ((WorkspaceContext) env.getContext()).getWorkspace();
            Model model = WorkspaceSchema.ctx(env);
            return model.create()
                        .selectFrom(Tables.EXISTENTIAL_ATTRIBUTE)
                        .where(Tables.EXISTENTIAL_ATTRIBUTE.WORKSPACE.equal(workspace.getId()))
                        .fetch()
                        .into(ExistentialAttributeRecord.class)
                        .stream()
                        .filter(r -> model.checkRead(r))
                        .map(r -> new AttributeValue(r))
                        .collect(Collectors.toList());
        }
        return ids.stream()
                  .map(s -> UUID.fromString(s))
                  .map(id -> AttributeValue.fetch(env, id))
                  .map(r -> new AttributeValue(r))
                  .collect(Collectors.toList());
    }
}
