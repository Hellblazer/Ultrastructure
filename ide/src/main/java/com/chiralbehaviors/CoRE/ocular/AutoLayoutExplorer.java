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

package com.chiralbehaviors.CoRE.ocular;

import java.io.FileInputStream;
import java.io.IOException;

import com.chiralbehaviors.graphql.layout.AutoLayout;
import com.chiralbehaviors.graphql.layout.Relation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellblazer.utils.Utils;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * A class to allow me to explore the autolayout. I hate UIs.
 * 
 * @author hhildebrand
 *
 */
public class AutoLayoutExplorer extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("AutoLayoutExplorer");
        initRootLayout(primaryStage);
    }

    public void initRootLayout(Stage primaryStage) throws IOException {
        AnchorPane rootLayout = new AnchorPane();

        String input = Utils.getDocument(new FileInputStream("src/test/resources/testQuery.gql"));
        String source = "allFilms";
        Relation schema = (Relation) AutoLayout.buildSchema(input, source);
        JsonNode data = new ObjectMapper().readTree(new FileInputStream("src/test/resources/testQuery.data"));
        data = data.get("data")
                   .get(source);
        schema.measure(data);
        //        ((Relation) schema.getChildren()
        //                          .get(0)).nestTables();

        Control control = schema.buildControl();
        // control.setPrefHeight(1024);
        rootLayout.setPrefWidth(schema.getTableColumnWidth());
        rootLayout.setPrefHeight(1024);
        rootLayout.getChildren()
                  .add(control);
        rootLayout.setMaxHeight(Double.MAX_VALUE);
        AnchorPane.setTopAnchor(control, 0.0);
        AnchorPane.setBottomAnchor(control, 0.0);
        AnchorPane.setLeftAnchor(control, 0.0);
        AnchorPane.setRightAnchor(control, 0.0);

        schema.setItems(control, data);

        ScrollPane scroller = new ScrollPane(rootLayout);
        scroller.setFitToWidth(true);

        Scene scene = new Scene(scroller);
        primaryStage.setScene(scene);

        primaryStage.show();
    }
}
