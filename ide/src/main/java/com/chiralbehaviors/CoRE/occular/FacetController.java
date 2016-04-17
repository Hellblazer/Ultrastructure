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

package com.chiralbehaviors.CoRE.occular;

import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * @author hhildebrand
 *
 */
public class FacetController {
    private static String         QUERY = "";

    private GraphQlApi            api;

    @FXML
    private TableView<ObjectNode> attributes;

    @FXML
    private TableView<ObjectNode> children;

    @FXML
    private ComboBox<ObjectNode>  classification;

    @FXML
    private ComboBox<ObjectNode>  classifier;

    @FXML
    private TextField             name;

    public void setFacet(String id) {

    }

    public GraphQlApi getApi() {
        return api;
    }

    public void setApi(GraphQlApi api) {
        this.api = api;
    }

}
