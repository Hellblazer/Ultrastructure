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

package com.chiralbehaviors.CoRE.phantasm.model;

import static com.chiralbehaviors.CoRE.jooq.Tables.EXISTENTIAL;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chiralbehaviors.CoRE.RecordsFactory;
import com.chiralbehaviors.CoRE.domain.Attribute;
import com.chiralbehaviors.CoRE.domain.ExistentialRuleform;
import com.chiralbehaviors.CoRE.domain.Relationship;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialAttributeRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialNetworkAuthorizationRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialNetworkRecord;
import com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialRecord;
import com.chiralbehaviors.CoRE.meta.Aspect;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.phantasm.java.PhantasmDefinition;
import com.chiralbehaviors.CoRE.phantasm.model.PhantasmTraversal.AttributeAuthorization;
import com.chiralbehaviors.CoRE.phantasm.model.PhantasmTraversal.NetworkAuthorization;
import com.google.common.base.Function;

/**
 * CRUD for Phantasms. This class is the animation procedure that maintains and
 * mediates the Phantasm/Facet constructs in Ultrastructure. It's a bit
 * unwieldy, because of the type signatures required for erasure. Provides a
 * centralized implementation of Phantasm CRUD and the security model for such.
 * 
 * @author hhildebrand
 *
 */
public class PhantasmCRUD {

    private final Relationship apply;
    private final Relationship create;
    private final Relationship delete;
    private final Relationship invoke;
    private final Relationship read;
    private final Relationship remove;
    private final Relationship update;
    protected final Model      model;

    public PhantasmCRUD(Model model) {
        this.model = model;
        create = model.getKernel()
                      .getCREATE();
        delete = model.getKernel()
                      .getDELETE();
        invoke = model.getKernel()
                      .getINVOKE();
        read = model.getKernel()
                    .getREAD();
        remove = model.getKernel()
                      .getREMOVE();
        update = model.getKernel()
                      .getUPDATE();
        apply = model.getKernel()
                     .getAPPLY();
    }

    /**
     * Add the child to the list of children of the instance
     * 
     * @param facet
     * @param instance
     * @param auth
     * @param child
     */
    public ExistentialRuleform addChild(NetworkAuthorization facet,
                                        ExistentialRuleform instance,
                                        NetworkAuthorization auth,
                                        ExistentialRuleform child) {
        if (instance == null) {
            return null;
        }
        cast(child, auth);
        if (!checkUPDATE(facet) || !checkUPDATE(auth)) {
            return instance;
        }

        model.getPhantasmModel()
             .link(instance, auth.getChildRelationship(), child,
                   model.getCurrentPrincipal()
                        .getPrincipal());
        return instance;
    }

    /**
     * Add the list of children to the instance
     * 
     * @param facet
     * @param instance
     * @param auth
     * @param children
     */
    public ExistentialRuleform addChildren(NetworkAuthorization facet,
                                           ExistentialRuleform instance,
                                           NetworkAuthorization auth,
                                           List<ExistentialRuleform> children) {
        if (instance == null) {
            return null;
        }
        if (!checkUPDATE(facet) || !checkUPDATE(auth)) {
            return instance;
        }
        children.stream()
                .filter(child -> checkREAD(child))
                .peek(child -> cast(child, auth))
                .forEach(child -> model.getPhantasmModel()
                                       .link(instance,
                                             auth.getChildRelationship(), child,
                                             model.getCurrentPrincipal()
                                                  .getPrincipal()));
        return instance;
    }

    /**
     * Apply the facet to the instance
     * 
     * @param facet
     * @param instance
     * @return
     * @throws SecurityException
     */
    @SuppressWarnings("unchecked")
    public ExistentialRuleform apply(NetworkAuthorization facet,
                                     ExistentialRuleform instance,
                                     Function<ExistentialRuleform, ExistentialRuleform> constructor) throws SecurityException {
        if (instance == null) {
            return null;
        }
        if (!model.getPhantasmModel()
                  .checkFacetCapability(facet.getAuth(), getAPPLY())) {
            return instance;
        }
        model.getPhantasmModel()
             .initialize(instance, facet.getAuth(), model.getCurrentPrincipal()
                                                         .getPrincipal());
        if (!checkInvoke(facet, instance)) {
            return null;
        }
        return constructor.apply(instance);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    /**
     * Throws ClassCastException if not an instance of the authorized facet type
     * 
     * @param ruleform
     * @param facet
     */
    public void cast(ExistentialRuleform ruleform,
                     NetworkAuthorization authorization) {
        if (!model.getPhantasmModel()
                  .isAccessible(ruleform,
                                authorization.getAuthorizedRelationship(),
                                authorization.getAuthorizedParent())) {
            throw new ClassCastException(String.format("%s not of facet type %s",
                                                       ruleform,
                                                       PhantasmDefinition.factString(model,
                                                                                     new Aspect<>(authorization.getClassifier(),
                                                                                                  authorization.getClassification()))));
        }
    }

    public boolean checkInvoke(NetworkAuthorization facet,
                               ExistentialRuleform instance) {
        Relationship invoke = getINVOKE();
        return model.getPhantasmModel()
                    .checkCapability(instance, invoke)
               && model.getPhantasmModel()
                       .checkFacetCapability(facet.getAuth(), invoke);
    }

    /**
     * Create a new instance of the facet
     * 
     * @param facet
     * @param name
     * @param description
     * @return
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    @SuppressWarnings("unchecked")
    public ExistentialRuleform createInstance(NetworkAuthorization facet,
                                              String name, String description,
                                              Function<ExistentialRuleform, ExistentialRuleform> constructor) {
        if (!model.getPhantasmModel()
                  .checkFacetCapability(facet.getAuth(), getCREATE())) {
            return null;
        }
        ExistentialRuleform instance;
        instance = (ExistentialRuleform) RecordsFactory.createExistential(model.getDSLContext(),
                                                                          facet.getAuth()
                                                                               .getClassification(),
                                                                          name,
                                                                          description,
                                                                          model.getCurrentPrincipal()
                                                                               .getPrincipal());
        model.getPhantasmModel()
             .initialize(instance, facet.getAuth(), model.getCurrentPrincipal()
                                                         .getPrincipal());
        if (!checkInvoke(facet, instance)) {
            return null;
        }
        return constructor.apply(instance);
    }

    public Relationship getAPPLY() {
        return apply;
    }

    /**
     * Answer the attribute value of the instance
     * 
     * @param facet
     * @param instance
     * @param stateAuth
     * 
     * @return
     */
    public Object getAttributeValue(NetworkAuthorization facet,
                                    ExistentialRuleform instance,
                                    AttributeAuthorization stateAuth) {
        if (instance == null) {
            return null;
        }
        if (!checkREAD(facet) || !checkREAD(stateAuth)) {
            return null;
        }
        Attribute authorizedAttribute = stateAuth.getAttribute();
        if (authorizedAttribute.getIndexed()) {
            return getIndexedAttributeValue(instance, authorizedAttribute);
        } else if (authorizedAttribute.getKeyed()) {
            return getMappedAttributeValue(instance, authorizedAttribute);
        }
        Object value = model.getPhantasmModel()
                            .getAttributeValue(instance, authorizedAttribute)
                            .getValue();
        return value;
    }

    /**
     * Answer the inferred and immediate network children of the instance
     * 
     * @param facet
     * @param instance
     * @param auth
     * 
     * @return
     */
    public List<ExistentialRuleform> getChildren(NetworkAuthorization facet,
                                                 ExistentialRuleform instance,
                                                 NetworkAuthorization auth) {
        if (instance == null) {
            return Collections.emptyList();
        }
        if (!checkREAD(facet) || !checkREAD(auth)) {
            return Collections.emptyList();
        }
        return model.getPhantasmModel()
                    .getChildren(instance, auth.getChildRelationship())
                    .stream()
                    .filter(child -> model.getPhantasmModel()
                                          .checkCapability(child, getREAD()))
                    .collect(Collectors.toList());

    }

    public Relationship getCREATE() {
        return create;
    }

    public Relationship getDELETE() {
        return delete;
    }

    /**
     * Answer the immediate, non inferred children of the instance
     * 
     * @param facet
     * @param instance
     * @param auth
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ExistentialRuleform> getImmediateChildren(NetworkAuthorization facet,
                                                          ExistentialRuleform instance,
                                                          NetworkAuthorization auth) {
        if (instance == null) {
            return Collections.emptyList();
        }
        if (!checkREAD(facet) || !checkREAD(auth)) {
            return Collections.emptyList();
        }
        return model.getPhantasmModel()
                    .getImmediateChildren(instance, auth.getChildRelationship())
                    .stream()
                    .map(r -> (ExistentialRuleform) r)
                    .filter(child -> model.getPhantasmModel()
                                          .checkCapability(child, getREAD()))
                    .collect(Collectors.toList());
    }

    /**
     * Answer the list of instances of this facet.
     * 
     * @param facet
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ExistentialRuleform> getInstances(NetworkAuthorization facet) {
        if (!model.getPhantasmModel()
                  .checkFacetCapability(facet.getAuth(), getREAD())) {
            return Collections.emptyList();
        }
        return model.getPhantasmModel()
                    .getChildren(facet.getClassification(),
                                 facet.getClassifier())
                    .stream()
                    .map(e -> (ExistentialRuleform) e)
                    .filter(instance -> checkREAD(instance))
                    .collect(Collectors.toList());
    }

    public Relationship getINVOKE() {
        return invoke;
    }

    public Model getModel() {
        return model;
    }

    public Relationship getREAD() {
        return read;
    }

    public Relationship getREMOVE() {
        return remove;
    }

    /**
     * Answer the singular network child of the instance
     * 
     * @param facet
     * @param instance
     * @param auth
     * 
     * @return
     */
    public ExistentialRuleform getSingularChild(NetworkAuthorization facet,
                                                ExistentialRuleform instance,
                                                NetworkAuthorization auth) {
        if (instance == null) {
            return null;
        }
        if (!checkREAD(facet) || !checkREAD(auth)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        ExistentialRuleform child = (ExistentialRuleform) model.getPhantasmModel()
                                                               .getImmediateChild(instance,
                                                                                  auth.getChildRelationship());
        return checkREAD(child) ? child : null;
    }

    public Relationship getUPDATE() {
        return update;
    }

    public List<ExistentialRuleform> lookup(List<String> ids) {
        return ids.stream()
                  .map(id -> existential(id))
                  .map(r -> RecordsFactory.resolve(r))
                  .filter(child -> model.getPhantasmModel()
                                        .checkCapability(child, getREAD()))
                  .collect(Collectors.toList());
    }

    private ExistentialRecord existential(String id) {
        return model.getDSLContext()
                    .selectFrom(EXISTENTIAL)
                    .where(EXISTENTIAL.ID.eq(UUID.fromString(id)))
                    .fetchOne();
    }

    public ExistentialRuleform lookup(String id) {
        return Optional.ofNullable(existential(id))
                       .map(r -> RecordsFactory.resolve(r))
                       .filter(rf -> rf != null)
                       .filter(child -> model.getPhantasmModel()
                                             .checkCapability(child, getREAD()))
                       .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public List<ExistentialRuleform> lookupRuleForm(List<String> ids) {
        return ids.stream()
                  .map(id -> existential(id))
                  .map(r -> RecordsFactory.resolve(r))
                  .map(r -> (ExistentialRuleform) r)
                  .collect(Collectors.toList());
    }

    /**
     * Remove the facet from the instance
     * 
     * @param facet
     * @param instance
     * @return
     * @throws SecurityException
     */
    public ExistentialRuleform remove(ExistentialNetworkAuthorizationRecord facet,
                                      ExistentialRuleform instance,
                                      boolean deleteAttributes) throws SecurityException {
        if (instance == null) {
            return null;
        }
        if (!model.getPhantasmModel()
                  .checkFacetCapability(facet, getREMOVE())) {
            return instance;
        }
        model.getPhantasmModel()
             .initialize(instance, facet, model.getCurrentPrincipal()
                                               .getPrincipal());
        return instance;
    }

    /**
     * Remove a child from the instance
     * 
     * @param facet
     * @param instance
     * @param auth
     * @param child
     */
    public ExistentialRuleform removeChild(NetworkAuthorization facet,
                                           ExistentialRuleform instance,
                                           NetworkAuthorization auth,
                                           ExistentialRuleform child) {
        if (instance == null) {
            return null;
        }
        if (!checkUPDATE(facet) || !checkUPDATE(auth)) {
            return instance;
        }
        model.getPhantasmModel()
             .unlink(instance, auth.getChildRelationship(), child);
        return instance;
    }

    /**
     * Remove the immediate child links from the instance
     * 
     * @param facet
     * @param instance
     * @param auth
     * @param children
     */
    public ExistentialRuleform removeChildren(NetworkAuthorization facet,
                                              ExistentialRuleform instance,
                                              NetworkAuthorization auth,
                                              List<ExistentialRuleform> children) {
        if (instance == null) {
            return null;
        }
        if (!checkUPDATE(facet) || !checkUPDATE(auth)) {
            return instance;
        }
        for (ExistentialRuleform child : children) {
            model.getPhantasmModel()
                 .unlink(instance, auth.getChildRelationship(), child);
        }
        return instance;
    }

    public ExistentialRuleform setAttributeValue(NetworkAuthorization facet,
                                                 ExistentialRuleform instance,
                                                 AttributeAuthorization stateAuth,
                                                 List<Object> value) {
        return setAttributeValue(facet, instance, stateAuth, value.toArray());
    }

    public ExistentialRuleform setAttributeValue(NetworkAuthorization facet,
                                                 ExistentialRuleform instance,
                                                 AttributeAuthorization stateAuth,
                                                 Map<String, Object> value) {
        if (!checkUPDATE(facet) || !checkUPDATE(stateAuth)) {
            return instance;
        }
        setAttributeMap(instance, stateAuth.getAttribute(), value);
        return instance;
    }

    public ExistentialRuleform setAttributeValue(NetworkAuthorization facet,
                                                 ExistentialRuleform instance,
                                                 AttributeAuthorization stateAuth,
                                                 Object value) {
        if (instance == null) {
            return null;
        }
        if (!checkUPDATE(facet) || !checkUPDATE(stateAuth)) {
            return instance;
        }
        model.getPhantasmModel()
             .getAttributeValue(instance, stateAuth.getAttribute())
             .setValue(value);
        return instance;
    }

    public ExistentialRuleform setAttributeValue(NetworkAuthorization facet,
                                                 ExistentialRuleform instance,
                                                 AttributeAuthorization stateAuth,
                                                 Object[] value) {
        if (!checkUPDATE(facet) || !checkUPDATE(stateAuth)) {
            return instance;
        }
        setAttributeArray(instance, stateAuth.getAttribute(), value);
        return instance;
    }

    /**
     * Set the immediate children of the instance to be the list of supplied
     * children. No inferred links will be explicitly added or deleted.
     * 
     * @param facet
     * @param instance
     * @param auth
     * @param children
     */
    public ExistentialRuleform setChildren(NetworkAuthorization facet,
                                           ExistentialRuleform instance,
                                           NetworkAuthorization auth,
                                           List<ExistentialRuleform> children) {
        if (instance == null) {
            return null;
        }
        if (!checkUPDATE(facet) || !checkUPDATE(auth)) {
            return instance;
        }

        for (ExistentialNetworkRecord childLink : model.getPhantasmModel()
                                                       .getImmediateChildrenLinks(instance,
                                                                                  auth.getChildRelationship())) {
            childLink.delete();
        }
        children.stream()
                .filter(child -> checkREAD(child))
                .peek(child -> cast(child, auth))
                .forEach(child -> model.getPhantasmModel()
                                       .link(instance,
                                             auth.getChildRelationship(), child,
                                             model.getCurrentPrincipal()
                                                  .getPrincipal()));
        return instance;
    }

    /**
     * @param description
     * @param id
     * @return
     */
    public ExistentialRuleform setDescription(ExistentialRuleform instance,
                                              String description) {
        if (instance == null) {
            return null;
        }
        if (!checkUPDATE(instance)) {
            return instance;
        }
        instance.setDescription(description);
        return instance;
    }

    public ExistentialRuleform setName(ExistentialRuleform instance,
                                       String name) {
        if (instance == null) {
            return null;
        }
        if (!checkUPDATE(instance)) {
            return instance;
        }
        instance.setName(name);
        return instance;
    }

    /**
     * Set the singular child of the instance.
     * 
     * @param facet
     * @param instance
     * @param auth
     * @param child
     */
    public ExistentialRuleform setSingularChild(NetworkAuthorization facet,
                                                ExistentialRuleform instance,
                                                NetworkAuthorization auth,
                                                ExistentialRuleform child) {
        if (instance == null) {
            return null;
        }

        if (!checkUPDATE(facet) || !checkUPDATE(auth)) {
            return instance;
        }

        if (child == null) {
            model.getPhantasmModel()
                 .unlinkImmediate(child, auth.getChildRelationship());
        } else {
            cast(child, auth);
            model.getPhantasmModel()
                 .setImmediateChild(instance, auth.getChildRelationship(),
                                    child, model.getCurrentPrincipal()
                                                .getPrincipal());
        }
        return instance;
    }

    private boolean checkREAD(AttributeAuthorization stateAuth) {
        return model.getPhantasmModel()
                    .checkCapability(stateAuth.getAuth(), getREAD());
    }

    private boolean checkREAD(ExistentialRuleform child) {
        return model.getPhantasmModel()
                    .checkCapability(child, getREAD());
    }

    private boolean checkREAD(NetworkAuthorization auth) {
        return model.getPhantasmModel()
                    .checkFacetCapability(auth.getAuth(), getREAD());
    }

    private boolean checkUPDATE(AttributeAuthorization stateAuth) {
        return model.getPhantasmModel()
                    .checkCapability(stateAuth.getAuth(), getUPDATE());
    }

    private boolean checkUPDATE(@SuppressWarnings("rawtypes") ExistentialRuleform child) {
        return model.getPhantasmModel()
                    .checkCapability(child, getUPDATE());
    }

    private boolean checkUPDATE(NetworkAuthorization auth) {
        return model.getPhantasmModel()
                    .checkCapability(auth.getAuth(), getUPDATE());
    }

    private Object[] getIndexedAttributeValue(ExistentialRuleform instance,
                                              Attribute authorizedAttribute) {

        ExistentialAttributeRecord[] attributeValues = getValueArray(instance,
                                                                     authorizedAttribute);

        Object[] values = (Object[]) Array.newInstance(authorizedAttribute.valueClass(),
                                                       attributeValues.length);
        for (ExistentialAttributeRecord value : attributeValues) {
            values[value.getSequenceNumber()] = value.getValue();
        }
        return values;
    }

    private Map<String, Object> getMappedAttributeValue(ExistentialRuleform instance,
                                                        Attribute authorizedAttribute) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, ExistentialAttributeRecord> entry : getValueMap(instance,
                                                                               authorizedAttribute).entrySet()) {
            map.put(entry.getKey(), entry.getValue()
                                         .getValue());
        }
        return map;
    }

    private ExistentialAttributeRecord[] getValueArray(ExistentialRuleform instance,
                                                       Attribute attribute) {
        List<? extends ExistentialAttributeRecord> values = model.getPhantasmModel()
                                                                 .getAttributeValues(instance,
                                                                                     attribute);
        int max = 0;
        for (ExistentialAttributeRecord value : values) {
            max = Math.max(max, value.getSequenceNumber() + 1);
        }
        @SuppressWarnings("unchecked")
        ExistentialAttributeRecord[] returnValue = new ExistentialAttributeRecord[max];
        for (ExistentialAttributeRecord form : values) {
            returnValue[form.getSequenceNumber()] = form;
        }
        return returnValue;
    }

    private Map<String, ExistentialAttributeRecord> getValueMap(ExistentialRuleform instance,
                                                                Attribute attribute) {
        Map<String, ExistentialAttributeRecord> map = new HashMap<>();
        for (ExistentialAttributeRecord value : model.getPhantasmModel()
                                                     .getAttributeValues(instance,
                                                                         attribute)) {
            map.put(value.getKey(), value);
        }
        return map;
    }

    private ExistentialAttributeRecord newAttributeValue(ExistentialRuleform instance,
                                                         Attribute attribute,
                                                         int i) {
        ExistentialAttributeRecord value = model.getPhantasmModel()
                                                .create(instance, attribute,
                                                        model.getCurrentPrincipal()
                                                             .getPrincipal());
        value.setSequenceNumber(i);
        return value;
    }

    private void setAttributeArray(ExistentialRuleform instance,
                                   Attribute authorizedAttribute,
                                   Object[] values) {
        ExistentialAttributeRecord[] old = getValueArray(instance,
                                                         authorizedAttribute);
        if (values == null) {
            if (old != null) {
                for (ExistentialAttributeRecord value : old) {
                    value.delete();
                }
            }
        } else if (old == null) {
            for (int i = 0; i < values.length; i++) {
                setValue(instance, authorizedAttribute, i, null, values[i]);
            }
        } else if (old.length == values.length) {
            for (int i = 0; i < values.length; i++) {
                setValue(instance, authorizedAttribute, i, old[i], values[i]);
            }
        } else if (old.length < values.length) {
            int i;
            for (i = 0; i < old.length; i++) {
                setValue(instance, authorizedAttribute, i, old[i], values[i]);
            }
            for (; i < values.length; i++) {
                setValue(instance, authorizedAttribute, i, null, values[i]);
            }
        } else if (old.length > values.length) {
            int i;
            for (i = 0; i < values.length; i++) {
                setValue(instance, authorizedAttribute, i, old[i], values[i]);
            }
            for (; i < old.length; i++) {
                old[i].delete();
            }
        }
    }

    private void setAttributeMap(ExistentialRuleform instance,
                                 Attribute authorizedAttribute,
                                 Map<String, Object> values) {
        Map<String, ExistentialAttributeRecord> valueMap = getValueMap(instance,
                                                                       authorizedAttribute);
        values.keySet()
              .stream()
              .filter(keyName -> !valueMap.containsKey(keyName))
              .forEach(keyName -> valueMap.remove(keyName));
        int maxSeq = 0;
        for (ExistentialAttributeRecord value : valueMap.values()) {
            maxSeq = Math.max(maxSeq, value.getSequenceNumber());
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            ExistentialAttributeRecord value = valueMap.get(entry.getKey());
            if (value == null) {
                value = newAttributeValue(instance, authorizedAttribute,
                                          ++maxSeq);
                value.insert();
                value.setKey(entry.getKey());
            }
            value.setValue(entry.getValue());
        }
    }

    private void setValue(ExistentialRuleform instance, Attribute attribute,
                          int i, ExistentialAttributeRecord existing,
                          Object newValue) {
        if (existing == null) {
            existing = newAttributeValue(instance, attribute, i);
            existing.insert();
        }
        existing.setValue(newValue);
    }
}
