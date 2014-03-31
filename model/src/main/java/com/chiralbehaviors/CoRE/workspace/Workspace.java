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

import com.chiralbehaviors.CoRE.ExistentialRuleform;
import com.chiralbehaviors.CoRE.attribute.Attribute;
import com.chiralbehaviors.CoRE.event.MetaProtocol;
import com.chiralbehaviors.CoRE.event.Protocol;
import com.chiralbehaviors.CoRE.event.status.StatusCode;
import com.chiralbehaviors.CoRE.meta.graph.Graph;
import com.chiralbehaviors.CoRE.network.NetworkRuleform;
import com.chiralbehaviors.CoRE.product.Product;

/**
 * @author hparry
 * 
 */
public interface Workspace {

	<T extends ExistentialRuleform<?,?>> T getEntityByName(Class<T> clazz,
			String name);

	Attribute getAttribute(String name);

	StatusCode getStatusCodeGraph(Product service);

	List<Protocol> getProtocolsFor(Product service);

	List<MetaProtocol> getMetaProtocolsFor(Product service);

	<RuleForm extends ExistentialRuleform<RuleForm, Network>, Network extends NetworkRuleform<RuleForm>> List<Graph<RuleForm, Network>> getRootedNetworksFor(
			RuleForm entity);
	
	<T extends ExistentialRuleform<?,?>> List<T> getAllEntities(Class<T> clazz);
	
	List<Protocol> getAllProtocols();
	
	List<MetaProtocol> getAllMetaProtocols();
}
