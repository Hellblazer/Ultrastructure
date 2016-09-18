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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * @author hhildebrand
 *
 */
public class Primitive extends SchemaNode {

    private float valueDefaultWidth;
    private Font  valueFont = Font.getDefault();

    public Primitive(String label) {
        super(label);
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.graphql.layout.SchemaNode#buildControl()
     */
    @Override
    public TextArea buildControl() {
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setMaxWidth(tableColumnWidth);
        textArea.setMinWidth(tableColumnWidth);
        textArea.setPrefRowCount(averageCardinality);
        textArea.setFont(valueFont);
        return textArea;
    }

    @Override
    public String toString() {
        return String.format("Primitive [%s:%s]", label, valueDefaultWidth);
    }

    @Override
    public String toString(int indent) {
        return toString();
    }

    @Override
    TableColumn<JsonNode, ?> buildTableColumn() {
        TableColumn<JsonNode, JsonNode> column = new TableColumn<>(label);
        column.setMinWidth(tableColumnWidth);
        column.getProperties()
              .put("deferToParentPrefWidth", Boolean.TRUE);
        column.setCellValueFactory(cellData -> new ObjectBinding<JsonNode>() {
            @Override
            protected JsonNode computeValue() {
                return cellData.getValue()
                               .get(field);
            }
        });
        column.setCellFactory(c -> new TableCell<JsonNode, JsonNode>() {
            @Override
            protected void updateItem(JsonNode item, boolean empty) {
                if (item == getItem())
                    return;
                super.updateItem(item, empty);
                super.setText(null);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                TextArea control = buildControl();
                control.setText(asText(item));
                control.setPrefRowCount(averageCardinality);
                super.setGraphic(control);
                setAlignment(Pos.CENTER);
            }
        });
        return column;
    }

    @Override
    float layout(float width) {
        return Math.max(width, tableColumnWidth);
    }

    @Override
    float measure(ArrayNode data) {
        float sum = 0;
        float maxWidth = 0;
        int cardSum = 0;
        for (JsonNode prim : data) {
            List<JsonNode> rows = asList(prim);
            cardSum += rows.size();
            float width = 0;
            for (JsonNode row : rows) {
                width += valueWidth(toString(row)) + 20;
                maxWidth = Math.max(maxWidth, width);
            }
            sum += rows.isEmpty() ? 1 : width / rows.size();
        }
        averageCardinality = Math.max(1, cardSum / data.size());
        float averageWidth = data.size() == 0 ? 0 : (sum / data.size()) + 1;
        if (maxWidth > averageWidth) {
            isVariableLength = true;
        }
        tableColumnWidth = Math.max(labelWidth(), averageWidth);
        if (averageCardinality == 1) {
            averageCardinality = (int) Math.max(1, maxWidth / tableColumnWidth);
        }
        tableColumnWidth += 15;
        return tableColumnWidth;
    }

    @Override
    NodeMaster outlineElement(float labelWidth) {
        HBox box = new HBox();
        TextArea labelText = new TextArea(label);
        labelText.setMinWidth(labelWidth);
        labelText.setMaxWidth(labelWidth);
        labelText.setPrefRowCount(1);
        box.getChildren()
           .add(labelText);
        TextArea control = buildControl();
        control.setPrefHeight(averageCardinality * labelHeight() + 20);
        box.getChildren()
           .add(control);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setVisible(true);
        AnchorPane.setTopAnchor(box, 0.0);
        AnchorPane.setBottomAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
        AnchorPane.setRightAnchor(box, 0.0);
        return new NodeMaster(item -> control.setText(asText(item)), box);
    }

    private String asText(JsonNode node) {
        if (node == null) {
            return "";
        }
        boolean first = true;
        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode row : ((ArrayNode) node)) {
                if (first) {
                    first = false;
                } else {
                    builder.append('\n');
                }
                builder.append(row.asText());
            }
            return builder.toString();
        }
        return node.asText();
    }

    private String toString(JsonNode value) {
        if (value == null) {
            return "";
        }
        if (value instanceof ArrayNode) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (JsonNode e : value) {
                if (first) {
                    first = false;
                    builder.append('[');
                } else {
                    builder.append(", ");
                }
                builder.append(e.asText());
            }
            builder.append(']');
            return builder.toString();
        } else {
            return value.asText();
        }
    }

    private float valueWidth(String text) {
        return FONT_LOADER.computeStringWidth(text, valueFont);
    }
}
