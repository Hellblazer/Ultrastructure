/**
 * AUTOGENERATED! DO NOT EDIT BY HAND.
 * Generated by WorkspaceGenerator.java
 *
 * (C) Copyright 2014 Chiral Behaviors, LLC. All Rights Reserved
 *
 
 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.workspace;

import static com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization.DOES_WORKSPACE_AUTH_EXIST;
import static com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization.GET_AUTHORIZATION;
import static com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization.GET_AUTHORIZATIONS_BY_TYPE;
import static com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization.GET_WORKSPACE;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.chiralbehaviors.CoRE.Ruleform;
import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.product.Product;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

@NamedQueries({ @NamedQuery(name = GET_WORKSPACE, query = "SELECT auth FROM WorkspaceAuthorization auth WHERE auth.definingProduct = :product"),
                @NamedQuery(name = GET_AUTHORIZATION, query = "SELECT auth FROM WorkspaceAuthorization auth "
                                                              + "WHERE auth.definingProduct = :product "
                                                              + "AND auth.key = :key"),
                @NamedQuery(name = GET_AUTHORIZATIONS_BY_TYPE, query = "SELECT auth FROM WorkspaceAuthorization auth "
                                                                       + "WHERE auth.definingProduct = :product "
                                                                       + "AND auth.type= :type"),
                @NamedQuery(name = DOES_WORKSPACE_AUTH_EXIST, query = "SELECT COUNT(auth) FROM WorkspaceAuthorization auth "
                                                                      + "WHERE auth.id = :id") })
@Entity
@Table(name = "workspace_authorization", schema = "ruleform")
@JsonAutoDetect(fieldVisibility = Visibility.PUBLIC_ONLY)
public class WorkspaceAuthorization extends Ruleform {
    public static final String DOES_WORKSPACE_AUTH_EXIST  = "workspaceAuthorization.doesAuthExist";
    public static final String GET_AUTHORIZATION          = "workspaceAuthorization.getAuthorization";
    public static final String GET_AUTHORIZATIONS_BY_TYPE = "workspaceAuthorization.getAuthorizationByType";
    public static final String GET_WORKSPACE              = "workspaceAuthorization.getWorkspace";

    private static final long serialVersionUID = 1L;

    public static String getWorkspaceAuthorizationColumnName(Class<?> ruleform) {
        StringBuilder builder = new StringBuilder();
        String simpleName = ruleform.getClass()
                                    .getSimpleName();
        builder.append(Character.toLowerCase(simpleName.charAt(0)));
        int i = 1;
        for (char c = simpleName.charAt(i); i < simpleName.length(); i++) {
            if (Character.isUpperCase(c)) {
                builder.append('_');
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(name = "defining_product")
    @JsonIgnore
    private Product definingProduct;
    private String  key;
    private String  type;

    public WorkspaceAuthorization() {
        super();
    }

    public WorkspaceAuthorization(Ruleform ruleform, Product definingProduct) {
        super();
        setRuleform(ruleform);
        setDefiningProduct(definingProduct);
    }

    public WorkspaceAuthorization(Ruleform ruleform, Product definingProduct,
                                  Agency updatedBy) {
        this(ruleform, definingProduct);
        setUpdatedBy(updatedBy);
    }

    public WorkspaceAuthorization(String key, Ruleform ruleform,
                                  Product definingProduct, Agency updatedBy) {
        this(ruleform, definingProduct, updatedBy);
        setKey(key);
    }

    public Product getDefiningProduct() {
        return definingProduct;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public void setDefiningProduct(Product definingProduct) {
        this.definingProduct = definingProduct;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRuleform(Ruleform ruleform) {
        ruleform.setWorkspace(this);
        type = ruleform.getClass()
                       .getSimpleName();
    }

    @Override
    public String toString() {
        return String.format("WorkspaceAuthorization [definingProduct=%s, key=%s, type=%s]",
                             definingProduct.getName(), key, type);
    }
}
