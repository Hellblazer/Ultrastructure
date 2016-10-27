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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.glassfish.jersey.internal.util.Producer;

import com.chiralbehaviors.graphql.layout.Layout;
import com.chiralbehaviors.graphql.layout.LayoutModel;
import com.chiralbehaviors.graphql.layout.controls.NestedColumnView;
import com.chiralbehaviors.graphql.layout.controls.NestedTableRow;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.util.concurrent.AtomicDouble;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

/**
 * @author hhildebrand
 *
 */
public class Relation extends SchemaNode implements Cloneable {

    private static final String ZERO = "0";

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
    private Relation               fold;
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
        justify(width, layout);
    }

    public Control buildControl(Layout layout) {
        if (isFold()) {
            return fold.buildControl(layout);
        }
        return useTable ? buildTable(n -> n, -1, layout)
                        : buildOutline(n -> n, -1, layout);
    }

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
    public double getTableColumnWidth() {
        if (isFold()) {
            return fold.getTableColumnWidth();
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
                                 justifiedWidth, getTableColumnWidth(),
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
    double buildTableColumn(int cardinality, NestingFunction nesting,
                            Layout layout,
                            ObservableList<TableColumn<JsonNode, ?>> parent,
                            boolean k) {
        if (isFold()) {
            return fold.buildTableColumn(averageCardinality, nesting, layout,
                                         parent, k);
        }

        TableColumn<JsonNode, JsonNode> column = new TableColumn<>(label);
        parent.add(column);
        column.getStyleClass()
              .add(tableColumnStyleClass());
        column.setPrefWidth(justifiedWidth);
        column.setMinWidth(justifiedWidth);

        Map<Primitive, Integer> leaves = new HashMap<>();
        int index = 0;
        for (Primitive leaf : gatherLeaves()) {
            leaves.put(leaf, index);
            index++;
        }
        AtomicDouble childHeight = new AtomicDouble();
        Producer<Double> childHeightF = () -> childHeight.get();
        AtomicDouble extendedHeight = new AtomicDouble();
        boolean isKey = children.get(0) instanceof Primitive;
        AtomicBoolean key = new AtomicBoolean(isKey);
        childHeight.set(children.stream()
                                .mapToDouble(child -> {
                                    return buildAndExtend(cardinality, nesting,
                                                          layout, column,
                                                          leaves, childHeightF,
                                                          extendedHeight,
                                                          key.get(), child);
                                })
                                .peek(h -> key.set(false))
                                .map(h -> Layout.snap(h))
                                .max()
                                .getAsDouble());
        double listInset = isKey ? layout.getNestedListInsets()
                                         .getTop()
                                   + layout.getNestedListInsets()
                                           .getBottom()
                                 : Math.max(layout.getNestedListInsets()
                                                  .getTop()
                                            + layout.getNestedListInsets()
                                                    .getBottom(),
                                            layout.getNestedListInsets()
                                                  .getTop() + layout.getNestedListInsets()
                                                                    .getBottom());
        extendedHeight.set(Layout.snap((childHeight.get() * cardinality)
                                       + listInset));
        return extendedHeight.get();

    }

    private double buildAndExtend(int cardinality, NestingFunction nesting,
                                  Layout layout,
                                  TableColumn<JsonNode, JsonNode> column,
                                  Map<Primitive, Integer> leaves,
                                  Producer<Double> childHeightF,
                                  AtomicDouble extendedHeight, boolean key,
                                  SchemaNode child) {
        double height = child.buildTableColumn(averageCardinality,
                                               nest(child, leaves, nesting,
                                                    childHeightF, layout,
                                                    cardinality, extendedHeight,
                                                    key, layout.getModel()),
                                               layout, column.getColumns(),
                                               key);
        if (child.isRelation()) {
            double nestedCellInset = layout.getNestedListCellInsets()
                                           .getTop()
                                     + layout.getNestedListCellInsets()
                                             .getBottom();
            double keyInsets = layout.getNestedKeyListCellInsets()
                                     .getTop()
                               + layout.getNestedKeyListCellInsets()
                                       .getBottom();
            height += key ? Math.max(keyInsets, nestedCellInset)
                          : nestedCellInset;
        }
        return height;
    }

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

    double getLabelWidth(Layout layout) {
        if (isFold()) {
            return fold.getLabelWidth(layout);
        }
        return layout.labelWidth(label);
    }

    double getValueHeight(int cardinality, Layout layout) {
        return useTable ? buildTable(n -> n, cardinality, layout)
                                                                 .getPrefHeight()
                        : (children.stream()
                                   .mapToDouble(child -> child.getValueHeight(averageCardinality,
                                                                              layout))
                                   .reduce((a, b) -> a + b)
                                   .getAsDouble()
                           * cardinality)
                          + layout.getOutlineListInsets()
                                  .getTop()
                          + layout.getOutlineListInsets()
                                  .getBottom()
                          + (layout.getLabelLineHeight() * 2);
    }

    @Override
    boolean isJusifiable() {
        if (isFold()) {
            return fold.isJusifiable();
        }
        return children.stream()
                       .map(child -> child.isJusifiable())
                       .reduce((a, b) -> a & b)
                       .get();
    }

    @Override
    void justify(double width, Layout layout) {
        if (isFold()) {
            fold.justify(width, layout);
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
        double listInset = layout.getOutlineListInsets()
                                 .getLeft()
                           + layout.getOutlineListInsets()
                                   .getRight();
        double tableInset = layout.getTableInsets()
                                  .getLeft()
                            + layout.getOutlineListInsets()
                                    .getRight();
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
        double tableWidth = getTableColumnWidth() + tableInset;
        if (tableWidth <= outlineWidth) {
            nestTables();
            return tableWidth;
        }
        return outlineWidth;
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
        double labelWidth = layout.labelWidth(label);
        labelWidth += layout.getLabelInsets()
                            .getLeft()
                      + layout.getLabelInsets()
                              .getRight();
        int sum = 0;
        tableColumnWidth = 0;
        double listInset = layout.getNestedListInsets()
                                 .getLeft()
                           + layout.getNestedListInsets()
                                   .getRight();
        double keyInset = layout.getNestedKeyListInsets()
                                .getLeft()
                          + layout.getNestedKeyListInsets()
                                  .getRight();
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
            tableColumnWidth += key
                                && !child.isRelation() ? layout.getTableKeyCellInsets()
                                                               .getLeft()
                                                         + layout.getTableKeyCellInsets()
                                                                 .getRight()
                                                       : layout.getTableCellInsets()
                                                               .getLeft()
                                                         + layout.getTableCellInsets()
                                                                 .getRight();
            key = false;
        }
        tableColumnWidth += (children.size() - 1) * listInset;

        tableColumnWidth += children.get(0)
                                    .isRelation() ? keyInset : 0; // Primitives are displayed in a relation without a list
        averageCardinality = Math.max(1, Math.round(sum / children.size()));
        tableColumnWidth = Math.max(labelWidth, tableColumnWidth);
        return isFold() ? fold.getTableColumnWidth() : getTableColumnWidth();
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
        Control control = useTable ? buildTable(n -> n, cardinality, layout)
                                   : buildOutline(n -> n, cardinality, layout);
        Parent element;
        TextArea labelText = new TextArea(label);
        labelText.getStyleClass()
                 .add(outlineLabelStyleClass());
        labelText.setWrapText(true);
        labelText.setPrefColumnCount(1);
        labelText.setMinWidth(labelWidth);
        labelText.setPrefWidth(labelWidth);
        double labelHeight = 2 * layout.getLabelLineHeight();
        labelText.setPrefHeight(labelHeight);
        Pane box;
        if (useTable) {
            box = new HBox();
            control.setPrefWidth(justifiedWidth);
        } else {
            box = new VBox();
            VBox.setVgrow(labelText, Priority.NEVER);
            VBox.setVgrow(control, Priority.ALWAYS);
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
        if (cardinality > 0) {
            list.setPrefHeight(getValueHeight(cardinality, layout));
        }
        list.getStyleClass()
            .add(outlineListStyleClass());
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

    /**
     * Builds the top level nested table
     */
    private Control buildTable(Function<JsonNode, JsonNode> extractor,
                               int cardinality, Layout layout) {
        if (isFold()) {
            return fold.buildTable(extract(extractor), averageCardinality,
                                   layout);
        }

        TableView<JsonNode> table = new TableView<>();
        table.getStyleClass()
             .add(tableStyleClass());
        layout.getModel()
              .apply(table, this);
        Map<Primitive, Integer> leaves = new HashMap<>();
        int index = 0;
        for (Primitive leaf : gatherLeaves()) {
            leaves.put(leaf, index);
            index++;
        }

        boolean isKey = !children.get(0)
                                 .isRelation();
        AtomicBoolean key = new AtomicBoolean(isKey);

        double nestedCellInset = layout.getTableCellInsets()
                                       .getTop()
                                 + layout.getTableCellInsets()
                                         .getBottom();
        double tableCellInset = isKey ? Math.max(layout.getTableKeyCellInsets()
                                                       .getTop()
                                                 + layout.getTableKeyCellInsets()
                                                         .getBottom(),
                                                 nestedCellInset)
                                      : nestedCellInset;

        AtomicDouble childHeight = new AtomicDouble();
        childHeight.set(children.stream()
                                .mapToDouble(child -> child.buildTableColumn(averageCardinality,
                                                                             (p,
                                                                              row, primitive) -> {
                                                                                 Double height = childHeight.get();
                                                                                 NestedColumnView view = new NestedColumnView();
                                                                                 Control destiny = p.apply(() -> ZERO,
                                                                                                           () -> height);
                                                                                 view.manifest(child,
                                                                                               destiny);
                                                                                 destiny.setPrefHeight(height);
                                                                                 return view;
                                                                             },
                                                                             layout,
                                                                             table.getColumns(),
                                                                             key.getAndSet(false)))
                                .map(h -> Layout.snap(h) + 0.5)
                                .max()
                                .getAsDouble());
        table.setPlaceholder(new Text());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefWidth(justifiedWidth);
        Double prefHeight = 0d;

        double rowHeight = childHeight.get() + tableCellInset;

        table.setRowFactory(tableView -> {
            NestedTableRow<JsonNode> row = new NestedTableRow<JsonNode>();
            row.setPrefHeight(rowHeight);
            layout.getModel()
                  .apply(row, Relation.this);
            return row;
        });

        if (cardinality > 0) {
            double headerHeight = layout.measureHeader(table);
            double tableInset = layout.getTableInsets()
                                      .getTop()
                                + layout.getTableInsets()
                                        .getBottom();
            prefHeight = (cardinality * rowHeight) + headerHeight + tableInset;
            table.setPrefHeight(prefHeight);
        }
        return table;
    }

    private ListCell<JsonNode> nestListCell(SchemaNode child,
                                            BiFunction<Producer<String>, Producer<Double>, Control> p,
                                            Producer<Double> childHeight,
                                            String id) {
        return new ListCell<JsonNode>() {
            Control childControl;
            {
                emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                    if (isEmpty) {
                        setGraphic(null);
                    } else {
                        setGraphic(childControl);
                    }
                });
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
            }

            @Override
            public void updateIndex(int i) {
                int oldIndex = getIndex();
                if (i != oldIndex) {
                    if (i < 0) {
                        setGraphic(null);
                        childControl = null;
                    } else {
                        childControl = p.apply(() -> String.format("%s.%s", id,
                                                                   i),
                                               childHeight);
                        setGraphic(childControl);
                    }
                }
                super.updateIndex(i);
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
                setItemsOf(childControl, child.extractFrom(item));
            }
        };
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
                        child.justify(available, layout);
                    } else {
                        child.justify(tableWidth, layout);
                    }
                } else {
                    child.justify(available, layout);
                }
            });
        }
    }

    private void justifyTable(double width, Layout layout) {
        justifiedWidth = width;
        double slack = width - getTableColumnWidth();
        assert slack >= 0 : String.format("Negative slack: %.2f (%.2f) \n%s",
                                          slack, width, this);
        double total = children.stream()
                               .filter(child -> child.isJusifiable())
                               .map(child -> child.getTableColumnWidth())
                               .reduce((a, b) -> a + b)
                               .orElse(0.0d);
        children.stream()
                .filter(child -> child.isJusifiable())
                .forEach(child -> child.justify(slack
                                                * (child.getTableColumnWidth()
                                                   / total)
                                                + child.getTableColumnWidth(),
                                                layout));
    }

    private NestingFunction nest(SchemaNode child,
                                 Map<Primitive, Integer> leaves,
                                 NestingFunction nesting,
                                 Producer<Double> childHeightF, Layout layout,
                                 int cardinality, AtomicDouble calcHeight,
                                 boolean key, LayoutModel model) {

        return (p, row, primitive) -> {
            return nesting.apply((parentId, renderedF) -> {
                final double renderedHeight = renderedF.call();
                final double childHeight = childHeightF.call();

                double keyInset = layout.getNestedKeyListInsets()
                                        .getTop()
                                  + layout.getNestedKeyListInsets()
                                          .getBottom();
                double nestedListInset = layout.getNestedListInsets()
                                               .getTop()
                                         + layout.getNestedListInsets()
                                                 .getBottom();
                double keyCellInset = layout.getNestedKeyListCellInsets()
                                            .getTop()
                                      + layout.getNestedKeyListCellInsets()
                                              .getBottom();
                double nestedCellInset = layout.getNestedListCellInsets()
                                               .getTop()
                                         + layout.getNestedListCellInsets()
                                                 .getBottom();

                final double listInset = key ? keyInset : nestedListInset;

                final double listCellInset = key ? keyCellInset
                                                 : nestedCellInset;

                double deficit = Math.max(0, renderedHeight - calcHeight.get()
                                             - listInset);

                final double childDeficit = Math.max(0, Layout.snap(deficit / cardinality));
                final double childLayoutHeight = childHeight + childDeficit;

                Integer primitiveColumn = leaves.get(primitive);
                String id = parentId.call();

                ListView<JsonNode> split = split(key, id, primitiveColumn, row,
                                                 leaves.size());
                model.apply(split, Relation.this, child);
                split.setMinHeight(renderedHeight);
                split.setPrefHeight(renderedHeight);
                split.setMaxHeight(renderedHeight);
                split.setFixedCellSize(childLayoutHeight);
                split.setCellFactory(c -> {
                    ListCell<JsonNode> cell = nestListCell(child, p,
                                                           () -> childLayoutHeight
                                                                 - listCellInset,
                                                           id);

                    if (key) {
                        cell.getStyleClass()
                            .add(nestedKeyListCellClass());
                    } else {
                        cell.getStyleClass()
                            .add(nestedListCellClass());
                    }
                    model.apply(cell, Relation.this);
                    return cell;
                });
                return split;
            }, row, primitive);
        };
    }

    private void nestTables() {
        useTable = true;
        children.forEach(child -> {
            if (child.isRelation()) {
                ((Relation) child).nestTables();
            }
        });
    }

    private ListCell<JsonNode> outlineListCell(double outlineLabelWidth,
                                               Function<JsonNode, JsonNode> extractor,
                                               Layout layout) {
        return new ListCell<JsonNode>() {
            HBox                                              cell     = new HBox();
            Map<SchemaNode, Pair<Consumer<JsonNode>, Parent>> controls = new HashMap<>();
            {
                cell.setMinWidth(0);
                cell.setPrefWidth(1);
            }
            {
                VBox box = new VBox();
                box.setPrefWidth(justifiedWidth);
                children.forEach(child -> {
                    Pair<Consumer<JsonNode>, Parent> master = child.outlineElement(outlineLabelWidth,
                                                                                   extractor,
                                                                                   averageCardinality,
                                                                                   layout);
                    controls.put(child, master);
                    box.getChildren()
                       .add(master.getValue());
                });
                cell.getChildren()
                    .add(box);
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
                super.setGraphic(cell);
            }
        };
    }

    private ListView<JsonNode> split(boolean key, String id,
                                     Integer primitiveColumn,
                                     NestedTableRow<JsonNode> row, int count) {
        ListView<JsonNode> content = new ListView<JsonNode>() {

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                if (count > 1) {
                    row.link(id, primitiveColumn, this, count);
                }
            }

        };
        ObservableList<String> styleClass = content.getStyleClass();

        styleClass.add(key ? nestedKeyListClass() : nestedListClass());
        if (!primitiveColumn.equals(count - 1)) {
            content.getStylesheets()
                   .add(getClass().getResource("hide-scrollbar.css")
                                  .toExternalForm());
        }
        content.setPlaceholder(new Text());
        content.setMinWidth(0);
        content.setPrefWidth(1);
        return content;
    }
}
