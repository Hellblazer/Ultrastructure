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
package com.chiralbehaviors.CoRE.attribute.unit;

import static com.chiralbehaviors.CoRE.ExistentialRuleform.GET_CHILDREN_SUFFIX;
import static com.chiralbehaviors.CoRE.ExistentialRuleform.GET_NETWORKS_SUFFIX;
import static com.chiralbehaviors.CoRE.ExistentialRuleform.USED_RELATIONSHIPS_SUFFIX;
import static com.chiralbehaviors.CoRE.attribute.unit.UnitNetwork.GET_CHILDREN;
import static com.chiralbehaviors.CoRE.attribute.unit.UnitNetwork.GET_NETWORKS;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.metamodel.SingularAttribute;

import com.chiralbehaviors.CoRE.Triggers;
import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.network.NetworkRuleform;
import com.chiralbehaviors.CoRE.network.Relationship;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization;
import com.chiralbehaviors.CoRE.workspace.WorkspaceAuthorization_;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author hhildebrand
 *
 */
@NamedQueries({
               @NamedQuery(name = GET_CHILDREN, query = "SELECT n.child FROM UnitNetwork n "
                                                        + "WHERE n.parent = :parent "
                                                        + "AND n.relationship = :relationship"),
               @NamedQuery(name = GET_NETWORKS, query = "SELECT n FROM ProductNetwork n "
                                                        + "WHERE n.parent = :parent "
                                                        + "AND n.relationship = :relationship "
                                                        + "AND n.child = :child") })
@Entity
@Table(name = "unit_network", schema = "ruleform")
public class UnitNetwork extends NetworkRuleform<Unit> {

    public static final String GET_CHILDREN           = "unitNetwork"
                                                        + GET_CHILDREN_SUFFIX;
    public static final String GET_NETWORKS           = "unitNetwork"
                                                        + GET_NETWORKS_SUFFIX;
    public static final String GET_USED_RELATIONSHIPS = "unitNetwork"
                                                        + USED_RELATIONSHIPS_SUFFIX;
    private static final long  serialVersionUID       = 1L;
    // many-to-one
    // association to Agency

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(name = "child")
    private Unit               child;

    // bi-directional many-to-one association to Agency
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(name = "parent")
    private Unit               parent;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(insertable = false, name = "premise1")
    private UnitNetwork        premise1;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(insertable = false, name = "premise2")
    private UnitNetwork        premise2;

    /**
     *
     */
    public UnitNetwork() {
        super();
    }

    /**
     * @param updatedBy
     */
    public UnitNetwork(Agency updatedBy) {
        super(updatedBy);
    }

    /**
     * @param relationship
     * @param updatedBy
     */
    public UnitNetwork(Relationship relationship, Agency updatedBy) {
        super(relationship, updatedBy);
    }

    /**
     * @param relationship
     * @param updatedBy
     */
    public UnitNetwork(Unit parent, Relationship relationship, Unit child,
                       Agency updatedBy) {
        super(relationship, updatedBy);
        this.parent = parent;
        this.child = child;
    }

    /**
     * @param id
     */
    public UnitNetwork(UUID id) {
        super(id);
    }

    @Override
    public void delete(Triggers triggers) {
        triggers.delete(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.network.NetworkRuleform#getChild()
     */
    @Override
    @JsonGetter
    public Unit getChild() {
        return child;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.network.NetworkRuleform#getParent()
     */
    @Override
    @JsonGetter
    public Unit getParent() {
        return parent;
    }

    /**
     * @return the premise1
     */
    @Override
    public UnitNetwork getPremise1() {
        return premise1;
    }

    /**
     * @return the premise2
     */
    @Override
    @JsonGetter
    public UnitNetwork getPremise2() {
        return premise2;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Ruleform#getWorkspaceAuthAttribute()
     */
    @Override
    @JsonIgnore
    public SingularAttribute<WorkspaceAuthorization, UnitNetwork> getWorkspaceAuthAttribute() {
        return WorkspaceAuthorization_.unitNetwork;
    }

    @Override
    public void persist(Triggers triggers) {
        triggers.persist(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.network.NetworkRuleform#setChild(com.chiralbehaviors
     * .CoRE.ExistentialRuleform)
     */
    @Override
    public void setChild(Unit child) {
        this.child = child;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.network.NetworkRuleform#setParent(com.chiralbehaviors.CoRE.ExistentialRuleform)
     */
    @Override
    public void setParent(Unit parent) {
        this.parent = parent;
    }
}
