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

import org.apache.log4j.Logger;
import org.kuali.rice.kew.actionrequest.ActionRequestFactory;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.engine.BlanketApproveEngine;
import org.kuali.rice.kew.engine.OrchestrationConfig;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.OrchestrationConfig.EngineCapability;
import org.kuali.rice.kew.engine.node.RequestsNode;
import org.kuali.rice.kew.exception.InvalidActionTakenException;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.exception.WorkflowServiceErrorException;
import org.kuali.rice.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.api.identity.principal.PrincipalContract;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Does a super user approve action.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class SuperUserApproveEvent extends SuperUserActionTakenEvent {

	private static final Logger LOG = Logger.getLogger(SuperUserApproveEvent.class);

    public SuperUserApproveEvent(DocumentRouteHeaderValue routeHeader, PrincipalContract principal) {
        super(KEWConstants.ACTION_TAKEN_SU_APPROVED_CD, routeHeader, principal);
        this.superUserAction = KEWConstants.SUPER_USER_APPROVE;
    }

    public SuperUserApproveEvent(DocumentRouteHeaderValue routeHeader, PrincipalContract principal, String annotation, boolean runPostProcessor) {
        super(KEWConstants.ACTION_TAKEN_SU_APPROVED_CD, routeHeader, principal, annotation, runPostProcessor);
        this.superUserAction = KEWConstants.SUPER_USER_APPROVE;
    }

	public void recordAction() throws InvalidActionTakenException {
		// TODO: this is used because calling this code from SuperUserAction without
        // it causes an optimistic lock
		setRouteHeader(KEWServiceLocator.getRouteHeaderService().getRouteHeader(getDocumentId(), true));

		DocumentType docType = getRouteHeader().getDocumentType();

        String errorMessage = super.validateActionRules();
        if (!org.apache.commons.lang.StringUtils.isEmpty(errorMessage)) {
            LOG.info("User not authorized");
            List<WorkflowServiceErrorImpl> errors = new ArrayList<WorkflowServiceErrorImpl>();
            errors.add(new WorkflowServiceErrorImpl(errorMessage, AUTHORIZATION));
            throw new WorkflowServiceErrorException(errorMessage, errors);
        }

        ActionTakenValue actionTaken = saveActionTaken();

	        notifyActionTaken(actionTaken);

		if (getRouteHeader().isInException() || getRouteHeader().isStateInitiated()) {
			LOG.debug("Moving document back to Enroute");
			String oldStatus = getRouteHeader().getDocRouteStatus();
			getRouteHeader().markDocumentEnroute();
			String newStatus = getRouteHeader().getDocRouteStatus();
			notifyStatusChange(newStatus, oldStatus);
			KEWServiceLocator.getRouteHeaderService().saveRouteHeader(getRouteHeader());
		}

		OrchestrationConfig config = new OrchestrationConfig(EngineCapability.BLANKET_APPROVAL, new HashSet<String>(), actionTaken, docType.getSuperUserApproveNotificationPolicy().getPolicyValue(), isRunPostProcessorLogic());
		RequestsNode.setSupressPolicyErrors(RouteContext.getCurrentRouteContext());
		try {
			completeAnyOutstandingCompleteApproveRequests(actionTaken, docType.getSuperUserApproveNotificationPolicy().getPolicyValue());
			BlanketApproveEngine blanketApproveEngine = KEWServiceLocator.getWorkflowEngineFactory().newEngine(config);
			blanketApproveEngine.process(getRouteHeader().getDocumentId(), null);
		} catch (Exception e) {
			LOG.error("Failed to orchestrate the document to SuperUserApproved.", e);
			throw new InvalidActionTakenException("Failed to orchestrate the document to SuperUserApproved.", e);
		}

	}

	@SuppressWarnings("unchecked")
	protected void completeAnyOutstandingCompleteApproveRequests(ActionTakenValue actionTaken, boolean sendNotifications) throws Exception {
		List<ActionRequestValue> actionRequests = KEWServiceLocator.getActionRequestService().findPendingByActionRequestedAndDocId(KEWConstants.ACTION_REQUEST_APPROVE_REQ, getDocumentId());
		actionRequests.addAll(KEWServiceLocator.getActionRequestService().findPendingByActionRequestedAndDocId(KEWConstants.ACTION_REQUEST_COMPLETE_REQ, getDocumentId()));
		for (ActionRequestValue actionRequest : actionRequests) {
			KEWServiceLocator.getActionRequestService().deactivateRequest(actionTaken, actionRequest);
		}
		if (sendNotifications) {
			new ActionRequestFactory(this.getRouteHeader()).generateNotifications(actionRequests, getPrincipal(), this.findDelegatorForActionRequests(actionRequests), KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, KEWConstants.ACTION_TAKEN_SU_APPROVED_CD);
		}
	}

	protected void markDocument() throws WorkflowException {
		// do nothing since we are overriding the entire behavior
	}
}
