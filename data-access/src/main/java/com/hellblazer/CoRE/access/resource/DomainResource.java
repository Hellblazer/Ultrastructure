/**
 * Copyright (C) 2012 Hal Hildebrand. All rights reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.w3c.dom.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellblazer.CoRE.access.formatting.PropertiesFormatter;
import com.hellblazer.CoRE.access.formatting.XMLFormatter;
import com.hellblazer.CoRE.security.AuthenticatedPrincipal;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;

/**
 * @author hhildebrand
 * 
 */
@Path("/v{version : \\d+}/services/data/meta/")
public class DomainResource {
    private static final char                   DOT = '.';
    private static Localizer                    loc = Localizer.forPackage(CrudResource.class);
    protected final OpenJPAEntityManagerFactory emf;
    protected final String                      unitName;

    /**
     * @param unitName
     * @param emf
     */
    public DomainResource(String unitName, OpenJPAEntityManagerFactory emf) {
        super();
        this.unitName = unitName;
        this.emf = emf;
    }

    @GET
    @Path("/domain")
    @Produces(MediaType.APPLICATION_XML)
    @Timed
    public byte[] getDomain(@PathParam("version") int version,
                            @Context UriInfo uriInfo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLFormatter formatter = new XMLFormatter();
        formatter.writeOut(getPersistenceContext().getMetamodel(),
                           loc.get("domain-title").toString(),
                           loc.get("domain-desc").toString(),
                           uriInfo.getRequestUri().getPath(), baos);
        return baos.toByteArray();
    }

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public byte[] getModel(@PathParam("version") int version,
                           @Context UriInfo uriInfo,
                           @Auth AuthenticatedPrincipal user)
                                                             throws JsonGenerationException,
                                                             JsonMappingException,
                                                             IOException {

        Metamodel model = getPersistenceContext().getMetamodel();
        DomainObject objs[] = new DomainObject[model.getEntities().size()];

        Set<EntityType<?>> entities = model.getEntities();
        int i = 0;
        for (EntityType<?> e : entities) {

            String[] fields = new String[e.getAttributes().size()];
            int j = 0;
            for (Attribute<?, ?> a : e.getAttributes()) {
                fields[j] = a.getName();
                j++;
            }

            DomainObject obj = new DomainObject(e.getName(), fields);
            objs[i] = obj;
            i++;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(objs);

    }

    public OpenJPAEntityManager getPersistenceContext() {
        return emf.createEntityManager();
    }

    @GET
    @Path("/properties")
    @Produces({ MediaType.APPLICATION_XML })
    @Timed
    public byte[] getProperties(@PathParam("version") int version)
                                                                  throws IOException {
        Map<String, Object> properties = getPersistenceContext().getProperties();
        removeBadEntries(properties);
        PropertiesFormatter formatter = new PropertiesFormatter();
        String caption = loc.get("properties-caption", unitName).toString();
        Document xml = formatter.createXML(caption, "", "", properties);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        formatter.write(xml, baos);
        return baos.toByteArray();
    }

    private void removeBadEntries(Map<String, Object> map) {
        Iterator<String> keys = map.keySet().iterator();
        for (; keys.hasNext();) {
            if (keys.next().indexOf(DOT) == -1) {
                keys.remove();
            }
        }
    }

    /**
     * Private class for serializing a ruleform into a JSON object. It's so
     * awesome that this works.
     * 
     * @author hparry
     * 
     */
    private class DomainObject {

        @JsonProperty("objectName")
        private String   name;

        @JsonProperty("objectFields")
        private String[] fields;

        DomainObject(String name, String[] fields) {
            this.name = name;
            this.fields = fields;
        }
    }

}
