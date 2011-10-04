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
package org.kuali.rice.kew.actions;

import org.apache.log4j.MDC;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.exception.InvalidActionTakenException;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.api.identity.principal.PrincipalContract;


import java.util.Iterator;
import java.util.List;


/**
 * Cancels a document at the request of a client app.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class CancelAction extends ActionTakenEvent {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CancelAction.class);

    public CancelAction(DocumentRouteHeaderValue rh, PrincipalContract principal) {
        super(KEWConstants.ACTION_TAKEN_CANCELED_CD, rh, principal);
    }

    public CancelAction(DocumentRouteHeaderValue rh, PrincipalContract principal, String annotation) {
        super(KEWConstants.ACTION_TAKEN_CANCELED_CD, rh, principal, annotation);
    }

    /* (non-Javadoc)
     * @see org.kuali.rice.kew.actions.ActionTakenEvent#isActionCompatibleRequest(java.util.List)
     */
    @Override
    public String validateActionRules() {
        return validateActionRules(getActionRequestService().findAllPendingRequests(routeHeader.getDocumentId()));
    }

    public String validateActionRules(List<ActionRequestValue> actionRequests) {
        // FYI delyea:  This is new validation check... was not being checked previously
        if (!getRouteHeader().isValidActionToTake(getActionPerformedCode())) {
            return "Document is not in a state to be cancelled";
        }
        List<ActionRequestValue> filteredActionRequests = filterActionRequestsByCode(actionRequests, KEWConstants.ACTION_REQUEST_COMPLETE_REQ);
        if (!isActionCompatibleRequest(filteredActionRequests)) {
            return "No request for the user is compatible with the Cancel Action";
        }
    	// check state before checking kim
        if (! KEWServiceLocator.getDocumentTypePermissionService().canCancel(getPrincipal().getPrincipalId(), getDocumentId(), getRouteHeader().getDocumentType(), getRouteHeader().getCurrentNodeNames(), getRouteHeader().getDocRouteStatus(), getRouteHeader().getInitiatorWorkflowId())) {
            return "User is not authorized to Cancel document";
        }
        return "";
    }

    /* (non-Javadoc)
     * @see org.kuali.rice.kew.actions.ActionTakenEvent#isActionCompatibleRequest(java.util.List)
     */
    @Override
    public boolean isActionCompatibleRequest(List<ActionRequestValue> requests) {

        // can always cancel saved or initiated document
        if (routeHeader.isStateInitiated() || routeHeader.isStateSaved()) {
            return true;
        }

        boolean actionCompatible = false;
        Iterator ars = requests.iterator();
        ActionRequestValue actionRequest = null;

        while (ars.hasNext()) {
            actionRequest = (ActionRequestValue) ars.next();
            String request = actionRequest.getActionRequested();

            // APPROVE and COMPLETE request matches CANCEL Taken code
            if ( (KEWConstants.ACTION_REQUEST_APPROVE_REQ.equals(request)) ||
                 (KEWConstants.ACTION_REQUEST_COMPLETE_REQ.equals(request)) ) {
                actionCompatible = true;
                break;
            }
        }

        return actionCompatible;
    }

    public void recordAction() throws InvalidActionTakenException {
        MDC.put("docId", getRouteHeader().getDocumentId());
        updateSearchableAttributesIfPossible();

        LOG.debug("Canceling document : " + annotation);

        List actionRequests = getActionRequestService().findAllValidRequests(getPrincipal().getPrincipalId(), getDocumentId(), KEWConstants.ACTION_REQUEST_COMPLETE_REQ);
        LOG.debug("Checking to see if the action is legal");
        String errorMessage = validateActionRules(actionRequests);
        if (!org.apache.commons.lang.StringUtils.isEmpty(errorMessage)) {
            throw new InvalidActionTakenException(errorMessage);
        }

        LOG.debug("Record the cancel action");
        ActionTakenValue actionTaken = saveActionTaken(findDelegatorForActionRequests(actionRequests));

        LOG.debug("Deactivate all pending action requests");
        actionRequests = getActionRequestService().findPendingByDoc(getDocumentId());

        getActionRequestService().deactivateRequests(actionTaken, actionRequests);
        notifyActionTaken(actionTaken);

        LOG.debug("Canceling document");

        try {
            String oldStatus = getRouteHeader().getDocRouteStatus();
            getRouteHeader().markDocumentCanceled();
            String newStatus = getRouteHeader().getDocRouteStatus();
            KEWServiceLocator.getRouteHeaderService().saveRouteHeader(getRouteHeader());
            notifyStatusChange(newStatus, oldStatus);
        } catch (WorkflowException ex) {
            LOG.warn(ex, ex);
            throw new InvalidActionTakenException(ex.getMessage());
        }
    }
}
