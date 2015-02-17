/**
 * (C) Copyright 2015 Chiral Behaviors, LLC. All Rights Reserved
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
import java.util.HashSet;
import java.util.Set;

import com.chiralbehaviors.CoRE.Triggers;
import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.agency.AgencyNetwork;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.attribute.AttributeNetwork;
import com.chiralbehaviors.CoRE.attribute.unit.Unit;
import com.chiralbehaviors.CoRE.attribute.unit.UnitNetwork;
import com.chiralbehaviors.CoRE.event.Job;
import com.chiralbehaviors.CoRE.event.ProductChildSequencingAuthorization;
import com.chiralbehaviors.CoRE.event.ProductParentSequencingAuthorization;
import com.chiralbehaviors.CoRE.event.ProductSelfSequencingAuthorization;
import com.chiralbehaviors.CoRE.event.ProductSiblingSequencingAuthorization;
import com.chiralbehaviors.CoRE.event.status.StatusCode;
import com.chiralbehaviors.CoRE.event.status.StatusCodeNetwork;
import com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing;
import com.chiralbehaviors.CoRE.location.Location;
import com.chiralbehaviors.CoRE.location.LocationNetwork;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.TriggerException;
import com.chiralbehaviors.CoRE.network.NetworkInference;
import com.chiralbehaviors.CoRE.network.Relationship;
import com.chiralbehaviors.CoRE.network.RelationshipNetwork;
import com.chiralbehaviors.CoRE.product.Product;
import com.chiralbehaviors.CoRE.product.ProductNetwork;
import com.chiralbehaviors.CoRE.time.Interval;
import com.chiralbehaviors.CoRE.time.IntervalNetwork;

/**
 * @author hhildebrand
 *
 *         This class implements the animations logic for the Ultrastructure
 *         model. Abstractly, this logic is driven by state events. Concretely,
 *         this is implemented by database triggers. This class models a simple
 *         state model of persist, update, delete style events. As the
 *         animations model is conceptually simple and unchanging, we don't need
 *         a general mechanism of dynamically registering triggers n' such. We
 *         just inline the animation logic in the state methods, delegating to
 *         the appropriate model for implementation. What this means in practice
 *         is that this is the class that creates the high level logic around
 *         state change of an Ultrastructure instance. This is the high level,
 *         disambiguation logic of Ultrastructure animation - the Rules Engine
 *         (tm).
 */
public class Animations implements Triggers {

    private boolean            inferAgencyNetwork;
    private boolean            inferAttributeNetwork;
    private boolean            inferIntervalNetwork;
    private boolean            inferLocationNetwork;
    private boolean            inferProductNetwork;
    private boolean            inferRelationshipNetwork;
    private boolean            inferStatusCodeNetwork;
    private boolean            inferUnitNetwork;
    private final Model        model;
    private final Set<Product> modifiedServices = new HashSet<>();

    public Animations(Model model) {
        this.model = model;
    }

    public void afterCommit() {
        reset();
    }

    public void afterRollback() {
        reset();
    }

    public void beforeCommit() throws TriggerException {
        try {
            model.getJobModel().validateStateGraph(modifiedServices);
        } catch (SQLException e) {
            throw new TriggerException(
                                       "StatusCodeSequencing validation failed",
                                       e);
        }
        propagate();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.agency.Agency)
     */
    @Override
    public void delete(Agency a) {
        inferAgencyNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.agency.AgencyNetwork)
     */
    @Override
    public void delete(AgencyNetwork a) {
        inferAgencyNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.attribute.Attribute)
     */
    @Override
    public void delete(Attribute a) {
        inferAttributeNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.attribute.AttributeNetwork)
     */
    @Override
    public void delete(AttributeNetwork a) {
        inferAttributeNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.time.Interval)
     */
    @Override
    public void delete(Interval i) {
        inferIntervalNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.time.IntervalNetwork)
     */
    @Override
    public void delete(IntervalNetwork i) {
        inferAttributeNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.location.Location)
     */
    @Override
    public void delete(Location l) {
        inferLocationNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.location.LocationNetwork)
     */
    @Override
    public void delete(LocationNetwork l) {
        inferLocationNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.network.NetworkInference)
     */
    @Override
    public void delete(NetworkInference inference) {
        propagateAll();
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.product.Product)
     */
    @Override
    public void delete(Product p) {
        inferProductNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.product.ProductNetwork)
     */
    @Override
    public void delete(ProductNetwork p) {
        inferProductNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.network.Relationship)
     */
    @Override
    public void delete(Relationship r) {
        inferRelationshipNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.network.RelationshipNetwork)
     */
    @Override
    public void delete(RelationshipNetwork r) {
        inferRelationshipNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.event.status.StatusCode)
     */
    @Override
    public void delete(StatusCode s) {
        inferStatusCodeNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.event.status.StatusCodeNetwork)
     */
    @Override
    public void delete(StatusCodeNetwork s) {
        inferStatusCodeNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.attribute.unit.Unit)
     */
    @Override
    public void delete(Unit u) {
        inferUnitNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#delete(com.chiralbehaviors.CoRE.attribute.unit.UnitNetwork)
     */
    @Override
    public void delete(UnitNetwork u) {
        inferUnitNetwork = true;
    }

    public void log(StatusCodeSequencing scs) {
        modifiedServices.add(scs.getService());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.agency.AgencyNetwork)
     */
    @Override
    public void persist(AgencyNetwork a) {
        inferAgencyNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.attribute.AttributeNetwork)
     */
    @Override
    public void persist(AttributeNetwork a) {
        inferAttributeNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.time.IntervalNetwork)
     */
    @Override
    public void persist(IntervalNetwork i) {
        inferIntervalNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.event.Job)
     */
    @Override
    public void persist(Job j) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.location.LocationNetwork)
     */
    @Override
    public void persist(LocationNetwork l) {
        inferLocationNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.event.ProductChildSequencingAuthorization)
     */
    @Override
    public void persist(ProductChildSequencingAuthorization pcsa) {
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.product.ProductNetwork)
     */
    @Override
    public void persist(ProductNetwork p) {
        inferProductNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.event.ProductParentSequencingAuthorization)
     */
    @Override
    public void persist(ProductParentSequencingAuthorization ppsa) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.event.ProductSelfSequencingAuthorization)
     */
    @Override
    public void persist(ProductSelfSequencingAuthorization pssa) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.event.ProductSiblingSequencingAuthorization)
     */
    @Override
    public void persist(ProductSiblingSequencingAuthorization pssa) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.network.RelationshipNetwork)
     */
    @Override
    public void persist(RelationshipNetwork r) {
        inferRelationshipNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.event.status.StatusCodeNetwork)
     */
    @Override
    public void persist(StatusCodeNetwork sc) {
        inferStatusCodeNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.event.status.StatusCodeSequencing)
     */
    @Override
    public void persist(StatusCodeSequencing scs) {
        modifiedServices.add(scs.getService());
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#persist(com.chiralbehaviors.CoRE.attribute.unit.UnitNetwork)
     */
    @Override
    public void persist(UnitNetwork u) {
        inferUnitNetwork = true;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.Triggers#update(com.chiralbehaviors.CoRE.event.Job)
     */
    @Override
    public void update(Job j) {
        // TODO Auto-generated method stub

    }

    private void clearPropagation() {
        inferIntervalNetwork = false;
        inferRelationshipNetwork = false;
        inferStatusCodeNetwork = false;
        inferUnitNetwork = false;
        inferProductNetwork = false;
        inferLocationNetwork = false;
        inferAttributeNetwork = false;
        inferAgencyNetwork = false;
        inferRelationshipNetwork = false;
    }

    private void propagate() {
        if (inferAgencyNetwork) {
            model.getAgencyModel().propagate();
        }
        if (inferAttributeNetwork) {
            model.getAttributeModel().propagate();
        }
        if (inferIntervalNetwork) {
            model.getIntervalModel().propagate();
        }
        if (inferLocationNetwork) {
            model.getLocationModel().propagate();
        }
        if (inferProductNetwork) {
            model.getProductModel().propagate();
        }
        if (inferRelationshipNetwork) {
            model.getRelationshipModel().propagate();
        }
        if (inferStatusCodeNetwork) {
            model.getStatusCodeModel().propagate();
        }
        if (inferUnitNetwork) {
            model.getUnitModel().propagate();
        }
    }

    private void propagateAll() {
        inferIntervalNetwork = true;
        inferRelationshipNetwork = true;
        inferStatusCodeNetwork = true;
        inferUnitNetwork = true;
        inferProductNetwork = true;
        inferLocationNetwork = true;
        inferAttributeNetwork = true;
        inferAgencyNetwork = true;
        inferRelationshipNetwork = true;
    }

    private void reset() {
        clearPropagation();
        modifiedServices.clear();
    }
}
