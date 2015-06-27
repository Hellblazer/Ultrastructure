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

package com.chiralbehaviors.CoRE.phantasm.jsonld;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.chiralbehaviors.CoRE.ExistentialRuleform;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.attribute.AttributeAuthorization;
import com.chiralbehaviors.CoRE.attribute.AttributeValue;
import com.chiralbehaviors.CoRE.meta.Aspect;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.NetworkedModel;
import com.chiralbehaviors.CoRE.network.NetworkAuthorization;
import com.chiralbehaviors.CoRE.network.NetworkRuleform;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author hhildebrand
 *
 */
public class FacetContextBuilder {
    public FacetContextBuilder(Model readOnlyModel) {
        this.readOnlyModel = readOnlyModel;
    }

    public static final String CLASSIFICATION = "classification";
    public static final String CLASSIFIER     = "classifier";
    public static final String CONTEXT        = "@context";
    public static final String ID             = "@id";
    public static final String TYPE           = "@type";

    private final Model readOnlyModel;

    public <RuleForm extends ExistentialRuleform<RuleForm, Network>, Network extends NetworkRuleform<RuleForm>> JsonNode buildContainer(Aspect<RuleForm> aspect,
                                                                                                                                        UriInfo uriInfo) {
        ObjectNode container = new ObjectNode(JsonNodeFactory.withExactBigDecimals(true));
        container.set(CONTEXT, buildContext(aspect, uriInfo));
        return container;
    }

    public <RuleForm extends ExistentialRuleform<RuleForm, Network>, Network extends NetworkRuleform<RuleForm>> JsonNode buildContext(Aspect<RuleForm> aspect,
                                                                                                                                      UriInfo uriInfo) {
        return buildContext(aspect,
                            readOnlyModel.getNetworkedModel(aspect.getClassification()),
                            aspect.getClassification().getClass().getSimpleName().toLowerCase(),
                            uriInfo);
    }

    private <RuleForm extends ExistentialRuleform<RuleForm, ?>> void addAttributeTerms(ObjectNode context,
                                                                                       Aspect<RuleForm> aspect,
                                                                                       NetworkedModel<RuleForm, ?, ?, ?> networkedModel) {
        for (AttributeAuthorization<RuleForm, ?> auth : networkedModel.getAttributeAuthorizations(aspect)) {
            String iri = iriFrom(auth.getAuthorizedAttribute());
            String type = typeFrom(auth.getAuthorizedAttribute());
            String term = auth.getAuthorizedAttribute().getName();
            if (type == null) {
                context.put(term, iri);
            } else {
                ObjectNode termDefinition = new ObjectNode(JsonNodeFactory.withExactBigDecimals(true));
                termDefinition.put(ID, iri);
                termDefinition.put(TYPE, type);
                context.set(term, termDefinition);
            }
        }
    }

    private <RuleForm extends ExistentialRuleform<RuleForm, ?>> void addNetworkAuthTerms(ObjectNode context,
                                                                                         Aspect<RuleForm> aspect,
                                                                                         NetworkedModel<RuleForm, ?, ?, ?> networkedModel,
                                                                                         String eeType,
                                                                                         UriInfo uriInfo) {
        for (NetworkAuthorization<RuleForm> auth : networkedModel.getNetworkAuthorizations(aspect)) {
            Aspect<RuleForm> childAspect = new Aspect<RuleForm>(auth.getAuthorizedRelationship(),
                                                                auth.getAuthorizedParent());
            if (auth.getName() != null) {
                ObjectNode termDefinition = new ObjectNode(JsonNodeFactory.withExactBigDecimals(true));
                termDefinition.put(ID,
                                   getTypeIri(eeType, childAspect, uriInfo));
                termDefinition.put(TYPE, ID);
                termDefinition.put(CLASSIFIER,
                                   childAspect.getClassifier().getName());
                termDefinition.put(CLASSIFICATION,
                                   childAspect.getClassification().getName());
                context.set(auth.getName(), termDefinition);
            }
        }
    }

    private <RuleForm extends ExistentialRuleform<RuleForm, ?>> void addXdomainAuthTerms(ObjectNode context,
                                                                                         Aspect<RuleForm> aspect,
                                                                                         NetworkedModel<RuleForm, ?, ?, ?> networkedModel,
                                                                                         String eeType) {

    }

    private <RuleForm extends ExistentialRuleform<RuleForm, ?>> JsonNode buildContext(Aspect<RuleForm> aspect,
                                                                                      NetworkedModel<RuleForm, ?, ?, ?> networkedModel,
                                                                                      String eeType,
                                                                                      UriInfo uriInfo) {
        ObjectNode context = new ObjectNode(JsonNodeFactory.withExactBigDecimals(true));
        addAttributeTerms(context, aspect, networkedModel);
        addNetworkAuthTerms(context, aspect, networkedModel, eeType, uriInfo);
        addXdomainAuthTerms(context, aspect, networkedModel, eeType);
        return context;
    }

    private String getTypeIri(String eeType, Aspect<?> aspect,
                              UriInfo uriInfo) {
        UriBuilder ub = uriInfo.getBaseUriBuilder();
        String classifier = aspect.getClassifier().getId().toString();
        String classification = aspect.getClassification().getId().toString();

        URI userUri = ub.path(FacetContextResource.class).path(classifier).path(classification).build();
        return userUri.toASCIIString();
    }

    private String iriFrom(Attribute authorizedAttribute) {
        AttributeValue<Attribute> iri = readOnlyModel.getAttributeModel().getAttributeValue(authorizedAttribute,
                                                                                            readOnlyModel.getKernel().getIRI());
        if (iri != null) {
            return iri.getTextValue();
        }
        switch (authorizedAttribute.getValueType()) {
            case TEXT:
                return "http://schema.org/text";
            case BINARY:
                return "http://schema.org/binary";
            case BOOLEAN:
                return "http://schema.org/boolean";
            case INTEGER:
                return "http://schema.org/integer";
            case NUMERIC:
                return "http://schema.org/numeric";
            case TIMESTAMP:
                return "http://schema.org/timestamp";
        }
        return null;
    }

    /**
     * @param authorizedAttribute
     * @return
     */
    private String typeFrom(Attribute authorizedAttribute) {
        AttributeValue<Attribute> irl = readOnlyModel.getAttributeModel().getAttributeValue(authorizedAttribute,
                                                                                            readOnlyModel.getKernel().getIRI());
        return irl != null ? irl.getTextValue() : null;
    }

}
