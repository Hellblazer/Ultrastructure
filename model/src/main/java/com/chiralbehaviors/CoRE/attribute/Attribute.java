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
package com.chiralbehaviors.CoRE.attribute;

import static com.chiralbehaviors.CoRE.attribute.Attribute.FIND_BY_NAME;
import static com.chiralbehaviors.CoRE.attribute.Attribute.FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS;
import static com.chiralbehaviors.CoRE.attribute.Attribute.FIND_CLASSIFIED_ATTRIBUTE_VALUES;
import static com.chiralbehaviors.CoRE.attribute.Attribute.GET_CHILD;
import static com.chiralbehaviors.CoRE.attribute.Attribute.GET_CHILD_RULES_BY_RELATIONSHIP;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.metamodel.SingularAttribute;

import com.chiralbehaviors.CoRE.ExistentialRuleform;
import com.chiralbehaviors.CoRE.Triggers;
import com.chiralbehaviors.CoRE.WellKnownObject.WellKnownAttribute;
import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.network.Relationship;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization_;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Existential ruleform for all attributes in the CoRE database. This table
 * defines and describes all attributes.
 *
 * @author hhildebrand
 *
 */
@NamedQueries({
               @NamedQuery(name = FIND_CLASSIFIED_ATTRIBUTE_VALUES, query = "SELECT "
                                                                            + "  attrValue "
                                                                            + "FROM "
                                                                            + "       AttributeMetaAttribute attrValue, "
                                                                            + "       AttributeMetaAttributeAuthorization auth, "
                                                                            + "       AttributeNetwork network "
                                                                            + "WHERE "
                                                                            + "        auth.authorizedAttribute = attrValue.attribute AND "
                                                                            + "        network.relationship = auth.classification AND "
                                                                            + "        network.child = auth.classifier AND"
                                                                            + "        attrValue.attribute = :ruleform AND "
                                                                            + "        auth.classification = :classification AND "
                                                                            + "        auth.classifier = :classifier "),
               @NamedQuery(name = FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS, query = "select ama from AttributeMetaAttributeAuthorization ama "
                                                                                    + "WHERE ama.classification = :classification "
                                                                                    + "AND ama.classifier = :classifier"),
               @NamedQuery(name = FIND_BY_NAME, query = "select e from Attribute e where e.name = :name"),
               @NamedQuery(name = GET_CHILD, query = "SELECT rn.child "
                                                     + "FROM AttributeNetwork rn "
                                                     + "WHERE rn.parent = :parent "
                                                     + "AND rn.relationship = :relationship"),
               @NamedQuery(name = GET_CHILD_RULES_BY_RELATIONSHIP, query = "SELECT n FROM AttributeNetwork n "
                                                                           + "WHERE n.parent = :attribute "
                                                                           + "AND n.relationship IN :relationships "
                                                                           + "ORDER by n.parent.name, n.relationship.name, n.child.name") })
@Entity
@Table(name = "attribute", schema = "ruleform")
public class Attribute extends ExistentialRuleform<Attribute, AttributeNetwork> {
    public static final String          FIND_BY_NAME                             = "attribute.findByName";
    public static final String          FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS = "attribute"
                                                                                   + FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_SUFFIX;
    public static final String          FIND_CLASSIFIED_ATTRIBUTE_VALUES         = "attribute"
                                                                                   + FIND_CLASSIFIED_ATTRIBUTE_VALUES_SUFFIX;
    public static final String          GET_CHILD                                = "attribute"
                                                                                   + GET_CHILDREN_SUFFIX;
    public static final String          GET_CHILD_RULES_BY_RELATIONSHIP          = "attribute"
                                                                                   + GET_CHILD_RULES_BY_RELATIONSHIP_SUFFIX;
    private static final long           serialVersionUID                         = 1L;

    // bi-directional many-to-one association to AttributeMetaAttribute
    @OneToMany(mappedBy = "attribute")
    @JsonIgnore
    private Set<AttributeMetaAttribute> attributes;

    private Integer                     inheritable                              = FALSE;

    // bi-directional many-to-one association to AttributeNetwork
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "child")
    private Set<AttributeNetwork>       networkByChild;

    // bi-directional many-to-one association to AttributeNetwork
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    private Set<AttributeNetwork>       networkByParent;

    @Column(name = "value_type")
    @Enumerated(EnumType.ORDINAL)
    private ValueType                   valueType;

    public Attribute() {
    }

    /**
     * @param updatedBy
     */
    public Attribute(Agency updatedBy) {
        super(updatedBy);
    }

    /**
     * @param name
     */
    public Attribute(String name) {
        super(name);
    }

    /**
     * @param name
     * @param updatedBy
     */
    public Attribute(String name, Agency updatedBy) {
        super(name, updatedBy);
    }

    /**
     * @param name
     * @param description
     */
    public Attribute(String name, String description) {
        super(name, description);
    }

    /**
     * @param name
     * @param description
     * @param updatedBy
     */
    public Attribute(String name, String description, Agency updatedBy) {
        super(name, description, updatedBy);
    }

    /**
     * @param name
     * @param description
     * @param updatedBy
     * @param valueType
     */
    public Attribute(String name, String description, Agency updatedBy,
                     ValueType valueType) {
        super(name, description, updatedBy);
        setValueType(valueType);
    }

    /**
     * @param name
     * @param description
     * @param updatedBy
     */
    public Attribute(String name, String description, ValueType valueType,
                     Agency updatedBy) {
        this(name, description, updatedBy);
        this.valueType = valueType;
    }

    /**
     * @param id
     */
    public Attribute(UUID id) {
        super(id);
    }

    public void addAttribute(AttributeMetaAttribute attribute) {
        attribute.setAttribute(this);
        attributes.add(attribute);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.Networked#addChildRelationship(com.chiralbehaviors
     * .CoRE .NetworkRuleform)
     */
    @Override
    public void addChildRelationship(AttributeNetwork relationship) {
        relationship.setChild(this);
        networkByChild.add(relationship);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.Networked#addParentRelationship(com.chiralbehaviors
     * .CoRE .NetworkRuleform)
     */
    @Override
    public void addParentRelationship(AttributeNetwork relationship) {
        relationship.setParent(this);
        networkByParent.add(relationship);
    }

    @Override
    public Attribute clone() {
        Attribute clone = (Attribute) super.clone();
        clone.attributes = null;
        clone.networkByChild = null;
        clone.networkByParent = null;
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
    public String getAnyId() {
        return WellKnownAttribute.ANY.id();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<AttributeMetaAttribute> getAttributes() {
        return attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<AttributeMetaAttribute> getAttributeValueClass() {
        return AttributeMetaAttribute.class;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getCopyId()
     */
    @Override
    public String getCopyId() {
        return WellKnownAttribute.COPY.id();
    }

    public Boolean getInheritable() {
        return toBoolean(inheritable);
    }

    @Override
    public Set<AttributeNetwork> getNetworkByChild() {
        if (networkByChild == null) {
            return Collections.emptySet();
        }
        return networkByChild;
    }

    @Override
    public Set<AttributeNetwork> getNetworkByParent() {
        if (networkByParent == null) {
            return Collections.emptySet();
        }
        return networkByParent;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkChildAttribute()
     */
    @Override
    public SingularAttribute<AttributeNetwork, Attribute> getNetworkChildAttribute() {
        return AttributeNetwork_.child;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkClass()
     */
    @Override
    public Class<AttributeNetwork> getNetworkClass() {
        return AttributeNetwork.class;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkParentAttribute()
     */
    @Override
    public SingularAttribute<AttributeNetwork, Attribute> getNetworkParentAttribute() {
        return AttributeNetwork_.parent;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNetworkWorkspaceAttribute()
     */
    @Override
    public SingularAttribute<WorkspaceAuthorization, AttributeNetwork> getNetworkWorkspaceAuthAttribute() {
        return WorkspaceAuthorization_.attributeNetwork;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getNotApplicableId()
     */
    @Override
    public String getNotApplicableId() {
        return WellKnownAttribute.NOT_APPLICABLE.id();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#getSameId()
     */
    @Override
    public String getSameId() {
        return WellKnownAttribute.SAME.id();
    }

    public ValueType getValueType() {
        return valueType;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Ruleform#getWorkspaceAuthAttribute()
     */
    @Override
    @JsonIgnore
    public SingularAttribute<WorkspaceAuthorization, Attribute> getWorkspaceAuthAttribute() {
        return WorkspaceAuthorization_.attribute;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isAny()
     */
    @Override
    public boolean isAny() {
        return WellKnownAttribute.ANY.id().equals(getId());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isAnyOrSame()
     */
    @Override
    public boolean isAnyOrSame() {
        return WellKnownAttribute.ANY.id().equals(getId())
               || WellKnownAttribute.SAME.id().equals(getId());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isCopy()
     */
    @Override
    public boolean isCopy() {
        return WellKnownAttribute.COPY.id().equals(getId());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.ExistentialRuleform#isNotApplicable()
     */
    @Override
    public boolean isNotApplicable() {
        return WellKnownAttribute.NOT_APPLICABLE.id().equals(getId());
    }

    @Override
    public boolean isSame() {
        return WellKnownAttribute.SAME.id().equals(getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.network.Networked#link(com.chiralbehaviors.CoRE
     * .network .Relationship, com.chiralbehaviors.CoRE.network.Networked,
     * com.chiralbehaviors.CoRE.agency.Agency, javax.persistence.EntityManager)
     */
    @Override
    public void link(Relationship r, Attribute child, Agency updatedBy,
                     Agency inverseSoftware, EntityManager em) {
        AttributeNetwork link = new AttributeNetwork(this, r, child, updatedBy);
        em.persist(link);
        AttributeNetwork inverse = new AttributeNetwork(child, r.getInverse(),
                                                        this, inverseSoftware);
        em.persist(inverse);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends AttributeValue<Attribute>> void setAttributes(Set<A> attributes) {
        this.attributes = (Set<AttributeMetaAttribute>) attributes;
    }

    public void setInheritable(Boolean inheritable) {
        this.inheritable = toInteger(inheritable);
    }

    @Override
    public void setNetworkByChild(Set<AttributeNetwork> children) {
        networkByChild = children;
    }

    @Override
    public void setNetworkByParent(Set<AttributeNetwork> parent) {
        networkByParent = parent;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }
}