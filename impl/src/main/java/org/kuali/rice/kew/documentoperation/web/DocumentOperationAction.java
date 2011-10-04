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
package org.kuali.rice.kew.documentoperation.web;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionlist.service.ActionListService;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.actionrequest.service.ActionRequestService;
import org.kuali.rice.kew.actionrequest.service.DocumentRequeuerService;
import org.kuali.rice.kew.actions.asyncservices.ActionInvocation;
import org.kuali.rice.kew.actions.asyncservices.ActionInvocationService;
import org.kuali.rice.kew.actions.asyncservices.BlanketApproveProcessorService;
import org.kuali.rice.kew.actions.asyncservices.MoveDocumentService;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.actiontaken.service.ActionTakenService;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.WorkflowDocumentFactory;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.api.document.DocumentProcessingQueue;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.engine.node.Branch;
import org.kuali.rice.kew.engine.node.BranchState;
import org.kuali.rice.kew.engine.node.NodeState;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.engine.node.service.BranchService;
import org.kuali.rice.kew.engine.node.service.RouteNodeService;
import org.kuali.rice.kew.exception.WorkflowServiceErrorException;
import org.kuali.rice.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.rice.kew.messaging.MessageServiceNames;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.kuali.rice.kew.rule.bo.RuleTemplateBo;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.web.KewKualiAction;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.ksb.messaging.service.KSBXMLService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Struts Action for doing editing of workflow documents.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DocumentOperationAction extends KewKualiAction {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentOperationAction.class);
	private static final String DEFAULT_LOG_MSG = "Admin change via document operation";

	public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		return mapping.findForward("basic");
	}

	public ActionForward getDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		DocumentOperationForm docForm = (DocumentOperationForm) form;
		String docId = null;
		
		// check if we have a plausible docId first
		if (StringUtils.isEmpty(docForm.getDocumentId())) {
			GlobalVariables.getMessageMap().putError("documentId", RiceKeyConstants.ERROR_REQUIRED, "Document ID");
		} else {
			try {
				docId = docForm.getDocumentId().trim();
			} catch (NumberFormatException nfe) {
				GlobalVariables.getMessageMap().putError("documentId", RiceKeyConstants.ERROR_NUMERIC, "Document ID");
			}
		}

		if (docId != null) {
			//to clear Document Field first;
			docForm.resetOps();
			DocumentRouteHeaderValue routeHeader = getRouteHeaderService().getRouteHeader(docId);
			List routeNodeInstances=getRouteNodeService().findRouteNodeInstances(docId);
			Map branches1=new HashMap();
			List branches=new ArrayList();

			if (routeHeader == null) {
				GlobalVariables.getMessageMap().putError("documentId", RiceKeyConstants.ERROR_EXISTENCE, "document");
			} else {
				materializeDocument(routeHeader);
				docForm.setRouteHeader(routeHeader);
				setRouteHeaderTimestampsToString(docForm);
				docForm.setRouteHeaderOp(KEWConstants.NOOP);
				docForm.setDocumentId(docForm.getDocumentId().trim());
				String initials="";
				for(Iterator lInitials=routeHeader.getInitialRouteNodeInstances().iterator();lInitials.hasNext();){
					String initial=((RouteNodeInstance)lInitials.next()).getRouteNodeInstanceId();
					LOG.debug(initial);
					initials=initials+initial+", ";
				}
				if(initials.trim().length()>1){
					initials=initials.substring(0,initials.lastIndexOf(","));
				}
				docForm.setInitialNodeInstances(initials);
				request.getSession().setAttribute("routeNodeInstances",routeNodeInstances);
				docForm.setRouteNodeInstances(routeNodeInstances);
				if(routeNodeInstances!=null){
					Iterator routeNodeInstanceIter=routeNodeInstances.iterator();
					while(routeNodeInstanceIter.hasNext()){
						RouteNodeInstance routeNodeInstance=(RouteNodeInstance) routeNodeInstanceIter.next();
						Branch branch=routeNodeInstance.getBranch();
						if (! branches1.containsKey(branch.getName())){
							branches1.put(branch.getName(),branch);
							branches.add(branch);
							LOG.debug(branch.getName()+"; "+branch.getBranchState());
						}
					}
					if(branches.size()<1){
						branches=null;
					}
				}
				branches1.clear();
				request.getSession().setAttribute("branches",branches);
				docForm.setBranches(branches);
			}
		}
			
		return mapping.findForward("basic");
	}

	/**
	 * Sets up various objects on the document which are required for use inside of the Struts and JSP framework.
	 *
	 * Specifically, if a document has action requests with null RouteNodeInstances, it will create empty node instance
	 * objects.
	 */
	private void materializeDocument(DocumentRouteHeaderValue document) {
		for (Iterator iterator = document.getActionRequests().iterator(); iterator.hasNext();) {
			ActionRequestValue request = (ActionRequestValue) iterator.next();
			if (request.getNodeInstance() == null) {
				request.setNodeInstance(new RouteNodeInstance());
			}
		}
	}

	public ActionForward clear(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		DocumentOperationForm docForm = (DocumentOperationForm) form;
		docForm.setRouteHeader(new DocumentRouteHeaderValue());
		docForm.setDocumentId(null);
		return mapping.findForward("basic");
	}

	public ActionMessages establishRequiredState(HttpServletRequest request, ActionForm form) throws Exception {
		return null;
	}

	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		DocumentOperationForm docForm = (DocumentOperationForm) form;
		boolean change = false;

		String routeHeaderOp = docForm.getRouteHeaderOp();
		if (!KEWConstants.UPDATE.equals(routeHeaderOp) && !KEWConstants.NOOP.equals(routeHeaderOp)) {
			throw new WorkflowServiceErrorException("Document operation not defined", new WorkflowServiceErrorImpl("Document operation not defined", "docoperation.operation.invalid"));
		}
		if (KEWConstants.UPDATE.equals(routeHeaderOp)) {
			setRouteHeaderTimestamps(docForm);
			DocumentRouteHeaderValue dHeader = docForm.getRouteHeader();
			String initials=docForm.getInitialNodeInstances();
			List<RouteNodeInstance> lInitials = new ArrayList<RouteNodeInstance>();
			if (StringUtils.isNotEmpty(initials)){ 
				StringTokenizer tokenInitials=new StringTokenizer(initials,",");
				while (tokenInitials.hasMoreTokens()) {
					String instanceId = tokenInitials.nextToken().trim();
					LOG.debug(instanceId);
					RouteNodeInstance instance = getRouteNodeService().findRouteNodeInstanceById(instanceId);
					lInitials.add(instance);
				}
			}
			dHeader.setInitialRouteNodeInstances(lInitials);
			getRouteHeaderService().validateRouteHeader(docForm.getRouteHeader());
			getRouteHeaderService().saveRouteHeader(docForm.getRouteHeader());
			change = true;
		}

		for (Iterator actionRequestIter = docForm.getActionRequestOps().iterator(); actionRequestIter.hasNext();) {
			DocOperationIndexedParameter actionRequestOp = (DocOperationIndexedParameter) actionRequestIter.next();
			int index = actionRequestOp.getIndex().intValue();
			String opValue = actionRequestOp.getValue();
			ActionRequestValue actionRequest = docForm.getRouteHeader().getDocActionRequest(index);
			String createDateParamName = "actionRequestCreateDate" + index;

			if (!KEWConstants.UPDATE.equals(opValue) && !KEWConstants.DELETE.equals(opValue) && !KEWConstants.NOOP.equals(opValue)) {
				throw new WorkflowServiceErrorException("Action request operation not defined", new WorkflowServiceErrorImpl("Action request operation not defined", "docoperation.actionrequest.operation.invalid"));
			}
			if (KEWConstants.UPDATE.equals(opValue)) {
				try {
					actionRequest.setCreateDate(new Timestamp(RiceConstants.getDefaultDateFormat().parse(request.getParameter(createDateParamName)).getTime()));
					actionRequest.setCreateDateString(RiceConstants.getDefaultDateFormat().format(actionRequest.getCreateDate()));
					actionRequest.setDocumentId(docForm.getRouteHeader().getDocumentId());
					actionRequest.setParentActionRequest(getActionRequestService().findByActionRequestId(actionRequest.getParentActionRequestId()));
					actionRequest.setActionTaken(getActionTakenService().findByActionTakenId(actionRequest.getActionTakenId()));
					if (actionRequest.getNodeInstance() != null && actionRequest.getNodeInstance().getRouteNodeInstanceId() == null) {
						actionRequest.setNodeInstance(null);
					} else if (actionRequest.getNodeInstance() != null && actionRequest.getNodeInstance().getRouteNodeInstanceId() != null) {
						actionRequest.setNodeInstance(KEWServiceLocator.getRouteNodeService().findRouteNodeInstanceById(actionRequest.getNodeInstance().getRouteNodeInstanceId()));
					}
					// getActionRequestService().validateActionRequest(actionRequest);
					getActionRequestService().saveActionRequest(actionRequest);
					change = true;
				} catch (ParseException pe) {
					throw new WorkflowServiceErrorException("Action request create date parsing error", new WorkflowServiceErrorImpl("Action request create date parsing error", "docoperation.actionrequests.dateparsing.error", actionRequest.getActionRequestId().toString()));
				}

			}
			if (KEWConstants.DELETE.equals(opValue)) {
			    getActionRequestService().deleteActionRequestGraph(actionRequest);
			    change = true;
			}
		}

		for (Iterator actionTakenIter = docForm.getActionTakenOps().iterator(); actionTakenIter.hasNext();) {
			DocOperationIndexedParameter actionTakenOp = (DocOperationIndexedParameter) actionTakenIter.next();
			int index = actionTakenOp.getIndex().intValue();
			String opValue = actionTakenOp.getValue();

			String actionDateParamName = "actionTakenActionDate" + index;
			ActionTakenValue actionTaken = docForm.getRouteHeader().getDocActionTaken(index);
			if (!KEWConstants.UPDATE.equals(opValue) && !KEWConstants.DELETE.equals(opValue) && !KEWConstants.NOOP.equals(opValue)) {
				throw new WorkflowServiceErrorException("Action taken operation not defined", new WorkflowServiceErrorImpl("Action taken operation not defined", "docoperation.actiontaken.operation.invalid"));
			}
			if (KEWConstants.UPDATE.equals(opValue)) {
				try {
					actionTaken.setActionDate(new Timestamp(RiceConstants.getDefaultDateFormat().parse(request.getParameter(actionDateParamName)).getTime()));
					actionTaken.setActionDateString(RiceConstants.getDefaultDateFormat().format(actionTaken.getActionDate()));
					// getActionTakenService().validateActionTaken(actionTaken);
					getActionTakenService().saveActionTaken(actionTaken);
					change = true;
				} catch (ParseException pe) {
					throw new WorkflowServiceErrorException("Action taken action date parsing error", new WorkflowServiceErrorImpl("Action taken action date parse error", "docoperation.actionstaken.dateparsing.error", actionTaken.getActionTakenId().toString()));
				}
			}
			if (KEWConstants.DELETE.equals(opValue)) {
				getActionTakenService().delete(actionTaken);
				change = true;
			}
		}

		for (Iterator actionItemIter = docForm.getActionItemOps().iterator(); actionItemIter.hasNext();) {
			DocOperationIndexedParameter actionItemOp = (DocOperationIndexedParameter) actionItemIter.next();
			int index = actionItemOp.getIndex().intValue();
			String opValue = actionItemOp.getValue();

			String dateAssignedParamName = "actionItemDateAssigned" + index;
			ActionItem actionItem = docForm.getRouteHeader().getDocActionItem(index);
			if (!KEWConstants.UPDATE.equals(opValue) && !KEWConstants.DELETE.equals(opValue) && !KEWConstants.NOOP.equals(opValue)) {
				throw new WorkflowServiceErrorException("Action Item operation not defined", new WorkflowServiceErrorImpl("Action Item operation not defined", "docoperation.operation.invalid"));
			}
			if (KEWConstants.UPDATE.equals(opValue)) {
				try {
					actionItem.setDateAssigned(new Timestamp(RiceConstants.getDefaultDateFormat().parse(request.getParameter(dateAssignedParamName)).getTime()));
					actionItem.setDateAssignedString(RiceConstants.getDefaultDateFormat().format(actionItem.getDateAssigned()));
					actionItem.setDocumentId(docForm.getRouteHeader().getDocumentId());
					// getActionItemService().validateActionItem(actionItem);
					getActionListService().saveActionItem(actionItem);
					change = true;
				} catch (ParseException pe) {
					throw new WorkflowServiceErrorException("Action item date assigned parsing error", new WorkflowServiceErrorImpl("Action item date assigned parse error", "docoperation.actionitem.dateassignedparsing.error", actionItem.getId().toString()));
				}
			}
			if (KEWConstants.DELETE.equals(opValue)) {
				getActionListService().deleteActionItem(actionItem);
				change = true;
			}
		}

		List routeNodeInstances=(List)(request.getSession().getAttribute("routeNodeInstances"));
		String ids = (docForm.getNodeStatesDelete() != null) ? docForm.getNodeStatesDelete().trim() : null;
		List statesToBeDeleted=new ArrayList();
		if(ids!=null && !ids.equals("")){
		    StringTokenizer idSets=new StringTokenizer(ids);
		    while (idSets.hasMoreTokens()) {
		    	String id=idSets.nextToken().trim();
		    	statesToBeDeleted.add(Long.valueOf(id));
		     }
		}

		for (Iterator routeNodeInstanceIter = docForm.getRouteNodeInstanceOps().iterator(); routeNodeInstanceIter.hasNext();) {
			DocOperationIndexedParameter routeNodeInstanceOp = (DocOperationIndexedParameter) routeNodeInstanceIter.next();
			int index = routeNodeInstanceOp.getIndex().intValue();
			String opValue = routeNodeInstanceOp.getValue();
            LOG.debug(opValue);
			RouteNodeInstance routeNodeInstance = (RouteNodeInstance)(routeNodeInstances.get(index));
			RouteNodeInstance routeNodeInstanceNew = (RouteNodeInstance)(docForm.getRouteNodeInstance(index));
			if (!KEWConstants.UPDATE.equals(opValue) && !KEWConstants.DELETE.equals(opValue) && !KEWConstants.NOOP.equals(opValue)) {
				throw new WorkflowServiceErrorException("Route Node Instance Operation not defined", new WorkflowServiceErrorImpl("Route Node Instance Operation not defined", "docoperation.routenodeinstance.operation.invalid"));
			}
			if (KEWConstants.UPDATE.equals(opValue)) {
				//LOG.debug("saving routeNodeInstance:"+routeNodeInstance.getRouteNodeInstanceId());
				//getRouteNodeService().save(routeNodeInstance);
				routeNodeInstance.setActive(routeNodeInstanceNew.isActive());
				LOG.debug(Boolean.toString(routeNodeInstanceNew.isActive()));
				routeNodeInstance.setComplete(routeNodeInstanceNew.isComplete());
				routeNodeInstance.setInitial(routeNodeInstanceNew.isInitial());
				List nodeStates=routeNodeInstance.getState();
				List nodeStatesNew=routeNodeInstanceNew.getState();

				if(nodeStates!=null){
					for(int i=0;i<nodeStates.size();i++){
					   NodeState nodeState=(NodeState)nodeStates.get(i);
					   NodeState nodeStateNew=(NodeState)nodeStatesNew.get(i);
					   if(nodeStateNew.getKey()!=null && ! nodeStateNew.getKey().trim().equals("")){
					   nodeState.setKey(nodeStateNew.getKey());
					   LOG.debug(nodeState.getKey());
					   nodeState.setValue(nodeStateNew.getValue());
					   LOG.debug(nodeState.getValue());
					   }
				    }
				}
				getRouteNodeService().save(routeNodeInstance);
				LOG.debug("saved");
				change = true;
			}


			if (KEWConstants.DELETE.equals(opValue)) {
				List nodeStates=routeNodeInstance.getState();
				List nodeStatesNew=routeNodeInstanceNew.getState();

				if(nodeStates!=null){
					for(int i=0;i<nodeStates.size();i++){
					   NodeState nodeState=(NodeState)nodeStates.get(i);
					   NodeState nodeStateNew=(NodeState)nodeStatesNew.get(i);
					   if(nodeStateNew.getKey()==null || nodeStateNew.getKey().trim().equals("")){
					     statesToBeDeleted.remove(nodeState.getNodeStateId());
					   }
				    }
				}
				getRouteNodeService().deleteByRouteNodeInstance(routeNodeInstance);
				LOG.debug(routeNodeInstance.getRouteNodeInstanceId()+" is deleted");
				change = true;
				break;
			}

			if (KEWConstants.NOOP.equals(opValue)){
				routeNodeInstanceNew.setActive(routeNodeInstance.isActive());
				routeNodeInstanceNew.setComplete(routeNodeInstance.isComplete());
				routeNodeInstanceNew.setInitial(routeNodeInstance.isInitial());
				List nodeStates=routeNodeInstance.getState();
				List nodeStatesNew=routeNodeInstanceNew.getState();
				if(nodeStates!=null){
				   for(int i=0;i<nodeStates.size();i++){
					   NodeState nodeState=(NodeState)nodeStates.get(i);
					   NodeState nodeStateNew=(NodeState)nodeStatesNew.get(i);
					   if(nodeStateNew.getKey()==null || nodeStateNew.getKey().trim().equals("")){
						     statesToBeDeleted.remove(nodeState.getNodeStateId());
					   }
					   nodeStateNew.setKey(nodeState.getKey());
					   nodeStateNew.setValue(nodeState.getValue());
				   }
				}
			}

			//((DocOperationIndexedParameter)(docForm.getRouteNodeInstanceOps().get(index))).setValue(KEWConstants.NOOP);
		}

		if(statesToBeDeleted!=null && statesToBeDeleted.size()>0){
			getRouteNodeService().deleteNodeStates(statesToBeDeleted);
		}


		List branches=(List)(request.getSession().getAttribute("branches"));
		String branchStateIds = (docForm.getBranchStatesDelete() != null) ? docForm.getBranchStatesDelete().trim() : null;
		List branchStatesToBeDeleted=new ArrayList();
		if(branchStateIds!=null && !branchStateIds.equals("")){
		    StringTokenizer idSets=new StringTokenizer(branchStateIds);
		    while (idSets.hasMoreTokens()) {
		    	String id=idSets.nextToken().trim();
		    	branchStatesToBeDeleted.add(Long.valueOf(id));
		    }
		}

		for (Iterator branchesOpIter = docForm.getBranchOps().iterator(); branchesOpIter.hasNext();) {
			DocOperationIndexedParameter branchesOp = (DocOperationIndexedParameter) branchesOpIter.next();
			int index = branchesOp.getIndex().intValue();
			String opValue = branchesOp.getValue();
            LOG.debug(opValue);
			Branch branch = (Branch)(branches.get(index));
			Branch branchNew = (Branch)(docForm.getBranche(index));
			if (!KEWConstants.UPDATE.equals(opValue) && !KEWConstants.NOOP.equals(opValue)) {
				throw new WorkflowServiceErrorException("Route Node Instance Operation not defined", new WorkflowServiceErrorImpl("Route Node Instance Operation not defined", "docoperation.routenodeinstance.operation.invalid"));
			}
			if (KEWConstants.UPDATE.equals(opValue)) {
				//LOG.debug("saving routeNodeInstance:"+routeNodeInstance.getRouteNodeInstanceId());
				//getRouteNodeService().save(routeNodeInstance);
				branch.setName(branchNew.getName());
				List branchStates=branch.getBranchState();
				List branchStatesNew=branchNew.getBranchState();
				if(branchStates!=null){
				   for(int i=0;i<branchStates.size();i++){
					   BranchState branchState=(BranchState)branchStates.get(i);
					   BranchState branchStateNew=(BranchState)branchStatesNew.get(i);
					   if(branchStateNew.getKey()!=null && ! branchStateNew.getKey().trim().equals("")){
					   branchState.setKey(branchStateNew.getKey());
					   LOG.debug(branchState.getKey());
					   branchState.setValue(branchStateNew.getValue());
					   LOG.debug(branchState.getValue());
					   }
				   }
				}
				getBranchService().save(branch);
				LOG.debug("branch saved");
				change = true;

			}


			if (KEWConstants.NOOP.equals(opValue)){
				branchNew.setName(branch.getName());
				List branchStates=branch.getBranchState();
				List branchStatesNew=branchNew.getBranchState();
				if(branchStates!=null){
				   for(int i=0;i<branchStates.size();i++){
					   BranchState branchState=(BranchState)branchStates.get(i);
					   BranchState branchStateNew=(BranchState)branchStatesNew.get(i);
					   if(branchStateNew.getKey()==null || branchStateNew.getKey().trim().equals("")){
						   branchStatesToBeDeleted.remove(branchState.getBranchStateId());
					   }
					   branchStateNew.setKey(branchState.getKey());
					   LOG.debug(branchState.getKey());
					   branchStateNew.setValue(branchState.getValue());
					   LOG.debug(branchState.getValue());
				   }
				}
			}
			//((DocOperationIndexedParameter)(docForm.getBranchOps().get(index))).setValue(KEWConstants.NOOP);
		}

		if(branchStatesToBeDeleted!=null && branchStatesToBeDeleted.size()>0){
			getBranchService().deleteBranchStates(branchStatesToBeDeleted);
		}

		WorkflowDocument workflowDocument = WorkflowDocumentFactory.loadDocument(GlobalVariables.getUserSession().getPrincipalId(), docForm.getDocumentId());

		String annotation = docForm.getAnnotation();
		if (StringUtils.isEmpty(annotation)) {
			annotation = DEFAULT_LOG_MSG;
		}
		workflowDocument.logAnnotation(annotation);

		ActionMessages messages = new ActionMessages();
		String forward = null;
		if (change) {
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("docoperation.operation.saved"));
			docForm.setRouteHeader(getRouteHeaderService().getRouteHeader(docForm.getRouteHeader().getDocumentId()));
			forward = "summary";
		} else {
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("docoperation.operation.noop"));
			forward = "basic";
		}
		saveMessages(request, messages);
		return mapping.findForward(forward);

	}

	private RouteHeaderService getRouteHeaderService() {
		return (RouteHeaderService) KEWServiceLocator.getService(KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
	}

	private RouteNodeService getRouteNodeService(){
		return (RouteNodeService) KEWServiceLocator.getService(KEWServiceLocator.ROUTE_NODE_SERVICE);
	}

	private ActionRequestService getActionRequestService() {
		return (ActionRequestService) KEWServiceLocator.getService(KEWServiceLocator.ACTION_REQUEST_SRV);
	}

	private ActionTakenService getActionTakenService() {
		return (ActionTakenService) KEWServiceLocator.getService(KEWServiceLocator.ACTION_TAKEN_SRV);
	}

	private ActionListService getActionListService() {
		return (ActionListService) KEWServiceLocator.getActionListService();
	}

	private void setRouteHeaderTimestamps(DocumentOperationForm docForm) {
		if (docForm.getCreateDate() == null || docForm.getCreateDate().trim().equals("")) {
			throw new WorkflowServiceErrorException("Document create date empty", new WorkflowServiceErrorImpl("Document create date empty", "docoperation.routeheader.createdate.empty"));
		} else {
			try {
				
//				String a_pat = "yyyy-MM-dd hh:mm:ss";
//				SimpleDateFormat fmt = new SimpleDateFormat(a_pat);
//				
//				System.out.println("**********************new*******************");
//				System.out.println(docForm.getCreateDate());
//				System.out.println("**********************old*******************");
//				System.out.println(docForm.getRouteHeader().getCreateDate());
//				System.out.println("********************************************");
//	
	//			docForm.getRouteHeader().setCreateDate(new Timestamp(fmt.parse(docForm.getCreateDate()).getTime()));

				docForm.getRouteHeader().setCreateDate(new Timestamp(RiceConstants.getDefaultDateAndTimeFormat().parse(docForm.getCreateDate()).getTime()));
			} catch (ParseException pe) {
				throw new WorkflowServiceErrorException("RouteHeader create date parsing error", new WorkflowServiceErrorImpl("Date parsing error", "docoperation.routeheader.createdate.invalid"));
			}
		}

		if (docForm.getStatusModDate() == null || docForm.getStatusModDate().trim().equals("")) {
			throw new WorkflowServiceErrorException("Document doc status mod date empty", new WorkflowServiceErrorImpl("Document doc status mod date empty", "docoperation.routeheader.statusmoddate.empty"));
		} else {
			try {
				docForm.getRouteHeader().setStatusModDate(new Timestamp(RiceConstants.getDefaultDateAndTimeFormat().parse(docForm.getStatusModDate()).getTime()));
			} catch (ParseException pe) {
				throw new WorkflowServiceErrorException("Document doc status date parsing error", new WorkflowServiceErrorImpl("Document doc status mod date parsing error", "docoperation.routeheader.statusmoddate.invalid"));
			}
		}

		if (docForm.getApprovedDate() != null && !docForm.getApprovedDate().trim().equals("")) {
			try {
				docForm.getRouteHeader().setApprovedDate(new Timestamp(RiceConstants.getDefaultDateAndTimeFormat().parse(docForm.getApprovedDate()).getTime()));
			} catch (ParseException pe) {
				throw new WorkflowServiceErrorException("Document approved date parsing error", new WorkflowServiceErrorImpl("Document approved date parsing error", "docoperation.routeheader.approveddate.invalid"));
			}

		}

		if (docForm.getFinalizedDate() != null && !docForm.getFinalizedDate().trim().equals("")) {
			try {
				docForm.getRouteHeader().setFinalizedDate(new Timestamp(RiceConstants.getDefaultDateAndTimeFormat().parse(docForm.getFinalizedDate()).getTime()));
			} catch (ParseException pe) {
				throw new WorkflowServiceErrorException("Document finalized date parsing error", new WorkflowServiceErrorImpl("Document finalized date parsing error", "docoperation.routeheader.finalizeddate.invalid"));
			}
		}

		if (docForm.getRouteStatusDate() != null && !docForm.getRouteStatusDate().trim().equals("")) {
			try {
				docForm.getRouteHeader().setRouteStatusDate(new Timestamp(RiceConstants.getDefaultDateAndTimeFormat().parse(docForm.getRouteStatusDate()).getTime()));
			} catch (ParseException pe) {
				throw new WorkflowServiceErrorException("Document route status date parsing error", new WorkflowServiceErrorImpl("Document route status date parsing error", "docoperation.routeheader.routestatusdate.invalid"));
			}

		}

		if (docForm.getRouteLevelDate() != null && !docForm.getRouteLevelDate().trim().equals("")) {
			try {
				docForm.getRouteHeader().setRouteLevelDate(new Timestamp(RiceConstants.getDefaultDateAndTimeFormat().parse(docForm.getRouteLevelDate()).getTime()));
			} catch (ParseException pe) {
				throw new WorkflowServiceErrorException("Document route level date parsing error", new WorkflowServiceErrorImpl("Document route level date parsing error", "docoperation.routeheader.routeleveldate.invalid"));
			}
		}
	}

	private void setRouteHeaderTimestampsToString(DocumentOperationForm docForm) {
		try {
			docForm.setCreateDate(RiceConstants.getDefaultDateAndTimeFormat().format(docForm.getRouteHeader().getCreateDate()));
			docForm.setStatusModDate(RiceConstants.getDefaultDateAndTimeFormat().format(docForm.getRouteHeader().getStatusModDate()));
			docForm.setApprovedDate(RiceConstants.getDefaultDateAndTimeFormat().format(docForm.getRouteHeader().getApprovedDate()));
			docForm.setFinalizedDate(RiceConstants.getDefaultDateAndTimeFormat().format(docForm.getRouteHeader().getFinalizedDate()));
			docForm.setRouteStatusDate(RiceConstants.getDefaultDateAndTimeFormat().format(docForm.getRouteHeader().getRouteStatusDate()));
			docForm.setRouteLevelDate(RiceConstants.getDefaultDateAndTimeFormat().format(docForm.getRouteHeader().getRouteLevelDate()));

		} catch (Exception e) {
			LOG.info("One or more of the dates in routeHeader may be null");
		}
	}

	public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		DocumentOperationForm docForm = (DocumentOperationForm) form;
		String lookupInvocationModule = docForm.getLookupInvocationModule();
		docForm.getRouteHeader().setDocumentId(docForm.getDocumentId());

		if (lookupInvocationModule != null && !lookupInvocationModule.trim().equals("")) {
			String lookupField = docForm.getLookupInvocationField();
			int lookupIndex = new Integer(docForm.getLookupInvocationIndex()).intValue();
			String networkId = request.getParameter("networkId");
			String principalId = KEWServiceLocator.getIdentityHelperService().getIdForPrincipalName(networkId);

			if (lookupInvocationModule.equals("RouteHeader")) {
				DocumentRouteHeaderValue routeHeader = docForm.getRouteHeader();
				if ("initiatorWorkflowId".equals(lookupField)) {
					routeHeader.setInitiatorWorkflowId(principalId);
				}
				if ("documentTypeId".equals(lookupField)) {
					DocumentType docType = getDocumentTypeService().findByName(request.getParameter("docTypeFullName"));
					routeHeader.setDocumentTypeId(docType.getDocumentTypeId());
				}
			}

			if (lookupInvocationModule.equals("ActionRequest")) {
				ActionRequestValue actionRequest = docForm.getRouteHeader().getDocActionRequest(lookupIndex);
				if ("routeMethodName".equals(lookupField)) {
//					actionRequest.setRouteMethodName(null);
					String id = request.getParameter("ruleTemplate.ruleTemplateId");
					if (id != null && !"".equals(id.trim())) {
						RuleTemplateBo ruleTemplate = KEWServiceLocator.getRuleTemplateService().findByRuleTemplateId(id);
//						if (ruleTemplate != null) {
//							actionRequest.setRouteMethodName(ruleTemplate.getName());
//						}
					}
				}
				if ("workflowId".equals(lookupField)) {
					actionRequest.setPrincipalId(principalId);
				}
				if ("workgroupId".equals(lookupField)) {
					if (request.getParameter("workgroupId") != null && !"".equals(request.getParameter("workgroupId").trim())) {
						actionRequest.setGroupId(request.getParameter("workgroupId"));
					} else {
						actionRequest.setGroupId(null);
					}
				}
				if ("roleName".equals(lookupField)) {
					actionRequest.setRoleName(request.getParameter("roleName"));
				}
			}
			if (lookupInvocationModule.equals("ActionTaken")) {
				ActionTakenValue actionTaken = docForm.getRouteHeader().getDocActionTaken(lookupIndex);
				if ("workflowId".equals(lookupField)) {
					Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(networkId);
					if (principal != null) {
						actionTaken.setPrincipalId(principal.getPrincipalId());
					} else {
						LOG.info("action taken user not found");
						actionTaken.setPrincipalId(null);
					}
				}
				if ("delegatorPrincipalId".equals(lookupField)) {
					Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(networkId);
					if (principal != null) {
						actionTaken.setDelegatorPrincipalId(principal.getPrincipalId());
					} else {
						LOG.info("action taken delegator user not found");
						actionTaken.setDelegatorPrincipalId(null);
					}
				}
				if ("delegatorGroupId".equals(lookupField)) {
					if (request.getParameter("workgroupId") != null && !"".equals(request.getParameter("workgroupId").trim())) {
						actionTaken.setDelegatorGroupId(request.getParameter("workgroupId"));
					} else {
						actionTaken.setDelegatorGroupId(null);
					}
				}
			}

			if (lookupInvocationModule.equals("ActionItem")) {
				ActionItem actionItem = docForm.getRouteHeader().getDocActionItem(lookupIndex);
				if ("workflowId".equals(lookupField)) {
					Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(networkId);
					if (principal != null) {
						actionItem.setPrincipalId(principal.getPrincipalId());
					} else {
						LOG.info("action item user not found");
						actionItem.setPrincipalId(null);
					}
				}

				if ("workgroupId".equals(lookupField)) {
					if (request.getParameter("workgroupId") != null && !"".equals(request.getParameter("workgroupId").trim())) {
						actionItem.setGroupId(request.getParameter("workgroupId"));
					} else {
						actionItem.setGroupId(null);
					}
				}
				if ("roleName".equals(lookupField)) {
					actionItem.setRoleName(request.getParameter("roleName"));
				}
				if ("delegatorPrincipalId".equals(lookupField)) {
					Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(networkId);
					if (principal != null) {
						actionItem.setDelegatorPrincipalId(principal.getPrincipalId());
					} else {
						LOG.info("action item delegator user not found");
						actionItem.setDelegatorPrincipalId(null);
					}
				}
				if ("delegatorGroupId".equals(lookupField)) {
					if (request.getParameter("workgroupId") != null && !"".equals(request.getParameter("workgroupId").trim())) {
						actionItem.setDelegatorGroupId(request.getParameter("workgroupId"));
					} else {
						actionItem.setDelegatorGroupId(null);
					}
				}
				if ("docName".equals(lookupField)) {
					DocumentType docType = getDocumentTypeService().findByName(request.getParameter("docTypeFullName"));
					actionItem.setDocName(docType.getName());
					actionItem.setDocLabel(docType.getLabel());
					actionItem.setDocHandlerURL(docType.getDocHandlerUrl());
				}
			}
		}

		return mapping.findForward("basic");
	}

	public ActionForward queueDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			DocumentOperationForm docForm = (DocumentOperationForm) form;
            DocumentProcessingQueue documentProcessingQueue = MessageServiceNames.getDocumentProcessingQueue(docForm.getRouteHeader());
			documentProcessingQueue.process(docForm.getDocumentId());
			ActionMessages messages = new ActionMessages();
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("general.message", "Document was successfully queued"));
			saveMessages(request, messages);
			return mapping.findForward("basic");
		} catch (Exception e) {
			throw new WorkflowRuntimeException(e);
		}
	}

	public ActionForward indexSearchableAttributes(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		DocumentOperationForm docForm = (DocumentOperationForm) form;
        DocumentAttributeIndexingQueue queue = KewApiServiceLocator.getDocumentAttributeIndexingQueue(docForm.getRouteHeader().getDocumentType().getApplicationId());
        queue.indexDocument(docForm.getRouteHeader().getDocumentId());
		ActionMessages messages = new ActionMessages();
		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("general.message", "Searchable Attribute Indexing was successfully scheduled"));
		saveMessages(request, messages);
		return mapping.findForward("basic");
	}

	public ActionForward queueDocumentRequeuer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		DocumentOperationForm docForm = (DocumentOperationForm) form;
		DocumentRequeuerService docRequeue = MessageServiceNames.getDocumentRequeuerService(docForm.getRouteHeader().getDocumentType().getApplicationId(), docForm.getRouteHeader().getDocumentId(), 0);
		docRequeue.requeueDocument(docForm.getRouteHeader().getDocumentId());
		ActionMessages messages = new ActionMessages();
		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("general.message", "Document Requeuer was successfully scheduled"));
		saveMessages(request, messages);
		return mapping.findForward("basic");
	}

	public ActionForward blanketApproveDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			DocumentOperationForm docForm = (DocumentOperationForm) form;
			String principalId = KimApiServiceLocator.getPersonService().getPersonByPrincipalName(docForm.getBlanketApproveUser()).getPrincipalId();
			Set<String> nodeNames = new HashSet<String>();
			if (!StringUtils.isBlank(docForm.getBlanketApproveNodes())) {
				String[] nodeNameArray = docForm.getBlanketApproveNodes().split(",");
				for (String nodeName : nodeNameArray) {
					nodeNames.add(nodeName.trim());
				}
			}
			BlanketApproveProcessorService blanketApprove = MessageServiceNames.getBlanketApproveProcessorService(docForm.getRouteHeader());
			blanketApprove.doBlanketApproveWork(docForm.getRouteHeader().getDocumentId(), principalId, docForm.getBlanketApproveActionTakenId(), nodeNames, true);
			ActionMessages messages = new ActionMessages();
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("general.message", "Blanket Approve Processor was successfully scheduled"));
			saveMessages(request, messages);
			return mapping.findForward("basic");
		} catch (Exception e) {
			throw new WorkflowRuntimeException(e);
		}
	}
	
	public ActionForward moveDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			DocumentOperationForm docForm = (DocumentOperationForm) form;
			String principalId = KEWServiceLocator.getIdentityHelperService().getIdForPrincipalName(docForm.getBlanketApproveUser());
			Set<String> nodeNames = new HashSet<String>();
			if (!StringUtils.isBlank(docForm.getBlanketApproveNodes())) {
				String[] nodeNameArray = docForm.getBlanketApproveNodes().split(",");
				for (String nodeName : nodeNameArray) {
					nodeNames.add(nodeName.trim());
				}
			}
			ActionTakenValue actionTaken = KEWServiceLocator.getActionTakenService().findByActionTakenId(docForm.getBlanketApproveActionTakenId());
			MoveDocumentService moveService = MessageServiceNames.getMoveDocumentProcessorService(docForm.getRouteHeader());
			moveService.moveDocument(principalId, docForm.getRouteHeader(), actionTaken, nodeNames);
			ActionMessages messages = new ActionMessages();
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("general.message", "Move Document Processor was successfully scheduled"));
			saveMessages(request, messages);
			return mapping.findForward("basic");
		} catch (Exception e) {
			throw new WorkflowRuntimeException(e);
		}
	}

	public ActionForward queueActionInvocation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			DocumentOperationForm docForm = (DocumentOperationForm) form;
			String principalId = KEWServiceLocator.getIdentityHelperService().getIdForPrincipalName(docForm.getActionInvocationUser());
			ActionInvocation invocation = new ActionInvocation(docForm.getActionInvocationActionItemId(), docForm.getActionInvocationActionCode());
			ActionInvocationService actionInvocationService = MessageServiceNames.getActionInvocationProcessorService(docForm.getRouteHeader());
			actionInvocationService.invokeAction(principalId, docForm.getRouteHeader().getDocumentId(), invocation);
			ActionMessages messages = new ActionMessages();
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("general.message", "Action Invocation Processor was successfully scheduled"));
			saveMessages(request, messages);
			return mapping.findForward("basic");
		} catch (Exception e) {
			throw new WorkflowRuntimeException(e);
		}
	}

	private DocumentTypeService getDocumentTypeService() {
		return (DocumentTypeService) KEWServiceLocator.getService(KEWServiceLocator.DOCUMENT_TYPE_SERVICE);
	}

	private BranchService getBranchService(){
		return (BranchService) KEWServiceLocator.getService(KEWServiceLocator.BRANCH_SERVICE);
	}
}
