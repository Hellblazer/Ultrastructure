/**
 * (C) Copyright 2012 Chiral Behaviors, LLC. All Rights Reserved
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

package com.chiralbehaviors.CoRE.meta.models;

import static com.chiralbehaviors.CoRE.jooq.Tables.AGENCY_EXISTENTIAL_GROUPING;
import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL;
import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL_ATTRIBUTE;
import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL_ATTRIBUTE_AUTHORIZATION;
import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL_NETWORK;
import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL_NETWORK_ATTRIBUTE;
import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION;
import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL_NETWORK_AUTHORIZATION;
import static com.chiralbehaviors.CoRE.jooq.Tables.FACET;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;

import com.chiralbehaviors.CoRE.domain.Agency;
import com.chiralbehaviors.CoRE.domain.Attribute;
import com.chiralbehaviors.CoRE.domain.ExistentialRuleform;
import com.chiralbehaviors.CoRE.domain.Relationship;
import com.chiralbehaviors.CoRE.jooq.enums.ExistentialDomain;
import com.chiralbehaviors.CoRE.jooq.tables.AgencyExistentialGrouping;
import com.chiralbehaviors.CoRE.jooq.tables.ExistentialAttribute;
import com.chiralbehaviors.CoRE.jooq.tables.ExistentialAttributeAuthorization;
import com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAttributeAuthorization;
import com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAuthorization;
import com.chiralbehaviors.CoRE.jooq.tables.Facet;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialAttributeAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialAttributeRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialNetworkAttributeAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialNetworkAttributeRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialNetworkAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialNetworkRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.FacetRecord;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.PhantasmModel;
import com.chiralbehaviors.CoRE.meta.workspace.EditableWorkspace;
import com.fasterxml.jackson.databind.JsonNode;
import com.hellblazer.utils.Tuple;

/**
 * @author hhildebrand
 *
 */
public class PhantasmModelImpl implements PhantasmModel {
    private static final Integer ZERO = Integer.valueOf(0);

    private final DSLContext     create;
    private final Model          model;

    public PhantasmModelImpl(Model model) {
        this.model = model;
        create = model.create();
    }

    @Override
    public void authorize(ExistentialRuleform ruleform,
                          Relationship relationship,
                          ExistentialRuleform authorized) {
        ExistentialNetworkRecord auth = create.newRecord(EXISTENTIAL_NETWORK);
        auth.setParent(ruleform.getId());
        auth.setRelationship(relationship.getId());
        auth.setChild(authorized.getId());
        auth.setUpdatedBy(model.getCurrentPrincipal()
                               .getPrincipal()
                               .getId());
        auth.insert();

        ExistentialNetworkRecord inverse = create.newRecord(EXISTENTIAL_NETWORK);
        inverse.setParent(authorized.getId());
        inverse.setRelationship(relationship.getInverse());
        inverse.setChild(ruleform.getId());
        inverse.setUpdatedBy(model.getCurrentPrincipal()
                                  .getPrincipal()
                                  .getId());
        inverse.insert();
    }

    @Override
    public void authorize(FacetRecord facet, Attribute attribute) {
        ExistentialAttributeAuthorizationRecord record = model.records()
                                                              .newExistentialAttributeAuthorization(facet,
                                                                                                    attribute);
        record.insert();
    }

    @Override
    public ExistentialRecord find(ExistentialAttribute attributeValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void authorizeAll(ExistentialRuleform ruleform,
                             Relationship relationship,
                             List<? extends ExistentialRuleform> authorized) {
        for (ExistentialRuleform agency : authorized) {
            authorize(ruleform, relationship, agency);
        }
    }

    @Override
    public void authorizeSingular(ExistentialRuleform ruleform,
                                  Relationship relationship,
                                  ExistentialRuleform authorized) {
        deauthorize(ruleform, relationship,
                    getImmediateChild(ruleform, relationship,
                                      authorized.getDomain()));
        authorize(ruleform, relationship, authorized);
    }

    @Override
    public boolean checkCapability(ExistentialAttributeAuthorizationRecord stateAuth,
                                   Relationship capability) {
        return checkCapability(model.getCurrentPrincipal()
                                    .getCapabilities(),
                               stateAuth, capability);
    }

    @Override
    public boolean checkCapability(ExistentialNetworkAttributeAuthorizationRecord stateAuth,
                                   Relationship capability) {
        return checkCapability(model.getCurrentPrincipal()
                                    .getCapabilities(),
                               stateAuth, capability);
    }

    @Override
    public boolean checkCapability(ExistentialNetworkAuthorizationRecord auth,
                                   Relationship capability) {
        return checkCapability(model.getCurrentPrincipal()
                                    .getCapabilities(),
                               auth, capability);
    }

    @Override
    public boolean checkCapability(ExistentialRuleform instance,
                                   Relationship capability) {
        return checkCapability(model.getCurrentPrincipal()
                                    .getCapabilities(),
                               instance, capability);
    }

    @Override
    public boolean checkCapability(FacetRecord facet, Relationship capability) {
        return checkCapability(model.getCurrentPrincipal()
                                    .getCapabilities(),
                               facet, capability);
    }

    /**
     * Check the capability of an agency on an attribute of a ruleform.
     */
    @Override
    public boolean checkCapability(List<Agency> agencies,
                                   ExistentialAttributeAuthorizationRecord stateAuth,
                                   Relationship capability) {
        ExistentialAttributeAuthorization required = EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.as("required");
        return ZERO.equals(create.selectCount()
                                 .from(required)
                                 .where(required.field(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY)
                                                .isNotNull())
                                 .and(required.field(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.FACET)
                                              .equal(stateAuth.getFacet()))
                                 .and(required.field(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORIZED_ATTRIBUTE)
                                              .equal(stateAuth.getAuthorizedAttribute()))
                                 .andNotExists(create.select(required.field(EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION.AUTHORITY))
                                                     .from(EXISTENTIAL_NETWORK)
                                                     .where(EXISTENTIAL_NETWORK.PARENT.in(agencies.stream()
                                                                                                  .map(a -> a.getId())
                                                                                                  .collect(Collectors.toList())))
                                                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(capability.getId()))
                                                     .and(EXISTENTIAL_NETWORK.CHILD.equal(required.field(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY))))
                                 .fetchOne()
                                 .value1());

    }

    /**
     * Check the capability of an agency on an attribute of the authorized
     * relationship of the facet child relationship.
     */
    @Override
    public boolean checkCapability(List<Agency> agencies,
                                   ExistentialNetworkAttributeAuthorizationRecord stateAuth,
                                   Relationship capability) {
        ExistentialNetworkAttributeAuthorization required = EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION.as("required");
        return ZERO.equals(create.selectCount()
                                 .from(required)
                                 .where(EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION.AUTHORITY.isNotNull())
                                 .and(EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION.NETWORK_AUTHORIZATION.equal(stateAuth.getNetworkAuthorization()))
                                 .and(EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION.AUTHORIZED_ATTRIBUTE.equal(stateAuth.getAuthorizedAttribute()))
                                 .andNotExists(create.select(required.field(EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION.AUTHORITY))
                                                     .from(EXISTENTIAL_NETWORK)
                                                     .where(EXISTENTIAL_NETWORK.PARENT.in(agencies.stream()
                                                                                                  .map(a -> a.getId())
                                                                                                  .collect(Collectors.toList())))
                                                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(capability.getId()))
                                                     .and(EXISTENTIAL_NETWORK.CHILD.equal(required.field(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY))))
                                 .fetchOne()
                                 .value1());
    }

    /**
     * Check the capability of an agency on the authorized relationship of the
     * facet child relationship.
     */
    @Override
    public boolean checkCapability(List<Agency> agencies,
                                   ExistentialNetworkAuthorizationRecord stateAuth,
                                   Relationship capability) {
        ExistentialNetworkAuthorization required = EXISTENTIAL_NETWORK_AUTHORIZATION.as("required");
        return ZERO.equals(create.selectCount()
                                 .from(required)
                                 .where(required.field(EXISTENTIAL_NETWORK_AUTHORIZATION.AUTHORITY)
                                                .isNotNull())
                                 .and(required.field(EXISTENTIAL_NETWORK_AUTHORIZATION.PARENT)
                                              .equal(stateAuth.getParent()))
                                 .and(required.field(EXISTENTIAL_NETWORK_AUTHORIZATION.RELATIONSHIP)
                                              .equal(stateAuth.getRelationship()))
                                 .and(required.field(EXISTENTIAL_NETWORK_AUTHORIZATION.CHILD)
                                              .equal(stateAuth.getChild()))
                                 .andNotExists(create.select(required.field(EXISTENTIAL_NETWORK_ATTRIBUTE_AUTHORIZATION.AUTHORITY))
                                                     .from(EXISTENTIAL_NETWORK)
                                                     .where(EXISTENTIAL_NETWORK.PARENT.in(agencies.stream()
                                                                                                  .map(a -> a.getId())
                                                                                                  .collect(Collectors.toList())))
                                                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(capability.getId()))
                                                     .and(EXISTENTIAL_NETWORK.CHILD.equal(required.field(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY))))
                                 .fetchOne()
                                 .value1());
    }

    /**
     * Check the capability of an agency on an instance.
     */
    @Override
    public boolean checkCapability(List<Agency> agencies,
                                   ExistentialRuleform instance,
                                   Relationship capability) {
        if (instance == null) {
            return true;
        }
        AgencyExistentialGrouping required = AGENCY_EXISTENTIAL_GROUPING.as("required");
        return ZERO.equals(create.selectCount()
                                 .from(required)
                                 .where(required.ENTITY.equal(instance.getId()))
                                 .andNotExists(create.select(required.field(EXISTENTIAL_NETWORK.AUTHORITY))
                                                     .from(EXISTENTIAL_NETWORK)
                                                     .where(EXISTENTIAL_NETWORK.PARENT.in(agencies.stream()
                                                                                                  .map(a -> a.getId())
                                                                                                  .collect(Collectors.toList())))
                                                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(capability.getId()))
                                                     .and(EXISTENTIAL_NETWORK.CHILD.equal(required.field(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY))))
                                 .fetchOne()
                                 .value1());
    }

    /**
     * Check the capability of an agency on the facet.
     */
    @Override
    public boolean checkCapability(List<Agency> agencies, FacetRecord facet,
                                   Relationship capability) {
        Facet required = FACET.as("required");
        return ZERO.equals(create.selectCount()
                                 .from(required)
                                 .where(required.field(FACET.AUTHORITY)
                                                .isNotNull())
                                 .and(required.field(FACET.CLASSIFIER)
                                              .equal(facet.getClassifier()))
                                 .and(required.field(FACET.CLASSIFICATION)
                                              .equal(facet.getClassification()))
                                 .andNotExists(create.select(required.field(FACET.AUTHORITY))
                                                     .from(EXISTENTIAL_NETWORK)
                                                     .where(EXISTENTIAL_NETWORK.PARENT.in(agencies.stream()
                                                                                                  .map(a -> a.getId())
                                                                                                  .collect(Collectors.toList())))
                                                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(capability.getId()))
                                                     .and(EXISTENTIAL_NETWORK.CHILD.equal(required.field(FACET.AUTHORITY))))
                                 .fetchOne()
                                 .value1());
    }

    @Override
    public ExistentialAttributeRecord create(ExistentialRuleform existential,
                                             Attribute attribute) {
        ExistentialAttributeRecord value = model.records()
                                                .newExistentialAttribute();
        value.setAttribute(attribute.getId());
        value.setExistential(existential.getId());
        return value;
    }

    @Override
    public void deauthorize(ExistentialRuleform existential,
                            Relationship relationship,
                            ExistentialRuleform authorized) {
        create.deleteFrom(EXISTENTIAL_NETWORK)
              .where(EXISTENTIAL_NETWORK.PARENT.equal(existential.getId()))
              .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
              .and(EXISTENTIAL_NETWORK.PARENT.equal(authorized.getId()))
              .execute();
        create.deleteFrom(EXISTENTIAL_NETWORK)
              .where(EXISTENTIAL_NETWORK.PARENT.equal(authorized.getId()))
              .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getInverse()))
              .and(EXISTENTIAL_NETWORK.PARENT.equal(existential.getId()))
              .execute();
    }

    @Override
    public void deauthorizeAll(ExistentialRuleform existential,
                               Relationship relationship,
                               List<? extends ExistentialRuleform> authorized) {
        for (ExistentialRuleform e : authorized) {
            deauthorize(existential, relationship, e);
        }
    }

    @Override
    public List<? extends ExistentialRuleform> getAllAuthorized(ExistentialRuleform ruleform,
                                                                Relationship relationship,
                                                                ExistentialDomain domain) {
        return create.selectDistinct(EXISTENTIAL.fields())
                     .from(EXISTENTIAL)
                     .join(EXISTENTIAL_NETWORK)
                     .on(EXISTENTIAL_NETWORK.PARENT.equal(ruleform.getId()))
                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                     .where(EXISTENTIAL.DOMAIN.equal(domain))
                     .and(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                     .fetch()
                     .into(ExistentialRecord.class)
                     .stream()
                     .map(r -> model.records()
                                    .resolve(r))
                     .collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#getAttributeAuthorizations(com
     * .hellblazer.CoRE.meta.Aspect, com.chiralbehaviors.CoRE.attribute.Attribute)
     */
    @Override
    public List<ExistentialAttributeAuthorizationRecord> getAttributeAuthorizations(FacetRecord aspect,
                                                                                    Attribute attribute) {

        return create.selectDistinct(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.fields())
                     .from(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.as("auth"))
                     .join(FACET.as("na"))
                     .on(FACET.ID.eq(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.FACET))
                     .join(EXISTENTIAL_NETWORK.as("network"))
                     .on(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(aspect.getClassifier()))
                     .and(EXISTENTIAL_NETWORK.CHILD.equal(aspect.getClassification()))
                     .and(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORIZED_ATTRIBUTE.equal(attribute.getId()))
                     .and(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY.isNull())
                     .fetch()
                     .into(ExistentialAttributeAuthorizationRecord.class);
    }

    @Override
    public List<ExistentialAttributeAuthorizationRecord> getAttributeAuthorizations(FacetRecord aspect,
                                                                                    boolean includeGrouping) {

        SelectConditionStep<Record> and = create.selectDistinct(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.fields())
                                                .from(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION)
                                                .where(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.FACET.equal(aspect.getId()));
        if (!includeGrouping) {
            and = and.and(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY.isNull());
        }
        return and.fetch()
                  .into(ExistentialAttributeAuthorizationRecord.class)
                  .stream()
                  .collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#getAttributesClassifiedBy(com
     * .hellblazer.CoRE.ExistentialRuleform, com.chiralbehaviors.CoRE.meta.Aspect)
     */
    @Override
    public List<ExistentialAttributeRecord> getAttributesClassifiedBy(ExistentialRuleform ruleform,
                                                                      FacetRecord aspect) {
        return create.selectDistinct(EXISTENTIAL_ATTRIBUTE.fields())
                     .from(EXISTENTIAL_ATTRIBUTE.as("attrValue"))

                     .join(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.as("auth"))
                     .on(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.FACET.equal(FACET.ID))

                     .join(FACET.as("na"))
                     .on(FACET.ID.eq(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.ID))
                     .and(FACET.CLASSIFICATION.eq(aspect.getClassification()))
                     .and(FACET.CLASSIFIER.eq(aspect.getClassifier()))

                     .join(EXISTENTIAL_NETWORK.as("network"))
                     .on(EXISTENTIAL_NETWORK.PARENT.equal(ruleform.getId()))
                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(FACET.CLASSIFIER))
                     .and(EXISTENTIAL_NETWORK.CHILD.equal(FACET.CLASSIFICATION))

                     .where(EXISTENTIAL_ATTRIBUTE.ATTRIBUTE.equal(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORIZED_ATTRIBUTE))
                     .and(EXISTENTIAL_ATTRIBUTE.EXISTENTIAL.eq(ruleform.getId()))
                     .fetch()
                     .into(ExistentialAttributeRecord.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#getAttributesGroupedBy(com.chiralbehaviors
     * .CoRE.ExistentialRuleform, com.chiralbehaviors.CoRE.agency.Agency)
     */
    @Override
    public List<ExistentialAttributeRecord> getAttributesGroupedBy(ExistentialRuleform ruleform,
                                                                   Agency groupingAgency) {
        return create.selectDistinct(EXISTENTIAL_ATTRIBUTE.fields())
                     .from(EXISTENTIAL_ATTRIBUTE)
                     .join(FACET)
                     .on(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(FACET.CLASSIFIER))
                     .join(FACET)
                     .on(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.FACET.eq(FACET.ID))
                     .join(EXISTENTIAL_NETWORK)
                     .on(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(FACET.CLASSIFIER))
                     .and(EXISTENTIAL_NETWORK.CHILD.equal(FACET.CLASSIFICATION))
                     .and(EXISTENTIAL_ATTRIBUTE.EXISTENTIAL.eq(ruleform.getId()))
                     .and(EXISTENTIAL_ATTRIBUTE_AUTHORIZATION.AUTHORITY.eq(groupingAgency.getId()))
                     .fetch()
                     .into(ExistentialAttributeRecord.class);
    }

    @Override
    public ExistentialNetworkAttributeRecord getAttributeValue(ExistentialNetworkRecord edge,
                                                               Attribute attribute) {
        ExistentialNetworkAttributeRecord result = create.selectFrom(EXISTENTIAL_NETWORK_ATTRIBUTE)
                                                         .where(EXISTENTIAL_NETWORK_ATTRIBUTE.EDGE.eq(edge.getId()))
                                                         .and(EXISTENTIAL_NETWORK_ATTRIBUTE.ATTRIBUTE.eq(attribute.getId()))
                                                         .fetchOne();
        if (result == null) {
            return null;
        }
        return result.into(ExistentialNetworkAttributeRecord.class);
    }

    @Override
    public ExistentialAttributeRecord getAttributeValue(ExistentialRuleform ruleform,
                                                        Attribute attribute) {
        List<ExistentialAttributeRecord> values = getAttributeValues(ruleform,
                                                                     attribute);
        if (values.size() > 1) {
            throw new IllegalStateException(String.format("%s has multiple values for %s",
                                                          attribute, ruleform));
        }
        if (values.size() == 0) {
            return null;
        }
        return values.get(0);
    }

    @Override
    public ExistentialNetworkAttributeRecord getAttributeValue(ExistentialRuleform parent,
                                                               Relationship r,
                                                               ExistentialRuleform child,
                                                               Attribute attribute) {
        ExistentialNetworkRecord edge = getImmediateChildLink(parent, r, child);
        if (edge == null) {
            return null;
        }
        return getAttributeValue(edge, attribute);
    }

    @Override
    public List<ExistentialAttributeRecord> getAttributeValues(ExistentialRuleform ruleform,
                                                               Attribute attribute) {
        return create.selectFrom(EXISTENTIAL_ATTRIBUTE)
                     .where(EXISTENTIAL_ATTRIBUTE.EXISTENTIAL.eq(ruleform.getId()))
                     .and(EXISTENTIAL_ATTRIBUTE.ATTRIBUTE.eq(attribute.getId()))
                     .orderBy(EXISTENTIAL_ATTRIBUTE.SEQUENCE_NUMBER)
                     .fetch()
                     .into(ExistentialAttributeRecord.class);
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.NetworkedModel#getChild(com.chiralbehaviors.CoRE.ExistentialRuleform, com.chiralbehaviors.CoRE.network.Relationship)
     */
    @Override
    public ExistentialRuleform getChild(ExistentialRuleform parent,
                                        Relationship relationship,
                                        ExistentialDomain domain) {
        Record result = create.selectDistinct(EXISTENTIAL.fields())
                              .from(EXISTENTIAL)
                              .join(EXISTENTIAL_NETWORK)
                              .on(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                              .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                              .and(EXISTENTIAL_NETWORK.CHILD.equal(EXISTENTIAL.ID))
                              .and(EXISTENTIAL.DOMAIN.equal(domain))
                              .fetchOne();
        if (result == null) {
            return null;
        }
        return model.records()
                    .resolve(result.into(ExistentialRecord.class));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#getNetwork(com.chiralbehaviors.CoRE
     * .network.Networked, com.chiralbehaviors.CoRE.network.Relationship)
     */
    @Override
    public List<ExistentialRuleform> getChildren(ExistentialRuleform parent,
                                                 Relationship relationship,
                                                 ExistentialDomain domain) {
        return create.selectDistinct(EXISTENTIAL.fields())
                     .from(EXISTENTIAL)
                     .join(EXISTENTIAL_NETWORK)
                     .on(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                     .and(EXISTENTIAL_NETWORK.CHILD.equal(EXISTENTIAL.ID))
                     .and(EXISTENTIAL.DOMAIN.equal(domain))
                     .fetch()
                     .into(ExistentialRecord.class)
                     .stream()
                     .map(r -> model.records()
                                    .resolve(r))
                     .collect(Collectors.toList());
    }

    @Override
    public FacetRecord getFacetDeclaration(Relationship classifier,
                                           ExistentialRuleform classification) {
        return create.selectFrom(FACET)
                     .where(FACET.CLASSIFIER.equal(classifier.getId()))
                     .and(FACET.CLASSIFICATION.equal(classification.getId()))
                     .fetchOne();
    }

    @Override
    public ExistentialRuleform getImmediateChild(ExistentialRuleform parent,
                                                 Relationship relationship,
                                                 ExistentialDomain domain) {
        Record result = create.selectDistinct(EXISTENTIAL.fields())
                              .from(EXISTENTIAL)
                              .join(EXISTENTIAL_NETWORK)
                              .on(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                              .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                              .and(EXISTENTIAL_NETWORK.CHILD.equal(EXISTENTIAL.ID))
                              .and(EXISTENTIAL.DOMAIN.equal(domain))
                              .and(EXISTENTIAL_NETWORK.INFERENCE.isNull())
                              .fetchOne();
        if (result == null) {
            return null;
        }
        return model.records()
                    .resolve(result.into(ExistentialRecord.class));
    }

    @Override
    public ExistentialNetworkRecord getImmediateChildLink(ExistentialRuleform parent,
                                                          Relationship relationship,
                                                          ExistentialRuleform child) {
        ExistentialNetworkRecord result = create.selectFrom(EXISTENTIAL_NETWORK)
                                                .where(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                                                .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                                                .and(EXISTENTIAL_NETWORK.CHILD.equal(child.getId()))
                                                .and(EXISTENTIAL_NETWORK.INFERENCE.isNull())
                                                .fetchOne();
        if (result == null) {
            return null;
        }
        return result.into(ExistentialNetworkRecord.class);
    }

    @Override
    public List<ExistentialRuleform> getImmediateChildren(ExistentialRuleform parent,
                                                          Relationship relationship,
                                                          ExistentialDomain domain) {
        return getImmediateChildren(parent.getId(), relationship.getId(),
                                    domain);
    }

    @Override
    public List<ExistentialNetworkRecord> getImmediateChildrenLinks(ExistentialRuleform parent,
                                                                    Relationship relationship,
                                                                    ExistentialDomain domain) {
        Result<Record> result = create.selectDistinct(EXISTENTIAL_NETWORK.fields())
                                      .from(EXISTENTIAL_NETWORK)
                                      .join(EXISTENTIAL)
                                      .on(EXISTENTIAL.DOMAIN.equal(domain))
                                      .and(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                                      .where(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                                      .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                                      .and(EXISTENTIAL_NETWORK.INFERENCE.isNull())
                                      .fetch();
        if (result == null) {
            return null;
        }
        return result.into(ExistentialNetworkRecord.class);
    }

    /**
     * @param parent
     * @param relationship
     * @return
     */
    @Override
    public ExistentialNetworkRecord getImmediateLink(ExistentialRuleform parent,
                                                     Relationship relationship,
                                                     ExistentialRuleform child) {
        ExistentialNetworkRecord result = create.selectFrom(EXISTENTIAL_NETWORK)
                                                .where(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                                                .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                                                .and(EXISTENTIAL_NETWORK.CHILD.equal(child.getId()))
                                                .and(EXISTENTIAL_NETWORK.INFERENCE.isNull())
                                                .fetchOne();
        if (result == null) {
            return null;
        }
        return result.into(ExistentialNetworkRecord.class);
    }

    @Override
    public Collection<ExistentialNetworkRecord> getImmediateNetworkEdges(ExistentialRuleform parent,
                                                                         ExistentialDomain domain) {
        return create.selectDistinct(EXISTENTIAL_NETWORK.fields())
                     .from(EXISTENTIAL_NETWORK)
                     .join(EXISTENTIAL)
                     .on(EXISTENTIAL.DOMAIN.equal(domain))
                     .and(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                     .where(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                     .and(EXISTENTIAL_NETWORK.INFERENCE.isNull())
                     .fetch()
                     .into(ExistentialNetworkRecord.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.NetworkedModel#getImmediateRelationships(com
     * .hellblazer.CoRE.ExistentialRuleform)
     */
    @Override
    public Collection<Relationship> getImmediateRelationships(ExistentialRuleform parent,
                                                              ExistentialDomain domain) {
        return create.selectDistinct(EXISTENTIAL.fields())
                     .from(EXISTENTIAL)
                     .join(EXISTENTIAL_NETWORK)
                     .on(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(EXISTENTIAL_NETWORK.ID))
                     .where(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                     .and(EXISTENTIAL.DOMAIN.equal(domain))
                     .fetch()
                     .into(Relationship.class);
    }

    @Override
    public List<ExistentialRuleform> getInferredChildren(ExistentialRuleform parent,
                                                         Relationship relationship,
                                                         ExistentialDomain domain) {
        return create.selectDistinct(EXISTENTIAL.fields())
                     .from(EXISTENTIAL)
                     .join(EXISTENTIAL_NETWORK)
                     .on(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                     .and(EXISTENTIAL_NETWORK.INFERENCE.isNotNull())
                     .where(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                     .and(EXISTENTIAL.DOMAIN.equal(domain))
                     .fetch()
                     .into(ExistentialRecord.class)
                     .stream()
                     .map(r -> model.records()
                                    .resolve(r))
                     .collect(Collectors.toList());
    }

    @Override
    public List<ExistentialRuleform> getInGroup(ExistentialRuleform parent,
                                                Relationship relationship,
                                                ExistentialDomain domain) {
        return create.selectDistinct(EXISTENTIAL.fields())
                     .from(EXISTENTIAL)
                     .join(EXISTENTIAL_NETWORK)
                     .on(EXISTENTIAL_NETWORK.CHILD.equal(EXISTENTIAL.ID))
                     .and(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                     .and(EXISTENTIAL_NETWORK.CHILD.notEqual(parent.getId()))
                     .where(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                     .and(EXISTENTIAL.DOMAIN.equal(domain))
                     .fetch()
                     .into(ExistentialRecord.class)
                     .stream()
                     .map(r -> model.records()
                                    .resolve(r))
                     .collect(Collectors.toList());
    }

    @Override
    public List<ExistentialNetworkRecord> getInterconnections(Collection<ExistentialRuleform> parents,
                                                              Collection<Relationship> relationships,
                                                              Collection<ExistentialRuleform> children) {
        //        if (parents == null || parents.size() == 0 || relationships == null
        //            || relationships.size() == 0 || children == null
        //            || children.size() == 0) {
        //            return null;
        //        }
        //        TypedQuery<RelationshipNetwork> query = em.createNamedQuery(RelationshipNetwork.GET_NETWORKS,
        //                                                                    RelationshipNetwork.class);
        //        query.setParameter("parents", parents);
        //        query.setParameter("relationships", relationships);
        //        query.setParameter("children", children);
        //        return query.getResultList();
        return null;
    }

    @Override
    public List<ExistentialNetworkAuthorizationRecord> getNetworkAuthorizations(FacetRecord aspect,
                                                                                boolean includeGrouping) {

        SelectConditionStep<ExistentialNetworkAuthorizationRecord> and = create.selectFrom(EXISTENTIAL_NETWORK_AUTHORIZATION)
                                                                               .where(EXISTENTIAL_NETWORK_AUTHORIZATION.PARENT.equal(aspect.getId()));

        if (!includeGrouping) {
            and = and.and(EXISTENTIAL_NETWORK_AUTHORIZATION.AUTHORITY.isNull());
        }
        return and.fetch()
                  .into(ExistentialNetworkAuthorizationRecord.class);
    }

    @Override
    public List<ExistentialRuleform> getNotInGroup(ExistentialRuleform parent,
                                                   Relationship relationship,
                                                   ExistentialDomain domain) {

        return create.selectFrom(EXISTENTIAL)
                     .whereNotExists(create.selectFrom(EXISTENTIAL_NETWORK)
                                           .where(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                                           .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                                           .and(EXISTENTIAL_NETWORK.CHILD.equal(EXISTENTIAL.ID)))
                     .and(EXISTENTIAL.DOMAIN.equal(domain))
                     .fetch()
                     .into(ExistentialRecord.class)
                     .stream()
                     .map(r -> model.records()
                                    .resolve(r))
                     .collect(Collectors.toList());
    }

    @Override
    public ExistentialRuleform getSingleChild(ExistentialRuleform parent,
                                              Relationship relationship,
                                              ExistentialDomain domain) {
        Record result = create.selectDistinct(EXISTENTIAL.fields())
                              .from(EXISTENTIAL)
                              .join(EXISTENTIAL_NETWORK)
                              .on(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                              .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                              .and(EXISTENTIAL_NETWORK.CHILD.equal(EXISTENTIAL.ID))
                              .where(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                              .and(EXISTENTIAL.DOMAIN.equal(domain))
                              .fetchOne();
        if (result == null) {
            return null;
        }
        return model.records()
                    .resolve(result.into(ExistentialRecord.class));
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.PhantasmModel#getTransitiveRelationships(com.chiralbehaviors.CoRE.domain.ExistentialRuleform, com.chiralbehaviors.CoRE.jooq.enums.ExistentialDomain)
     */
    @Override
    public List<ExistentialRuleform> getTransitiveRelationships(ExistentialRuleform a,
                                                                ExistentialDomain damain) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.PhantasmModel#getValue(com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialAttributeRecord)
     */
    @Override
    public Object getValue(ExistentialAttributeRecord attributeValue) {
        Attribute attribute = model.records()
                                   .resolve(attributeValue.getAttribute());
        switch (attribute.getValueType()) {
            case Binary:
                return attributeValue.getBinaryValue();
            case Boolean:
                return attributeValue.getBooleanValue();
            case Integer:
                return attributeValue.getIntegerValue();
            case Numeric:
                return attributeValue.getNumericValue();
            case Text:
                return attributeValue.getTextValue();
            case Timestamp:
                return attributeValue.getTimestampValue();
            case JSON:
                return attributeValue.getJsonValue();
            default:
                throw new IllegalStateException(String.format("Invalid value type: %s",
                                                              attribute.getValueType()));
        }
    }

    @Override
    public final void initialize(ExistentialRuleform ruleform,
                                 FacetRecord aspect) {
        initialize(ruleform, aspect, null);
    }

    @Override
    public final void initialize(ExistentialRuleform ruleform,
                                 FacetRecord aspect,
                                 EditableWorkspace workspace) {
        ExistentialRecord classification = model.records()
                                                .resolve(aspect.getClassification());
        if (getImmediateChildren(ruleform.getId(), aspect.getClassifier(),
                                 classification.getDomain()).isEmpty()) {
            UUID inverseRelationship = ((Relationship) model.records()
                                                            .resolve(aspect.getClassifier())).getInverse();
            Tuple<ExistentialNetworkRecord, ExistentialNetworkRecord> links = link(ruleform.getId(),
                                                                                   aspect.getClassifier(),
                                                                                   aspect.getClassification(),
                                                                                   inverseRelationship);
            if (workspace != null) {
                workspace.add(links.a);
                workspace.add(links.b);
            }
        }
        for (ExistentialAttributeAuthorizationRecord authorization : getAttributeAuthorizations(aspect,
                                                                                                false)) {
            Attribute authorizedAttribute = create.selectFrom(EXISTENTIAL)
                                                  .where(EXISTENTIAL.ID.equal(authorization.getAuthorizedAttribute()))
                                                  .fetchOne()
                                                  .into(Attribute.class);
            if (!authorizedAttribute.getKeyed()
                && !authorizedAttribute.getIndexed()) {
                if (getAttributeValue(ruleform, authorizedAttribute) == null) {
                    ExistentialAttributeRecord attribute = create(ruleform,
                                                                  authorizedAttribute);
                    attribute.insert();
                    setValue(authorizedAttribute, attribute, authorization);
                    if (workspace != null) {
                        workspace.add(attribute);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.NetworkedModel#isAccessible(com.chiralbehaviors.CoRE.ExistentialRuleform, com.chiralbehaviors.CoRE.network.Relationship, com.chiralbehaviors.CoRE.network.Relationship, com.chiralbehaviors.CoRE.ExistentialRuleform, com.chiralbehaviors.CoRE.network.Relationship)
     */
    @Override
    public boolean isAccessible(ExistentialRuleform parent,
                                Relationship relationship,
                                ExistentialRuleform child) {
        return !ZERO.equals(create.selectCount()
                                  .from(EXISTENTIAL)
                                  .join(EXISTENTIAL_NETWORK)
                                  .on(EXISTENTIAL_NETWORK.CHILD.equal(EXISTENTIAL.ID))
                                  .and(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
                                  .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
                                  .and(EXISTENTIAL_NETWORK.CHILD.notEqual(parent.getId()))
                                  .fetchOne()
                                  .value1());
    }

    @Override
    public Tuple<ExistentialNetworkRecord, ExistentialNetworkRecord> link(ExistentialRuleform parent,
                                                                          Relationship r,
                                                                          ExistentialRuleform child) {
        return link(parent.getId(), r.getId(), child.getId(), r.getInverse());
    }

    public Tuple<ExistentialNetworkRecord, ExistentialNetworkRecord> link(UUID parent,
                                                                          UUID r,
                                                                          UUID child,
                                                                          UUID inverseR) {
        ExistentialNetworkRecord forward = model.records()
                                                .newExistentialNetwork();
        forward.setParent(parent);
        forward.setRelationship(r);
        forward.setChild(child);
        forward.insert();

        ExistentialNetworkRecord inverse = model.records()
                                                .newExistentialNetwork();
        inverse.setParent(child);
        inverse.setRelationship(inverseR);
        inverse.setChild(parent);
        inverse.insert();
        return new Tuple<>(forward, inverse);
    }

    @Override
    public void setAttributeValue(ExistentialAttributeRecord value) {
        //        Attribute attribute = value.getAttribute();
        //        Attribute validatingAttribute = model.getAttributeModel()
        //                                             .getSingleChild(attribute,
        //                                                             model.getKernel()
        //                                                                  .getIsValidatedBy());
        //        if (validatingAttribute != null) {
        //            TypedQuery<AttributeMetaAttribute> query = em.createNamedQuery(AttributeMetaAttribute.GET_ATTRIBUTE,
        //                                                                           AttributeMetaAttribute.class);
        //            query.setParameter("ruleform", validatingAttribute);
        //            query.setParameter("attribute", attribute);
        //            List<AttributeMetaAttribute> attrs = query.getResultList();
        //            if (attrs == null || attrs.size() == 0) {
        //                throw new IllegalArgumentException("No valid values for attribute "
        //                                                   + attribute.getName());
        //            }
        //            boolean valid = false;
        //            for (AttributeMetaAttribute ama : attrs) {
        //                if (ama.getValue() != null && ama.getValue()
        //                                                 .equals(value.getValue())) {
        //                    valid = true;
        //                    em.persist(value);
        //                }
        //            }
        //            if (!valid) {
        //                throw new IllegalArgumentException(String.format("%s is not a valid value for attribute %s",
        //                                                                 value.getValue(),
        //                                                                 attribute));
        //            }
        //        }

    }

    @Override
    public void setAuthorized(ExistentialRuleform ruleform,
                              Relationship relationship,
                              List<? extends ExistentialRuleform> authorized,
                              ExistentialDomain domain) {
        deauthorizeAll(ruleform, relationship,
                       getAllAuthorized(ruleform, relationship, domain));
        authorizeAll(ruleform, relationship, authorized);
    }

    @Override
    public void setImmediateChild(ExistentialRuleform parent,
                                  Relationship relationship,
                                  ExistentialRuleform child) {

        unlink(parent, relationship, child);
        link(parent, relationship, child);

    }

    @Override
    public void setValue(ExistentialAttributeAuthorizationRecord auth,
                         Object value) {
        Attribute attribute = model.records()
                                   .resolve(auth.getAuthorizedAttribute());
        switch (attribute.getValueType()) {
            case Binary:
                auth.setBinaryValue((byte[]) value);
                break;
            case Boolean:
                auth.setBooleanValue((Boolean) value);
                break;
            case Integer:
                auth.setIntegerValue((Integer) value);
                break;
            case Numeric:
                auth.setNumericValue((BigDecimal) value);
                break;
            case Text:
                auth.setTextValue((String) value);
                break;
            case Timestamp:
                auth.setTimestampValue((Timestamp) value);
                break;
            case JSON:
                auth.setJsonValue((JsonNode) value);
                break;
            default:
                throw new IllegalStateException(String.format("Invalid value type: %s",
                                                              attribute.getValueType()));
        }

        auth.setUpdatedBy(model.getCurrentPrincipal()
                               .getPrincipal()
                               .getId());
        auth.update();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.PhantasmModel#setValue(com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialAttributeRecord, java.lang.Object)
     */
    @Override
    public void setValue(ExistentialAttributeRecord attributeValue,
                         Object value) {
        Attribute attribute = model.records()
                                   .resolve(attributeValue.getAttribute());
        switch (attribute.getValueType()) {
            case Binary:
                attributeValue.setBinaryValue((byte[]) value);
                break;
            case Boolean:
                attributeValue.setBooleanValue((Boolean) value);
                break;
            case Integer:
                attributeValue.setIntegerValue((Integer) value);
                break;
            case Numeric:
                attributeValue.setNumericValue((BigDecimal) value);
                break;
            case Text:
                attributeValue.setTextValue((String) value);
                break;
            case Timestamp:
                attributeValue.setTimestampValue((Timestamp) value);
                break;
            case JSON:
                attributeValue.setJsonValue((JsonNode) value);
                break;
            default:
                throw new IllegalStateException(String.format("Invalid value type: %s",
                                                              attribute.getValueType()));
        }
        attributeValue.setUpdatedBy(model.getCurrentPrincipal()
                                         .getPrincipal()
                                         .getId());
        attributeValue.setUpdated(new Timestamp(System.currentTimeMillis()));
        attributeValue.update();
    }

    @Override
    public void setValue(ExistentialNetworkAttributeAuthorizationRecord auth,
                         Object value) {
        Attribute attribute = model.records()
                                   .resolve(auth.getAuthorizedAttribute());
        switch (attribute.getValueType()) {
            case Binary:
                auth.setBinaryValue((byte[]) value);
                break;
            case Boolean:
                auth.setBooleanValue((Boolean) value);
                break;
            case Integer:
                auth.setIntegerValue((Integer) value);
                break;
            case Numeric:
                auth.setNumericValue((BigDecimal) value);
                break;
            case Text:
                auth.setTextValue((String) value);
                break;
            case Timestamp:
                auth.setTimestampValue((Timestamp) value);
                break;
            case JSON:
                auth.setJsonValue((JsonNode) value);
                break;
            default:
                throw new IllegalStateException(String.format("Invalid value type: %s",
                                                              attribute.getValueType()));
        }

        auth.setUpdatedBy(model.getCurrentPrincipal()
                               .getPrincipal()
                               .getId());
        auth.update();
    }

    @Override
    public void setValue(ExistentialNetworkAttributeRecord attributeValue,
                         Object value) {
        Attribute attribute = model.records()
                                   .resolve(attributeValue.getAttribute());
        switch (attribute.getValueType()) {
            case Binary:
                attributeValue.setBinaryValue((byte[]) value);
                break;
            case Boolean:
                attributeValue.setBooleanValue((Boolean) value);
                break;
            case Integer:
                attributeValue.setIntegerValue((Integer) value);
                break;
            case Numeric:
                attributeValue.setNumericValue((BigDecimal) value);
                break;
            case Text:
                attributeValue.setTextValue((String) value);
                break;
            case Timestamp:
                attributeValue.setTimestampValue((Timestamp) value);
                break;
            case JSON:
                attributeValue.setJsonValue((JsonNode) value);
                break;
            default:
                throw new IllegalStateException(String.format("Invalid value type: %s",
                                                              attribute.getValueType()));
        }

        attributeValue.setUpdatedBy(model.getCurrentPrincipal()
                                         .getPrincipal()
                                         .getId());
        attributeValue.setUpdated(new Timestamp(System.currentTimeMillis()));
        attributeValue.update();
    }

    @Override
    public void unlink(ExistentialRuleform parent, Relationship relationship,
                       ExistentialRuleform child) {
        create.deleteFrom(EXISTENTIAL_NETWORK)
              .where(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
              .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
              .and(EXISTENTIAL_NETWORK.CHILD.equal(child.getId()))
              .execute();
    }

    @Override
    public void unlinkImmediate(ExistentialRuleform parent,
                                Relationship relationship) {
        create.deleteFrom(EXISTENTIAL_NETWORK)
              .where(EXISTENTIAL_NETWORK.PARENT.equal(parent.getId()))
              .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship.getId()))
              .and(EXISTENTIAL_NETWORK.INFERENCE.isNull())
              .execute();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.meta.PhantasmModel#valueClass(com.chiralbehaviors.CoRE.domain.Attribute)
     */
    @Override
    public Class<?> valueClass(Attribute attribute) {
        switch (attribute.getValueType()) {
            case Binary:
                return byte[].class;
            case Boolean:
                return Boolean.class;
            case Integer:
                return Integer.class;
            case Numeric:
                return BigDecimal.class;
            case Text:
                return String.class;
            case Timestamp:
                return Timestamp.class;
            case JSON:
                return Map.class;
            default:
                throw new IllegalStateException(String.format("Invalid value type: %s",
                                                              attribute.getValueType()));
        }
    }

    private List<ExistentialRuleform> getImmediateChildren(UUID parent,
                                                           UUID relationship,
                                                           ExistentialDomain domain) {
        return create.select(EXISTENTIAL.fields())
                     .from(EXISTENTIAL, EXISTENTIAL_NETWORK)
                     .where(EXISTENTIAL_NETWORK.PARENT.equal(parent))
                     .and(EXISTENTIAL_NETWORK.RELATIONSHIP.equal(relationship))
                     .and(EXISTENTIAL_NETWORK.INFERENCE.isNull())
                     .and(EXISTENTIAL.ID.equal(EXISTENTIAL_NETWORK.CHILD))
                     .and(EXISTENTIAL.DOMAIN.equal(domain))
                     .fetch()
                     .into(ExistentialRecord.class)
                     .stream()
                     .map(r -> model.records()
                                    .resolve(r))
                     .collect(Collectors.toList());
    }

    private void setValue(Attribute attribute, ExistentialAttributeRecord value,
                          ExistentialAttributeAuthorizationRecord authorization) {
        switch (attribute.getValueType()) {
            case Binary:
                value.setBinaryValue(authorization.getBinaryValue());
                break;
            case Boolean:
                value.setBooleanValue(authorization.getBooleanValue());
                break;
            case Integer:
                value.setIntegerValue(authorization.getIntegerValue());
                break;
            case JSON:
                value.setJsonValue(authorization.getJsonValue());
                break;
            case Numeric:
                value.setNumericValue(authorization.getNumericValue());
                break;
            case Text:
                value.setTextValue(authorization.getTextValue());
                break;
            case Timestamp:
                value.setTimestampValue(authorization.getTimestampValue());
            default:
                throw new IllegalStateException(String.format("Unknown value type %s",
                                                              attribute.getValueType()));
        }
        value.setUpdated(new Timestamp(System.currentTimeMillis()));
        value.update();
    }

    @Override
    public List<Agency> findByAttributeValue(Attribute attribute, Object query,
                                             ExistentialDomain domain) {
        // TODO Auto-generated method stub
        return null;
    }
}
