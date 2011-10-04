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
package org.kuali.rice.kew.messaging;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kew.actionrequest.service.DocumentRequeuerService;
import org.kuali.rice.kew.actions.asyncservices.ActionInvocationService;
import org.kuali.rice.kew.actions.asyncservices.BlanketApproveProcessorService;
import org.kuali.rice.kew.actions.asyncservices.MoveDocumentService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentProcessingQueue;
import org.kuali.rice.kew.api.mail.ImmediateEmailReminderService;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.ksb.api.KsbApiServiceLocator;
import org.kuali.rice.ksb.messaging.service.KSBXMLService;


/**
 * Utility class for accessing names of common asynchronous services.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class MessageServiceNames {

	public static final QName DOCUMENT_PROCESSING_QUEUE = new QName(KewApiConstants.Namespaces.KEW_NAMESPACE_2_0, "documentProcessingQueueSoap");

	public static final String ACTION_LIST_IMMEDIATE_REMINDER_SERVICE = "ImmediateEmailService";

	public static final String BLANKET_APPROVE_PROCESSING_SERVICE = "BlanketApproveProcessorService";

	public static final String DOCUMENT_REQUEUE_PROCESSING_SERVICE = "DocumentRequeueProcessorService";

	public static final String ROLE_POKER = "RolePokerProcessorService";

	public static final String MOVE_DOCUMENT_PROCESSOR = "MoveDocumentProcessor";

	public static final String ACTION_INVOCATION_PROCESSOR = "ActionInvocationProcessor";

	private static QName getQName(String baseServiceName, DocumentRouteHeaderValue document) {
		if (document != null) {
			return new QName(document.getDocumentType().getApplicationId(), baseServiceName);
		}
		return new QName(baseServiceName);
	}

	private static QName getQName(String baseServiceName, String applicationId) {
		if (!StringUtils.isEmpty(applicationId)) {
			return new QName(applicationId, baseServiceName);
		}
		return new QName(baseServiceName);
	}

    public static DocumentProcessingQueue getDocumentProcessingQueue(DocumentRouteHeaderValue document) {
        return (DocumentProcessingQueue)getServiceAsynchronously(DOCUMENT_PROCESSING_QUEUE, document);
    }

	public static MoveDocumentService getMoveDocumentProcessorService(DocumentRouteHeaderValue document) {
		return (MoveDocumentService) getServiceAsynchronously(getQName(MOVE_DOCUMENT_PROCESSOR, document), document);
	}

	public static ActionInvocationService getActionInvocationProcessorService(DocumentRouteHeaderValue document) {
		return (ActionInvocationService) getServiceAsynchronously(getQName(ACTION_INVOCATION_PROCESSOR, document), document);
	}

	public static BlanketApproveProcessorService getBlanketApproveProcessorService(DocumentRouteHeaderValue document) {
		return (BlanketApproveProcessorService) getServiceAsynchronously(getQName(BLANKET_APPROVE_PROCESSING_SERVICE, document), document);
	}
	
	public static DocumentRequeuerService getDocumentRequeuerService(String applicationId, String documentId, long waitTime) {
		QName serviceName = getQName(DOCUMENT_REQUEUE_PROCESSING_SERVICE, applicationId);
		if (waitTime > 0) {
			return (DocumentRequeuerService) getDelayedServiceAsynchronously(serviceName, documentId, waitTime);
		}
		return (DocumentRequeuerService) getServiceAsynchronously(serviceName, documentId, applicationId);
	}

	public static Object getServiceAsynchronously(QName serviceName, DocumentRouteHeaderValue document) {
        String applicationId = document.getDocumentType().getApplicationId();
		return getServiceAsynchronously(serviceName, getDocId(document), applicationId);
	}

	public static Object getServiceAsynchronously(QName serviceName, String documentId, String applicationId) {
		return KsbApiServiceLocator.getMessageHelper().getServiceAsynchronously(serviceName, applicationId, null, null, (documentId == null ? null : documentId.toString()), null);
	}

	public static Object getDelayedServiceAsynchronously(QName serviceName, DocumentRouteHeaderValue document, long waitTime) {
		return getDelayedServiceAsynchronously(serviceName, getDocId(document), waitTime);
	}

	public static Object getDelayedServiceAsynchronously(QName serviceName, String documentId, long waitTime) {
		return KsbApiServiceLocator.getMessageHelper().getServiceAsynchronously(serviceName, null, (documentId == null ? null : documentId.toString()), null, waitTime);
	}

	private static String getDocId(DocumentRouteHeaderValue document) {
		return (document == null ? null : document.getDocumentId());
	}

}
