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

package com.chiralbehaviors.graphql.layout.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;

/**
 * @author hhildebrand
 *
 */
abstract public class SchemaNode {

    public class ColumnMaster {
        public final TableColumn<JsonNode, ?> column;
        public final Consumer<JsonNode>       items;
        public final Node                     node;

        public ColumnMaster(Consumer<JsonNode> items,
                            TableColumn<JsonNode, ?> column, Node node) {
            this.items = items;
            this.column = column;
            this.node = node;
        }
    }

    public class NodeMaster {
        public final Consumer<JsonNode> items;
        public final Node               node;

        public NodeMaster(Consumer<JsonNode> items, Node node) {
            this.items = items;
            this.node = node;
        }
    }

    static FontLoader    FONT_LOADER  = Toolkit.getToolkit()
                                               .getFontLoader();

    protected static int SCROLL_WIDTH = 34;

    public static ArrayNode asArray(JsonNode node) {
        if (node.isArray()) {
            return (ArrayNode) node;
        }

        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        array.add(node);
        return array;
    }

    public static List<JsonNode> asList(JsonNode jsonNode) {
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

    final String field;
    float        justifiedWidth   = 0;
    String       label;
    Font         labelFont        = Font.getDefault();
    float        tableColumnWidth = 0;

    boolean      variableLength   = false;

    public SchemaNode(String field) {
        this(field, field);
    }

    public SchemaNode(String field, String label) {
        this.label = label;
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public String getLabel() {
        return label;
    }

    public float getTableColumnWidth() {
        return tableColumnWidth;
    }

    public boolean isRelation() {
        return false;
    }

    public boolean isVariableLength() {
        return variableLength;
    }

    public void setItems(Control control, JsonNode data) {
        if (data == null) {
            data = JsonNodeFactory.instance.arrayNode();
        }
        List<JsonNode> dataList = asList(data);
        ObservableListWrapper<JsonNode> observedData = new ObservableListWrapper<>(dataList);
        if (control instanceof ListView) {
            @SuppressWarnings("unchecked")
            ListView<JsonNode> listView = (ListView<JsonNode>) control;
            listView.setItems(observedData);
        } else if (control instanceof TableView) {
            @SuppressWarnings("unchecked")
            TableView<JsonNode> tableView = (TableView<JsonNode>) control;
            tableView.setItems(observedData);
        } else {
            throw new IllegalArgumentException(String.format("Unknown control %s",
                                                             control));
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTableColumnWidth(float tableColumnWidth) {
        this.tableColumnWidth = tableColumnWidth;
    }

    public void setVariableLength(boolean variableLength) {
        this.variableLength = variableLength;
    }

    abstract public String toString(int indent);

    abstract TableColumn<JsonNode, JsonNode> buildTableColumn(Function<JsonNode, ListView<JsonNode>> nesting,
                                                              int cardinality,
                                                              Function<JsonNode, JsonNode> extractor);

    void constrain(TableColumn<?, ?> column) {
        column.setStyle("-fx-padding: 0 0 0 0;");
        column.setPrefWidth(justifiedWidth);
        column.setMaxWidth(justifiedWidth);
        column.setMinWidth(justifiedWidth);
        column.getProperties()
              .put("deferToParentPrefWidth", Boolean.TRUE);
    }

    void justify(float width) {
        if (variableLength) {
            justifiedWidth = width;
        }
    }

    float labelHeight() {
        return FONT_LOADER.getFontMetrics(labelFont)
                          .getLineHeight();
    }

    float labelWidth() {
        return FONT_LOADER.computeStringWidth(label, labelFont) + 12;
    }

    abstract float layout(float width);

    abstract float measure(ArrayNode data);

    abstract NodeMaster outlineElement(float labelWidth,
                                       Function<JsonNode, JsonNode> extractor,
                                       int cardinality);

    float outlineWidth() {
        return tableColumnWidth;
    }
}
