/**
 * Copyright (C) 2013 Hal Hildebrand. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.CoRE.authorization;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.hellblazer.CoRE.attribute.Unit;
import com.hellblazer.CoRE.resource.Resource;

/**
 * @author hhildebrand
 * 
 */
@Entity
@DiscriminatorValue(WorkspaceAuthorization.PRODUCT_UNIT)
public class WorkspaceUnitAuthorization extends WorkspaceAuthorization {

    private static final long serialVersionUID = 1L;

    {
        setAuthorizationType(WorkspaceAuthorization.PRODUCT_UNIT);
    }

    @ManyToOne
    @JoinColumn(name = "unit")
    protected Unit            unit;

    public WorkspaceUnitAuthorization() {
        super();
    }

    public WorkspaceUnitAuthorization(Long id) {
        super(id);
    }

    public WorkspaceUnitAuthorization(Long id, Resource updatedBy) {
        super(id, updatedBy);
    }

    public WorkspaceUnitAuthorization(Resource updatedBy) {
        super(updatedBy);
    }

    public WorkspaceUnitAuthorization(String notes) {
        super(notes);
    }

    public WorkspaceUnitAuthorization(String notes, Resource updatedBy) {
        super(notes, updatedBy);
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

}
