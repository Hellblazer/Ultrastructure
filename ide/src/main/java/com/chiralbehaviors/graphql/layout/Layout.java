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

import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_LABEL;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_NEST_KEY_LIST;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_NEST_KEY_LIST_CELL;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_NEST_LIST;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_NEST_LIST_CELL;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_OUTLINE_LIST;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_OUTLINE_LIST_CELL;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_TABLE;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_TABLE_CELL;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_TABLE_COLUMN;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_TABLE_KEY_CELL;
import static com.chiralbehaviors.graphql.layout.schema.SchemaNode.AUTO_LAYOUT_VALUE;

import java.lang.reflect.Field;
import java.util.List;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class Layout {
    private static FontLoader FONT_LOADER = Toolkit.getToolkit()
                                                   .getFontLoader();
    private static Insets     ZERO_INSETS = new Insets(0);

    public static Insets add(Insets a, Insets b) {
        return new Insets(a.getTop() + b.getTop(), a.getRight() + b.getRight(),
                          a.getBottom() + b.getBottom(),
                          a.getLeft() + b.getLeft());
    }

    public static double snap(double value) {
        return Math.ceil(value);
    }

    private Font         labelFont               = Font.getDefault();
    private Insets       labelInsets             = ZERO_INSETS;
    private double       labelLineHeight         = 0;
    private LayoutModel  model;
    private Insets       nestedKeyListCellInsets = ZERO_INSETS;
    private Insets       nestedKeyListInsets     = ZERO_INSETS;
    private Insets       nestedListCellInsets    = ZERO_INSETS;
    private Insets       nestedListInsets        = ZERO_INSETS;
    private Insets       outlineListCellInsets   = ZERO_INSETS;
    private Insets       outlineListInsets       = ZERO_INSETS;
    private List<String> styleSheets;
    private Insets       tableCellInsets         = ZERO_INSETS;
    private Insets       tableInsets             = ZERO_INSETS;
    private Insets       tableKeyCellInsets      = ZERO_INSETS;
    private Insets       tableRowInsets;
    private Font         valueFont               = Font.getDefault();
    private Insets       valueInsets             = ZERO_INSETS;
    private double       valueLineHeight         = 0;

    public Layout(List<String> styleSheets) {
        this(styleSheets, new LayoutModel() {
        });
    }

    public Layout(List<String> styleSheets, LayoutModel model) {
        this.model = model;
        this.styleSheets = styleSheets;
        TextArea valueText = new TextArea("Lorem Ipsum");
        TextArea labelText = new TextArea("Lorem Ipsum");

        valueText.getStyleClass()
                 .add(AUTO_LAYOUT_VALUE);
        valueText.getStyleClass()
                 .add(AUTO_LAYOUT_LABEL);

        ListView<String> outlineList = new ListView<>();
        outlineList.getStyleClass()
                   .add(AUTO_LAYOUT_OUTLINE_LIST);

        ListCell<String> outlineListCell = new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
            };
        };
        outlineListCell.getStyleClass()
                       .add(AUTO_LAYOUT_OUTLINE_LIST_CELL);
        outlineList.setCellFactory(s -> outlineListCell);

        ListCell<String> nestedKeyListCell = new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
            };
        };
        nestedKeyListCell.getStyleClass()
                         .add(AUTO_LAYOUT_NEST_KEY_LIST_CELL);
        ListView<String> nestedKeyList = new ListView<>();
        nestedKeyList.getStyleClass()
                     .add(AUTO_LAYOUT_NEST_KEY_LIST);
        nestedKeyList.setCellFactory(s -> nestedKeyListCell);

        ListCell<String> nestedListCell = new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
            };
        };
        nestedListCell.getStyleClass()
                      .add(AUTO_LAYOUT_NEST_LIST_CELL);
        ListView<String> nestedList = new ListView<>();
        nestedList.getStyleClass()
                  .add(AUTO_LAYOUT_NEST_LIST);
        nestedList.setCellFactory(s -> nestedListCell);

        TableCell<String, String> tableCell = new TableCell<String, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
            }
        };
        tableCell.getStyleClass()
                 .add(AUTO_LAYOUT_TABLE_CELL);

        TableCell<String, String> tableKeyCell = new TableCell<String, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
            }
        };
        tableKeyCell.getStyleClass()
                    .add(AUTO_LAYOUT_TABLE_KEY_CELL);

        TableView<String> table = new TableView<>();
        table.getStyleClass()
             .add(AUTO_LAYOUT_TABLE);

        TableRow<String> tableRow = new TableRow<>();
        table.setRowFactory(v -> tableRow);

        TableColumn<String, String> column = new TableColumn<>("");
        column.setCellFactory(c -> tableCell);
        column.getStyleClass()
              .add(AUTO_LAYOUT_TABLE_COLUMN);
        column.setCellValueFactory(s -> new SimpleStringProperty(s.getValue()));

        TableColumn<String, String> keyColumn = new TableColumn<>("");
        keyColumn.setCellFactory(c -> tableKeyCell);
        keyColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue()));
        keyColumn.getStyleClass()
                 .add(AUTO_LAYOUT_TABLE_COLUMN);
        table.getColumns()
             .add(column);
        table.getColumns()
             .add(keyColumn);

        VBox root = new VBox();
        root.getChildren()
            .addAll(table, outlineList, nestedKeyList, nestedList, valueText,
                    labelText);
        Scene scene = new Scene(root, 800, 600);
        if (styleSheets != null) {
            scene.getStylesheets()
                 .addAll(styleSheets);
        }
        valueText.applyCss();
        valueText.layout();

        labelText.applyCss();
        labelText.layout();

        ObservableList<String> tableItems = table.getItems();
        tableItems.add("Lorem ipsum");
        table.setItems(null);
        table.setItems(tableItems);

        ObservableList<String> listItems = outlineList.getItems();
        listItems.add("Lorem ipsum");
        outlineList.setItems(null);
        outlineList.setItems(listItems);
        outlineList.requestLayout();

        root.applyCss();
        root.layout();
        table.applyCss();
        table.layout();
        table.refresh();
        tableRow.applyCss();
        tableRow.layout();

        outlineList.applyCss();
        outlineList.layout();
        outlineList.refresh();
        outlineListCell.applyCss();
        outlineListCell.layout();

        listItems = nestedKeyList.getItems();
        listItems.add("Lorem ipsum");
        nestedKeyList.setItems(null);
        nestedKeyList.setItems(listItems);
        nestedKeyList.applyCss();
        nestedKeyList.layout();
        nestedKeyList.refresh();

        nestedKeyListCell.applyCss();
        nestedKeyListCell.layout();

        listItems = nestedList.getItems();
        listItems.add("Lorem ipsum");
        nestedList.setItems(null);
        nestedList.setItems(listItems);
        nestedList.applyCss();
        nestedList.layout();
        nestedList.refresh();
        nestedListCell.applyCss();
        nestedListCell.layout();

        valueFont = valueText.getFont();

        labelFont = valueFont;

        nestedListCellInsets = new Insets(nestedListCell.snappedTopInset(),
                                          nestedListCell.snappedRightInset(),
                                          nestedListCell.snappedBottomInset(),
                                          nestedListCell.snappedLeftInset());

        nestedListInsets = new Insets(nestedList.snappedTopInset(),
                                      nestedList.snappedRightInset(),
                                      nestedList.snappedBottomInset(),
                                      nestedList.snappedLeftInset());

        nestedKeyListCellInsets = new Insets(nestedKeyListCell.snappedTopInset(),
                                             nestedKeyListCell.snappedRightInset(),
                                             nestedKeyListCell.snappedBottomInset(),
                                             nestedKeyListCell.snappedLeftInset());

        nestedKeyListInsets = new Insets(nestedKeyList.snappedTopInset(),
                                         nestedKeyList.snappedRightInset(),
                                         nestedKeyList.snappedBottomInset(),
                                         nestedKeyList.snappedLeftInset());

        outlineListCellInsets = new Insets(outlineListCell.snappedTopInset(),
                                           outlineListCell.snappedRightInset(),
                                           outlineListCell.snappedBottomInset(),
                                           outlineListCell.snappedLeftInset());

        outlineListInsets = new Insets(outlineList.snappedTopInset(),
                                       outlineList.snappedRightInset(),
                                       outlineList.snappedBottomInset(),
                                       outlineList.snappedLeftInset());

        tableCellInsets = new Insets(tableCell.snappedTopInset(),
                                     tableCell.snappedRightInset(),
                                     tableCell.snappedBottomInset(),
                                     tableCell.snappedLeftInset());
        tableKeyCellInsets = new Insets(tableKeyCell.snappedTopInset(),
                                        tableKeyCell.snappedRightInset(),
                                        tableKeyCell.snappedBottomInset(),
                                        tableKeyCell.snappedLeftInset());

        tableInsets = new Insets(table.snappedTopInset(),
                                 table.snappedRightInset(),
                                 table.snappedBottomInset(),
                                 table.snappedLeftInset());

        tableRowInsets = new Insets(tableRow.snappedTopInset(),
                                    tableRow.snappedRightInset(),
                                    tableRow.snappedBottomInset(),
                                    tableRow.snappedLeftInset());
        Field contentField;
        try {
            contentField = TextAreaSkin.class.getDeclaredField("contentView");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        contentField.setAccessible(true);
        Region content;
        try {
            content = (Region) contentField.get(valueText.getSkin());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        valueInsets = new Insets(content.snappedTopInset(),
                                 content.snappedRightInset(),
                                 content.snappedBottomInset(),
                                 content.snappedLeftInset());

        labelInsets = valueInsets;
        valueLineHeight = FONT_LOADER.getFontMetrics(valueFont)
                                     .getLineHeight();
        labelLineHeight = FONT_LOADER.getFontMetrics(labelFont)
                                     .getLineHeight();
    }

    public double getLabelHorizontalInset() {
        return labelInsets.getLeft() + labelInsets.getRight();
    }

    public double getLabelLineHeight() {
        return labelLineHeight;
    }

    public double getLabelVerticalInset() {
        return labelInsets.getTop() + labelInsets.getBottom();
    }

    public LayoutModel getModel() {
        return model;
    }

    public double getNestedKeyListCellHorizontalInset() {
        return nestedKeyListCellInsets.getLeft()
               + nestedKeyListCellInsets.getRight();
    }

    public double getNestedKeyListCellVerticalInset() {
        return nestedKeyListCellInsets.getTop()
               + nestedKeyListCellInsets.getBottom();
    }

    public double getNestedKeyListHorizontalInset() {
        return nestedKeyListInsets.getLeft() + nestedKeyListInsets.getRight();
    }

    public double getNestedKeyListVerticalInset() {
        return nestedKeyListInsets.getTop() + nestedKeyListInsets.getBottom();
    }

    public double getNestedListCellHorizontalInset() {
        return nestedListCellInsets.getLeft() + nestedListCellInsets.getRight();
    }

    public double getNestedListCellVerticalInset() {
        return nestedListCellInsets.getTop() + nestedListCellInsets.getBottom();
    }

    public double getNestedListHorizontalInset() {
        return nestedListInsets.getLeft() + nestedListInsets.getRight();
    }

    public double getNestedListVerticalInset() {
        return nestedListInsets.getTop() + nestedListInsets.getBottom();
    }

    public double getOutlineListCellHorizontalInset() {
        return outlineListCellInsets.getLeft()
               + outlineListCellInsets.getRight();
    }

    public double getOutlineListCellVerticalInset() {
        return outlineListCellInsets.getTop()
               + outlineListCellInsets.getBottom();
    }

    public double getOutlineListHorizontalInset() {
        return outlineListInsets.getLeft() + outlineListInsets.getRight();
    }

    public double getOutlineListVerticalInset() {
        return outlineListInsets.getTop() + outlineListInsets.getBottom();
    }

    public double getTableCellHorizontalInset() {
        return tableCellInsets.getLeft() + tableCellInsets.getRight();
    }

    public double getTableCellVerticalInset() {
        return tableCellInsets.getTop() + tableCellInsets.getBottom();
    }

    public double getTableHorizontalInset() {
        return tableInsets.getLeft() + tableInsets.getRight();
    }

    public double getTableKeyCellHorizontalInset() {
        return tableKeyCellInsets.getLeft() + tableKeyCellInsets.getRight();
    }

    public double getTableKeyCellVerticalInset() {
        return tableKeyCellInsets.getTop() + tableKeyCellInsets.getBottom();
    }

    public double getTableRowHorizontalInset() {
        return tableRowInsets.getLeft() + tableRowInsets.getRight();
    }

    public double getTableRowVerticalInset() {
        return tableRowInsets.getTop() + tableRowInsets.getBottom();
    }

    public double getTableVerticalInset() {
        return tableInsets.getTop() + tableInsets.getBottom();
    }

    public double getValueHorizontalInset() {
        return valueInsets.getLeft() + valueInsets.getRight();
    }

    public double getValueLineHeight() {
        return valueLineHeight;
    }

    public double getValueVerticalInset() {
        return valueInsets.getTop() + valueInsets.getBottom();
    }

    public double labelWidth(String label) {
        return FONT_LOADER.computeStringWidth(String.format("W%sW\n", label),
                                              labelFont);
    }

    public double measureHeader(TableView<?> table) {
        Group root = new Group(table);
        Scene scene = new Scene(root);
        if (styleSheets != null) {
            scene.getStylesheets()
                 .addAll(styleSheets);
        }
        root.applyCss();
        root.layout();
        table.applyCss();
        table.layout();
        @SuppressWarnings("rawtypes")
        TableHeaderRow headerRow = ((TableViewSkinBase) table.getSkin()).getTableHeaderRow();
        root.getChildren()
            .clear();
        return headerRow.getHeight();
    }

    @Override
    public String toString() {
        return "Layout [labelFont=" + labelFont + "\n labelInsets="
               + labelInsets + "\n labelLineHeight=" + labelLineHeight
               + "\n model=" + model + "\n nestedKeyListCellInsets="
               + nestedKeyListCellInsets + "\n nestedKeyListInsets="
               + nestedKeyListInsets + "\n nestedListCellInsets="
               + nestedListCellInsets + "\n nestedListInsets="
               + nestedListInsets + "\n outlineListCellInsets="
               + outlineListCellInsets + "\n outlineListInsets="
               + outlineListInsets + "\n styleSheets=" + styleSheets
               + "\n tableCellInsets=" + tableCellInsets + "\n tableInsets="
               + tableInsets + "\n tableKeyCellInsets=" + tableKeyCellInsets
               + "\n valueFont=" + valueFont + "\n valueInsets=" + valueInsets
               + "\n valueLineHeight=" + valueLineHeight + "]";
    }

    public double valueDoubleSpaceWidth() {
        return FONT_LOADER.computeStringWidth("WW", valueFont);
    }

    public double valueWidth(String value) {
        return FONT_LOADER.computeStringWidth(String.format("W%sW\n", value),
                                              valueFont);
    }
}
