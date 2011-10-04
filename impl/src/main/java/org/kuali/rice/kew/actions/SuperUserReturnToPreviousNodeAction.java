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
package org.kuali.rice.kew.actions;

import org.kuali.rice.kew.exception.InvalidActionTakenException;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.api.identity.principal.PrincipalContract;



/**
 * Does a return to previous as a superuser
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SuperUserReturnToPreviousNodeAction extends SuperUserActionTakenEvent {
    
    private String nodeName;
    
    public SuperUserReturnToPreviousNodeAction(DocumentRouteHeaderValue routeHeader, PrincipalContract principal) {
        super(KEWConstants.ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS_CD, routeHeader, principal);
        this.superUserAction = KEWConstants.SUPER_USER_RETURN_TO_PREVIOUS_ROUTE_LEVEL;
    }
    
    public SuperUserReturnToPreviousNodeAction(DocumentRouteHeaderValue routeHeader, PrincipalContract principal, String annotation, boolean runPostProcessor, String nodeName) {
        super(KEWConstants.ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS_CD, routeHeader, principal, annotation, runPostProcessor);
        this.superUserAction = KEWConstants.SUPER_USER_RETURN_TO_PREVIOUS_ROUTE_LEVEL;
        this.nodeName = nodeName;
    }
    
    protected void markDocument() throws WorkflowException {
        if (getRouteHeader().isInException()) {
            //this.event = new DocumentRouteStatusChange(this.documentId, this.getRouteHeader().getAppDocId(), this.getRouteHeader().getDocRouteStatus(), KEWConstants.ROUTE_HEADER_ENROUTE_CD);
            getRouteHeader().markDocumentEnroute();
        }
        ReturnToPreviousNodeAction returnAction = new ReturnToPreviousNodeAction(this.getActionTakenCode(), getRouteHeader(), getPrincipal(), annotation, nodeName, true, isRunPostProcessorLogic());
        returnAction.setSuperUserUsage(true);
        returnAction.performAction();
    }
    
    protected void processActionRequests() throws InvalidActionTakenException {
        //do nothing
    }

}
