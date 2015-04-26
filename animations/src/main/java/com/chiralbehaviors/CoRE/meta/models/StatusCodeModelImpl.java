/**
 * (C) Copyright 2012 Chiral Behaviors, LLC. All Rights Reserved
 *
 
 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.meta.models;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.event.status.StatusCode;
import com.chiralbehaviors.CoRE.event.status.StatusCodeAttribute;
import com.chiralbehaviors.CoRE.event.status.StatusCodeAttributeAuthorization;
import com.chiralbehaviors.CoRE.event.status.StatusCodeNetwork;
import com.chiralbehaviors.CoRE.event.status.StatusCodeNetworkAuthorization;
import com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.StatusCodeModel;
import com.chiralbehaviors.CoRE.network.Aspect;
import com.chiralbehaviors.CoRE.network.Facet;
import com.chiralbehaviors.CoRE.product.Product;
import com.chiralbehaviors.CoRE.relationship.Relationship;

/**
 * @author hhildebrand
 *
 */
public class StatusCodeModelImpl
        extends
        AbstractNetworkedModel<StatusCode, StatusCodeNetwork, StatusCodeAttributeAuthorization, StatusCodeAttribute>
        implements StatusCodeModel {

    /**
     * @param em
     */
    public StatusCodeModelImpl(Model model) {
        super(model);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#authorize(com.chiralbehaviors.CoRE
     * .meta.Aspect, com.chiralbehaviors.CoRE.attribute.Attribute[])
     */
    @Override
    public void authorize(Aspect<StatusCode> aspect, Attribute... attributes) {
        StatusCodeNetworkAuthorization auth = new StatusCodeNetworkAuthorization(
                                                                                 kernel.getCore());
        auth.setClassifier(aspect.getClassifier());
        auth.setClassification(aspect.getClassification());
        em.persist(auth);
        for (Attribute attribute : attributes) {
            StatusCodeAttributeAuthorization authorization = new StatusCodeAttributeAuthorization(
                                                                                                  attribute,
                                                                                                  kernel.getCoreModel());
            authorization.setNetworkAuthorization(auth);
            em.persist(authorization);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#create(com.chiralbehaviors.CoRE.network
     * .Networked)
     */
    @Override
    public StatusCode create(StatusCode prototype) {
        StatusCode copy = prototype.clone();
        em.detach(copy);
        em.persist(copy);
        copy.setUpdatedBy(kernel.getCoreModel());
        for (StatusCodeNetwork network : prototype.getNetworkByParent()) {
            network.getParent().link(network.getRelationship(), copy,
                                     kernel.getCoreModel(),
                                     kernel.getInverseSoftware(), em);
        }
        for (StatusCodeAttribute attribute : prototype.getAttributes()) {
            StatusCodeAttribute clone = (StatusCodeAttribute) attribute.clone();
            em.detach(clone);
            em.persist(clone);
            clone.setStatusCode(copy);
            clone.setUpdatedBy(kernel.getCoreModel());
        }
        return copy;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.NetworkedModel#create(java.lang.String, java.lang.String, com.chiralbehaviors.CoRE.network.Aspect)
     */
    @Override
    public Facet<StatusCode, StatusCodeAttribute> create(String name,
                                                         String description,
                                                         Aspect<StatusCode> aspect,
                                                         Agency updatedBy) {
        StatusCode statusCode = new StatusCode(name, description,
                                               kernel.getCoreModel());
        em.persist(statusCode);
        return new Facet<StatusCode, StatusCodeAttribute>(
                                                          aspect,
                                                          statusCode,
                                                          initialize(statusCode,
                                                                     aspect,
                                                                     updatedBy)) {
        };
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#create(com.chiralbehaviors.CoRE.meta
     * .Aspect<RuleForm>[])
     */
    @SafeVarargs
    @Override
    public final StatusCode create(String name, String description,
                                   Aspect<StatusCode> aspect, Agency updatedBy,
                                   Aspect<StatusCode>... aspects) {
        StatusCode agency = new StatusCode(name, description,
                                           kernel.getCoreModel());
        em.persist(agency);
        initialize(agency, aspect, updatedBy);
        if (aspects != null) {
            for (Aspect<StatusCode> a : aspects) {
                initialize(agency, a, updatedBy);
            }
        }
        return agency;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.NetworkedModel#getInterconnections(java.util.List, java.util.List, java.util.List)
     */
    @Override
    public List<StatusCodeNetwork> getInterconnections(Collection<StatusCode> parents,
                                                       Collection<Relationship> relationships,
                                                       Collection<StatusCode> children) {
        TypedQuery<StatusCodeNetwork> query = em.createNamedQuery(StatusCodeNetwork.GET_NETWORKS,
                                                                  StatusCodeNetwork.class);
        query.setParameter("parents", parents);
        query.setParameter("relationship", relationships);
        query.setParameter("children", children);
        return query.getResultList();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.StatusCodeModel#getStatusCodes(com.chiralbehaviors.CoRE.product.Product)
     */
    @Override
    public Collection<StatusCode> getStatusCodes(Product service) {
        Set<StatusCode> codes = new HashSet<StatusCode>();
        TypedQuery<StatusCode> query = em.createNamedQuery(StatusCodeSequencing.GET_PARENT_STATUS_CODES_SERVICE,
                                                           StatusCode.class);
        query.setParameter("service", service);
        codes.addAll(query.getResultList());
        query = em.createNamedQuery(StatusCodeSequencing.GET_CHILD_STATUS_CODES_SERVICE,
                                    StatusCode.class);
        query.setParameter("service", service);
        codes.addAll(query.getResultList());
        return codes;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.StatusCodeModel#getStatusCodeSequencing(com.chiralbehaviors.CoRE.product.Product)
     */
    @Override
    public List<StatusCodeSequencing> getStatusCodeSequencing(Product service) {
        TypedQuery<StatusCodeSequencing> query = em.createNamedQuery(StatusCodeSequencing.GET_ALL_STATUS_CODE_SEQUENCING,
                                                                     StatusCodeSequencing.class);
        query.setParameter("service", service);
        return query.getResultList();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.StatusCodeModel#getStatusCodeSequencingChild(com.chiralbehaviors.CoRE.product.Product, com.chiralbehaviors.CoRE.event.status.StatusCode)
     */
    @Override
    public List<StatusCodeSequencing> getStatusCodeSequencingChild(Product service,
                                                                   StatusCode child) {
        TypedQuery<StatusCodeSequencing> query = em.createNamedQuery(StatusCodeSequencing.GET_CHILD_STATUS_CODE_SEQUENCING_SERVICE,
                                                                     StatusCodeSequencing.class);
        query.setParameter("service", service);
        query.setParameter("statusCode", child);
        return query.getResultList();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.StatusCodeModel#getStatusCodeSequencingChild(com.chiralbehaviors.CoRE.event.status.StatusCode)
     */
    @Override
    public Collection<StatusCodeSequencing> getStatusCodeSequencingChild(StatusCode child) {
        TypedQuery<StatusCodeSequencing> query = em.createNamedQuery(StatusCodeSequencing.GET_CHILD_STATUS_CODE_SEQUENCING,
                                                                     StatusCodeSequencing.class);
        query.setParameter("statusCode", child);
        return query.getResultList();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.StatusCodeModel#getStatusCodeSequencingParent(com.chiralbehaviors.CoRE.product.Product, com.chiralbehaviors.CoRE.event.status.StatusCode)
     */
    @Override
    public List<StatusCodeSequencing> getStatusCodeSequencingParent(Product service,
                                                                    StatusCode parent) {
        TypedQuery<StatusCodeSequencing> query = em.createNamedQuery(StatusCodeSequencing.GET_PARENT_STATUS_CODE_SEQUENCING_SERVICE,
                                                                     StatusCodeSequencing.class);
        query.setParameter("service", service);
        query.setParameter("statusCode", parent);
        return query.getResultList();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.StatusCodeModel#getStatusCodeSequencingParent(com.chiralbehaviors.CoRE.event.status.StatusCode)
     */
    @Override
    public List<StatusCodeSequencing> getStatusCodeSequencingParent(StatusCode parent) {
        TypedQuery<StatusCodeSequencing> query = em.createNamedQuery(StatusCodeSequencing.GET_PARENT_STATUS_CODE_SEQUENCING,
                                                                     StatusCodeSequencing.class);
        query.setParameter("statusCode", parent);
        return query.getResultList();
    }

    @Override
    public StatusCodeAttribute create(StatusCode ruleform, Attribute attribute,
                                      Agency updatedBy) {
        return new StatusCodeAttribute(ruleform, attribute, updatedBy);
    }
}
