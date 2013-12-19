/**
 * Copyright (C) 2013 Hal Hildebrand. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.CoRE.meta.models;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import com.hellblazer.CoRE.agency.Agency;
import com.hellblazer.CoRE.network.NetworkInference;
import com.hellblazer.CoRE.network.Relationship;
import com.hellblazer.CoRE.time.Interval;
import com.hellblazer.CoRE.time.IntervalNetwork;

/**
 * @author hhildebrand
 * 
 */
public class IntervalModelTest extends AbstractModelTest {

    @Test
    public void testSimpleNetworkPropagation() {
        Agency core = model.getKernel().getCore();
        Relationship equals = model.getKernel().getEquals();

        em.getTransaction().begin();

        Relationship equals2 = new Relationship("equals 2",
                                                "an alias for equals", core);
        equals2.setInverse(equals2);
        em.persist(equals2);
        NetworkInference aEqualsA = new NetworkInference(equals, equals2,
                                                         equals, core);
        em.persist(aEqualsA);
        Interval a = new Interval(BigDecimal.valueOf(0),
                                  BigDecimal.valueOf(100), "A", core);
        em.persist(a);
        Interval b = new Interval(BigDecimal.valueOf(0),
                                  BigDecimal.valueOf(100), "B", core);
        em.persist(b);
        Interval c = new Interval(BigDecimal.valueOf(0),
                                  BigDecimal.valueOf(100), "C", core);
        em.persist(c);
        IntervalNetwork edgeA = new IntervalNetwork(a, equals, b, core);
        em.persist(edgeA);
        IntervalNetwork edgeB = new IntervalNetwork(b, equals2, c, core);
        em.persist(edgeB);

        em.getTransaction().commit();
        em.clear();

        List<IntervalNetwork> edges = em.createQuery("SELECT edge FROM IntervalNetwork edge WHERE edge.inferred = TRUE",
                                                     IntervalNetwork.class).getResultList();
        assertEquals(2, edges.size());
    }
}
