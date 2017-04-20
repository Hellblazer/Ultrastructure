/**
 * Copyright (c) 2017 Chiral Behaviors, LLC, all rights reserved.
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

package com.chiralbehaviors.CoRE.universal;

import static com.chiralbehaviors.CoRE.universal.Universal.textOrNull;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author halhildebrand
 *
 */
public class Action {
    private Map<String, String> extract;
    private String              frameBy;
    private String              query;

    public Action() {
        extract = new HashMap<>();
    }

    public Action(ObjectNode action) {
        this(textOrNull(action.get("query")), textOrNull(action.get("frameBy")),
             Page.extract(textOrNull(action.get("extract"))));
    }

    public Action(String query, String frameBy, Map<String, String> extract) {
        this.query = query;
        this.frameBy = frameBy;
        this.extract = extract;
    }

    public Map<String, String> getExtract() {
        return extract;
    }

    public String getFrameBy() {
        return frameBy;
    }

    public String getQuery() {
        return query;
    }

    public void setExtract(Map<String, String> extract) {
        this.extract = extract;
    }

    public void setFrameBy(String frameBy) {
        this.frameBy = frameBy;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
