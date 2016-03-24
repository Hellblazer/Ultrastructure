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

import static com.chiralbehaviors.CoRE.jooq.Tables.JOB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chiralbehaviors.CoRE.domain.ExistentialRuleform;
import com.chiralbehaviors.CoRE.domain.Location;
import com.chiralbehaviors.CoRE.domain.Product;
import com.chiralbehaviors.CoRE.domain.Relationship;
import com.chiralbehaviors.CoRE.domain.StatusCode;
import com.chiralbehaviors.CoRE.jooq.tables.records.ChildSequencingAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.JobChronologyRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.JobRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.MetaProtocolRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ParentSequencingAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ProtocolRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.SelfSequencingAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.StatusCodeSequencingRecord;
import com.chiralbehaviors.CoRE.meta.InferenceMap;
import com.chiralbehaviors.CoRE.meta.JobModel;
import com.hellblazer.utils.Tuple;

/**
 * @author hhildebrand
 *
 */
public class JobModelTest extends AbstractModelTest {

    private static JobModel        jobModel;

    private static OrderProcessing scenario;

    @BeforeClass
    public static void before() throws Exception {
        create.transaction(c -> {
            OrderProcessingLoader loader = new OrderProcessingLoader(model);
            loader.load();
            scenario = loader.createWorkspace(model)
                             .getAccessor(OrderProcessing.class);
        });
        jobModel = model.getJobModel();
        // model.setLogConfiguration(Utils.getDocument(JobModelTest.class.getResourceAsStream("/test-log-db.xml")));
    }

    @Test
    public void testDeliverWithoutMetaProtocol() {
        StatusCode startState = model.records()
                                     .newStatusCode("begin");
        // startState.setPropagateChildren(true);
        startState.insert();

        StatusCode delivered = model.records()
                                    .newStatusCode("delivered");
        delivered.insert();

        StatusCode shipState = model.records()
                                    .newStatusCode("shipping");
        shipState.insert();

        Product kiki = model.records()
                            .newProduct("Kiki's Delivery Service");
        kiki.insert();

        Product shipping = model.records()
                                .newProduct("Kiki's Shipping Service");
        shipping.insert();

        Product bento = model.records()
                             .newProduct("Tonkatsu Bento Box");
        bento.insert();

        StatusCodeSequencingRecord sequence = model.records()
                                                   .newStatusCodeSequencing(kiki,
                                                                            startState,
                                                                            delivered);
        sequence.insert();

        StatusCodeSequencingRecord childSequence = model.records()
                                                        .newStatusCodeSequencing(shipping,
                                                                                 shipState,
                                                                                 delivered);
        childSequence.insert();

        ProtocolRecord p = jobModel.newInitializedProtocol(kiki);
        p.setProduct(bento.getId());
        p.setRequester(kernel.getCore()
                             .getId());
        p.setDeliverTo(kernel.getAnyLocation()
                             .getId());
        p.setDeliverFrom(kernel.getAnyLocation()
                               .getId());
        p.setAssignTo(kernel.getCore()
                            .getId());
        p.setChildService(shipping.getId());
        p.setChildProduct(bento.getId());
        p.insert();

        JobRecord job = model.getJobModel()
                             .newInitializedJob(kiki);
        job.setAssignTo(kernel.getCore()
                              .getId());
        job.setProduct(bento.getId());
        job.setDeliverTo(kernel.getAnyLocation()
                               .getId());
        job.setDeliverFrom(kernel.getAnyLocation()
                                 .getId());
        job.setRequester(kernel.getCore()
                               .getId());
        jobModel.changeStatus(job, kernel.getUnset(), "transition during test");
        job.insert();
        jobModel.changeStatus(job, startState, "transition during test");

        JobRecord j = model.create()
                           .selectFrom(JOB)
                           .where(JOB.SERVICE.equal(shipping.getId()))
                           .fetchOne();
        assertNotNull(j);
    }

    @Test
    public void testEuOrder() throws Exception {
        JobRecord order = model.getJobModel()
                               .newInitializedJob(scenario.getDeliver());
        order.setAssignTo(scenario.getOrderFullfillment()
                                  .getId());
        order.setProduct(scenario.getABC486()
                                 .getId());
        order.setDeliverTo(scenario.getRC31()
                                   .getId());
        order.setDeliverFrom(scenario.getFactory1()
                                     .getId());
        order.setRequester(scenario.getCarfleurBon()
                                   .getId());
        order.insert();
        jobModel.changeStatus(order, scenario.getAvailable(),
                              "transition during test");
        jobModel.changeStatus(order, scenario.getActive(),
                              "transition during test");
        List<MetaProtocolRecord> metaProtocols = jobModel.getMetaprotocols(order);
        assertEquals(1, metaProtocols.size());
        Map<ProtocolRecord, InferenceMap> protocols = jobModel.getProtocols(order);
        assertEquals(2, protocols.size());
        List<JobRecord> jobs = jobModel.getAllChildren(order);
        assertEquals(6, jobs.size());
    }

    @Test
    public void testGenerateJobs() throws Exception {
        JobRecord job = model.getJobModel()
                             .newInitializedJob(scenario.getDeliver());
        job.setAssignTo(scenario.getOrderFullfillment()
                                .getId());
        job.setProduct(scenario.getABC486()
                               .getId());
        job.setDeliverTo(scenario.getRSB225()
                                 .getId());
        job.setDeliverFrom(scenario.getFactory1()
                                   .getId());
        job.setRequester(scenario.getGeorgetownUniversity()
                                 .getId());
        jobModel.changeStatus(job, scenario.getAvailable(), "Test transition");

        List<JobRecord> jobs = jobModel.generateImplicitJobs(job);
        TestDebuggingUtil.printJobs(jobs);
    }

    @Test
    public void testGenerateJobsFromProtocols() throws Exception {
        Product service = model.records()
                               .newProduct("test service");
        service.insert();
        MetaProtocolRecord mp = jobModel.newInitializedMetaProtocol(service);
        mp.setAssignTo(kernel.getDevelopedBy()
                             .getId());
        mp.setDeliverTo(kernel.getGreaterThanOrEqual()
                              .getId());
        mp.insert();
        ProtocolRecord p = jobModel.newInitializedProtocol(service);
        p.setAssignTo(kernel.getPropagationSoftware()
                            .getId());
        p.insert();
        JobRecord order = jobModel.newInitializedJob(service);
        order.setAssignTo(kernel.getCoreUser()
                                .getId());
        Location loc = model.records()
                            .newLocation("crap location");
        loc.insert();
        order.setDeliverTo(loc.getId());
        order.insert();
        TestDebuggingUtil.printProtocolGaps(jobModel.findProtocolGaps(order));
        TestDebuggingUtil.printMetaProtocolGaps(jobModel.findMetaProtocolGaps(order));
        List<ProtocolRecord> protocols = model.getJobModel()
                                              .getProtocolsFor(service);
        assertEquals(1, protocols.size());
        List<JobRecord> jobs = model.getJobModel()
                                    .generateImplicitJobs(order);
        for (JobRecord j : jobs) {
            assertNotNull(j.getAssignTo());
            assertNotNull(j.getService());
            assertNotNull(j.getProduct());
            assertNotNull(j.getDeliverTo());
            assertNotNull(j.getDeliverFrom());
            assertNotNull(j.getRequester());
            assertNotNull(j.getUpdatedBy());

        }

    }

    @Test
    public void testGetActiveJobs() throws Exception {
        JobRecord order = model.getJobModel()
                               .newInitializedJob(scenario.getDeliver());
        order.setAssignTo(scenario.getOrderFullfillment()
                                  .getId());
        order.setProduct(scenario.getABC486()
                                 .getId());
        order.setDeliverTo(scenario.getRSB225()
                                   .getId());
        order.setDeliverFrom(scenario.getFactory1()
                                     .getId());
        order.setRequester(scenario.getGeorgetownUniversity()
                                   .getId());
        order.insert();
        jobModel.changeStatus(order, scenario.getAvailable(),
                              "transition during test");
        jobModel.changeStatus(order, scenario.getActive(),
                              "transition during test");

        List<StatusCode> states = Arrays.asList(scenario.getActive(),
                                                scenario.getAvailable(),
                                                scenario.getAbandoned());
        List<JobRecord> active = jobModel.getActiveJobsFor(scenario.getOrderFullfillment(),
                                                           states);
        assertEquals(1, active.size());
    }

    @Test
    public void testIteration() throws Exception {
        Product service = model.records()
                               .newProduct("test service");
        service.insert();
        Product childService = model.records()
                                    .newProduct("child test service");
        childService.insert();
        Product parent = model.records()
                              .newProduct("Parent");
        parent.insert();
        Product child1 = model.records()
                              .newProduct("Child 1");
        child1.insert();
        Product child2 = model.records()
                              .newProduct("Child 2");
        child2.insert();
        Product child3 = model.records()
                              .newProduct("Child 3");
        child3.insert();
        Relationship childRelationship = model.records()
                                              .newRelationship("child of",
                                                               "test relationship",
                                                               "parentOf",
                                                               "test relationship inverse");
        model.getPhantasmModel()
             .link(parent, childRelationship, child1);
        model.getPhantasmModel()
             .link(parent, childRelationship, child2);
        model.getPhantasmModel()
             .link(parent, childRelationship, child3);

        List<Tuple<StatusCode, StatusCode>> sequencings = new ArrayList<>();
        sequencings.add(new Tuple<StatusCode, StatusCode>(scenario.getAvailable(),
                                                          scenario.getCompleted()));
        model.getJobModel()
             .createStatusCodeSequencings(service, sequencings);
        model.getJobModel()
             .createStatusCodeSequencings(childService, sequencings);

        ChildSequencingAuthorizationRecord auth = model.records()
                                                       .newChildSequencingAuthorization(service,
                                                                                        scenario.getAvailable(),
                                                                                        childService,
                                                                                        scenario.getAvailable());
        auth.insert();
        ProtocolRecord p = jobModel.newInitializedProtocol(service);
        p.setChildrenRelationship(childRelationship.getId());
        p.setChildService(childService.getId());
        p.insert();
        JobRecord order = jobModel.newInitializedJob(service);
        order.setProduct(parent.getId());
        order.setStatus(kernel.getUnset()
                              .getId());
        order.insert();
        List<ProtocolRecord> protocols = model.getJobModel()
                                              .getProtocolsFor(service);
        assertEquals(1, protocols.size());
        List<JobRecord> jobs = model.getJobModel()
                                    .insert(order, protocols.get(0));
        assertEquals(3, jobs.size());
        jobModel.changeStatus(order, scenario.getAvailable(), null);

        for (JobRecord j : jobs) {
            assertEquals(scenario.getAvailable(), j.getStatus());
        }
    }

    @Test
    public void testJobChronologyOnStatusUpdate() throws Exception {
        JobRecord order = model.getJobModel()
                               .newInitializedJob(scenario.getDeliver());
        order.setAssignTo(scenario.getOrderFullfillment()
                                  .getId());
        order.setProduct(scenario.getABC486()
                                 .getId());
        order.setDeliverTo(scenario.getRSB225()
                                   .getId());
        order.setDeliverFrom(scenario.getFactory1()
                                     .getId());
        order.setRequester(scenario.getGeorgetownUniversity()
                                   .getId());
        jobModel.changeStatus(order, scenario.getAvailable(),
                              "Test transition");
        order.insert();
        List<JobChronologyRecord> chronologies = model.getJobModel()
                                                      .getChronologyForJob(order);
        assertEquals(String.format("Invalid number of chronologies: %s",
                                   chronologies),
                     2, chronologies.size());
        List<String> fieldErrors = verifyChronologyFields(order,
                                                          chronologies.get(1));

        assertEquals(fieldErrors.toString(), 0, fieldErrors.size());
        model.getJobModel()
             .changeStatus(order, scenario.getActive(), null);
        chronologies = model.getJobModel()
                            .getChronologyForJob(order);
        assertEquals(3, chronologies.size());
        for (JobChronologyRecord c : chronologies) {
            fieldErrors = verifyChronologyFields(order, c);
            if (fieldErrors == null || fieldErrors.size() == 0) {
                break;
            }
        }
        assertEquals(0, fieldErrors.size());
    }

    @Test
    public void testJobGenerationAndSequencing() throws Exception {
        Product pushit = model.records()
                              .newProduct("Pushit Service", null);
        pushit.insert();

        Product shoveit = model.records()
                               .newProduct("Shoveit Service", null);
        shoveit.insert();

        Product pullIt = model.records()
                              .newProduct("Pullit Service", null);
        pullIt.insert();

        StatusCode pushingMe = model.records()
                                    .newStatusCode("Pushing Me", null);
        pushingMe.setPropagateChildren(true);
        pushingMe.insert();

        StatusCode shovingMe = model.records()
                                    .newStatusCode("Shoving Me", null);
        shovingMe.insert();

        ProtocolRecord p = model.getJobModel()
                                .newInitializedProtocol(pushit);
        p.setProduct(pushit.getId());
        p.setChildService(shoveit.getId());
        p.insert();
        model.getJobModel()
             .createStatusCodeChain(pushit,
                                    new StatusCode[] { pushingMe, shovingMe,
                                                       scenario.getCompleted() });

        SelfSequencingAuthorizationRecord auth = model.records()
                                                      .newSelfSequencingAuthorization(pushit,
                                                                                      pushingMe,
                                                                                      shovingMe);
        auth.insert();

        JobRecord push = model.getJobModel()
                              .newInitializedJob(pushit);

        List<JobRecord> children = model.getJobModel()
                                        .getAllChildren(push);
        assertEquals(0, children.size());

        model.getJobModel()
             .changeStatus(push, pushingMe, null);
        push.setProduct(pushit.getId());
        children = model.getJobModel()
                        .getAllChildren(push);
        assertEquals(1, children.size());
        assertEquals(shovingMe, push.getStatus());
    }

    @Test
    public void testMetaProtocols() throws Exception {
        JobRecord job = model.getJobModel()
                             .newInitializedJob(scenario.getDeliver());
        job.setAssignTo(scenario.getOrderFullfillment()
                                .getId());
        job.setProduct(scenario.getABC486()
                               .getId());
        job.setDeliverTo(scenario.getRSB225()
                                 .getId());
        job.setDeliverFrom(scenario.getFactory1()
                                   .getId());
        job.setRequester(scenario.getGeorgetownUniversity()
                                 .getId());
        jobModel.changeStatus(job, scenario.getAvailable(), "Test transition");
        List<MetaProtocolRecord> metaProtocols = jobModel.getMetaprotocols(job);
        assertEquals(1, metaProtocols.size());
        Map<ProtocolRecord, InferenceMap> txfm = jobModel.getProtocols(job);
        assertEquals(2, txfm.size());
        List<ProtocolRecord> protocols = new ArrayList<>(txfm.keySet());
        assertEquals(scenario.getDeliver(), protocols.get(0)
                                                     .getService());
        assertEquals(scenario.getDeliver(), protocols.get(1)
                                                     .getService());
        assertEquals(kernel.getAnyAgency(), protocols.get(0)
                                                     .getRequester());
        assertEquals(kernel.getAnyAgency(), protocols.get(1)
                                                     .getRequester());
        assertEquals(kernel.getAnyProduct(), protocols.get(0)
                                                      .getProduct());
        assertEquals(kernel.getAnyProduct(), protocols.get(1)
                                                      .getProduct());
        assertEquals(kernel.getAnyLocation(), protocols.get(0)
                                                       .getDeliverFrom());
        assertEquals(kernel.getAnyLocation(), protocols.get(1)
                                                       .getDeliverFrom());
        assertEquals(kernel.getAnyLocation(), protocols.get(0)
                                                       .getDeliverTo());
        assertEquals(kernel.getAnyLocation(), protocols.get(1)
                                                       .getDeliverTo());
        assertEquals(scenario.getFactory1Agency(), protocols.get(0)
                                                            .getChildAssignTo());
        assertEquals(scenario.getFactory1Agency(), protocols.get(1)
                                                            .getChildAssignTo());
        if (protocols.get(0)
                     .getChildService()
                     .equals(scenario.getPick())) {
            assertEquals(scenario.getShip(), protocols.get(1)
                                                      .getChildService());
        } else {
            assertEquals(scenario.getShip(), protocols.get(0)
                                                      .getChildService());
            assertEquals(scenario.getPick(), protocols.get(1)
                                                      .getChildService());
        }

        job = model.getJobModel()
                   .newInitializedJob(scenario.getPrintPurchaseOrder());
        job.setAssignTo(scenario.getOrderFullfillment()
                                .getId());
        job.setProduct(scenario.getABC486()
                               .getId());
        job.setDeliverTo(scenario.getRSB225()
                                 .getId());
        job.setDeliverFrom(scenario.getFactory1()
                                   .getId());
        job.setRequester(scenario.getCarfleurBon()
                                 .getId());
        jobModel.changeStatus(job, kernel.getUnset(), "Transition from test");
        metaProtocols = jobModel.getMetaprotocols(job);
        assertEquals(1, metaProtocols.size());
        txfm = jobModel.getProtocols(job);
        assertEquals(1, txfm.size());
    }

    @Test
    public void testNonExemptOrder() throws Exception {
        JobRecord order = model.getJobModel()
                               .newInitializedJob(scenario.getDeliver());
        order.setAssignTo(scenario.getOrderFullfillment()
                                  .getId());
        order.setProduct(scenario.getABC486()
                                 .getId());
        order.setDeliverTo(scenario.getBHT37()
                                   .getId());
        order.setDeliverFrom(scenario.getFactory1()
                                     .getId());
        order.setRequester(scenario.getOrgA()
                                   .getId());
        order.insert();
        jobModel.changeStatus(order, scenario.getAvailable(),
                              "transition during test");
        jobModel.changeStatus(order, scenario.getActive(),
                              "transition during test");
        List<MetaProtocolRecord> metaProtocols = jobModel.getMetaprotocols(order);
        assertEquals(1, metaProtocols.size());
        Map<ProtocolRecord, InferenceMap> protocols = jobModel.getProtocols(order);
        assertEquals(2, protocols.size());
        List<JobRecord> jobs = jobModel.getAllChildren(order);
        assertEquals(6, jobs.size());
    }

    @Test
    public void testOrder() throws Exception {
        JobRecord order = model.getJobModel()
                               .newInitializedJob(scenario.getDeliver());
        order.setAssignTo(scenario.getOrderFullfillment()
                                  .getId());
        order.setProduct(scenario.getABC486()
                                 .getId());
        order.setDeliverTo(scenario.getRSB225()
                                   .getId());
        order.setDeliverFrom(scenario.getFactory1()
                                     .getId());
        order.setRequester(scenario.getGeorgetownUniversity()
                                   .getId());
        order.insert();
        jobModel.changeStatus(order, scenario.getAvailable(),
                              "transition during test");
        jobModel.changeStatus(order, scenario.getActive(),
                              "transition during test");
        List<MetaProtocolRecord> metaProtocols = jobModel.getMetaprotocols(order);
        assertEquals(1, metaProtocols.size());
        Map<ProtocolRecord, InferenceMap> protocols = jobModel.getProtocols(order);
        assertEquals(2, protocols.size());
        List<JobRecord> jobs = jobModel.getAllChildren(order);
        assertEquals(5, jobs.size());

        JobRecord creditCheck = model.create()
                                     .selectFrom(JOB)
                                     .where(JOB.SERVICE.equal(scenario.getCheckCredit()
                                                                      .getId()))
                                     .fetchOne();
        assertEquals(scenario.getAvailable(), creditCheck.getStatus());
        jobModel.changeStatus(creditCheck, scenario.getActive(),
                              "transition during test");
        jobModel.changeStatus(creditCheck, scenario.getCompleted(),
                              "transition during test");
        JobRecord pick = model.create()
                              .selectFrom(JOB)
                              .where(JOB.SERVICE.equal(scenario.getPick()
                                                               .getId()))
                              .fetchOne();
        assertEquals(scenario.getAvailable(), pick.getStatus());
        jobModel.changeStatus(pick, scenario.getActive(),
                              "transition during test");
        jobModel.changeStatus(pick, scenario.getCompleted(),
                              "transition during test");
        pick = model.create()
                    .selectFrom(JOB)
                    .where(JOB.SERVICE.equal(scenario.getPick()
                                                     .getId()))
                    .fetchOne();
        JobRecord ship = model.create()
                              .selectFrom(JOB)
                              .where(JOB.SERVICE.equal(scenario.getShip()
                                                               .getId()))
                              .fetchOne();
        List<JobRecord> pickSiblings = jobModel.getActiveSubJobsForService(model.records()
                                                                                .resolve(pick.getParent()),
                                                                           scenario.getShip());
        assertEquals(1, pickSiblings.size());
        assertEquals(scenario.getWaitingOnPurchaseOrder(), ship.getStatus());
        JobRecord fee = model.create()
                             .selectFrom(JOB)
                             .where(JOB.SERVICE.equal(scenario.getFee()
                                                              .getId()))
                             .fetchOne();
        ;
        jobModel.changeStatus(fee, scenario.getActive(),
                              "transition during test");
        jobModel.changeStatus(fee, scenario.getCompleted(),
                              "transition during test");
        JobRecord printPO = model.create()
                                 .selectFrom(JOB)
                                 .where(JOB.SERVICE.equal(scenario.getPrintPurchaseOrder()
                                                                  .getId()))
                                 .fetchOne();
        assertEquals(scenario.getAvailable(), printPO.getStatus());
        jobModel.changeStatus(printPO, scenario.getActive(),
                              "transition during test");
        jobModel.changeStatus(printPO, scenario.getCompleted(),
                              "transition during test");
        ship = model.create()
                    .selectFrom(JOB)
                    .where(JOB.SERVICE.equal(scenario.getShip()
                                                     .getId()))
                    .fetchOne();
        assertEquals(scenario.getAvailable(), ship.getStatus());
        jobModel.changeStatus(ship, scenario.getActive(),
                              "transition during test");
        jobModel.changeStatus(ship, scenario.getCompleted(),
                              "transition during test");
        JobRecord deliver = model.create()
                                 .selectFrom(JOB)
                                 .where(JOB.SERVICE.equal(scenario.getDeliver()
                                                                  .getId()))
                                 .fetchOne();
        assertEquals(scenario.getCompleted(), deliver.getStatus());
    }

    public void testSelfSequencing() {
        Product service = model.records()
                               .newProduct("My Service", null);
        service.insert();
        StatusCode a = model.records()
                            .newStatusCode("A", null);
        a.insert();
        StatusCode b = model.records()
                            .newStatusCode("B", null);
        b.insert();
        StatusCode c = model.records()
                            .newStatusCode("C", null);
        c.insert();

        List<Tuple<StatusCode, StatusCode>> sequences = new ArrayList<>();
        sequences.add(new Tuple<StatusCode, StatusCode>(a, b));
        sequences.add(new Tuple<StatusCode, StatusCode>(b, c));
        model.getJobModel()
             .createStatusCodeSequencings(service, sequences);

        SelfSequencingAuthorizationRecord auth = model.records()
                                                      .newSelfSequencingAuthorization();
        auth.setService(service.getId());
        auth.setStatusCode(b.getId());
        auth.setStatusToSet(c.getId());
        auth.insert();

        JobRecord job = model.getJobModel()
                             .newInitializedJob(service);
        job.insert();
        model.getJobModel()
             .changeStatus(job, a, null);
        model.getJobModel()
             .changeStatus(job, b, null);

        assertEquals(c, job.getStatus());

    }

    @Test
    public void testSelfSequencingAuthorization() {
        Product service = model.records()
                               .newProduct("Kick ass", null);
        service.insert();

        StatusCode kickingAss = model.records()
                                     .newStatusCode("Kicking Ass", null);
        kickingAss.insert();

        StatusCode takingNames = model.records()
                                      .newStatusCode("Taking Names", null);
        takingNames.insert();

        StatusCodeSequencingRecord sequence = model.records()
                                                   .newStatusCodeSequencing(service,
                                                                            kickingAss,
                                                                            takingNames);
        sequence.insert();

        SelfSequencingAuthorizationRecord auth = model.records()
                                                      .newSelfSequencingAuthorization(service,
                                                                                      kickingAss,
                                                                                      takingNames);
        auth.insert();

        JobRecord job = model.getJobModel()
                             .newInitializedJob(service);
        job.update();

        model.getJobModel()
             .changeStatus(job, kickingAss, "taking names");

        assertEquals(takingNames, job.getStatus());
    }

    @Test
    public void testTerminateChildrenParent() throws IOException {
        Product pushit = model.records()
                              .newProduct("Pushit Service", null);
        pushit.insert();

        Product shoveit = model.records()
                               .newProduct("shoveit Service", null);
        shoveit.insert();

        Product pullit = model.records()
                              .newProduct("Pullit Service", null);
        pullit.insert();

        StatusCode pushingMe = model.records()
                                    .newStatusCode("Pushing Me", null);
        pushingMe.setPropagateChildren(true);
        pushingMe.insert();

        StatusCode shovingMe = model.records()
                                    .newStatusCode("Shoving Me", null);
        shovingMe.insert();

        ProtocolRecord p = model.getJobModel()
                                .newInitializedProtocol(pushit);
        p.setChildService(shoveit.getId());
        p.insert();

        ProtocolRecord p2 = model.getJobModel()
                                 .newInitializedProtocol(shoveit);
        p2.setChildService(pullit.getId());
        p2.insert();

        model.getJobModel()
             .createStatusCodeChain(pushit,
                                    new StatusCode[] { pushingMe, shovingMe });
        model.getJobModel()
             .createStatusCodeChain(shoveit,
                                    new StatusCode[] { pushingMe, shovingMe });
        model.getJobModel()
             .createStatusCodeChain(pullit,
                                    new StatusCode[] { pushingMe, shovingMe });

        ChildSequencingAuthorizationRecord auth = model.records()
                                                       .newChildSequencingAuthorization(shoveit,
                                                                                        shovingMe,
                                                                                        pullit,
                                                                                        shovingMe);
        auth.insert();
        ParentSequencingAuthorizationRecord auth2 = model.records()
                                                         .newParentSequencingAuthorization(shoveit,
                                                                                           shovingMe,
                                                                                           pushit,
                                                                                           shovingMe);
        auth2.insert();

        JobRecord push = model.getJobModel()
                              .newInitializedJob(pushit);

        List<JobRecord> children = model.getJobModel()
                                        .getAllChildren(push);
        assertEquals(2, children.size());

        for (JobRecord j : children) {
            model.getJobModel()
                 .changeStatus(j, pushingMe, null);
        }
        model.getJobModel()
             .changeStatus(push, pushingMe, null);
        List<JobRecord> active = model.getJobModel()
                                      .getActiveSubJobsForService(push,
                                                                  shoveit);
        assertEquals(1, active.size());
        JobRecord shovingJob = active.get(0);

        model.getJobModel()
             .changeStatus(shovingJob, shovingMe, null);
        assertEquals(shovingMe, push.getStatus());
    }

    /**
     * Returns a list of fields that do not match between job and chronology
     *
     * @param job
     * @param jobChronology
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private List<String> verifyChronologyFields(JobRecord job,
                                                JobChronologyRecord jobChronology) throws Exception {
        String[] fieldsToMatch = new String[] { "status", "requester",
                                                "assignTo", "deliverFrom",
                                                "deliverTo" };
        List<String> unmatchedFields = new LinkedList<>();
        if (!jobChronology.getJob()
                          .equals(job)) {
            unmatchedFields.add("job");
            return unmatchedFields;
        }
        for (String field : fieldsToMatch) {
            ExistentialRuleform jobRf = (ExistentialRuleform) PropertyUtils.getSimpleProperty(job,
                                                                                              field);
            ExistentialRuleform chronoRf = (ExistentialRuleform) PropertyUtils.getSimpleProperty(jobChronology,
                                                                                                 field);
            if (chronoRf == null && jobRf == null) {
                continue;
            }
            if (!chronoRf.equals(jobRf)) {
                unmatchedFields.add(field);
            }
        }

        return unmatchedFields;
    }
}
