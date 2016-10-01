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

import java.util.List;
import java.util.function.Function;

import com.chiralbehaviors.graphql.layout.ListViewWithVisibleRowCount;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.beans.binding.ObjectBinding;

/**
 * @author hhildebrand
 *
 */
public class Primitive extends SchemaNode {

    private float valueDefaultWidth = 0;
    private Font  valueFont         = Font.getDefault();

    public Primitive(String label) {
        super(label);
    }

    @Override
    public String toString() {
        return String.format("Primitive [%s:%.2f(%.2f)]", label, justifiedWidth,
                             tableColumnWidth);
    }

    @Override
    public String toString(int indent) {
        return toString();
    }

    TableColumn<String, ?> buildHeader() {
        TableColumn<String, ?> header = new TableColumn<>(label);
        float width = justifiedWidth;
        header.setPrefWidth(width);
        header.setMaxWidth(width);
        header.setMinWidth(width);
        header.setStyle("-fx-padding: 0 0 0 0;");
        header.getProperties()
              .put("deferToParentPrefWidth", Boolean.TRUE);
        return header;
    }

    @Override
    TableColumn<JsonNode, JsonNode> buildTableColumn(Function<JsonNode, ListView<JsonNode>> nesting,
                                                     int cardinality,
                                                     Function<JsonNode, JsonNode> topLevel) {
        Function<JsonNode, ListView<JsonNode>> baseNest = n -> {
            ListView<JsonNode> parent = nesting.apply(n);
            parent.setCellFactory(c -> new ListCell<JsonNode>() {
                ListView<JsonNode> base = split(cardinality);

                @Override
                protected void updateItem(JsonNode item, boolean empty) {
                    if (item == getItem()) {
                        return;
                    }
                    super.updateItem(item, empty);
                    super.setText(null);
                    if (empty || item == null) {
                        super.setGraphic(null);
                        return;
                    }
                    setGraphic(base);
                    setItems(base, item.get(field));
                }
            });
            return parent;
        };

        TableColumn<JsonNode, JsonNode> column = new TableColumn<>(label);
        constrain(column);
        column.setCellValueFactory(cellData -> new ObjectBinding<JsonNode>() {
            @Override
            protected JsonNode computeValue() {
                return cellData.getValue();
            }
        });

        if (nesting == null) {
            column.setCellFactory(c -> new TableCell<JsonNode, JsonNode>() {
                TextArea control = buildControl(cardinality);

                @Override
                protected void updateItem(JsonNode item, boolean empty) {
                    if (item == getItem()) {
                        return;
                    }
                    super.updateItem(item, empty);
                    super.setText(null);
                    if (empty) {
                        super.setGraphic(null);
                        return;
                    }
                    control.setText(asText(item.get(field)));
                    control.setPrefRowCount(cardinality);
                    setGraphic(control);
                }
            });
        } else {
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
                    ListView<JsonNode> nestedView = baseNest.apply(item);
                    setGraphic(nestedView);
                    setItems(nestedView, topLevel.apply(item));
                }
            });
        }
        return column;
    }

    private ListView<JsonNode> split(int cardinality) {
        ListViewWithVisibleRowCount<JsonNode> content = new ListViewWithVisibleRowCount<>();
        content.setCellFactory(c -> new ListCell<JsonNode>() {
            TextArea control = buildControl(cardinality);

            @Override
            protected void updateItem(JsonNode item, boolean empty) {
                if (item == getItem()) {
                    return;
                }
                super.updateItem(item, empty);
                super.setText(null);
                if (empty) {
                    super.setGraphic(null);
                    return;
                }
                control.setText(asText(item));
                control.setPrefRowCount(cardinality);
                setGraphic(control);
            }
        });
        content.setPrefWidth(justifiedWidth);
        content.visibleRowCountProperty()
               .set(cardinality);
        return content;
    }

    @Override
    float layout(float width) {
        return variableLength ? width : Math.min(width, tableColumnWidth);
    }

    @Override
    float measure(ArrayNode data) {
        float sum = 0;
        float maxWidth = 0;
        for (JsonNode prim : data) {
            List<JsonNode> rows = asList(prim);
            float width = 0;
            for (JsonNode row : rows) {
                width += valueWidth(toString(row));
                maxWidth = Math.max(maxWidth, width);
            }
            sum += rows.isEmpty() ? 1 : width / rows.size();
        }
        float averageWidth = data.size() == 0 ? 0 : (sum / data.size());

        if (maxWidth > valueDefaultWidth && maxWidth > averageWidth) {
            variableLength = true;
        }
        tableColumnWidth = Math.max(labelWidth(),
                                    Math.max(valueDefaultWidth, averageWidth))
                           + 4;
        justifiedWidth = tableColumnWidth;
        return tableColumnWidth;
    }

    @Override
    NodeMaster outlineElement(float labelWidth,
                              Function<JsonNode, JsonNode> extractor,
                              int cardinality) {
        HBox box = new HBox();
        TextArea labelText = new TextArea(label);
        labelText.setStyle("-fx-background-color: red;");
        labelText.setPrefWidth(labelWidth);
        labelText.setPrefRowCount(cardinality);
        box.getChildren()
           .add(labelText);
        TextArea control = buildControl(cardinality);
        box.getChildren()
           .add(control);
        box.setPrefWidth(justifiedWidth);
        return new NodeMaster(item -> {
            control.setText(asText(extractor.apply(item)
                                            .get(field)));
        }, box);
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

    private TextArea buildControl(int cardinality) {
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefWidth(justifiedWidth);
        textArea.setPrefRowCount(cardinality);
        textArea.setFont(valueFont);
        return textArea;
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
