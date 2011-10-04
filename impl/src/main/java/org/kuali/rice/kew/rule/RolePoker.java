/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
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

import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;

/**
 * Defines a service for "poking" a role for a specified document.  When a role is 
 * "poked" it will attempt to re-resolve the members of a role.
 * 
 * @see RoleAttribute
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface RolePoker {
	
	void reResolveRole(String documentId, String roleName, String qualifiedRoleNameLabel) throws RiceIllegalArgumentException;
	void reResolveRole(String documentId, String roleName) throws RiceIllegalArgumentException;

}
