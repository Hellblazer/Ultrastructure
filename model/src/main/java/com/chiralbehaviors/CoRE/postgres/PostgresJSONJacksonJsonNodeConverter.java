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

package com.chiralbehaviors.CoRE.postgres;

import java.io.IOException;

import org.jooq.Converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * @author hhildebrand
 *
 */
public class PostgresJSONJacksonJsonNodeConverter
        implements Converter<Object, JsonNode> {

    private static final ObjectMapper MAPPER           = new ObjectMapper();
    private static final long         serialVersionUID = 1L;

    @Override
    public JsonNode from(Object t) {
        try {
            return t == null ? NullNode.instance : MAPPER.readTree(t + "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<Object> fromType() {
        return Object.class;
    }

    @Override
    public Object to(JsonNode u) {
        try {
            return u == null
                   || u.equals(NullNode.instance) ? null
                                                  : MAPPER.writeValueAsString(u);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<JsonNode> toType() {
        return JsonNode.class;
    }
}