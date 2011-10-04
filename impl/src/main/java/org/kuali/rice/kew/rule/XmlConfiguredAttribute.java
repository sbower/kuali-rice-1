/*
 * Copyright 2007-2009 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.rule;

import org.kuali.rice.kew.rule.bo.RuleAttribute;

/**
 * Represents an attribute which is configured via XML.  In these cases, a reference
 * to the RuleAttribute BO is required in order to extract the XML config.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface XmlConfiguredAttribute {
	public void setRuleAttribute(RuleAttribute ruleAttribute);
}
