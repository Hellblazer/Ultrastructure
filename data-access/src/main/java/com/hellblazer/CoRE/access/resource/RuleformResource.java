/**
 * Copyright (C) 2014 Halloran Parry. All rights reserved.
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
package com.hellblazer.CoRE.access.resource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.hellblazer.CoRE.Ruleform;

/**
 * @author hparry
 *
 */
@Path("/v{version : \\d+}/services/data/ruleform")
public class RuleformResource {
    
    private EntityManager em;
    
    public RuleformResource(EntityManagerFactory emf) {
        this.em = emf.createEntityManager();
    }
    
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, "text/json"})
    public List<String> getRuleformTypes() {
        Metamodel m = em.getMetamodel();
        Set<EntityType<?>> entities = m.getEntities();
        List<String> types = new LinkedList<String>();
        for (EntityType<?> e : entities) {
            types.add(e.getName());
        }
        return types;
    }
    
    @GET
    @Path("/{ruleform}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, "text/json" })
    public Ruleform getResource(@PathParam("ruleform") String type,
                        @PathParam("id") long id) throws ClassNotFoundException {
        String qualifiedName = "com.hellblazer.CoRE." + type.toLowerCase() + "." + type;
        @SuppressWarnings("unchecked")
        Class<? extends Ruleform> c = (Class<? extends Ruleform>) Class.forName(qualifiedName);
        Ruleform rf = em.find(c, id);
        return rf;
    }
    
    @GET
    @Path("/{ruleform}")
    @Produces({ MediaType.APPLICATION_JSON, "text/json" })
    public List<Ruleform> getResource(@PathParam("ruleform") String type) throws ClassNotFoundException {
        String qualifiedName = "com.hellblazer.CoRE." + type.toLowerCase() + "." + type;
        @SuppressWarnings("unchecked")
        Class<? extends Ruleform> c = (Class<? extends Ruleform>) Class.forName(qualifiedName);
        Query qr = em.createNamedQuery(type.toLowerCase() + Ruleform.FIND_ALL_SUFFIX, c);
        @SuppressWarnings("unchecked")
        List<Ruleform> results = qr.getResultList();
        return results;
    }
    
    
    @POST
    @Path("/{ruleform}")
    @Produces({MediaType.APPLICATION_JSON, "text/json"})
    public long insertRuleform(@PathParam("ruleform") String type,
                               Ruleform rf) {
        if (!rf.getClass().getSimpleName().equals(type)) {
            throw new IllegalArgumentException(String.format("expected type based on URL: %s, type of uploaded object: %s", type, rf.getClass().getSimpleName()));
        }
        
        em.getTransaction().begin();
        Map<Ruleform, Ruleform> map = new HashMap<Ruleform, Ruleform>();
        rf.manageEntity(em, map);
        em.getTransaction().commit();
        em.refresh(rf);
        return rf.getId();
    }

}
