/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krms.impl.repository;

import org.kuali.rice.krms.api.repository.rule.RuleDefinition;

/**
 * This is the interface for accessing KRMS repository Rule related bos 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public interface RuleBoService {
	public RuleDefinition createRule(RuleDefinition rule);
	public void updateRule(RuleDefinition rule);
	
	public RuleDefinition getRuleByRuleId(String ruleId);
	public RuleDefinition getRuleByNameAndNamespace(String name, String namespace);
	
//	public void createRuleAttribute(RuleAttribute ruleAttribute);
//	public void updateRuleAttribute(RuleAttribute ruleAttribute);
//	
//	public RuleAttribute getRuleAttributeById(String attrId);

//	public void setBusinessObjectService(final BusinessObjectService businessObjectService);
//	public List<RuleDefinition> convertListOfBosToImmutables(final Collection<RuleBo> ruleBos);
}
