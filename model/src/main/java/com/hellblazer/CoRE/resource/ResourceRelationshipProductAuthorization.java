/**
 * Copyright (C) 2013 Halloran Parry. All rights reserved.
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

package com.hellblazer.CoRE.resource;

import javax.persistence.DiscriminatorValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.hellblazer.CoRE.network.Relationship;
import com.hellblazer.CoRE.product.Product;

/**
 * @author Halloran Parry
 * 
 */
@javax.persistence.Entity
@DiscriminatorValue("product")
public class ResourceRelationshipProductAuthorization extends
        ResourceAuthorization {
    private static final long serialVersionUID = 1L;

    //bi-directional many-to-one association to Location
    @ManyToOne
    @JoinColumn(name = "product")
    private Product           product;

    public ResourceRelationshipProductAuthorization() {
        setRuleformType("product");
    }

    /**
	 * @param user
	 * @param owns
	 * @param channel1
	 * @param user2
	 */
	public ResourceRelationshipProductAuthorization(Resource resource,
			Relationship rel, Product product, Resource updatedBy) {
		this.resource = resource;
		this.product = product;
		this.relationship = rel;
		setUpdatedBy(updatedBy);
	}

	/**
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

}
