/*
 * Copyright 2005-2007 The Kuali Foundation
 *
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.kuali.rice.kew.actions.asyncservices;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.reflect.DataDefinition;
import org.kuali.rice.kew.actions.ActionTakenEvent;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.exception.InvalidActionTakenException;
import org.kuali.rice.kew.exception.ResourceUnavailableException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.api.identity.principal.Principal;

/**
 * Service for doing the actual work of a mass action in the action list. Represents a single action on a single
 * document.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ActionInvocationProcessor implements ActionInvocationService { // implements RouteQueueProcessor {

    private static final Logger LOG = Logger.getLogger(ActionInvocationProcessor.class);

    @Override
    public void invokeAction(String principalId, String documentId, ActionInvocation invocation) {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null or blank");
        }

        if (StringUtils.isBlank(documentId)) {
            throw new RiceIllegalArgumentException("documentId is null");
        }

        if (invocation == null) {
            throw new RiceIllegalArgumentException("invocation is null");
        }


        KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
        DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);

        Principal principal = KEWServiceLocator.getIdentityHelperService().getPrincipal(principalId);
        List<DataDefinition> parameters = new ArrayList<DataDefinition>();
        parameters.add(new DataDefinition(document));
        parameters.add(new DataDefinition(principal));
        parameters.add(new DataDefinition(""));

        try {
            final ActionTakenEvent action = KEWServiceLocator.getActionRegistry().createAction(invocation.getActionCode(), parameters);
            if (!document.isValidActionToTake(invocation.getActionCode())) {
                LOG.warn("Action "
                        + invocation.getActionCode()
                        + " is not a valid action to take against document "
                        + document.getDocumentId()
                        + " by principal with name '"
                        + principal.getPrincipalName()
                        + "'");
                return;
            } else if (!KEWServiceLocator.getActionRegistry().getValidActions(principal, document).getActionTakenCodes()
                    .contains(action.getActionTakenCode())) {
                LOG.warn("Action "
                        + action.getActionTakenCode()
                        + " is not valid for document "
                        + document.getDocumentId()
                        + " by principal with name '"
                        + principal.getPrincipalName()
                        + "'");
                return;
            }
            action.performAction();
        } catch (ResourceUnavailableException e) {
            throw new WorkflowRuntimeException(e);
        } catch (InvalidActionTakenException e) {
            throw new WorkflowRuntimeException(e);
        }

    }

}
