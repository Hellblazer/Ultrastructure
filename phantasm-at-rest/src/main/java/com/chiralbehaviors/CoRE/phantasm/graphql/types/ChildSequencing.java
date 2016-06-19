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

package com.chiralbehaviors.CoRE.phantasm.graphql.types;

import static com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.resolve;

import java.util.UUID;

import com.chiralbehaviors.CoRE.jooq.Tables;
import com.chiralbehaviors.CoRE.jooq.tables.records.ChildSequencingAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialRecord;
import com.chiralbehaviors.CoRE.phantasm.graphql.WorkspaceSchema;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Agency;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Product;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.StatusCode;

import graphql.annotations.GraphQLField;
import graphql.schema.DataFetchingEnvironment;

/**
 * @author hhildebrand
 *
 */
public class ChildSequencing {

    public static class ChildSequencingState {
        @GraphQLField
        public String  authority;
        @GraphQLField
        public String  nextChild;
        @GraphQLField
        public String  nextChildStatus;
        @GraphQLField
        public String  notes;
        @GraphQLField
        public Integer sequenceNumber;
        @GraphQLField
        public String  service;
        @GraphQLField
        public String  statusCode;

        public void update(ChildSequencingAuthorizationRecord record) {
            if (authority != null) {
                record.setAuthority(UUID.fromString(authority));
            }
            if (nextChild != null) {
                record.setNextChild(UUID.fromString(nextChild));
            }
            if (nextChildStatus != null) {
                record.setNextChildStatus(UUID.fromString(nextChildStatus));
            }
            if (notes != null) {
                record.setNotes(notes);
            }
            if (service != null) {
                record.setService(UUID.fromString(service));
            }
            if (statusCode != null) {
                record.setStatusCode(UUID.fromString(statusCode));
            }
            if (sequenceNumber != null) {
                record.setSequenceNumber(sequenceNumber);
            }
        }
    }

    public static class ChildSequencingUpdateState
            extends ChildSequencingState {
        @GraphQLField
        public String id;
    }

    public static ChildSequencing fetch(DataFetchingEnvironment env, UUID id) {
        return new ChildSequencing(WorkspaceSchema.ctx(env)
                                                  .create()
                                                  .selectFrom(Tables.CHILD_SEQUENCING_AUTHORIZATION)
                                                  .where(Tables.CHILD_SEQUENCING_AUTHORIZATION.ID.equal(id))
                                                  .fetchOne());
    }

    private final ChildSequencingAuthorizationRecord record;

    public ChildSequencing(ChildSequencingAuthorizationRecord record) {
        assert record != null;
        this.record = record;
    }

    @GraphQLField
    public Agency getAuthority(DataFetchingEnvironment env) {
        ExistentialRecord a = resolve(env, record.getAuthority());
        if (a == null) {
            return null;
        }
        return new Agency(a);
    }

    @GraphQLField
    public String getId() {
        return record.getId()
                     .toString();
    }

    @GraphQLField
    public Product getNextChild(DataFetchingEnvironment env) {
        return new Product(WorkspaceSchema.ctx(env)
                                          .records()
                                          .resolve(record.getNextChild()));
    }

    @GraphQLField
    public StatusCode getNextChildStatus(DataFetchingEnvironment env) {
        return new StatusCode(WorkspaceSchema.ctx(env)
                                             .records()
                                             .resolve(record.getNextChildStatus()));
    }

    @GraphQLField
    public String getNotes() {
        return record.getNotes();
    }

    public ChildSequencingAuthorizationRecord getRecord() {
        return record;
    }

    @GraphQLField
    public Integer getSequenceNumber() {
        return record.getSequenceNumber();
    }

    @GraphQLField
    public Product getService(DataFetchingEnvironment env) {
        return new Product(WorkspaceSchema.ctx(env)
                                          .records()
                                          .resolve(record.getService()));
    }

    @GraphQLField
    public StatusCode getStatusCode(DataFetchingEnvironment env) {
        return new StatusCode(WorkspaceSchema.ctx(env)
                                             .records()
                                             .resolve(record.getStatusCode()));
    }

    @GraphQLField
    public Agency getUpdatedBy(DataFetchingEnvironment env) {
        return new Agency(WorkspaceSchema.ctx(env)
                                         .records()
                                         .resolve(record.getUpdatedBy()));
    }

    @GraphQLField
    public Integer getVersion() {
        return record.getVersion();
    }
}
