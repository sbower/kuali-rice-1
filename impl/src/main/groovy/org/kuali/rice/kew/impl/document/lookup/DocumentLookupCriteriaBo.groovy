package org.kuali.rice.kew.impl.document.lookup

import org.kuali.rice.krad.bo.BusinessObject
import org.kuali.rice.kew.doctype.bo.DocumentType
import org.kuali.rice.kew.service.KEWServiceLocator
import org.kuali.rice.kew.doctype.bo.DocumentTypeEBO
import org.kuali.rice.kim.api.services.KimApiServiceLocator
import org.kuali.rice.kim.api.identity.Person
import org.kuali.rice.kew.api.document.lookup.DocumentLookupResult
import org.kuali.rice.kew.api.document.Document
import org.kuali.rice.core.api.CoreApiServiceLocator
import org.kuali.rice.kew.api.document.DocumentStatus
import java.sql.Timestamp

/**
 * Defines the business object that specifies the criteria used on document lookups.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
class DocumentLookupCriteriaBo implements BusinessObject {

    String documentTypeName
    String documentId
    String statusCode
    String applicationDocumentId
    String applicationDocumentStatus
    String title
    String initiatorPrincipalName
    String viewerPrincipalName
    String groupViewerName
    String groupViewerId
    String approverPrincipalName
    String routeNodeName
    String routeNodeLogic
    Timestamp dateCreated
    Timestamp dateLastModified
    Timestamp dateApproved
    Timestamp dateFinalized
    String saveName

    void refresh() {
        // nothing to refresh
    }

    DocumentType getDocumentType() {
        if (documentTypeName == null) {
            return null
        }
    	return KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName)
    }

    Person getInitiatorPerson() {
        if (initiatorPrincipalName == null) {
            return null
        }
        return KimApiServiceLocator.getPersonService().getPerson(initiatorPrincipalName)
    }

    Person getApproverPerson() {
        if (approverPrincipalName == null) {
            return null
        }
        return KimApiServiceLocator.getPersonService().getPerson(approverPrincipalName)
    }

    Person getViewerPerson() {
        if (viewerPrincipalName == null) {
            return null
        }
        return KimApiServiceLocator.getPersonService().getPerson(viewerPrincipalName)
    }


    String getStatusLabel() {
        if (statusCode == null) {
            return ""
        }
        return DocumentStatus.fromCode(statusCode).getLabel()
    }

    String getDocumentTypeLabel() {
        DocumentType documentType = getDocumentType()
        if (documentType != null) {
            return documentType.getLabel()
        }
        return ""
    }

    /**
     * Returns the route image which can be used to construct the route log link in custom lookup helper code.
     */
    String getRouteLog() {
        return "<img alt=\"Route Log for Document\" src=\"images/my_route_log.gif\"/>";
    }

    void populateFromDocumentLookupResult(DocumentLookupResult result) {
        Document document = result.document
        documentTypeName = document.documentTypeName
        documentId = document.documentId
        statusCode = document.status.code
        applicationDocumentId = document.applicationDocumentId
        applicationDocumentStatus = document.applicationDocumentStatus
        title = document.title
        initiatorPrincipalName = principalIdToName(document.initiatorPrincipalId)
        dateCreated = new Timestamp(document.dateCreated.getMillis())
    }

    private String principalIdToName(String principalId) {
        if (principalId.trim()) {
            return KimApiServiceLocator.getIdentityService().getPrincipal(principalId).getPrincipalName()
        }
        return null
    }

}
