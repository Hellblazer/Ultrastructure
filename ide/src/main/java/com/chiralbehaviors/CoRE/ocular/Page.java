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

package com.chiralbehaviors.CoRE.ocular;

import java.util.Map;

import com.chiralbehaviors.graphql.layout.schema.Relation;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author hhildebrand
 *
 */
public class Page {
    public static class Route {
        @JsonProperty
        private String              path;
        @JsonProperty
        private Map<String, String> extract;

        public String getPath() {
            return path;
        }

        public Map<String, String> getExtract() {
            return extract;
        }
    }

    @JsonProperty
    private String             endpoint;
    @JsonProperty
    private String             query;
    @JsonProperty
    private Map<String, Route> routing;
    @JsonProperty
    private String             title;

    public String getQuery() {
        return query;
    }

    public Route getRoute(Relation relation) {
        return routing.get(relation.getField());
    }

    public String getTitle() {
        return title;
    }
}
