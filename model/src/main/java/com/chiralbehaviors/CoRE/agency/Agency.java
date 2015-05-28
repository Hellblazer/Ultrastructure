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
package com.chiralbehaviors.CoRE.agency;

import static com.chiralbehaviors.CoRE.agency.Agency.FIND_ALL;
import static com.chiralbehaviors.CoRE.agency.Agency.FIND_BY_NAME;
import static com.chiralbehaviors.CoRE.agency.Agency.FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS;
import static com.chiralbehaviors.CoRE.agency.Agency.FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_FOR_ATTRIBUTE;
import static com.chiralbehaviors.CoRE.agency.Agency.FIND_CLASSIFIED_ATTRIBUTE_VALUES;
import static com.chiralbehaviors.CoRE.agency.Agency.FIND_GROUPED_ATTRIBUTE_AUTHORIZATIONS;
import static com.chiralbehaviors.CoRE.agency.Agency.GET_ALL_PARENT_RELATIONSHIPS;
import static com.chiralbehaviors.CoRE.agency.Agency.GET_CHILDREN;
import static com.chiralbehaviors.CoRE.agency.Agency.GET_CHILD_RULES_BY_RELATIONSHIP;
import static com.chiralbehaviors.CoRE.agency.AgencyAttribute.GET_ATTRIBUTE;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.metamodel.SingularAttribute;

import com.chiralbehaviors.CoRE.ExistentialRuleform;
import com.chiralbehaviors.CoRE.Ruleform;
import com.chiralbehaviors.CoRE.Triggers;
import com.chiralbehaviors.CoRE.WellKnownObject.WellKnownAgency;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.attribute.AttributeValue;
import com.chiralbehaviors.CoRE.product.ProductNetwork;
import com.chiralbehaviors.CoRE.relationship.Relationship;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization_;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An agency that can provide information, produce products, or perform work.
 *
 * Examples are people, lab groups, software, books, bank accounts, output files
 * of computational analyses, etc.
 *
 * @author hhildebrand
 *
 */
@NamedQueries({
               @NamedQuery(name = FIND_ALL, query = "select a from Agency a"),
               @NamedQuery(name = FIND_BY_NAME, query = "select e from Agency e where e.name = :name"),
               @NamedQuery(name = FIND_CLASSIFIED_ATTRIBUTE_VALUES, query = "SELECT "
                                                                            + "  attrValue "
                                                                            + "FROM "
                                                                            + "       AgencyAttribute attrValue, "
                                                                            + "       AgencyAttributeAuthorization auth, "
                                                                            + "       AgencyNetworkAuthorization na, "
                                                                            + "       AgencyNetwork network "
                                                                            + "WHERE "
                                                                            + "        auth.authorizedAttribute = attrValue.attribute "
                                                                            + "    AND auth.networkAuthorization = na "
                                                                            + "    AND network.relationship = na.classifier "
                                                                            + "    AND network.child = na.classification"
                                                                            + "    AND attrValue.agency = :ruleform "
                                                                            + "    AND na.classifier = :classifier "
                                                                            + "    AND na.classification= :classification "),
               @NamedQuery(name = FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_FOR_ATTRIBUTE, query = "SELECT "
                                                                                                  + "  auth "
                                                                                                  + "FROM "
                                                                                                  + "       AgencyAttributeAuthorization auth, "
                                                                                                  + "       AgencyNetworkAuthorization na, "
                                                                                                  + "       AgencyNetwork network "
                                                                                                  + "WHERE "
                                                                                                  + "        auth.networkAuthorization = na "
                                                                                                  + "    AND auth.authorizedAttribute = :attribute "
                                                                                                  + "    AND network.relationship = na.classifier "
                                                                                                  + "    AND network.child = na.classification"
                                                                                                  + "    AND na.classifier = :classifier "
                                                                                                  + "    AND na.classification= :classification "),
               @NamedQuery(name = FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS, query = "select auth from AgencyAttributeAuthorization auth "
                                                                                    + "WHERE auth.networkAuthorization.classifier = :classifier "
                                                                                    + "AND auth.networkAuthorization.classification = :classification "
                                                                                    + "AND auth.authorizedAttribute IS NOT NULL"),
               @NamedQuery(name = FIND_GROUPED_ATTRIBUTE_AUTHORIZATIONS, query = "select ra from AgencyAttributeAuthorization ra "
                                                                                 + "WHERE ra.groupingAgency = :groupingAgency"),
               @NamedQuery(name = GET_CHILDREN, query = "SELECT n.child "
                                                        + "FROM AgencyNetwork n "
                                                        + "WHERE n.parent = :p "
                                                        + "AND n.relationship = :r"),
               @NamedQuery(name = GET_ALL_PARENT_RELATIONSHIPS, query = "SELECT n "
                                                                        + "FROM AgencyNetwork n "
                                                                        + "WHERE n.child = :c"),
               @NamedQuery(name = GET_CHILD_RULES_BY_RELATIONSHIP, query = "SELECT n FROM AgencyNetwork n "
                                                                           + "WHERE n.parent = :agency "
                                                                           + "AND n.relationship IN :relationships "
                                                                           + "ORDER by n.parent.name, n.relationship.name, n.child.name") })
@Entity
@Table(name = "agency", schema = "ruleform")
public class Agency extends ExistentialRuleform<Agency, AgencyNetwork> {
    public static final String   AGENCY_ATTRIBUTES_BY_CLASSIFICATION                    = "agency.AgencyAttributesByClassification";

    public static final String   AUTHORIZED_AGENCY_ATTRIBUTES                           = "agency.authorizedAttributes";
    public static final String   FIND_ALL                                               = "agency"
                                                                                          + Ruleform.FIND_ALL_SUFFIX;
    public static final String   FIND_BY_NAME                                           = "agency"
                                                                                          + FIND_BY_NAME_SUFFIX;
    public static final String   FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS               = "agency"
                                                                                          + FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_SUFFIX;
    public static final String   FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_FOR_ATTRIBUTE = "agency"
                                                                                          + FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_FOR_ATTRIBUTE_SUFFIX;
    public static final String   FIND_CLASSIFIED_ATTRIBUTE_VALUES                       = "agency"
                                                                                          + FIND_CLASSIFIED_ATTRIBUTE_VALUES_SUFFIX;
    public static final String   FIND_GROUPED_ATTRIBUTE_AUTHORIZATIONS                  = "agency"
                                                                                          + FIND_GROUPED_ATTRIBUTE_VALUES_SUFFIX;
    public static final String   GET_ALL_PARENT_RELATIONSHIPS                           = "agency"
                                                                                          + GET_ALL_PARENT_RELATIONSHIPS_SUFFIX;
    public static final String   GET_CHILD_RULES_BY_RELATIONSHIP                        = "agency"
                                                                                          + GET_CHILD_RULES_BY_RELATIONSHIP_SUFFIX;
    public static final String   GET_CHILDREN                                           = "agency"
                                                                                          + GET_CHILDREN_SUFFIX;
    public static final String   QUALIFIED_ENTITY_NETWORK_RULES                         = "agency.qualifiedEntityNetworkRules";

    private static final long    serialVersionUID                                       = 1L;

    // bi-directional many-to-one association to AgencyAttribute
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "agency")
    @JsonIgnore
    private Set<AgencyAttribute> attributes;

    // bi-directional many-to-one association to AgencyNetwork

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "child")
    @JsonIgnore
    private Set<AgencyNetwork>   networkByChild;

    // bi-directional many-to-one association to AgencyNetwork
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @JsonIgnore
    private Set<AgencyNetwork>   networkByParent;

    public Agency() {
    }

    /**
     * @param updatedBy
     */
    public Agency(Agency updatedBy) {
        super(updatedBy);
    }

    /**
     * @param name
     */
    public Agency(String name) {
        super(name);
    }

    /**
     * @param name
     * @param updatedBy
     */
    public Agency(String name, Agency updatedBy) {
        super(name, updatedBy);
    }

    /**
     * @param name
     * @param description
     */
    public Agency(String name, String description) {
        super(name, description);
    }

    /**
     * @param name
     * @param description
     * @param updatedBy
     */
    public Agency(String name, String description, Agency updatedBy) {
        super(name, description, updatedBy);
    }

    /**
     * @param id
     */
    public Agency(UUID id) {
        super(id);
    }

    public void addAttribute(AgencyAttribute attribute) {
        attribute.setAgency(this);
        attributes.add(attribute);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.Networked#addChildRelationship(com.chiralbehaviors
     * .CoRE.NetworkRuleform)
     */
    @Override
    public void addChildRelationship(AgencyNetwork relationship) {
        relationship.setChild(this);
        networkByChild.add(relationship);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.Networked#addParentRelationship(com.chiralbehaviors
     * .CoRE.NetworkRuleform)
     */
    @Override
    public void addParentRelationship(AgencyNetwork relationship) {
        relationship.setParent(this);
        networkByParent.add(relationship);
    }

    @Override
    public Agency clone() {
        Agency clone = (Agency) super.clone();
        clone.networkByChild = null;
        clone.networkByParent = null;
        clone.attributes = null;
        return clone;
    }

    @Override
    public void delete(Triggers triggers) {
        triggers.delete(this);
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getAnyId()
     */
    @Override
    public UUID getAnyId() {
        return WellKnownAgency.ANY.id();
    }

    /**
     * Retrieves the unique AgencyAttribute rule for the given Agency and
     * Attribute.
     *
     * @param agency
     *            The Agency the Attribute applies to
     * @param attribute
     *            The Attribute of the Agency
     * @return the unique AgencyAttribute rule, or <code>null</code> if no such
     *         rule exists
     */
    public AgencyAttribute getAttribute(EntityManager em, Attribute attribute) {
        return em.createNamedQuery(GET_ATTRIBUTE, AgencyAttribute.class).setParameter("agency",
                                                                                      this).setParameter("attribute",
                                                                                                         attribute).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<AgencyAttribute> getAttributes() {
        return attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<AgencyAttribute> getAttributeValueClass() {
        return AgencyAttribute.class;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getCopyId()
     */
    @Override
    public UUID getCopyId() {
        return WellKnownAgency.COPY.id();
    }

    @Override
    public Set<AgencyNetwork> getNetworkByChild() {
        if (networkByChild == null) {
            return Collections.emptySet();
        }
        return networkByChild;
    }

    @Override
    public Set<AgencyNetwork> getNetworkByParent() {
        if (networkByParent == null) {
            return Collections.emptySet();
        }
        return networkByParent;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkChildAttribute()
     */
    @Override
    public SingularAttribute<AgencyNetwork, Agency> getNetworkChildAttribute() {
        return AgencyNetwork_.child;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkClass()
     */
    @Override
    public Class<AgencyNetwork> getNetworkClass() {
        return AgencyNetwork.class;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkParentAttribute()
     */
    @Override
    public SingularAttribute<AgencyNetwork, Agency> getNetworkParentAttribute() {
        return AgencyNetwork_.parent;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkWorkspaceAttribute()
     */
    @Override
    public SingularAttribute<WorkspaceAuthorization, AgencyNetwork> getNetworkWorkspaceAuthAttribute() {
        return WorkspaceAuthorization_.agencyNetwork;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNotApplicableId()
     */
    @Override
    public UUID getNotApplicableId() {
        return WellKnownAgency.NOT_APPLICABLE.id();
    }

    public List<ProductNetwork> getQualifiedEntityNetworkRules(EntityManager em) {
        return em.createNamedQuery(QUALIFIED_ENTITY_NETWORK_RULES,
                                   ProductNetwork.class).setParameter("agency",
                                                                      this).getResultList();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getSameId()
     */
    @Override
    public UUID getSameId() {
        return WellKnownAgency.SAME.id();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Ruleform#getWorkspaceAuthAttribute()
     */
    @Override
    @JsonIgnore
    public SingularAttribute<WorkspaceAuthorization, Agency> getWorkspaceAuthAttribute() {
        return WorkspaceAuthorization_.agency;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isAny()
     */
    @Override
    public boolean isAny() {
        return WellKnownAgency.ANY.id().equals(getId());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isAnyOrSame()
     */
    @Override
    public boolean isAnyOrSame() {
        return WellKnownAgency.ANY.id().equals(getId())
               || WellKnownAgency.SAME.id().equals(getId());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isCopy()
     */
    @Override
    public boolean isCopy() {
        return WellKnownAgency.COPY.id().equals(getId());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isNotApplicable()
     */
    @Override
    public boolean isNotApplicable() {
        return WellKnownAgency.NOT_APPLICABLE.id().equals(getId());
    }

    @Override
    public boolean isSame() {
        return WellKnownAgency.SAME.id().equals(getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.network.Networked#link(com.chiralbehaviors.CoRE
     * .network.Relationship, com.chiralbehaviors.CoRE.network.Networked,
     * com.chiralbehaviors.CoRE.agency.Agency, javax.persistence.EntityManager)
     */
    @Override
    public AgencyNetwork link(Relationship r, Agency child, Agency updatedBy,
                              Agency inverseSoftware, EntityManager em) {
        assert r != null : "Relationship cannot be null";
        assert child != null;
        assert updatedBy != null;
        assert em != null;

        AgencyNetwork link = new AgencyNetwork(this, r, child, updatedBy);
        em.persist(link);
        AgencyNetwork inverse = new AgencyNetwork(child, r.getInverse(), this,
                                                  inverseSoftware);
        em.persist(inverse);
        return link;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends AttributeValue<Agency>> void setAttributes(Set<A> attributes) {
        this.attributes = (Set<AgencyAttribute>) attributes;
    }

    @Override
    public void setNetworkByChild(Set<AgencyNetwork> AgencyNetworks3) {
        networkByChild = AgencyNetworks3;
    }

    @Override
    public void setNetworkByParent(Set<AgencyNetwork> AgencyNetworks2) {
        networkByParent = AgencyNetworks2;
    }
}