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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.chiralbehaviors.graphql.layout.Layout;
import com.chiralbehaviors.graphql.layout.RelationTableRow;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import graphql.language.Definition;
import graphql.language.Field;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.language.Selection;
import graphql.parser.Parser;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

/**
 * @author hhildebrand
 *
 */
public class Relation extends SchemaNode implements Cloneable {
    public static Relation buildSchema(String query) {
        for (Definition definition : new Parser().parseDocument(query)
                                                 .getDefinitions()) {
            if (definition instanceof OperationDefinition) {
                OperationDefinition operation = (OperationDefinition) definition;
                if (operation.getOperation()
                             .equals(Operation.QUERY)) {
                    for (Selection selection : operation.getSelectionSet()
                                                        .getSelections()) {
                        if (selection instanceof Field) {
                            return Relation.buildSchema((Field) selection);
                        }
                    }
                }
            }
        }
        throw new IllegalStateException(String.format("Invalid query, cannot find a source: %s",
                                                      query));
    }

    public static Relation buildSchema(String query, String source) {
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

    private static Relation buildSchema(Field parentField) {
        Relation parent = new Relation(parentField.getName());
        for (Selection selection : parentField.getSelectionSet()
                                              .getSelections()) {
            if (selection instanceof Field) {
                Field field = (Field) selection;
                if (field.getSelectionSet() == null) {
                    if (!field.getName()
                              .equals("id")) {
                        parent.addChild(new Primitive(field.getName()));
                    }
                } else {
                    parent.addChild(buildSchema(field));
                }
            } else if (selection instanceof InlineFragment) {

            } else if (selection instanceof FragmentSpread) {

            }
        }
        return parent;
    }

    private boolean                autoFold           = true;
    private int                    averageCardinality = 1;
    private final List<SchemaNode> children           = new ArrayList<>();
    private double                 contentHeight      = 0;
    private double                 elementHeight      = 0;
    private Relation               fold;
    private double                 rowHeight          = 0;
    private double                 tableColumnWidth   = 0;
    private boolean                useTable           = false;

    public Relation(String label) {
        super(label);
    }

    public void addChild(SchemaNode child) {
        children.add(child);
    }

    public void autoLayout(double width, Layout layout) {
        layout(width, layout);
        justify(1, width, layout);
        layoutOutline(1, layout);
    }

    public Control buildControl(Layout layout) {
        return buildControl(1, layout);
    }

    @Override
    public JsonNode extractFrom(JsonNode jsonNode) {
        if (isFold()) {
            return fold.extractFrom(super.extractFrom(jsonNode));
        }
        return super.extractFrom(jsonNode);
    }

    public int getAverageCardinality() {
        return averageCardinality;
    }

    public List<SchemaNode> getChildren() {
        return children;
    }

    @Override
    public double getTableColumnWidth(Layout layout) {
        if (isFold()) {
            return fold.getTableColumnWidth(layout);
        }
        return tableColumnWidth;
    }

    @JsonProperty
    public boolean isFold() {
        return fold != null;
    }

    @Override
    public boolean isRelation() {
        return true;
    }

    @Override
    public boolean isUseTable() {
        if (isFold()) {
            return fold.isUseTable();
        }
        return useTable;
    }

    public void measure(JsonNode jsonNode, Layout layout) {
        if (jsonNode.isArray()) {
            ArrayNode array = (ArrayNode) jsonNode;
            measure(array, layout, false);
        } else {
            ArrayNode singleton = JsonNodeFactory.instance.arrayNode();
            singleton.add(jsonNode);
            measure(singleton, layout, false);
        }
    }

    public void setAverageCardinality(int averageCardinality) {
        this.averageCardinality = averageCardinality;
    }

    public void setFold(boolean fold) {
        this.fold = (fold && children.size() == 1 && children.get(0)
                                                             .isRelation()) ? (Relation) children.get(0)
                                                                            : null;
    }

    @Override
    public void setItems(Control control, JsonNode data) {
        if (data == null) {
            data = JsonNodeFactory.instance.arrayNode();
        }
        if (isFold()) {
            fold.setItems(control, flatten(data));
        } else {
            super.setItems(control, data);
        }
    }

    public void setUseTable(boolean useTable) {
        this.useTable = useTable;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public String toString(int indent) {
        StringBuffer buf = new StringBuffer();
        buf.append(String.format("Relation [%s:%.2f(%.2f) x %s]", label,
                                 justifiedWidth, tableColumnWidth,
                                 averageCardinality));
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
    TableColumn<JsonNode, JsonNode> buildColumn() {
        if (isFold()) {
            return fold.buildColumn();
        }
        TableColumn<JsonNode, JsonNode> column = super.buildColumn();
        column.setUserData(this);
        ObservableList<TableColumn<JsonNode, ?>> columns = column.getColumns();
        children.forEach(child -> columns.add(child.buildColumn()));
        return column;
    }

    @Override
    Function<Double, Pair<Consumer<JsonNode>, Node>> buildColumn(Function<JsonNode, JsonNode> extractor,
                                                                 Map<SchemaNode, TableColumn<JsonNode, ?>> columnMap,
                                                                 int cardinality,
                                                                 Layout layout) {
        if (isFold()) {
            return fold.buildColumn(extract(extractor), columnMap,
                                    averageCardinality, layout);
        }

        List<Function<Double, Pair<Consumer<JsonNode>, Node>>> fields = new ArrayList<>();
        for (SchemaNode child : children) {
            fields.add(child.buildColumn(n -> n, columnMap, averageCardinality,
                                         layout));
        }
        double nestedCellInset = layout.getListCellVerticalInset();
        double cellHeight = elementHeight + nestedCellInset;
        double calculatedHeight = (cellHeight * cardinality)
                                  + layout.getListVerticalInset();

        return rendered -> {
            double deficit = Math.max(0, rendered - calculatedHeight);
            double childDeficit = Math.max(0, deficit / cardinality);
            double extended = Layout.snap(cellHeight + childDeficit);

            ListView<JsonNode> row = new ListView<JsonNode>();
            row.setFixedCellSize(extended);
            row.setMinHeight(rendered);
            row.setPrefHeight(rendered);
            row.setCellFactory(c -> {
                ListCell<JsonNode> cell = rowCell(fields, extended, layout);
                cell.setPrefHeight(extended);
                layout.getModel()
                      .apply(cell, Relation.this);
                return cell;
            });
            return new Pair<Consumer<JsonNode>, Node>(node -> {
                setItems(row, extractor.apply(node));
            }, row);
        };
    }

    @Override
    List<Primitive> gatherLeaves() {
        List<Primitive> leaves = new ArrayList<>();
        for (SchemaNode child : children) {
            leaves.addAll(child.gatherLeaves());
        }
        return leaves;
    }

    @Override
    Function<JsonNode, JsonNode> getFoldExtractor(Function<JsonNode, JsonNode> extractor) {
        if (isFold()) {
            return fold.getFoldExtractor(extract(extractor));
        }
        return super.getFoldExtractor(extractor);
    }

    @Override
    double getLabelWidth(Layout layout) {
        if (isFold()) {
            return fold.getLabelWidth(layout);
        }
        return layout.textWidth(label);
    }

    @Override
    boolean isJusifiable() {
        if (isFold()) {
            return fold.isJusifiable();
        }
        return true;
        //        return children.stream()
        //                       .map(child -> child.isJusifiable())
        //                       .reduce((a, b) -> a & b)
        //                       .get();
    }

    @Override
    void justify(int cardinality, double width, Layout layout) {
        if (isFold()) {
            fold.justify(averageCardinality, width, layout);
            return;
        }
        if (width <= 0)
            return;

        if (useTable) {
            justifyTable(width, layout);
        } else {
            justifyOutline(width, layout);
        }
    }

    /**
     * Layout of the receiver
     * 
     * @param width
     *            - the width alloted to the relation
     * @return
     */
    @Override
    double layout(double width, Layout layout) {
        if (isFold()) {
            return fold.layout(width, layout);
        }
        useTable = false;
        double listInset = layout.getListHorizontalInset();
        double tableInset = layout.getTableHorizontalInset();
        double available = width - children.stream()
                                           .mapToDouble(child -> child.getLabelWidth(layout))
                                           .max()
                                           .getAsDouble();
        double outlineWidth = children.stream()
                                      .mapToDouble(child -> child.layout(available,
                                                                         layout))
                                      .max()
                                      .getAsDouble()
                              + listInset;
        double tableWidth = tableColumnWidth + tableInset;
        if (tableWidth <= outlineWidth) {
            nestTable();
            return tableWidth;
        }
        return outlineWidth;
    }

    @Override
    double layoutOutline(int cardinality, Layout layout) {
        if (isFold()) {
            return fold.layoutOutline(averageCardinality, layout);
        }
        if (isUseTable()) {
            return layoutTable(cardinality, layout);
        }
        List<Double> childHeights = new ArrayList<>();

        elementHeight = Layout.snap(children.stream()
                                            .mapToDouble(child -> child.layoutOutline(averageCardinality,
                                                                                      layout))
                                            .peek(h -> childHeights.add(h))
                                            .reduce((a, b) -> a + b)
                                            .getAsDouble());
        contentHeight = Layout.snap(cardinality * (elementHeight
                                                   + layout.getListCellVerticalInset()))
                        + layout.getListVerticalInset();
        double labeledHeight = Layout.snap(contentHeight + labelHeight(layout));
        return labeledHeight;
    }

    @Override
    double layoutRow(int cardinality, Layout layout) {
        if (isFold()) {
            return fold.layoutRow(averageCardinality, layout);
        }

        elementHeight = children.stream()
                                .mapToDouble(child -> Layout.snap(child.layoutRow(averageCardinality,
                                                                                  layout)))
                                .max()
                                .getAsDouble();

        return extendedHeight(layout, cardinality);
    }

    @Override
    double measure(ArrayNode data, Layout layout, boolean k) {
        if (fold == null && autoFold && children.size() == 1
            && children.get(children.size() - 1) instanceof Relation) {
            fold = ((Relation) children.get(children.size() - 1));
        }
        if (data.isNull() || children.size() == 0) {
            return 0;
        }
        double labelWidth = layout.textWidth(label);
        labelWidth += layout.getTextHorizontalInset();
        int sum = 0;
        tableColumnWidth = 0;
        double listInset = layout.getListHorizontalInset();
        boolean key = true;
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
            sum += data.size() == 0 ? 1 : Math.round(cardSum / data.size());
            tableColumnWidth += child.measure(aggregate, layout, key);
            key = false;
        }
        tableColumnWidth += listInset;
        averageCardinality = (int) Math.ceil(sum / children.size());
        tableColumnWidth = Math.max(labelWidth, tableColumnWidth);
        return isFold() ? fold.getTableColumnWidth(layout) : getTableColumnWidth(layout);
    }

    @Override
    Pair<Consumer<JsonNode>, Parent> outlineElement(double labelWidth,
                                                    Function<JsonNode, JsonNode> extractor,
                                                    int cardinality,
                                                    Layout layout) {
        if (isFold()) {
            return fold.outlineElement(labelWidth, extract(extractor),
                                       averageCardinality, layout);
        }
        Control control = useTable ? buildNestedTable(n -> n, cardinality,
                                                      layout)
                                   : buildOutline(n -> n, cardinality, layout);
        Parent element;
        TextArea labelText = new TextArea(label);
        labelText.getStyleClass()
                 .add(outlineLabelStyleClass());
        labelText.setWrapText(true);
        labelText.setPrefColumnCount(1);
        labelText.setMinWidth(labelWidth);
        labelText.setPrefWidth(labelWidth);
        Pane box;
        if (useTable) {
            box = new HBox();
            control.setPrefWidth(justifiedWidth);
            box.setMinHeight(contentHeight);
            box.setPrefHeight(contentHeight);
        } else {
            box = new VBox();
            box.setPrefWidth(justifiedWidth);
            labelText.setPrefHeight(labelHeight(layout));
            labelText.setMaxHeight(labelHeight(layout));
        }
        box.getChildren()
           .add(labelText);
        box.getChildren()
           .add(control);
        element = box;

        return new Pair<>(item -> {
            if (item == null) {
                return;
            }
            JsonNode extracted = extractor.apply(item);
            JsonNode extractedField = extracted == null ? null
                                                        : extracted.get(field);
            setItems(control, extractedField);
        }, element);
    }

    private Control buildControl(int cardinality, Layout layout) {
        if (isFold()) {
            return fold.buildControl(averageCardinality, layout);
        }
        return useTable ? buildNestedTable(n -> n, 1, layout)
                        : buildOutline(n -> n, 1, layout);
    }

    private TableView<JsonNode> buildNestedTable(Function<JsonNode, JsonNode> extractor,
                                                 int cardinality,
                                                 Layout layout) {
        if (isFold()) {
            return fold.buildNestedTable(extract(extractor), averageCardinality,
                                         layout);
        }
        TableView<JsonNode> table = tableBase();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        children.forEach(child -> table.getColumns()
                                       .add(child.buildColumn()));

        Map<SchemaNode, TableColumn<JsonNode, ?>> columnMap = new HashMap<>();
        List<TableColumn<JsonNode, ?>> columns = table.getColumns();
        while (!columns.isEmpty()) {
            List<TableColumn<JsonNode, ?>> leaves = new ArrayList<>();
            columns.forEach(c -> {
                columnMap.put((SchemaNode) c.getUserData(), c);
                leaves.addAll(c.getColumns());
            });
            columns = leaves;
        }

        Function<Double, Pair<Consumer<JsonNode>, Node>> topLevel = buildColumn(n -> n,
                                                                                columnMap,
                                                                                1,
                                                                                layout);

        double height = extendedHeight(layout, 1);

        table.setRowFactory(tableView -> {
            Pair<Consumer<JsonNode>, Node> relationRow = topLevel.apply(height);
            RelationTableRow row = new RelationTableRow(relationRow.getKey(),
                                                        relationRow.getValue());
            layout.getModel()
                  .apply(row, Relation.this);
            return row;
        });

        layout.getModel()
              .apply(table, this);
        table.setFixedCellSize(height);
        table.setPrefHeight(contentHeight);
        if (cardinality > 1) {
            table.setMinHeight(contentHeight);
        }
        return table;
    }

    private ListView<JsonNode> buildOutline(Function<JsonNode, JsonNode> extractor,
                                            int cardinality, Layout layout) {
        if (isFold()) {
            return fold.buildOutline(extract(extractor), averageCardinality,
                                     layout);
        }

        double outlineLabelWidth = children.stream()
                                           .mapToDouble(child -> child.getLabelWidth(layout))
                                           .max()
                                           .getAsDouble();
        ListView<JsonNode> list = new ListView<>();
        layout.getModel()
              .apply(list, this);
        list.setPrefHeight(contentHeight + layout.getListVerticalInset());
        list.setFixedCellSize(elementHeight
                              + layout.getListCellVerticalInset());
        list.getStyleClass()
            .add(AUTO_LAYOUT_OUTLINE_LIST);
        list.setCellFactory(c -> {
            ListCell<JsonNode> cell = outlineListCell(outlineLabelWidth,
                                                      extractor, layout);
            layout.getModel()
                  .apply(cell, this);
            return cell;
        });
        list.setMinWidth(0);
        list.setPrefWidth(1);
        list.setPlaceholder(new Text());
        return list;
    }

    private double extendedHeight(Layout layout, int cardinality) {
        double extendedHeight = Layout.snap(cardinality * (elementHeight
                                                           + layout.getListCellVerticalInset()))
                                + layout.getListVerticalInset();
        return extendedHeight;
    }

    private ArrayNode flatten(JsonNode data) {
        ArrayNode flattened = JsonNodeFactory.instance.arrayNode();
        if (data != null) {
            if (data.isArray()) {
                data.forEach(item -> {
                    flattened.addAll(asArray(item.get(fold.getField())));
                });
            } else {
                flattened.addAll(asArray(data.get(fold.getField())));
            }
        }
        return flattened;
    }

    private void justifyOutline(double width, Layout layout) {
        if (isJusifiable()) {
            double outlineLabelWidth = children.stream()
                                               .mapToDouble(child -> child.getLabelWidth(layout))
                                               .max()
                                               .getAsDouble();
            justifiedWidth = width;
            double available = width - outlineLabelWidth;
            double tableWidth = width;
            children.forEach(child -> {
                if (child.isRelation()) {
                    if (((Relation) child).isUseTable()) {
                        child.justify(averageCardinality, available, layout);
                    } else {
                        child.justify(averageCardinality, tableWidth, layout);
                    }
                } else {
                    child.justify(averageCardinality, available, layout);
                }
            });
        }
    }

    private void justifyTable(double width, Layout layout) {
        justifiedWidth = width;
        double slack = width - getTableColumnWidth(layout);
        assert slack >= 0 : String.format("Negative slack: %.2f (%.2f) \n%s",
                                          slack, width, this);
        double total = children.stream()
                               .filter(child -> child.isJusifiable())
                               .map(child -> child.getTableColumnWidth(layout))
                               .reduce((a, b) -> a + b)
                               .orElse(0.0d);
        children.stream()
                .filter(child -> child.isJusifiable())
                .forEach(child -> child.justify(averageCardinality,
                                                slack * (child.getTableColumnWidth(layout)
                                                         / total) + child.getTableColumnWidth(layout),
                                                layout));
    }

    private double layoutTable(int cardinality, Layout layout) {
        TableView<JsonNode> table = tableBase();
        children.forEach(child -> table.getColumns()
                                       .add(child.buildColumn()));
        table.setPrefWidth(justifiedWidth);
        rowHeight = layoutRow(averageCardinality, layout)
                    + layout.getListVerticalInset()
                    + layout.getTableRowVerticalInset();
        contentHeight = (rowHeight * cardinality) + layout.measureHeader(table)
                        + layout.getTableVerticalInset();
        return contentHeight;
    }

    private void nestTable() {
        useTable = true;
        children.forEach(child -> {
            if (child.isRelation()) {
                ((Relation) child).nestTable();
            }
        });
    }

    private ListCell<JsonNode> outlineListCell(double outlineLabelWidth,
                                               Function<JsonNode, JsonNode> extractor,
                                               Layout layout) {
        return new ListCell<JsonNode>() {
            VBox                                              cell;
            Map<SchemaNode, Pair<Consumer<JsonNode>, Parent>> controls = new HashMap<>();
            {
                itemProperty().addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        if (cell == null) {
                            initialize(outlineLabelWidth, extractor, layout);
                        }
                        setGraphic(cell);
                    }
                });
                emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                    if (isEmpty) {
                        setGraphic(null);
                    } else {
                        setGraphic(cell);
                    }
                });
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                setAlignment(Pos.CENTER);
                getStyleClass().add(AUTO_LAYOUT_OUTLINE_LIST_CELL);
            }

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
                children.forEach(child -> {
                    controls.get(child)
                            .getKey()
                            .accept(item);
                });
            }

            private void initialize(double outlineLabelWidth,
                                    Function<JsonNode, JsonNode> extractor,
                                    Layout layout) {
                cell = new VBox();
                cell.setMinWidth(0);
                cell.setPrefWidth(1);
                cell.setMinHeight(elementHeight);
                cell.setPrefHeight(elementHeight);
                children.forEach(child -> {
                    Pair<Consumer<JsonNode>, Parent> master = child.outlineElement(outlineLabelWidth,
                                                                                   extractor,
                                                                                   averageCardinality,
                                                                                   layout);
                    controls.put(child, master);
                    cell.getChildren()
                        .add(master.getValue());
                });
            }
        };
    }

    private ListCell<JsonNode> rowCell(List<Function<Double, Pair<Consumer<JsonNode>, Node>>> fields,
                                       double resolvedHeight, Layout layout) {

        return new ListCell<JsonNode>() {
            private Consumer<JsonNode> master;
            private HBox               row;
            {
                emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                    if (isEmpty) {
                        setGraphic(null);
                    } else {
                        setGraphic(row);
                    }
                });
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(JsonNode item, boolean empty) {
                if (item == getItem()) {
                    return;
                }
                super.updateItem(item, empty);
                if (empty || item == null) {
                    return;
                }
                if (row == null) {
                    buildRow();
                }
                setGraphic(row);
                master.accept(item);
            }

            private void buildRow() {
                row = new HBox();
                row.setPrefHeight(resolvedHeight);
                List<Consumer<JsonNode>> consumers = new ArrayList<>();
                fields.forEach(p -> {
                    Pair<Consumer<JsonNode>, Node> pair = p.apply(resolvedHeight);
                    row.getChildren()
                       .add(pair.getValue());
                    consumers.add(pair.getKey());
                });
                master = node -> consumers.forEach(c -> c.accept(node));
            }
        };
    }

    private TableView<JsonNode> tableBase() {
        TableView<JsonNode> table = new TableView<>();
        table.getStyleClass()
             .add(AUTO_LAYOUT_TABLE);
        table.setPlaceholder(new Text());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefWidth(justifiedWidth);
        return table;
    }
}
