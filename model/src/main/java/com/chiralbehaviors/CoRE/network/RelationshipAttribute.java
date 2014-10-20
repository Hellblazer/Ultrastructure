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
package com.chiralbehaviors.CoRE.network;

import static com.chiralbehaviors.CoRE.network.RelationshipAttribute.GET_ATTRIBUTE;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.metamodel.SingularAttribute;

import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.attribute.AttributeValue;
import com.chiralbehaviors.CoRE.attribute.unit.Unit;

/**
 * @author hhildebrand
 *
 */
@Entity
@Table(name = "relationship_attribute", schema = "ruleform")
@NamedQueries({ @NamedQuery(name = GET_ATTRIBUTE, query = "select ra from RelationshipAttribute ra where ra.relationship = :relationship and ra.attribute = :attribute") })
public class RelationshipAttribute extends AttributeValue<Relationship> {
    public static final String GET_ATTRIBUTE    = "relationshipAttribute.getAttribute";
    private static final long  serialVersionUID = 1L;

    // bi-directional many-to-one association to Relationship

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "relationship")
    private Relationship       relationship;

    public RelationshipAttribute() {
        super();
    }

    public RelationshipAttribute(Agency updatedBy) {
        super(updatedBy);
    }

    public RelationshipAttribute(Attribute attribute) {
        super(attribute);
    }

    public RelationshipAttribute(Attribute attribute, Agency updatedBy) {
        super(attribute, updatedBy);
    }

    public RelationshipAttribute(Attribute attribute, BigDecimal value,
                                 Agency updatedBy) {
        super(attribute, value, updatedBy);
    }

    public RelationshipAttribute(Attribute attribute, boolean value,
                                 Agency updatedBy) {
        super(attribute, value, updatedBy);
    }

    public RelationshipAttribute(Attribute attribute, int value,
                                 Agency updatedBy) {
        super(attribute, value, updatedBy);
    }

    public RelationshipAttribute(Attribute attribute, String value,
                                 Agency updatedBy) {
        super(attribute, value, updatedBy);
    }

    public RelationshipAttribute(Attribute attribute, Unit unit) {
        super(attribute, unit);
    }

    public RelationshipAttribute(UUID id) {
        super(id);
    }

    public Relationship getRelationship() {
        return relationship;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.attribute.AttributeValue#getRuleformAttribute()
     */
    @Override
    public SingularAttribute<? extends AttributeValue<Relationship>, Relationship> getRuleformAttribute() {
        return RelationshipAttribute_.relationship;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.attribute.AttributeValue#getRuleformClass()
     */
    @Override
    public Class<Relationship> getRuleformClass() {
        return Relationship.class;
    }

    public void setRelationship(Relationship interval) {
        relationship = interval;
    }
}
