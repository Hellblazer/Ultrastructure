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

package com.chiralbehaviors.CoRE.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.chiralbehaviors.CoRE.UuidGenerator;
import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.agency.AgencyNetwork;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.attribute.AttributeNetwork;
import com.chiralbehaviors.CoRE.attribute.unit.Unit;
import com.chiralbehaviors.CoRE.attribute.unit.UnitNetwork;
import com.chiralbehaviors.CoRE.event.status.StatusCode;
import com.chiralbehaviors.CoRE.event.status.StatusCodeNetwork;
import com.chiralbehaviors.CoRE.json.CoREModule;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownAgency;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownAttribute;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownInterval;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownLocation;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownProduct;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownRelationship;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownStatusCode;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject.WellKnownUnit;
import com.chiralbehaviors.CoRE.location.Location;
import com.chiralbehaviors.CoRE.location.LocationNetwork;
import com.chiralbehaviors.CoRE.network.Relationship;
import com.chiralbehaviors.CoRE.network.RelationshipNetwork;
import com.chiralbehaviors.CoRE.product.Product;
import com.chiralbehaviors.CoRE.product.ProductNetwork;
import com.chiralbehaviors.CoRE.time.Interval;
import com.chiralbehaviors.CoRE.time.IntervalNetwork;
import com.chiralbehaviors.CoRE.workspace.WorkspaceSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Repository of immutable kernal rules
 * 
 * This used to be the standard. Now we use workspaces. However, kernel is a
 * fundamental workspace, and it's needed a lot. Consequently, because of the
 * way we do Java stored procedures, reentrancy requires a new image of the
 * kernel workspace in the context of the entity manager. Sucks to be us.
 * 
 * Anyways, this is a much faster load than the CachedWorkspace. Saves a lot of
 * overhead in txns that involve reentrant java calls - which, is like every
 * one.
 *
 * @author hhildebrand
 *
 */
public class KernelImpl implements Kernel {

    public static final String SELECT_TABLE              = "SELECT table_schema || '.' || table_name AS name FROM information_schema.tables WHERE table_schema='ruleform' AND table_type='BASE TABLE' ORDER BY table_name";
    public static final String ZERO                      = UuidGenerator.toBase64(new UUID(
                                                                                           0,
                                                                                           0));
    static final String        KERNEL_WORKSPACE_RESOURCE = "/kernel-workspace.json";

    public static void clear(EntityManager em) throws SQLException {
        Connection connection = em.unwrap(Connection.class);
        connection.setAutoCommit(false);
        alterTriggers(connection, false);
        ResultSet r = connection.createStatement().executeQuery(KernelImpl.SELECT_TABLE);
        while (r.next()) {
            String table = r.getString("name");
            String query = String.format("DELETE FROM %s", table);
            connection.createStatement().execute(query);
        }
        r.close();
        alterTriggers(connection, true);
        connection.commit();
    }

    public static void clearAndLoadKernel(EntityManager em)
                                                           throws SQLException,
                                                           IOException {
        KernelImpl.clear(em);
        em.getTransaction().begin();
        KernelImpl.loadKernel(em);
        em.getTransaction().commit();
    }

    public static Kernel getKernel(EntityManager em) {
        return new KernelImpl(em);
    }

    public static void loadKernel(EntityManager em) throws IOException {
        KernelImpl.loadKernel(em,
                              KernelImpl.class.getResourceAsStream(KernelImpl.KERNEL_WORKSPACE_RESOURCE));
    }

    public static void loadKernel(EntityManager em, InputStream is)
                                                                   throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new CoREModule());
        WorkspaceSnapshot workspace = mapper.readValue(is,
                                                       WorkspaceSnapshot.class);
        workspace.retarget(em);
    }

    static void alterTriggers(Connection connection, boolean enable)
                                                                    throws SQLException {
        for (String table : new String[] { "ruleform.agency",
                "ruleform.product", "ruleform.location" }) {
            String query = String.format("ALTER TABLE %s %s TRIGGER ALL",
                                         table, enable ? "ENABLE" : "DISABLE");
            connection.createStatement().execute(query);
        }
        ResultSet r = connection.createStatement().executeQuery(KernelImpl.SELECT_TABLE);
        while (r.next()) {
            String table = r.getString("name");
            String query = String.format("ALTER TABLE %s %s TRIGGER ALL",
                                         table, enable ? "ENABLE" : "DISABLE");
            connection.createStatement().execute(query);
        }
        r.close();
    }

    /**
     *
     * @param wko
     * @return the {@link Agency} corresponding to the well known object
     */
    static Agency find(EntityManager em, WellKnownAgency wko) {
        return em.find(Agency.class, wko.id());
    }

    /**
     *
     * @param wko
     * @return the {@link Attribute} corresponding to the well known object
     */
    static Attribute find(EntityManager em, WellKnownAttribute wko) {
        return em.find(Attribute.class, wko.id());
    }

    static Interval find(EntityManager em, WellKnownInterval wko) {
        return em.find(Interval.class, wko.id());
    }

    /**
     *
     * @param wko
     * @return the {@link Location} corresponding to the well known object
     */
    static Location find(EntityManager em, WellKnownLocation wko) {
        return em.find(Location.class, wko.id());
    }

    /**
     *
     * @param wko
     * @return the {@link Product} corresponding to the well known object
     */
    static Product find(EntityManager em, WellKnownProduct wko) {
        return em.find(Product.class, wko.id());
    }

    /**
     *
     * @param wko
     * @return the {@link StatusCode} corresponding to the well known object
     */
    static StatusCode find(EntityManager em, WellKnownStatusCode wko) {
        return em.find(StatusCode.class, wko.id());
    }

    static Unit find(EntityManager em, WellKnownUnit wko) {
        return em.find(Unit.class, wko.id());
    }

    private final Agency              agency;
    private final Agency              anyAgency;
    private final Attribute           anyAttribute;
    private final Interval            anyInterval;
    private final Location            anyLocation;
    private final Product             anyProduct;
    private final Relationship        anyRelationship;
    private final StatusCode          anyStatusCode;
    private final Unit                anyUnit;
    private final Attribute           attribute;
    private final Relationship        contains;
    private final Agency              copyAgency;
    private final Attribute           copyAttribute;
    private final Interval            copyInterval;
    private final Location            copyLocation;
    private final Product             copyProduct;
    private final Relationship        copyRelationship;
    private final StatusCode          copyStatusCode;
    private final Unit                copyUnit;
    private final Agency              core;
    private final Agency              coreAnimationSoftware;
    private final Agency              coreModel;
    private final Agency              coreUser;
    private final Relationship        developed;
    private final Relationship        developedBy;
    private final Relationship        equals;
    private final Relationship        formerMemberOf;
    private final Relationship        greaterThan;
    private final Relationship        greaterThanOrEqual;
    private final Relationship        hadMember;
    private final Relationship        hasException;
    private final Relationship        hasHead;
    private final Relationship        hasMember;
    private final Relationship        hasVersion;
    private final Relationship        headOf;
    private final Relationship        includes;
    private final Agency              inverseSoftware;
    private final Relationship        inWorkspace;
    private final Relationship        isA;
    private final Relationship        isContainedIn;
    private final Relationship        isExceptionTo;
    private final Relationship        isLocationOf;
    private final Relationship        lessThan;
    private final Relationship        lessThanOrEqual;
    private final Location            location;
    private final Attribute           loginAttribute;
    private final Relationship        mapsToLocation;
    private final Relationship        memberOf;
    private final Agency              notApplicableAgency;
    private final Attribute           notApplicableAttribute;
    private final Interval            notApplicableInterval;
    private final Location            notApplicableLocation;
    private final Product             notApplicableProduct;
    private final Relationship        notApplicableRelationship;
    private final StatusCode          notApplicableStatusCode;
    private final Unit                notApplicableUnit;
    private final Relationship        ownedBy;
    private final Relationship        owns;
    private final Attribute           passwordHashAttribute;
    private final Product             product;
    private final Agency              propagationSoftware;
    private final Relationship        prototype;
    private final Relationship        prototypeOf;
    private final AgencyNetwork       rootAgencyNetwork;
    private final AttributeNetwork    rootAttributeNetwork;
    private final IntervalNetwork     rootIntervalNetwork;
    private final LocationNetwork     rootLocationNetwork;
    private final ProductNetwork      rootProductNetwork;
    private final RelationshipNetwork rootRelationshipNetwork;
    private final StatusCodeNetwork   rootStatusCodeNetwork;
    private final UnitNetwork         rootUnitNetwork;
    private final Agency              sameAgency;
    private final Attribute           sameAttribute;

    private final Interval            sameInterval;

    private final Location            sameLocation;

    private final Product             sameProduct;

    private final Relationship        sameRelationship;

    private final StatusCode          sameStatusCode;

    private final Unit                sameUnit;

    private final Agency              specialSystemAgency;

    private final Agency              superUser;

    private final StatusCode          unset;

    private final Unit                unsetUnit;

    private final Relationship        versionOf;

    private final Product             workspace;

    private final Relationship        workspaceOf;

    public KernelImpl(EntityManager em) {

        attribute = find(em, WellKnownAttribute.ATTRIBUTE);
        anyAttribute = find(em, WellKnownAttribute.ANY);
        copyAttribute = find(em, WellKnownAttribute.COPY);
        notApplicableAttribute = find(em, WellKnownAttribute.NOT_APPLICABLE);
        sameAttribute = find(em, WellKnownAttribute.SAME);
        loginAttribute = find(em, WellKnownAttribute.LOGIN);
        passwordHashAttribute = find(em, WellKnownAttribute.PASSWORD_HASH);

        product = find(em, WellKnownProduct.ENTITY);
        anyProduct = find(em, WellKnownProduct.ANY);
        copyProduct = find(em, WellKnownProduct.COPY);
        sameProduct = find(em, WellKnownProduct.SAME);
        notApplicableProduct = find(em, WellKnownProduct.NOT_APPLICABLE);
        workspace = find(em, WellKnownProduct.WORKSPACE);

        location = find(em, WellKnownLocation.LOCATION);
        anyLocation = find(em, WellKnownLocation.ANY);
        copyLocation = find(em, WellKnownLocation.COPY);
        notApplicableLocation = find(em, WellKnownLocation.NOT_APPLICABLE);
        sameLocation = find(em, WellKnownLocation.SAME);

        coreUser = find(em, WellKnownAgency.CORE_USER);
        agency = find(em, WellKnownAgency.AGENCY);
        anyAgency = find(em, WellKnownAgency.ANY);
        copyAgency = find(em, WellKnownAgency.COPY);
        core = find(em, WellKnownAgency.CORE);
        coreAnimationSoftware = find(em,
                                     WellKnownAgency.CORE_ANIMATION_SOFTWARE);
        propagationSoftware = find(em, WellKnownAgency.PROPAGATION_SOFTWARE);
        specialSystemAgency = find(em, WellKnownAgency.SPECIAL_SYSTEM_AGENCY);
        coreModel = find(em, WellKnownAgency.CORE_MODEL);
        superUser = find(em, WellKnownAgency.SUPER_USER);
        inverseSoftware = find(em, WellKnownAgency.INVERSE_SOFTWARE);
        sameAgency = find(em, WellKnownAgency.SAME);
        notApplicableAgency = find(em, WellKnownAgency.NOT_APPLICABLE);

        anyRelationship = find(em, WellKnownRelationship.ANY);
        copyRelationship = find(em, WellKnownRelationship.COPY);
        sameRelationship = find(em, WellKnownRelationship.SAME);
        isContainedIn = find(em, WellKnownRelationship.IS_CONTAINED_IN);
        contains = find(em, WellKnownRelationship.CONTAINS);
        isA = find(em, WellKnownRelationship.IS_A);
        includes = find(em, WellKnownRelationship.INCLUDES);
        hasException = find(em, WellKnownRelationship.HAS_EXCEPTION);
        isExceptionTo = find(em, WellKnownRelationship.IS_EXCEPTION_TO);
        isLocationOf = find(em, WellKnownRelationship.IS_LOCATION_OF);
        mapsToLocation = find(em, WellKnownRelationship.MAPS_TO_LOCATION);
        prototype = find(em, WellKnownRelationship.PROTOTYPE);
        prototypeOf = find(em, WellKnownRelationship.PROTOTYPE_OF);
        greaterThan = find(em, WellKnownRelationship.GREATER_THAN);
        lessThan = find(em, WellKnownRelationship.LESS_THAN);
        equals = find(em, WellKnownRelationship.EQUALS);
        lessThanOrEqual = find(em, WellKnownRelationship.LESS_THAN_OR_EQUAL);
        greaterThanOrEqual = find(em,
                                  WellKnownRelationship.GREATER_THAN_OR_EQUAL);
        developed = find(em, WellKnownRelationship.DEVELOPED);
        developedBy = find(em, WellKnownRelationship.DEVELOPED_BY);
        versionOf = find(em, WellKnownRelationship.VERSION_OF);
        hasVersion = find(em, WellKnownRelationship.HAS_VERSION);
        hasMember = find(em, WellKnownRelationship.HAS_MEMBER);
        memberOf = find(em, WellKnownRelationship.MEMBER_OF);
        headOf = find(em, WellKnownRelationship.HEAD_OF);
        hasHead = find(em, WellKnownRelationship.HAS_HEAD);
        hadMember = find(em, WellKnownRelationship.HAD_MEMBER);
        formerMemberOf = find(em, WellKnownRelationship.FORMER_MEMBER_OF);
        notApplicableRelationship = find(em,
                                         WellKnownRelationship.NOT_APPLICABLE);
        ownedBy = find(em, WellKnownRelationship.OWNED_BY);
        owns = find(em, WellKnownRelationship.OWNS);
        inWorkspace = find(em, WellKnownRelationship.IN_WORKSPACE);
        workspaceOf = find(em, WellKnownRelationship.WORKSPACE_OF);

        unset = find(em, WellKnownStatusCode.UNSET);
        anyStatusCode = find(em, WellKnownStatusCode.ANY);
        copyStatusCode = find(em, WellKnownStatusCode.COPY);
        sameStatusCode = find(em, WellKnownStatusCode.SAME);
        notApplicableStatusCode = find(em, WellKnownStatusCode.NOT_APPLICABLE);

        unsetUnit = find(em, WellKnownUnit.UNSET);
        anyUnit = find(em, WellKnownUnit.ANY);
        copyUnit = find(em, WellKnownUnit.COPY);
        sameUnit = find(em, WellKnownUnit.SAME);
        notApplicableUnit = find(em, WellKnownUnit.NOT_APPLICABLE);

        anyInterval = find(em, WellKnownInterval.ANY);
        copyInterval = find(em, WellKnownInterval.COPY);
        sameInterval = find(em, WellKnownInterval.SAME);
        notApplicableInterval = find(em, WellKnownInterval.NOT_APPLICABLE);

        rootAgencyNetwork = em.find(AgencyNetwork.class, ZERO);
        rootAttributeNetwork = em.find(AttributeNetwork.class, ZERO);
        rootIntervalNetwork = em.find(IntervalNetwork.class, ZERO);
        rootLocationNetwork = em.find(LocationNetwork.class, ZERO);
        rootProductNetwork = em.find(ProductNetwork.class, ZERO);
        rootRelationshipNetwork = em.find(RelationshipNetwork.class, ZERO);
        rootStatusCodeNetwork = em.find(StatusCodeNetwork.class, ZERO);
        rootUnitNetwork = em.find(UnitNetwork.class, ZERO);
        detatch(em);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getAgency()
     */
    @Override
    public Agency getAgency() {
        return agency;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getAnyAgency()
     */
    @Override
    public Agency getAnyAgency() {
        return anyAgency;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getAnyAttribute()
     */
    @Override
    public Attribute getAnyAttribute() {
        return anyAttribute;
    }

    @Override
    public Interval getAnyInterval() {
        return anyInterval;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getAnyLocation()
     */
    @Override
    public Location getAnyLocation() {
        return anyLocation;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getAnyProduct()
     */
    @Override
    public Product getAnyProduct() {
        return anyProduct;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getAnyRelationship()
     */
    @Override
    public Relationship getAnyRelationship() {
        return anyRelationship;
    }

    @Override
    public StatusCode getAnyStatusCode() {
        return anyStatusCode;
    }

    @Override
    public Unit getAnyUnit() {
        return anyUnit;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getAttribute()
     */
    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getContains()
     */
    @Override
    public Relationship getContains() {
        return contains;
    }

    @Override
    public Agency getCopyAgency() {
        return copyAgency;
    }

    @Override
    public Attribute getCopyAttribute() {
        return copyAttribute;
    }

    @Override
    public Interval getCopyInterval() {
        return copyInterval;
    }

    @Override
    public Location getCopyLocation() {
        return copyLocation;
    }

    @Override
    public Product getCopyProduct() {
        return copyProduct;
    }

    @Override
    public Relationship getCopyRelationship() {
        return copyRelationship;
    }

    @Override
    public StatusCode getCopyStatusCode() {
        return copyStatusCode;
    }

    @Override
    public Unit getCopyUnit() {
        return copyUnit;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getCore()
     */
    @Override
    public Agency getCore() {
        return core;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.chiralbehaviors.CoRE.meta.models.Kernel#getCoreAnimationProcedure()
     */
    @Override
    public Agency getCoreAnimationSoftware() {
        return coreAnimationSoftware;
    }

    @Override
    public Agency getCoreModel() {
        return coreModel;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getCoreUser()
     */
    @Override
    public Agency getCoreUser() {
        return coreUser;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getDeveloped()
     */
    @Override
    public Relationship getDeveloped() {
        return developed;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getDevelopedBy()
     */
    @Override
    public Relationship getDevelopedBy() {
        return developedBy;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getEquals()
     */
    @Override
    public Relationship getEquals() {
        return equals;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getFormerMemberOf()
     */
    @Override
    public Relationship getFormerMemberOf() {
        return formerMemberOf;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getGreaterThan()
     */
    @Override
    public Relationship getGreaterThan() {
        return greaterThan;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getGreaterThanOrEqual()
     */
    @Override
    public Relationship getGreaterThanOrEqual() {
        return greaterThanOrEqual;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getHadMember()
     */
    @Override
    public Relationship getHadMember() {
        return hadMember;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getHasException()
     */
    @Override
    public Relationship getHasException() {
        return hasException;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getHasHead()
     */
    @Override
    public Relationship getHasHead() {
        return hasHead;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getHasMember()
     */
    @Override
    public Relationship getHasMember() {
        return hasMember;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getHasVersion()
     */
    @Override
    public Relationship getHasVersion() {
        return hasVersion;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getHeadOf()
     */
    @Override
    public Relationship getHeadOf() {
        return headOf;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getIncludes()
     */
    @Override
    public Relationship getIncludes() {
        return includes;
    }

    @Override
    public Agency getInverseSoftware() {
        return inverseSoftware;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.kernel.Kernel#getInWorkspace()
     */
    @Override
    public Relationship getInWorkspace() {
        return inWorkspace;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getIsA()
     */
    @Override
    public Relationship getIsA() {
        return isA;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getIsContainedIn()
     */
    @Override
    public Relationship getIsContainedIn() {
        return isContainedIn;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getIsExceptionTo()
     */
    @Override
    public Relationship getIsExceptionTo() {
        return isExceptionTo;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getIsLocationOf()
     */
    @Override
    public Relationship getIsLocationOf() {
        return isLocationOf;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getLessThan()
     */
    @Override
    public Relationship getLessThan() {
        return lessThan;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getLessThanOrEqual()
     */
    @Override
    public Relationship getLessThanOrEqual() {
        return lessThanOrEqual;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getLocation()
     */
    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Attribute getLoginAttribute() {
        return loginAttribute;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getMapsToLocation()
     */
    @Override
    public Relationship getMapsToLocation() {
        return mapsToLocation;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getMemberOf()
     */
    @Override
    public Relationship getMemberOf() {
        return memberOf;
    }

    /**
     * @return the notApplicableAgency
     */
    @Override
    public Agency getNotApplicableAgency() {
        return notApplicableAgency;
    }

    /**
     * @return the notApplicableAttribute
     */
    @Override
    public Attribute getNotApplicableAttribute() {
        return notApplicableAttribute;
    }

    @Override
    public Interval getNotApplicableInterval() {
        return notApplicableInterval;
    }

    /**
     * @return the notApplicableLocation
     */
    @Override
    public Location getNotApplicableLocation() {
        return notApplicableLocation;
    }

    /**
     * @return the notApplicableProduct
     */
    @Override
    public Product getNotApplicableProduct() {
        return notApplicableProduct;
    }

    /**
     * @return the notApplicableRelationship
     */
    @Override
    public Relationship getNotApplicableRelationship() {
        return notApplicableRelationship;
    }

    @Override
    public StatusCode getNotApplicableStatusCode() {
        return notApplicableStatusCode;
    }

    @Override
    public Unit getNotApplicableUnit() {
        return notApplicableUnit;
    }

    /**
     * @return the ownedBy
     */
    @Override
    public Relationship getOwnedBy() {
        return ownedBy;
    }

    /**
     * @return the owns
     */
    @Override
    public Relationship getOwns() {
        return owns;
    }

    @Override
    public Attribute getPasswordHashAttribute() {
        return passwordHashAttribute;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getProduct()
     */
    @Override
    public Product getProduct() {
        return product;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getPropagationSoftware()
     */
    @Override
    public Agency getPropagationSoftware() {
        return propagationSoftware;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getPrototype()
     */
    @Override
    public Relationship getPrototype() {
        return prototype;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getPrototypeOf()
     */
    @Override
    public Relationship getPrototypeOf() {
        return prototypeOf;
    }

    /**
     * @return the rootAgencyNetwork
     */
    @Override
    public AgencyNetwork getRootAgencyNetwork() {
        return rootAgencyNetwork;
    }

    /**
     * @return the rootAttributeNetwork
     */
    @Override
    public AttributeNetwork getRootAttributeNetwork() {
        return rootAttributeNetwork;
    }

    /**
     * @return the rootIntervalNetwork
     */
    @Override
    public IntervalNetwork getRootIntervalNetwork() {
        return rootIntervalNetwork;
    }

    /**
     * @return the rootLocationNetwork
     */
    @Override
    public LocationNetwork getRootLocationNetwork() {
        return rootLocationNetwork;
    }

    /**
     * @return the rootProductNetwork
     */
    @Override
    public ProductNetwork getRootProductNetwork() {
        return rootProductNetwork;
    }

    /**
     * @return the rootRelationshipNetwork
     */
    @Override
    public RelationshipNetwork getRootRelationshipNetwork() {
        return rootRelationshipNetwork;
    }

    /**
     * @return the rootStatusCodeNetwork
     */
    @Override
    public StatusCodeNetwork getRootStatusCodeNetwork() {
        return rootStatusCodeNetwork;
    }

    /**
     * @return the rootUnitNetwork
     */
    @Override
    public UnitNetwork getRootUnitNetwork() {
        return rootUnitNetwork;
    }

    /**
     * @return the sameAgency
     */
    @Override
    public Agency getSameAgency() {
        return sameAgency;
    }

    @Override
    public Attribute getSameAttribute() {
        return sameAttribute;
    }

    @Override
    public Interval getSameInterval() {
        return sameInterval;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.Kernel#getSameLocation()
     */
    @Override
    public Location getSameLocation() {
        return sameLocation;
    }

    /**
     * @return the sameProduct
     */
    @Override
    public Product getSameProduct() {
        return sameProduct;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getSameRelationship()
     */
    @Override
    public Relationship getSameRelationship() {
        return sameRelationship;
    }

    @Override
    public StatusCode getSameStatusCode() {
        return sameStatusCode;
    }

    @Override
    public Unit getSameUnit() {
        return sameUnit;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getSpecialSystemAgency()
     */
    @Override
    public Agency getSpecialSystemAgency() {
        return specialSystemAgency;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.Kernel#getSuperUser()
     */
    @Override
    public Agency getSuperUser() {
        return superUser;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.Kernel#getUnset()
     */
    @Override
    public StatusCode getUnset() {
        return unset;
    }

    /* (non-Javadoc)
     * @see com.chiralbehaviors.CoRE.kernel.Kernel#getUnsetUnit()
     */
    @Override
    public Unit getUnsetUnit() {
        return unsetUnit;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.meta.models.Kernel#getVersionOf()
     */
    @Override
    public Relationship getVersionOf() {
        return versionOf;
    }

    @Override
    public Product getWorkspace() {
        return workspace;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.chiralbehaviors.CoRE.kernel.Kernel#getWorkspaceOf()
     */
    @Override
    public Relationship getWorkspaceOf() {
        return workspaceOf;
    }

    public void detatch(EntityManager em) {
        for (Field field : getClass().getDeclaredFields()) {
            try {
                em.detach(field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException(
                                                String.format("Can't detach field %s",
                                                              field), e);
            }
        }
    }

    /**
     *
     * @param wko
     * @return the {@link Relationship} corresponding to the well known object
     */
    Relationship find(EntityManager em, WellKnownRelationship wko) {
        return em.find(Relationship.class, wko.id());
    }
}
