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

package com.chiralbehaviors.CoRE.meta.models;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.postgresql.pljava.TriggerData;

import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.event.status.StatusCode;
import com.chiralbehaviors.CoRE.event.status.StatusCodeAttribute;
import com.chiralbehaviors.CoRE.event.status.StatusCodeAttributeAuthorization;
import com.chiralbehaviors.CoRE.event.status.StatusCodeNetwork;
import com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing;
import com.chiralbehaviors.CoRE.jsp.JSP;
import com.chiralbehaviors.CoRE.jsp.StoredProcedure;
import com.chiralbehaviors.CoRE.kernel.Kernel;
import com.chiralbehaviors.CoRE.kernel.KernelImpl;
import com.chiralbehaviors.CoRE.meta.StatusCodeModel;
import com.chiralbehaviors.CoRE.network.Aspect;
import com.chiralbehaviors.CoRE.product.Product;

/**
 * @author hhildebrand
 * 
 */
public class StatusCodeModelImpl
        extends
        AbstractNetworkedModel<StatusCode, StatusCodeNetwork, StatusCodeAttributeAuthorization, StatusCodeAttribute>
        implements StatusCodeModel {

    private static class Call<T> implements StoredProcedure<T> {
        private final Procedure<T> procedure;

        public Call(Procedure<T> procedure) {
            this.procedure = procedure;
        }

        @Override
        public T call(EntityManager em) throws Exception {
            return procedure.call(new StatusCodeModelImpl(em));
        }

        @Override
        public String toString() {
            return "Call [" + procedure + "]";
        }
    }

    private static interface Procedure<T> {
        T call(StatusCodeModelImpl productModel) throws Exception;
    }

    public static void propagate_deductions(final TriggerData data)
                                                                   throws Exception {
        execute(new Procedure<Void>() {
            @Override
            public Void call(StatusCodeModelImpl agencyModel) throws Exception {
                agencyModel.propagate_network(data.getNew().getLong(1));
                return null;
            }
        });
    }

    public static void track_network_deleted(final TriggerData data)
                                                                    throws Exception {
        execute(new Procedure<Void>() {
            @Override
            public Void call(StatusCodeModelImpl agencyModel) throws Exception {
                agencyModel.networkEdgeDeleted(data.getOld().getLong("parent"),
                                               data.getOld().getLong("relationship"));
                return null;
            }
        });
    }

    private static <T> T execute(Procedure<T> procedure) throws SQLException {
        return JSP.call(new Call<T>(procedure));
    }

    /**
     * @param em
     */
    public StatusCodeModelImpl(EntityManager em) {
        super(em, new KernelImpl(em));
    }

    /**
     * @param em
     */
    public StatusCodeModelImpl(EntityManager em, Kernel kernel) {
        super(em, kernel);
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
        for (Attribute attribute : attributes) {
            StatusCodeAttributeAuthorization authorization = new StatusCodeAttributeAuthorization(
                                                                                                  aspect.getClassification(),
                                                                                                  aspect.getClassifier(),
                                                                                                  attribute,
                                                                                                  kernel.getCoreModel());
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
                                   Aspect<StatusCode> aspect,
                                   Aspect<StatusCode>... aspects) {
        StatusCode agency = new StatusCode(name, description,
                                           kernel.getCoreModel());
        em.persist(agency);
        initialize(agency, aspect);
        if (aspects != null) {
            for (Aspect<StatusCode> a : aspects) {
                initialize(agency, a);
            }
        }
        return agency;
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

    /**
     * @param agency
     * @param aspect
     */
    protected void initialize(StatusCode agency, Aspect<StatusCode> aspect) {
        agency.link(aspect.getClassification(), aspect.getClassifier(),
                    kernel.getCoreModel(), kernel.getInverseSoftware(), em);
        for (StatusCodeAttributeAuthorization authorization : getAttributeAuthorizations(aspect)) {
            StatusCodeAttribute attribute = new StatusCodeAttribute(
                                                                    authorization.getAuthorizedAttribute(),
                                                                    kernel.getCoreModel());
            attribute.setStatusCode(agency);
            defaultValue(attribute);
            em.persist(attribute);
        }
    }
}
