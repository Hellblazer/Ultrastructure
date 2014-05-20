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

package com.chiralbehaviors.CoRE.meta.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import com.chiralbehaviors.CoRE.attribute.ValueType;
import com.chiralbehaviors.CoRE.kernel.Kernel;
import com.chiralbehaviors.CoRE.kernel.WellKnownObject;
import com.chiralbehaviors.CoRE.meta.BootstrapLoader;

/**
 * @author hhildebrand
 * 
 */
public class KernelTest {

    @Test
    public void testKernel() throws Exception {
        InputStream is = getClass().getResourceAsStream("/jpa.properties");
        Properties properties = new Properties();
        properties.load(is);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(WellKnownObject.CORE,
                                                                          properties);
        EntityManager em = emf.createEntityManager();
        BootstrapLoader loader = new BootstrapLoader(em);
        em.getTransaction().begin();
        loader.clear();
        em.getTransaction().commit();
        em.getTransaction().begin();
        loader.bootstrap();
        em.getTransaction().commit();

        Kernel kernel = new ModelImpl(em).getKernel();
        assertNotNull(kernel.getAnyAttribute());
        assertNotNull(kernel.getAnyProduct());
        assertNotNull(kernel.getAnyLocation());
        assertNotNull(kernel.getAnyRelationship());
        assertNotNull(kernel.getAnyAgency());
        assertNotNull(kernel.getAttribute());
        assertNotNull(kernel.getContains());
        assertNotNull(kernel.getCore());
        assertNotNull(kernel.getCoreAnimationSoftware());
        assertNotNull(kernel.getDeveloped());
        assertNotNull(kernel.getDevelopedBy());
        assertNotNull(kernel.getProduct());
        assertNotNull(kernel.getEquals());
        assertNotNull(kernel.getFormerMemberOf());
        assertNotNull(kernel.getGreaterThan());
        assertNotNull(kernel.getGreaterThanOrEqual());
        assertNotNull(kernel.getHadMember());
        assertNotNull(kernel.getHasException());
        assertNotNull(kernel.getHasHead());
        assertNotNull(kernel.getHasMember());
        assertNotNull(kernel.getHasVersion());
        assertNotNull(kernel.getHeadOf());
        assertNotNull(kernel.getIncludes());
        assertNotNull(kernel.getIsA());
        assertNotNull(kernel.getIsContainedIn());
        assertNotNull(kernel.getIsExceptionTo());
        assertNotNull(kernel.getIsLocationOf());
        assertNotNull(kernel.getLessThan());
        assertNotNull(kernel.getLessThanOrEqual());
        assertNotNull(kernel.getLocation());
        assertNotNull(kernel.getMapsToLocation());
        assertNotNull(kernel.getMemberOf());
        assertNotNull(kernel.getOriginalAttribute());
        assertNotNull(kernel.getOriginalProduct());
        assertNotNull(kernel.getOriginalLocation());
        assertNotNull(kernel.getOriginalAgency());
        assertNotNull(kernel.getPropagationSoftware());
        assertNotNull(kernel.getPrototype());
        assertNotNull(kernel.getPrototypeOf());
        assertNotNull(kernel.getAgency());
        assertNotNull(kernel.getSameRelationship());
        assertNotNull(kernel.getSpecialSystemAgency());
        assertNotNull(kernel.getVersionOf());
        assertNotNull(kernel.getCoreModel());
        assertNotNull(kernel.getCoreUser());
        assertNotNull(kernel.getPasswordHashAttribute());
        assertNotNull(kernel.getLoginAttribute());
        assertNotNull(kernel.getUnset());
        assertNotNull(kernel.getInverseSoftware());

        assertEquals(ValueType.TEXT,
                     kernel.getPasswordHashAttribute().getValueType());
        assertEquals(ValueType.TEXT, kernel.getLoginAttribute().getValueType());
    }
}
