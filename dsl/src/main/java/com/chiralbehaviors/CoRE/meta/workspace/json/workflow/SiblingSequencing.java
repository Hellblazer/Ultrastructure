/**
 * Copyright (c) 2018 Chiral Behaviors, LLC, all rights reserved.
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

package com.chiralbehaviors.CoRE.meta.workspace.json.workflow;

/**
 * @author halhildebrand
 *
 */
public class SiblingSequencing {
    public String  next;
    public String  parent;
    public boolean replace;
    public int     sequence;
    public String  sibling;
    public String  status;

    @Override
    public String toString() {
        return String.format("SiblingSequencing [parent=%s, status=%s, sibling=%s, next=%s, replace=%s, sequence=%s]",
                             parent, status, sibling, next, replace, sequence);
    }
}
