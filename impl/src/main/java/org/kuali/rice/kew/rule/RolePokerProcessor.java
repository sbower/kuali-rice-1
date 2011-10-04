/*
 * Copyright 2005-2008 The Kuali Foundation
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

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;

import java.util.ArrayList;
import java.util.List;


/**
 * RolePoker implementation which handles initiating refresh of the "poked" role.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RolePokerProcessor implements RolePoker {

    @Override
	public void reResolveRole(String documentId, String roleName, String qualifiedRoleNameLabel) {
        if (StringUtils.isBlank(documentId)) {
			throw new RiceIllegalArgumentException("documentId is null or blank");
		}
        if (StringUtils.isBlank(roleName)) {
			throw new RiceIllegalArgumentException("roleName is null or blank");
		}

		KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
		DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);
		if (qualifiedRoleNameLabel == null) {
			KEWServiceLocator.getRoleService().reResolveRole(document, roleName);
		} else {
			KEWServiceLocator.getRoleService().reResolveQualifiedRole(document, roleName, qualifiedRoleNameLabel);
		}
	}

    @Override
	public void reResolveRole(String documentId, String roleName) {
		reResolveRole(documentId, roleName, null);
	}
}
