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

package com.chiralbehaviors.CoRE.attribute;

import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.chiralbehaviors.CoRE.Ruleform;
import com.chiralbehaviors.CoRE.agency.Agency;
import com.chiralbehaviors.CoRE.network.Relationship;

/**
 * The authorization for attributes on attributes
 * 
 * @author hhildebrand
 * 
 */
@Entity
@Table(name = "attr_meta_attr_auth", schema = "ruleform")
public class AttributeMetaAttributeAuthorization extends
        ClassifiedAttributeAuthorization<Attribute> {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "classifier")
    private Attribute         classifier;

    /**
     * 
     */
    public AttributeMetaAttributeAuthorization() {
        super();
    }

    /**
     * @param updatedBy
     */
    public AttributeMetaAttributeAuthorization(Agency updatedBy) {
        super(updatedBy);
    }

    /**
     * @param classifier
     * @param classification
     * @param attribute
     * @param coreModel
     */
    public AttributeMetaAttributeAuthorization(Attribute classifier,
                                               Relationship classification,
                                               Attribute authorizedAttribute,
                                               Agency updatedBy) {
        super(classification, authorizedAttribute, updatedBy);
        this.classifier = classifier;
    }

    /**
     * @param id
     */
    public AttributeMetaAttributeAuthorization(UUID id) {
        super(id);
    }

    /**
     * @param classification
     * @param updatedBy
     */
    public AttributeMetaAttributeAuthorization(Relationship classification,
                                               Agency updatedBy) {
        super(classification, updatedBy);
    }

    /**
     * @param classification
     * @param authorized
     * @param updatedBy
     */
    public AttributeMetaAttributeAuthorization(Relationship classification,
                                               Attribute authorized,
                                               Agency updatedBy) {
        super(classification, authorized, updatedBy);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chiralbehaviors.CoRE.attribute.ClassifiedAttributeAuthorization#
     * getClassifier()
     */
    @Override
    public Attribute getClassifier() {
        return classifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chiralbehaviors.CoRE.attribute.ClassifiedAttributeAuthorization#
     * setClassifier(com.chiralbehaviors.CoRE.network.Networked)
     */
    @Override
    public void setClassifier(Attribute classifier) {
        this.classifier = classifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.chiralbehaviors.CoRE.Ruleform#traverseForeignKeys(javax.persistence
     * .EntityManager, java.util.Map)
     */
    @Override
    public void traverseForeignKeys(EntityManager em,
                                    Map<Ruleform, Ruleform> knownObjects) {
        if (classifier != null) {
            classifier = (Attribute) classifier.manageEntity(em, knownObjects);
        }
        super.traverseForeignKeys(em, knownObjects);
    }
}
