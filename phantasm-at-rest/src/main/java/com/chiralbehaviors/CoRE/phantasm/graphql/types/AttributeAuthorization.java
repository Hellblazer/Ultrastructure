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

import java.util.Base64;
import java.util.UUID;

import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialAttributeAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialRecord;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Agency;
import com.chiralbehaviors.CoRE.phantasm.graphql.types.Existential.Attribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.annotations.GraphQLField;
import graphql.schema.DataFetchingEnvironment;

/**
 * @author hhildebrand
 *
 */
public class AttributeAuthorization {
    private final ExistentialAttributeAuthorizationRecord record;

    public AttributeAuthorization(ExistentialAttributeAuthorizationRecord record) {
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
    public Attribute getAuthorizedAttribute(DataFetchingEnvironment env) {
        return new Attribute(resolve(env, record.getAuthorizedAttribute()));
    }

    @GraphQLField
    public String getBinaryValue() {
        if (record.getBinaryValue() == null) {
            return null;
        }
        return Base64.getEncoder()
                     .encodeToString(record.getBinaryValue());
    }

    @GraphQLField
    public Boolean getBooleanValue() {
        return record.getBooleanValue();
    }

    @GraphQLField
    public Facet getFacet(DataFetchingEnvironment env) {
        return Facet.fetch(env, record.getFacet());
    }

    @GraphQLField
    public UUID getId() {
        return record.getId();
    }

    @GraphQLField
    public Integer getIntegerValue() {
        return record.getIntegerValue();
    }

    @GraphQLField
    public String getJsonValue() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(record.getJsonValue());
    }

    @GraphQLField
    public String getNotes() {
        return record.getNotes();
    }

    @GraphQLField
    public Double getNumericValue() {
        if (record.getNumericValue() == null) {
            return null;
        }
        return record.getNumericValue()
                     .doubleValue();
    }

    @GraphQLField
    public String getTextValue() {
        return record.getTextValue();
    }

    @GraphQLField
    public Long getTimestampValue() {
        if (record.getTimestampValue() == null) {
            return null;
        }
        return record.getTimestampValue()
                     .getTime();
    }

    @GraphQLField
    public Agency getUpdatedBy(DataFetchingEnvironment env) {
        return new Agency(resolve(env, record.getUpdatedBy()));
    }

    @GraphQLField
    public Integer getVersion() {
        return record.getVersion();
    }

}
