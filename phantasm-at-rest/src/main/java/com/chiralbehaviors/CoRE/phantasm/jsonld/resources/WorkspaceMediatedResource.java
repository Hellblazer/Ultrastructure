/**
 * Copyright (c) 2015 Chiral Behaviors, LLC, all rights reserved.
 * 
 
 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.phantasm.jsonld.resources;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.chiralbehaviors.CoRE.ExistentialRuleform;
import com.chiralbehaviors.CoRE.Ruleform;
import com.chiralbehaviors.CoRE.meta.Aspect;
import com.chiralbehaviors.CoRE.meta.workspace.WorkspaceScope;
import com.chiralbehaviors.CoRE.phantasm.jsonld.Facet;
import com.chiralbehaviors.CoRE.relationship.Relationship;

/**
 * @author hhildebrand
 *
 */
@Path("json-ld/workspace-mediated")
@Produces({ "application/json", "text/json" })
public class WorkspaceMediatedResource extends TransactionalResource {

    @Context
    private UriInfo uriInfo;

    public WorkspaceMediatedResource(EntityManagerFactory emf) {
        super(emf);
    }

    @Path("{workspace}/facet/{ruleform-type}/{classifier}/{classification}")
    @GET
    public Response getAllInstances(@PathParam("workspace") String workspace,
                                    @PathParam("ruleform-type") String ruleformType,
                                    @PathParam("classifier") String classifier,
                                    @PathParam("classification") String classification) {
        UUID workspaceUUID;
        workspaceUUID = WorkspaceResource.toUUID(workspace);
        WorkspaceScope scope = readOnlyModel.getWorkspaceModel().getScoped(workspaceUUID);
        if (scope == null) {
            throw new WebApplicationException(String.format("Workspace not found: %s",
                                                            workspaceUUID),
                                              Status.NOT_FOUND);
        }
        Ruleform relationship = WorkspaceResource.resolve(classifier, scope);
        Ruleform ruleform = WorkspaceResource.resolve(classification, scope);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Aspect<?> aspect = new Aspect((Relationship) relationship,
                                      (ExistentialRuleform) ruleform);
        return Response.seeOther(Facet.getAllInstancesIri(aspect)).build();
    }

    @Path("{workspace}/facet/context/{ruleform-type}/{classifier}/{classification}")
    @GET
    public Response getContext(@PathParam("workspace") String workspace,
                               @PathParam("ruleform-type") String ruleformType,
                               @PathParam("classifier") String classifier,
                               @PathParam("classification") String classification) {
        UUID workspaceUUID;
        workspaceUUID = WorkspaceResource.toUUID(workspace);
        WorkspaceScope scope = readOnlyModel.getWorkspaceModel().getScoped(workspaceUUID);
        if (scope == null) {
            throw new WebApplicationException(String.format("Workspace not found: %s",
                                                            workspaceUUID),
                                              Status.NOT_FOUND);
        }
        Ruleform relationship = WorkspaceResource.resolve(classifier, scope);
        Ruleform ruleform = WorkspaceResource.resolve(classification, scope);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Aspect<?> aspect = new Aspect((Relationship) relationship,
                                      (ExistentialRuleform) ruleform);
        return Response.seeOther(Facet.getContextIri(aspect)).build();
    }

    @Path("{workspace}/facet/{ruleform-type}/{classifier}/{classification}/{instance}")
    @GET
    public Response getInstance(@PathParam("workspace") String workspace,
                                @PathParam("ruleform-type") String ruleformType,
                                @PathParam("classifier") String classifier,
                                @PathParam("classification") String classification,
                                @PathParam("instance") UUID existential) {
        UUID workspaceUUID;
        workspaceUUID = WorkspaceResource.toUUID(workspace);
        WorkspaceScope scope = readOnlyModel.getWorkspaceModel().getScoped(workspaceUUID);
        if (scope == null) {
            throw new WebApplicationException(String.format("Workspace not found: %s",
                                                            workspaceUUID),
                                              Status.NOT_FOUND);
        }
        Ruleform relationship = WorkspaceResource.resolve(classifier, scope);
        Ruleform ruleform = WorkspaceResource.resolve(classification, scope);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Aspect<?> aspect = new Aspect((Relationship) relationship,
                                      (ExistentialRuleform) ruleform);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ExistentialRuleform instance = readOnlyModel.getNetworkedModel((ExistentialRuleform) ruleform).find(existential);
        return Response.seeOther(Facet.getInstanceIri(aspect,
                                                      (ExistentialRuleform<?, ?>) instance)).build();
    }

    @Path("{workspace}/facet/term/{ruleform-type}/{classifier}/{classification}/{term}")
    @GET
    public Response getTerm(@PathParam("workspace") String workspace,
                            @PathParam("ruleform-type") String ruleformType,
                            @PathParam("classifier") String classifier,
                            @PathParam("classification") String classification,
                            @PathParam("term") String term) {
        UUID workspaceUUID;
        workspaceUUID = WorkspaceResource.toUUID(workspace);
        WorkspaceScope scope = readOnlyModel.getWorkspaceModel().getScoped(workspaceUUID);
        if (scope == null) {
            throw new WebApplicationException(String.format("Workspace not found: %s",
                                                            workspaceUUID),
                                              Status.NOT_FOUND);
        }
        Ruleform relationship = WorkspaceResource.resolve(classifier, scope);
        Ruleform ruleform = WorkspaceResource.resolve(classification, scope);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Aspect<?> aspect = new Aspect((Relationship) relationship,
                                      (ExistentialRuleform) ruleform);
        return Response.seeOther(Facet.getTermIri(aspect, term)).build();
    }

    @Path("{workspace}/facet/{ruleform-type}/{classifier}/{classification}/{instance}/{traversal:.+}")
    @GET
    public Response select(@PathParam("workspace") String workspace,
                           @PathParam("ruleform-type") String ruleformType,
                           @PathParam("classifier") String classifier,
                           @PathParam("classification") String classification,
                           @PathParam("instance") String existential,
                           @PathParam("traversal") List<PathSegment> traversal) {
        UUID workspaceUUID;
        workspaceUUID = WorkspaceResource.toUUID(workspace);
        WorkspaceScope scope = readOnlyModel.getWorkspaceModel().getScoped(workspaceUUID);
        if (scope == null) {
            throw new WebApplicationException(String.format("Workspace not found: %s",
                                                            workspaceUUID),
                                              Status.NOT_FOUND);
        }
        Ruleform relationship = WorkspaceResource.resolve(classifier, scope);
        Ruleform ruleform = WorkspaceResource.resolve(classification, scope);
        Ruleform instance = WorkspaceResource.resolve(existential, scope);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Aspect<?> aspect = new Aspect((Relationship) relationship,
                                      (ExistentialRuleform) ruleform);
        return Response.seeOther(Facet.getSelectIri(aspect,
                                                    (ExistentialRuleform<?, ?>) instance,
                                                    traversal)).build();
    }
}