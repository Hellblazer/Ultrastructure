/**
 * (C) Copyright 2014 Chiral Behaviors, LLC. All Rights Reserved
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

package com.chiralbehaviors.CoRE.workspace;

import java.util.List;

import javax.persistence.EntityManager;

import com.chiralbehaviors.CoRE.Ruleform;

/**
 * @author hhildebrand
 *
 */
public interface Workspace {

    <T extends Ruleform> T get(String key);

    <T> T getAccessor(Class<T> accessorInterface);

    <T extends Ruleform> List<T> getCollection(Class<T> ruleformClass);

    WorkspaceSnapshot getSnapshot();

    void refreshFrom(EntityManager em);

    void replaceFrom(EntityManager em);

    void retarget(EntityManager em);
}
