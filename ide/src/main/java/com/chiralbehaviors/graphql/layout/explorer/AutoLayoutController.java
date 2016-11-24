/**
f * Copyright (c) 2016 Chiral Behaviors, LLC, all rights reserved.
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

package com.chiralbehaviors.graphql.layout.explorer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chiralbehaviors.graphql.layout.AutoLayoutView;
import com.chiralbehaviors.graphql.layout.schema.Relation;
import com.chiralbehaviors.graphql.layout.schema.SchemaNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.javafx.webkit.WebConsoleListener;

import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

@SuppressWarnings("restriction")
public class AutoLayoutController {
    public class ActiveState extends QueryState {
        public ActiveState() {
            super();
        }

        @Override
        public void setData(String data) {
            super.setData(data);
            try {
                setQueryState(new QueryState(this));
            } catch (Exception e) {
                log.error("unable to set query state", e);
            }
        }
    }

    private static final String DATA        = "data";
    private static final String ERRORS      = "errors";
    private static final Logger log         = LoggerFactory.getLogger(AutoLayoutController.class);

    private final ActiveState   activeQuery = new ActiveState();
    @FXML
    private AnchorPane          anchor;
    private AutoLayoutView      layout;
    @FXML
    private ToggleGroup         page;
    private QueryState          queryState;
    @FXML
    private BorderPane          root;
    private SchemaView          schemaView;
    @FXML
    private RadioButton         showLayout;
    @FXML
    private RadioButton         showQuery;
    @FXML
    private RadioButton         showSchema;

    public AutoLayoutController(QueryState queryState) throws IOException {
        initialize();
        this.activeQuery.initializeFrom(queryState);
        this.queryState = queryState;
        load();
        anchor.getChildren()
              .add(layout);
        Node graphiql = constructGraphiql();
        showLayout.setSelected(true);
        page.selectedToggleProperty()
            .addListener((o, p, c) -> {
                try {
                    anchor.getChildren()
                          .clear();
                    RadioButton prev = (RadioButton) p;
                    RadioButton current = (RadioButton) c;

                    if (prev == showSchema) {
                        layout.autoLayout();
                    }

                    if (current == showLayout) {
                        anchor.getChildren()
                              .add(layout);
                    } else if (current == showSchema) {
                        anchor.getChildren()
                              .add(schemaView);
                    } else if (current == showQuery) {
                        anchor.getChildren()
                              .add(graphiql);
                    } else {
                        throw new IllegalStateException(String.format("Invalid radio button: %s",
                                                                      current));
                    }
                } catch (Exception e) {
                    log.error("exception processing toggle", e);
                }
            });
    }

    public Parent getRoot() {
        return root;
    }

    public AutoLayoutView getLayout() {
        return layout;
    }

    public void setQueryState(QueryState state) {
        String previousDataString = queryState.getData();
        if (state == null) {
            state = new QueryState();
        }
        if (queryState.equals(state)) {
            return;
        }
        queryState = state;
        if (previousDataString == state.getData()) {
            return;
        }
        JsonNode data;
        try {
            data = new ObjectMapper().readTree(queryState.getData());
        } catch (IOException e) {
            log.warn("Cannot deserialize json data {}", queryState.getData());
            data = JsonNodeFactory.instance.arrayNode();
        }

        JsonNode errors = data.get(ERRORS);
        if ((errors != null && errors.size() != 0) || !data.has(DATA)
            || !data.get(DATA)
                    .has(queryState.getSelection())) {
            queryState.setData(null);
            data = JsonNodeFactory.instance.arrayNode();
        } else {
            data = data.get(DATA)
                       .get(queryState.getSelection());
        }
        setData(SchemaNode.asArray(data));
    }

    private Node constructGraphiql() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chiralbehaviors/graphql/layout/graphiql.fxml"));
        Node graphiql = loader.load();
        AnchorPane.setTopAnchor(graphiql, 0.0);
        AnchorPane.setLeftAnchor(graphiql, 0.0);
        AnchorPane.setBottomAnchor(graphiql, 0.0);
        AnchorPane.setRightAnchor(graphiql, 0.0);
        GraphiqlController controller = loader.getController();
        WebEngine engine = controller.webview.getEngine();
        engine.getLoadWorker()
              .stateProperty()
              .addListener((o, oldState, newState) -> {
                  if (newState == State.SUCCEEDED) {
                      JSObject jsobj = (JSObject) engine.executeScript("window");
                      jsobj.call("setApp", activeQuery);
                  }
              });
        initialize(engine);
        controller.url.setText(queryState.getTargetURL());
        controller.url.textProperty()
                      .addListener((o, p, c) -> {
                          if (c != null) {
                              activeQuery.setTargetURL(c);
                              JSObject jsobj = (JSObject) engine.executeScript("window");
                              jsobj.setMember("app", activeQuery);
                              engine.reload();
                          }
                      });

        controller.selection.setText(queryState.getSelection());
        controller.selection.textProperty()
                            .addListener((o, p, c) -> {
                                if (c != null) {
                                    activeQuery.setSelection(c);
                                }
                            });

        return graphiql;
    }

    private void initialize() {
        layout = new AutoLayoutView();
        AnchorPane.setTopAnchor(layout, 0.0);
        AnchorPane.setLeftAnchor(layout, 0.0);
        AnchorPane.setBottomAnchor(layout, 0.0);
        AnchorPane.setRightAnchor(layout, 0.0);

        schemaView = new SchemaView();
        AnchorPane.setTopAnchor(schemaView, 0.0);
        AnchorPane.setLeftAnchor(schemaView, 0.0);
        AnchorPane.setBottomAnchor(schemaView, 0.0);
        AnchorPane.setRightAnchor(schemaView, 0.0);
    }

    private void initialize(WebEngine engine) {
        WebConsoleListener.setDefaultListener(new WebConsoleListener() {
            @Override
            public void messageAdded(WebView webView, String message,
                                     int lineNumber, String sourceId) {
                System.out.println("Console: [" + sourceId + ":" + lineNumber
                                   + "] " + message);

            }
        });
        engine.load(getClass().getResource("/com/chiralbehaviors/graphql/layout/ide.html")
                              .toExternalForm());
    }

    private void load() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setLocation(getClass().getResource("/com/chiralbehaviors/graphql/layout/autolayout.fxml"));
        loader.load();
    }

    private void setData(ArrayNode data) {
        assert data != null;
        if (queryState.getQuery() == null) {
            return;
        }
        Relation schema = (Relation) Relation.buildSchema(queryState.getQuery(),
                                                          queryState.getSelection());
        schemaView.setRoot(schema);
        layout.setRoot(schema);
        layout.measure(data);
        layout.setData(data);
        layout.autoLayout();
    }
}
