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
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * @author hhildebrand
 *
 */
public class Relation extends SchemaNode implements Cloneable {
    private int                    averageCardinality;
    private final List<SchemaNode> children          = new ArrayList<>();
    private int                    outlineLabelWidth = 0;
    private boolean                useTable          = false;

    public Relation(String label) {
        super(label);
    }

    public void addChild(SchemaNode child) {
        children.add(child);
        outlineLabelWidth = Math.max(child.label.length(), outlineLabelWidth);
    }

    public int getAverageCardinality() {
        return averageCardinality;
    }

    public List<SchemaNode> getChildren() {
        return children;
    }

    public int getOutlineLabelWidth() {
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

    @Override
    public String toString() {
        return String.format("Relation [%s]", getLabel());
    }

    @Override
    protected void measure(ArrayNode data) {
        if (data.isNull()) {
            return;
        }
        int sum = 0;
        for (SchemaNode child : children) {
            ArrayNode aggregate = JsonNodeFactory.instance.arrayNode();
            int cardSum = 0;
            for (JsonNode node : data) {
                JsonNode sub = node.get(child.getLabel());
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
        averageCardinality = sum / children.size();
    }
}
