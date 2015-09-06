/**
s * (C) Copyright 2012 Chiral Behaviors, LLC. All Rights Reserved
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

import static com.chiralbehaviors.CoRE.ExistentialRuleform.GET_CHILDREN_SUFFIX;
import static com.chiralbehaviors.CoRE.ExistentialRuleform.GET_NETWORKS_SUFFIX;
import static com.chiralbehaviors.CoRE.agency.AgencyNetwork.GET_CHILDREN;
import static com.chiralbehaviors.CoRE.agency.AgencyNetwork.GET_NETWORKS;
import static com.chiralbehaviors.CoRE.agency.AgencyNetwork.GET_USED_RELATIONSHIPS;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.chiralbehaviors.CoRE.Triggers;
import com.chiralbehaviors.CoRE.network.NetworkRuleform;
import com.chiralbehaviors.CoRE.relationship.Relationship;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The network relationships of agencies
 *
 * @author hhildebrand
 *
 */
@NamedQueries({ @NamedQuery(name = GET_USED_RELATIONSHIPS, query = "select distinct n.relationship from AgencyNetwork n"),
                @NamedQuery(name = GET_CHILDREN, query = "SELECT n.child FROM AgencyNetwork n "
                                                         + "WHERE n.parent = :parent "
                                                         + "AND n.relationship = :relationship"),
                @NamedQuery(name = GET_NETWORKS, query = "SELECT n FROM AgencyNetwork n "
                                                         + "WHERE n.parent = :parent "
                                                         + "AND n.relationship = :relationship "
                                                         + "AND n.child = :child") })
@Entity
@Table(name = "agency_network", schema = "ruleform")
public class AgencyNetwork extends NetworkRuleform<Agency> {
    public static final String GET_CHILDREN           = "agencyNetwork"
                                                        + GET_CHILDREN_SUFFIX;
    public static final String GET_NETWORKS           = "agencyNetwork"
                                                        + GET_NETWORKS_SUFFIX;
    public static final String GET_USED_RELATIONSHIPS = "agencyNetwork.getUsedRelationships";
    private static final long  serialVersionUID       = 1L;

    // bi-directional many-to-one association to Agency
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(name = "child")
    private Agency child;

    //bi-directional many-to-one association to Agency
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.DETACH })
    @JoinColumn(name = "parent")
    private Agency parent;

    @ManyToOne(cascade = { CascadeType.PERSIST,
                           CascadeType.DETACH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "premise1")
    private AgencyNetwork premise1;

    @ManyToOne(cascade = { CascadeType.PERSIST,
                           CascadeType.DETACH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "premise2")
    private AgencyNetwork premise2;

    public AgencyNetwork() {
    }

    /**
     * @param updatedBy
     */
    public AgencyNetwork(Agency updatedBy) {
        super(updatedBy);
    }

    /**
     * @param relationship
     * @param updatedBy
     */
    public AgencyNetwork(Agency parent, Relationship relationship, Agency child,
                         Agency updatedBy) {
        super(relationship, updatedBy);
        this.parent = parent;
        this.child = child;
    }

    /**
     * @param relationship
     * @param updatedBy
     */
    public AgencyNetwork(Relationship relationship, Agency updatedBy) {
        super(relationship, updatedBy);
    }

    /**
     * @param id
     */
    public AgencyNetwork(UUID id) {
        super(id);
    }

    @Override
    public void delete(Triggers triggers) {
        triggers.delete(this);
    }

    @Override
    @JsonIgnore
    public Class<?> getAttributeClass() {
        return AgencyNetworkAttribute.class;
    }

    @Override
    @JsonGetter
    public Agency getChild() {
        return child;
    }

    @Override
    @JsonGetter
    public Agency getParent() {
        return parent;
    }

    /**
     * @return the premise1
     */
    @Override
    @JsonGetter
    public AgencyNetwork getPremise1() {
        return premise1;
    }

    /**
     * @return the premise2
     */
    @Override
    @JsonGetter
    public AgencyNetwork getPremise2() {
        return premise2;
    }

    public List<Relationship> getUsedRelationships(EntityManager em) {
        return em.createNamedQuery(GET_USED_RELATIONSHIPS, Relationship.class)
                 .getResultList();
    }

    @Override
    public void persist(Triggers triggers) {
        triggers.persist(this);
    }

    @Override
    public void setChild(Agency agency3) {
        child = agency3;
    }

    @Override
    public void setParent(Agency agency2) {
        parent = agency2;
    }
}
