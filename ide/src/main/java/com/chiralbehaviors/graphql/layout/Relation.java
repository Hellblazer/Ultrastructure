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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.javafx.collections.ObservableListWrapper;

import javafx.beans.binding.ObjectBinding;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * @author hhildebrand
 *
 */
public class Relation extends SchemaNode implements Cloneable {

    private int                    averageCardinality = 1;
    private final List<SchemaNode> children           = new ArrayList<>();
    private RelationConstraints    constraints;
    private float                  outlineLabelWidth  = 0;

    private boolean                useTable           = false;

    public Relation(String label) {
        super(label);
    }

    public void addChild(SchemaNode child) {
        children.add(child);
        outlineLabelWidth = Math.max(child.labelWidth(), outlineLabelWidth);
    }

    @Override
    public Control buildControl() {
        return useTable ? buildNestedTable() : buildOutline();
    }

    public int getAverageCardinality() {
        return averageCardinality;
    }

    public List<SchemaNode> getChildren() {
        return children;
    }

    public RelationConstraints getConstraints() {
        return constraints;
    }

    public float getOutlineLabelWidth() {
        return outlineLabelWidth;
    }

    public boolean isUseTable() {
        return useTable;
    }

    public void measure(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            ArrayNode array = (ArrayNode) jsonNode;
            measure(array);
        } else {
            ArrayNode singleton = JsonNodeFactory.instance.arrayNode();
            singleton.add(jsonNode);
            measure(singleton);
        }
    }

    public void setConstraints(RelationConstraints constraints) {
        this.constraints = constraints;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setItems(Control control, JsonNode item) {
        if (control instanceof ListView) {
            ((ListView<?>) control).setItems(new ObservableListWrapper(asList(item)));
        } else if (control instanceof TableView) {
            ((TableView<?>) control).setItems(new ObservableListWrapper(asList(item)));
        } else {
            throw new IllegalArgumentException(String.format("Unknown control %s",
                                                             control));
        }
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public String toString(int indent) {
        StringBuffer buf = new StringBuffer();
        buf.append(String.format("Relation [%s:%s x %s]", getLabel(),
                                 tableColumnWidth, averageCardinality));
        buf.append('\n');
        children.forEach(c -> {
            for (int i = 0; i < indent; i++) {
                buf.append("    ");
            }
            buf.append("  - ");
            buf.append(c.toString(indent + 1));
            buf.append('\n');
        });
        return buf.toString();
    }

    @Override
    protected TableColumn<JsonNode, ?> buildTableColumn() {
        TableColumn<JsonNode, List<JsonNode>> column = new TableColumn<>(label);
        column.setPrefWidth(tableColumnWidth);
        column.setCellValueFactory(cellData -> new ObjectBinding<List<JsonNode>>() {
            @Override
            protected List<JsonNode> computeValue() {
                return asList(cellData.getValue()
                                      .get(field));
            }

        });
        column.setCellFactory(c -> new TableCell<JsonNode, List<JsonNode>>() {
            @Override
            protected void updateItem(List<JsonNode> item, boolean empty) {
                if (item == getItem())
                    return;

                TableView<JsonNode> table = buildNestedTable();
                table.setItems(new ObservableListWrapper<>(item));

                super.updateItem(item, empty);
                super.setText(null);
                super.setGraphic(table);
            }
        });
        return column;
    }

    @Override
    protected void measure(ArrayNode data) {
        if (data.isNull() || children.size() == 0) {
            return;
        }
        int sum = 0;
        for (SchemaNode child : children) {
            ArrayNode aggregate = JsonNodeFactory.instance.arrayNode();
            int cardSum = 0;
            for (JsonNode node : data) {
                JsonNode sub = node.get(child.field);
                if (sub instanceof ArrayNode) {
                    aggregate.addAll((ArrayNode) sub);
                    cardSum += sub.size();
                } else {
                    aggregate.add(sub);
                    cardSum += 1;
                }
            }
            sum += data.size() == 0 ? 0 : cardSum / data.size();
            child.measure(aggregate);
            tableColumnWidth += child.tableColumnWidth;
        }
        averageCardinality = Math.max(1, sum / children.size());
    }

    private List<JsonNode> asList(JsonNode jsonNode) {
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

    private TableView<JsonNode> buildNestedTable() {
        TableView<JsonNode> table = new TableView<>();
        ObservableList<TableColumn<JsonNode, ?>> columns = table.getColumns();
        children.forEach(node -> {
            columns.add(node.buildTableColumn());
        });
        return table;
    }

    private ListView<JsonNode> buildOutline() {
        ListView<JsonNode> list = new ListView<>();
        list.setCellFactory(c -> new ListCell<JsonNode>() {
            @Override
            protected void updateItem(JsonNode item, boolean empty) {
                if (item == getItem())
                    return;
                super.updateItem(item, empty);
                super.setText(null);
                super.setGraphic(outlineElement(item));
            }
        });
        return list;
    }

    private VBox outlineElement(JsonNode item) {
        VBox vBox = new VBox();
        vBox.getChildren()
            .add(new Text(label));
        children.forEach(child -> {
            vBox.getChildren()
                .add(outlineElement(child,
                                    item == null ? JsonNodeFactory.instance.arrayNode()
                                                 : item.get(child.field)));
        });
        return vBox;
    }

    private HBox outlineElement(SchemaNode node, JsonNode item) {
        HBox box = new HBox();
        box.getChildren()
           .add(new Text(node.label));
        Control control = node.buildControl();
        setItems(control, item);
        box.getChildren()
           .add(control);
        return box;
    }
}
