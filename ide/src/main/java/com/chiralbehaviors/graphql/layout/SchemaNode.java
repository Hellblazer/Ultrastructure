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

package com.chiralbehaviors.graphql.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Font;

/**
 * @author hhildebrand
 *
 */
abstract public class SchemaNode {

    static final String INDENT         = " * ";
    static final String INDENT_MEASURE = "****";

    public class NodeMaster {
        public final Consumer<JsonNode> items;
        public final Node               node;

        public NodeMaster(Consumer<JsonNode> items, Node node) {
            this.items = items;
            this.node = node;
        }
    }

    static FontLoader FONT_LOADER              = Toolkit.getToolkit()
                                                        .getFontLoader();
    int               averageCardinality       = 1;
    final String      field;
    boolean           isVariableLength         = false;
    float             justifiedWidth           = 0;
    final String      label;
    Font              labelFont                = Font.getDefault();
    boolean           startNewOutlineColumn    = false;
    boolean           startNewOutlineColumnSet = false;
    float             tableColumnWidth         = 0;
    boolean           useVerticalTableHeader   = false;

    public SchemaNode(String field) {
        this(field, field);
    }

    public SchemaNode(String field, String label) {
        this.label = label;
        this.field = field;
    }

    abstract public Control buildControl();

    public String getField() {
        return field;
    }

    public String getLabel() {
        return label;
    }

    abstract public String toString(int indent);

    List<JsonNode> asList(JsonNode jsonNode) {
        List<JsonNode> nodes = new ArrayList<>();
        if (jsonNode == null) {
            return nodes;
        }
        if (jsonNode.isArray()) {
            jsonNode.forEach(node -> nodes.add(node));
        } else {
            return Collections.singletonList(jsonNode);
        }
        return nodes;
    }

    abstract TableColumn<JsonNode, ?> buildTableColumn();

    void incrementNesting() {
        // noop
    }

    float indentWidth() {
        return FONT_LOADER.computeStringWidth(INDENT_MEASURE, labelFont);
    }

    void justify(float width) {
        justifiedWidth = width;
    }

    float labelHeight() {
        return FONT_LOADER.getFontMetrics(labelFont)
                          .getLineHeight();
    }

    float labelWidth() {
        return FONT_LOADER.computeStringWidth(label, labelFont);
    }

    abstract float layout(float width);

    abstract float measure(ArrayNode data);

    abstract NodeMaster outlineElement(float labelWidth);

    float outlineWidth() {
        return tableColumnWidth;
    }
}
