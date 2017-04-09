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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author hhildebrand
 *
 */
public class Spa {
    private final String            description;
    private final String            frame;
    private final String            name;
    private final String            root;
    private final Map<String, Page> routes;

    public Spa(String name, String description, String frame, String root,
               Map<String, Page> routes) {
        this.name = name;
        this.description = description;
        this.frame = frame;
        this.root = root;
        this.routes = routes;
    }

    public Spa(ObjectNode app) {
        this(textOrNull(app.get("name")), textOrNull(app.get("description")),
             textOrNull(app.get("frame")), textOrNull(app.get("root")),
             routes((ArrayNode) app.get("pages")));
    }

    public static Map<String, Page> routes(ArrayNode pages) {
        Map<String, Page> routes = new HashMap<>();
        pages.forEach(p -> {
            routes.put(p.get("_edge")
                        .get("route")
                        .asText(),
                       new Page((ObjectNode) p));
        });
        return routes;
    }

    public String getDescription() {
        return description;
    }

    public String getFrame() {
        return frame;
    }

    public String getName() {
        return name;
    }

    public Page getRoot() {
        return routes.get(root);
    }

    public Page route(String path) {
        return routes.get(path);
    }
}