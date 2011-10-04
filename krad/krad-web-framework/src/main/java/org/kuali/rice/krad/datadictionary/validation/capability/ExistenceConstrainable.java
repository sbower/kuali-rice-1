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
package org.kuali.rice.krad.datadictionary.validation.capability;

import org.kuali.rice.krad.datadictionary.validation.constraint.ExistenceConstraint;

/**
 * Validation capability to constrain a value to be required or not required to be non-empty. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org) 
 */
public interface ExistenceConstrainable extends Constrainable, ExistenceConstraint {

	// To match up with legacy code for AttributeDefinition, the existence constraint isRequired() member is a field
	// on the definition, making the capability a sub-interface of the constraint
	
}
