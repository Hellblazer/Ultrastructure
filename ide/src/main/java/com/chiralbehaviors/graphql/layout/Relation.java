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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.javafx.collections.ObservableListWrapper;

import graphql.language.Definition;
import graphql.language.Field;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.language.Selection;
import graphql.parser.Parser;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * @author hhildebrand
 *
 */
public class Relation extends SchemaNode implements Cloneable {

    public static SchemaNode buildSchema(Field parentField) {
        Relation parent = new Relation(parentField.getName());
        for (Selection selection : parentField.getSelectionSet()
                                              .getSelections()) {
            if (selection instanceof Field) {
                Field field = (Field) selection;
                if (field.getSelectionSet() == null) {
                    parent.addChild(new Primitive(field.getName()));
                } else {
                    parent.addChild(buildSchema(field));
                }
            } else if (selection instanceof InlineFragment) {

            } else if (selection instanceof FragmentSpread) {

            }
        }
        return parent;
    }

    public static SchemaNode buildSchema(String query, String source) {
        for (Definition definition : new Parser().parseDocument(query)
                                                 .getDefinitions()) {
            if (definition instanceof OperationDefinition) {
                OperationDefinition operation = (OperationDefinition) definition;
                if (operation.getOperation()
                             .equals(Operation.QUERY)) {
                    for (Selection selection : operation.getSelectionSet()
                                                        .getSelections()) {
                        if (selection instanceof Field) {
                            Field field = (Field) selection;
                            if (source.equals(field.getName())) {
                                return Relation.buildSchema(field);
                            }
                        }
                    }
                }
            }
        }
        throw new IllegalStateException(String.format("Invalid query, cannot find source: %s",
                                                      source));
    }

    private final List<SchemaNode> children          = new ArrayList<>();
    private RelationConstraints    constraints;

    private float                  outlineLabelWidth = 0;

    private boolean                useTable          = false;

    public Relation(String label) {
        super(label);
    }

    public void addChild(SchemaNode child) {
        children.add(child);
        outlineLabelWidth = Math.max(child.labelWidth() + 20,
                                     outlineLabelWidth);
    }

    @Override
    public Control buildControl() {
        return useTable ? buildNestedTable() : buildOutline();
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

    public void nestTables() {
        this.useTable = true;
        children.forEach(child -> {
            if (child instanceof Relation) {
                ((Relation) child).nestTables();
            }
        });
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
        column.setMinWidth(tableColumnWidth);
        column.setCellValueFactory(cellData -> new ObjectBinding<List<JsonNode>>() {
            @Override
            protected List<JsonNode> computeValue() {
                return asList(cellData.getValue()
                                      .get(field));
            }
        });
        column.setCellFactory(c -> new TableCell<JsonNode, List<JsonNode>>() {
            TableView<JsonNode> table = buildNestedTable();

            @Override
            protected void updateItem(List<JsonNode> item, boolean empty) {
                if (item == getItem())
                    return;
                super.updateItem(item, empty);
                super.setText(null);
                if (empty) {
                    super.setGraphic(null);
                    return;
                }
                item = item == null ? Collections.emptyList() : item;
                table.setItems(new ObservableListWrapper<>(item));
                super.setGraphic(table);
                setAlignment(Pos.CENTER);
            }
        });
        return column;
    }

    @Override
    protected float measure(ArrayNode data) {
        if (data.isNull() || children.size() == 0) {
            return 0;
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
            tableColumnWidth += child.measure(aggregate);
        }
        averageCardinality = Math.max(1, sum / children.size());
        tableColumnWidth += 30;
        return tableColumnWidth;
    }

    @Override
    protected NodeMaster outlineElement(float labelWidth) {
        VBox box = new VBox(5);
        TextArea labelText = new TextArea(label);
        labelText.setMinWidth(labelWidth);
        labelText.setMinHeight(labelHeight());
        box.getChildren()
           .add(labelText);
        Control control = buildControl();
        box.getChildren()
           .add(control);
        box.setVisible(true);
        box.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setTopAnchor(box, 0.0);
        AnchorPane.setBottomAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
        AnchorPane.setRightAnchor(box, 0.0);
        return new NodeMaster(item -> setItems(control, item), box);
    }

    private TableView<JsonNode> buildNestedTable() {
        TableViewWithVisibleRowCount<JsonNode> table = new TableViewWithVisibleRowCount<>();
        ObservableList<TableColumn<JsonNode, ?>> columns = table.getColumns();
        children.forEach(node -> {
            columns.add(node.buildTableColumn());
        });
        table.setVisible(true);
        table.setMinWidth(tableColumnWidth);
        table.visibleRowCountProperty()
             .set(averageCardinality);
        AnchorPane.setTopAnchor(table, 0.0);
        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        table.getProperties()
             .put("deferToParentPrefWidth", Boolean.TRUE);
        //        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private ListView<JsonNode> buildOutline() {
        ListView<JsonNode> list = new ListViewFixed<>();
        Map<SchemaNode, NodeMaster> controls = new HashMap<>();
        list.setCellFactory(c -> new ListCell<JsonNode>() {
            AnchorPane anchor = new AnchorPane();

            {
                VBox box = new VBox(5);
                children.forEach(child -> {
                    NodeMaster master = child.outlineElement(outlineLabelWidth);
                    controls.put(child, master);
                    box.getChildren()
                       .add(master.node);
                });
                box.setVisible(true);
                box.setAlignment(Pos.CENTER_LEFT);
                anchor.getChildren()
                      .add(box);
                AnchorPane.setTopAnchor(box, 0.0);
                AnchorPane.setBottomAnchor(box, 0.0);
                AnchorPane.setLeftAnchor(box, 0.0);
                AnchorPane.setRightAnchor(box, 0.0);
                setGraphic(anchor);
            }

            @Override
            protected void updateItem(JsonNode item, boolean empty) {
                if (item == getItem())
                    return;
                super.updateItem(item, empty);
                super.setText(null);
                if (empty) {
                    super.setGraphic(null);
                    return;
                }
                super.setGraphic(anchor);
                children.forEach(child -> {
                    controls.get(child).items.accept(item.get(child.field));
                });
            }
        });
        list.setPrefWidth(tableColumnWidth);
        list.setVisible(true);
        AnchorPane.setTopAnchor(list, 0.0);
        AnchorPane.setBottomAnchor(list, 0.0);
        AnchorPane.setLeftAnchor(list, 0.0);
        AnchorPane.setRightAnchor(list, 0.0);
        return list;
    }
}