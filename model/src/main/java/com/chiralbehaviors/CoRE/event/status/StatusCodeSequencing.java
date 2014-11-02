/**
 * (C) Copyright 2012 Chiral Behaviors, LLC. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chiralbehaviors.CoRE.event.status;

import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.ENSURE_VALID_SERVICE_STATUS;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.GET_ALL_STATUS_CODE_SEQUENCING;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.GET_CHILD_STATUS_CODES_SERVICE;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.GET_CHILD_STATUS_CODE_SEQUENCING;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.GET_CHILD_STATUS_CODE_SEQUENCING_SERVICE;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.GET_PARENT_STATUS_CODES_SERVICE;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.GET_PARENT_STATUS_CODE_SEQUENCING;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.GET_PARENT_STATUS_CODE_SEQUENCING_SERVICE;
import static com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing.IS_VALID_NEXT_STATUS;

import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.metamodel.SingularAttribute;

import com.chiralbehaviors.CoRE.Ruleform;
import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.product.Product;
import com.chiralbehaviors.CoRE.product.ProductNetwork;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization_;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the status_code_sequencing database table.
 *
 */
@NamedQueries({
               @NamedQuery(name = ENSURE_VALID_SERVICE_STATUS, query = "SELECT COUNT(scs.id) "
                                                                       + "FROM StatusCodeSequencing AS scs "
                                                                       + "WHERE scs.service = :service "
                                                                       + "  AND (scs.parentCode = :code "
                                                                       + "       OR scs.childCode = :code)"),
               @NamedQuery(name = IS_VALID_NEXT_STATUS, query = "SELECT COUNT(scs.id) "
                                                                + "FROM StatusCodeSequencing AS scs "
                                                                + "WHERE scs.service = :service "
                                                                + "  AND scs.parentCode = :parentCode "
                                                                + "  AND scs.childCode = :childCode"),
               @NamedQuery(name = GET_PARENT_STATUS_CODES_SERVICE, query = "SELECT DISTINCT(scs.parentCode) "
                                                                           + " FROM StatusCodeSequencing scs "
                                                                           + " WHERE scs.service = :service"),
               @NamedQuery(name = GET_CHILD_STATUS_CODES_SERVICE, query = "SELECT DISTINCT(scs.childCode) "
                                                                          + " FROM StatusCodeSequencing scs "
                                                                          + " WHERE scs.service = :service"),
               @NamedQuery(name = GET_CHILD_STATUS_CODE_SEQUENCING_SERVICE, query = "SELECT scs FROM StatusCodeSequencing scs "
                                                                                    + " WHERE scs.service = :service"
                                                                                    + "   AND scs.childCode = :statusCode"),
               @NamedQuery(name = GET_PARENT_STATUS_CODE_SEQUENCING, query = "SELECT scs FROM StatusCodeSequencing scs "
                                                                             + " WHERE scs.parentCode = :statusCode"),
               @NamedQuery(name = GET_CHILD_STATUS_CODE_SEQUENCING, query = "SELECT scs FROM StatusCodeSequencing scs "
                                                                            + " WHERE scs.childCode = :statusCode"),
               @NamedQuery(name = GET_PARENT_STATUS_CODE_SEQUENCING_SERVICE, query = "SELECT scs FROM StatusCodeSequencing scs "
                                                                                     + " WHERE scs.service = :service"
                                                                                     + "   AND scs.parentCode = :statusCode"),
               @NamedQuery(name = GET_ALL_STATUS_CODE_SEQUENCING, query = "SELECT scs "
                                                                          + " FROM StatusCodeSequencing scs "
                                                                          + " WHERE scs.service = :service") })
@Entity
@Table(name = "status_code_sequencing", schema = "ruleform")
public class StatusCodeSequencing extends Ruleform {
    public static final String  ENSURE_VALID_SERVICE_STATUS               = "statusCodeSequencing.ensureValidServiceAndStatus";
    public static final String  GET_ALL_STATUS_CODE_SEQUENCING            = "statusCodeSequencing.getAllStatusCodeSequencing";
    public static final String  GET_CHILD_STATUS_CODE_SEQUENCING          = "statusCodeSequencing.getChildStatusCodeSequencing";
    public static final String  GET_CHILD_STATUS_CODE_SEQUENCING_SERVICE  = "statusCodeSequencing.getChildStatusCodeSequencingService";
    public static final String  GET_CHILD_STATUS_CODES_SERVICE            = "statusCodeSequencing.getChildStatusCodes";
    public static final String  GET_PARENT_STATUS_CODE_SEQUENCING         = "statusCodeSequencing.getParentStatusCodeSequencing";
    public static final String  GET_PARENT_STATUS_CODE_SEQUENCING_SERVICE = "statusCodeSequencing.getParentStatusCodeSequencingService";
    public static final String  GET_PARENT_STATUS_CODES_SERVICE           = "statusCodeSequencing.getParentStatusCodes";
    public static final String  IS_VALID_NEXT_STATUS                      = "statusCodeSequencing.isValidNextStatus";

    private static final long   serialVersionUID                          = 1L;

    // bi-directional many-to-one association to StatusCode
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(name = "child_code")
    private StatusCode          childCode;

    // bi-directional many-to-one association to StatusCode
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(name = "parent_code")
    private StatusCode          parentCode;

    // bi-directional many-to-one association to Event
    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST,
            CascadeType.DETACH })
    @JoinColumn(name = "service")
    private Product             service;

    // bi-directional many-to-one association to StatusCode
    @OneToMany(mappedBy = "child", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ProductNetwork> statusCodeByChild;

    // bi-directional many-to-one association to StatusCode
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ProductNetwork> statusCodeByParent;

    public StatusCodeSequencing() {
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Ruleform#getWorkspaceAuthAttribute()
     */
    @Override
    @JsonIgnore
    public SingularAttribute<WorkspaceAuthorization, StatusCodeSequencing> getWorkspaceAuthAttribute() {
        return WorkspaceAuthorization_.statusCodeSequencing;
    }

    /**
     * @param updatedBy
     */
    public StatusCodeSequencing(Agency updatedBy) {
        super(updatedBy);
    }

    public StatusCodeSequencing(Product service, StatusCode parent,
                                StatusCode child, Agency updatedBy) {
        super(updatedBy);
        this.service = service;
        parentCode = parent;
        childCode = child;
    }

    /**
     * @param notes
     */
    public StatusCodeSequencing(String notes) {
        super(notes);
    }

    /**
     * @param notes
     * @param updatedBy
     */
    public StatusCodeSequencing(String notes, Agency updatedBy) {
        super(notes, updatedBy);
    }

    /**
     * @param id
     */
    public StatusCodeSequencing(UUID id) {
        super(id);
    }

    /**
     * @param id
     * @param updatedBy
     */
    public StatusCodeSequencing(UUID id, Agency updatedBy) {
        super(id, updatedBy);
    }

    public StatusCode getChildCode() {
        return childCode;
    }

    public StatusCode getParentCode() {
        return parentCode;
    }

    /**
     * @return the service
     */
    public Product getService() {
        return service;
    }

    /**
     * @return the statusCodeByChild
     */
    public Set<ProductNetwork> getStatusCodeByChild() {
        return statusCodeByChild;
    }

    /**
     * @return the statusCodeByParent
     */
    public Set<ProductNetwork> getStatusCodeByParent() {
        return statusCodeByParent;
    }

    public void setChildCode(StatusCode statusCode1) {
        childCode = statusCode1;
    }

    public void setParentCode(StatusCode statusCode2) {
        parentCode = statusCode2;
    }

    /**
     * @param service
     *            the service to set
     */
    public void setService(Product service) {
        this.service = service;
    }

    /**
     * @param statusCodeByChild
     *            the statusCodeByChild to set
     */
    public void setStatusCodeByChild(Set<ProductNetwork> statusCodeByChild) {
        this.statusCodeByChild = statusCodeByChild;
    }

    /**
     * @param statusCodeByParent
     *            the statusCodeByParent to set
     */
    public void setStatusCodeByParent(Set<ProductNetwork> statusCodeByParent) {
        this.statusCodeByParent = statusCodeByParent;
    }
}
