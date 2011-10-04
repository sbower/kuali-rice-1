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
package org.kuali.rice.kew.docsearch;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.Ignore;
import org.junit.Test;
import org.kuali.rice.core.framework.persistence.jdbc.sql.SQLUtils;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.WorkflowDocumentFactory;
import org.kuali.rice.kew.api.document.lookup.DocumentLookupCriteria;
import org.kuali.rice.kew.api.document.lookup.DocumentLookupResult;
import org.kuali.rice.kew.api.document.lookup.DocumentLookupResults;
import org.kuali.rice.kew.api.document.lookup.RouteNodeLookupLogic;
import org.kuali.rice.kew.docsearch.service.DocumentSearchService;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.test.KEWTestCase;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.web.KeyValueSort;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.test.BaselineTestCase;
import org.kuali.rice.test.TestHarnessServiceLocator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@BaselineTestCase.BaselineMode(BaselineTestCase.Mode.NONE)
public class DocumentSearchTest extends KEWTestCase {
    private static final String KREW_DOC_HDR_T = "KREW_DOC_HDR_T";
    private static final String INITIATOR_COL = "INITR_PRNCPL_ID";

    DocumentSearchService docSearchService;

    @Override
    protected void loadTestData() throws Exception {
        loadXmlFile("SearchAttributeConfig.xml");
        
    }

    @Override
    protected void setUpAfterDataLoad() throws Exception {
        docSearchService = (DocumentSearchService)KEWServiceLocator.getService(KEWServiceLocator.DOCUMENT_SEARCH_SERVICE);
    }

    @Test public void testDocSearch() throws Exception {
        Person user = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("bmcgough");
        DocumentLookupCriteria.Builder criteria = DocumentLookupCriteria.Builder.create();
        DocumentLookupResults results = null;
        criteria.setTitle("*IN");
        criteria.setSaveName("bytitle");
        results = docSearchService.lookupDocuments(user.getPrincipalId(), criteria.build());
        criteria = DocumentLookupCriteria.Builder.create();
        criteria.setTitle("*IN-CFSG");
        criteria.setSaveName("for in accounts");
        results = docSearchService.lookupDocuments(user.getPrincipalId(), criteria.build());
        criteria = DocumentLookupCriteria.Builder.create();
        criteria.setDateApprovedFrom(new DateTime(2004, 9, 16, 0, 0));
        results = docSearchService.lookupDocuments(user.getPrincipalId(), criteria.build());
        criteria = DocumentLookupCriteria.Builder.create();
        criteria.setRouteNodeName("AdHoc");
        criteria.setRouteNodeLookupLogic(RouteNodeLookupLogic.EXACTLY);
        results = docSearchService.lookupDocuments(user.getPrincipalId(), criteria.build());
        user = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("bmcgough");
        DocumentLookupCriteria savedCriteria = docSearchService.getSavedSearchCriteria(user.getPrincipalId(), "bytitle");
        assertNotNull(savedCriteria);
        assertEquals("bytitle", savedCriteria.getSaveName());
        savedCriteria = docSearchService.getSavedSearchCriteria(user.getPrincipalId(), "for in accounts");
        assertNotNull(savedCriteria);
        assertEquals("for in accounts", savedCriteria.getSaveName());
    }

    @Test
    public void testDocSearch_criteriaModified() throws Exception {
        String principalId = getPrincipalId("ewestfal");

        // if no criteria is specified, the dateCreatedFrom is defaulted to today
        DocumentLookupCriteria.Builder criteria = DocumentLookupCriteria.Builder.create();
        DocumentLookupResults results = docSearchService.lookupDocuments(principalId, criteria.build());
        assertTrue("criteria should have been modified", results.isCriteriaModified());
        assertNull("original date created from should have been null", criteria.getDateCreatedFrom());
        assertNotNull("modified date created from should be non-null", results.getCriteria().getDateCreatedFrom());
        assertEquals("Criteria date minus today's date should equal the constant value",
                KEWConstants.DOCUMENT_SEARCH_NO_CRITERIA_CREATE_DATE_DAYS_AGO.intValue(),
                getDifferenceInDays(results.getCriteria().getDateCreatedFrom()));

        // now set some attributes which should still result in modified criteria since they don't count toward
        // determining if the criteria is empty or not
        criteria.setMaxResults(new Integer(50));
        criteria.setSaveName("myRadSearch");
        results = docSearchService.lookupDocuments(principalId, criteria.build());
        assertTrue("criteria should have been modified", results.isCriteriaModified());
        assertNotNull("modified date created from should be non-null", results.getCriteria().getDateCreatedFrom());

        // now set the title, when only title is specified, date created from is defaulted
        criteria.setTitle("My rad title search!");
        results = docSearchService.lookupDocuments(principalId, criteria.build());
        assertTrue("criteria should have been modified", results.isCriteriaModified());
        assertNotNull("modified date created from should be non-null", results.getCriteria().getDateCreatedFrom());
        assertEquals("Criteria date minus today's date should equal the constant value",
                Math.abs(KEWConstants.DOCUMENT_SEARCH_DOC_TITLE_CREATE_DATE_DAYS_AGO.intValue()),
                getDifferenceInDays(results.getCriteria().getDateCreatedFrom()));

        // now set another field on the criteria, modification should *not* occur
        criteria.setApplicationDocumentId("12345");
        results = docSearchService.lookupDocuments(principalId, criteria.build());
        assertFalse("criteria should *not* have been modified", results.isCriteriaModified());
        assertNull("modified date created from should still be null", results.getCriteria().getDateCreatedFrom());
        assertEquals("both criterias should be equal", criteria.build(), results.getCriteria());
    }

    /**
     * Test for https://test.kuali.org/jira/browse/KULRICE-1968 - Document search fails when users are missing
     * Tests that we can safely search on docs whose initiator no longer exists in the identity management system
     * This test searches by doc type name criteria.
     * @throws Exception
     */
    @Test public void testDocSearch_MissingInitiator() throws Exception {
        String documentTypeName = "SearchDocType";
        DocumentType docType = ((DocumentTypeService)KEWServiceLocator.getService(KEWServiceLocator.DOCUMENT_TYPE_SERVICE)).findByName(documentTypeName);
        String userNetworkId = "arh14";
        // route a document to enroute and route one to final
        WorkflowDocument workflowDocument = WorkflowDocumentFactory.createDocument(getPrincipalId(userNetworkId), documentTypeName);
        workflowDocument.setTitle("testDocSearch_MissingInitiator");
        workflowDocument.route("routing this document.");

        // verify the document is enroute for jhopf
        workflowDocument = WorkflowDocumentFactory.loadDocument(getPrincipalId("jhopf"),workflowDocument.getDocumentId());
        assertTrue(workflowDocument.isEnroute());
        assertTrue(workflowDocument.isApprovalRequested());

        // now nuke the initiator...
        new JdbcTemplate(TestHarnessServiceLocator.getDataSource()).execute("update " + KREW_DOC_HDR_T + " set " + INITIATOR_COL + " = 'bogus user' where DOC_HDR_ID = " + workflowDocument.getDocumentId());


        Person user = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("jhopf");
        DocumentLookupCriteria.Builder criteria = DocumentLookupCriteria.Builder.create();
        criteria.setDocumentTypeName(documentTypeName);
        DocumentLookupResults results = docSearchService.lookupDocuments(user.getPrincipalId(), criteria.build());
        assertEquals("Search returned invalid number of documents", 1, results.getLookupResults().size());
    }

    /**
     * Test for https://test.kuali.org/jira/browse/KULRICE-1968 - Tests that we get an error if we try and search on an initiator that doesn't exist in the IDM system
     * @throws Exception
     */
    @Test public void testDocSearch_SearchOnMissingInitiator() throws Exception {
        String documentTypeName = "SearchDocType";
        DocumentType docType = ((DocumentTypeService)KEWServiceLocator.getService(KEWServiceLocator.DOCUMENT_TYPE_SERVICE)).findByName(documentTypeName);
        String userNetworkId = "arh14";
        // route a document to enroute and route one to final
        WorkflowDocument workflowDocument = WorkflowDocumentFactory.createDocument(getPrincipalId(userNetworkId), documentTypeName);
        workflowDocument.setTitle("testDocSearch_MissingInitiator");
        workflowDocument.route("routing this document.");

        // verify the document is enroute for jhopf
        workflowDocument = WorkflowDocumentFactory.loadDocument(getPrincipalId("jhopf"),workflowDocument.getDocumentId());
        assertTrue(workflowDocument.isEnroute());
        assertTrue(workflowDocument.isApprovalRequested());

        // now nuke the initiator...
        new JdbcTemplate(TestHarnessServiceLocator.getDataSource()).execute("update " + KREW_DOC_HDR_T + " set " + INITIATOR_COL + " = 'bogus user' where DOC_HDR_ID = " + workflowDocument.getDocumentId());


        Person user = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("jhopf");
        DocumentLookupCriteria.Builder criteria = DocumentLookupCriteria.Builder.create();
        criteria.setInitiatorPrincipalName("bogus user");

        DocumentLookupResults results = docSearchService.lookupDocuments(user.getPrincipalId(),
                criteria.build());
        int size = results.getLookupResults().size();
        assertTrue("Searching by an invalid initiator should return nothing", size == 0);

    }

    @Test public void testDocSearch_RouteNodeName() throws Exception {
        loadXmlFile("DocSearchTest_RouteNode.xml");
        String documentTypeName = "SearchDocType_RouteNodeTest";
    	DocumentType docType = ((DocumentTypeService)KEWServiceLocator.getService(KEWServiceLocator.DOCUMENT_TYPE_SERVICE)).findByName(documentTypeName);
        String userNetworkId = "rkirkend";

        // route a document to enroute and route one to final
        WorkflowDocument workflowDocument = WorkflowDocumentFactory.createDocument(getPrincipalId(userNetworkId), documentTypeName);
        workflowDocument.setTitle("Routing style");
        workflowDocument.route("routing this document.");
        // verify the document is enroute for jhopf
        workflowDocument = WorkflowDocumentFactory.loadDocument(getPrincipalId("jhopf"),workflowDocument.getDocumentId());
        assertTrue(workflowDocument.isEnroute());
        assertTrue(workflowDocument.isApprovalRequested());
        workflowDocument.approve("");
        workflowDocument = WorkflowDocumentFactory.loadDocument(getPrincipalId("jhopf"),workflowDocument.getDocumentId());
        assertTrue(workflowDocument.isFinal());
        workflowDocument = WorkflowDocumentFactory.createDocument(getPrincipalId(userNetworkId), documentTypeName);
        workflowDocument.setTitle("Routing style");
        workflowDocument.route("routing this document.");
        // verify the document is enroute for jhopf
        workflowDocument = WorkflowDocumentFactory.loadDocument(getPrincipalId("jhopf"),workflowDocument.getDocumentId());
        assertTrue(workflowDocument.isEnroute());
        assertTrue(workflowDocument.isApprovalRequested());


        Person user = KimApiServiceLocator.getPersonService().getPersonByPrincipalName(userNetworkId);
        DocumentLookupCriteria.Builder criteria = DocumentLookupCriteria.Builder.create();
        criteria.setDocumentTypeName(documentTypeName);
        DocumentLookupResults results = docSearchService.lookupDocuments(user.getPrincipalId(),
                criteria.build());
        assertEquals("Search returned invalid number of documents", 2, results.getLookupResults().size());

        criteria.setRouteNodeName(getRouteNodeForSearch(documentTypeName,workflowDocument.getNodeNames()));
        criteria.setRouteNodeLookupLogic(RouteNodeLookupLogic.EXACTLY);
        results = docSearchService.lookupDocuments(user.getPrincipalId(), criteria.build());
        assertEquals("Search returned invalid number of documents", 1, results.getLookupResults().size());

        // load the document type again to change the route node ids
        loadXmlFile("DocSearchTest_RouteNode.xml");

        workflowDocument = WorkflowDocumentFactory.loadDocument(getPrincipalId("jhopf"),workflowDocument.getDocumentId());
        assertTrue(workflowDocument.isEnroute());
        assertTrue(workflowDocument.isApprovalRequested());
        criteria.setRouteNodeName(getRouteNodeForSearch(documentTypeName, workflowDocument.getNodeNames()));
        results = docSearchService.lookupDocuments(user.getPrincipalId(), criteria.build());
        assertEquals("Search returned invalid number of documents", 1, results.getLookupResults().size());

    }

    private String getRouteNodeForSearch(String documentTypeName, Set<String> nodeNames) {
        assertEquals(1,	nodeNames.size());
	String expectedNodeName = nodeNames.iterator().next();
        List routeNodes = KEWServiceLocator.getRouteNodeService().getFlattenedNodes(KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName), true);
        for (Iterator iterator = routeNodes.iterator(); iterator.hasNext();) {
	    RouteNode node = (RouteNode) iterator.next();
	    if (expectedNodeName.equals(node.getRouteNodeName())) {
		return node.getRouteNodeName();
	    }
	}
        return null;
    }

    @Test public void testGetNamedDocSearches() throws Exception {
        List namedSearches = docSearchService.getNamedSearches(getPrincipalId("bmcgough"));
        assertNotNull(namedSearches);
    }

    private static int getDifferenceInDays(DateTime compareDate) {
        return Days.daysBetween(compareDate, new DateTime()).getDays();
    }

    /**
     * Tests the usage of wildcards on the regular document search attributes.
     * @throws Exception
     */

    @Test public void testDocSearch_WildcardsOnRegularAttributes() throws Exception {
    	// TODO: Add some wildcard testing for the document type attribute once wildcards are usable with it.

    	// Route some test documents.
    	String docTypeName = "SearchDocType";
    	String[] principalNames = {"bmcgough", "quickstart", "rkirkend"};
    	String[] titles = {"The New Doc", "Document Number 2", "Some New Document"};
    	String[] docIds = new String[titles.length];
    	String[] appDocIds = {"6543", "5432", "4321"};
    	String[] approverNames = {null, "jhopf", null};
    	for (int i = 0; i < titles.length; i++) {
        	WorkflowDocument workflowDocument = WorkflowDocumentFactory.createDocument(getPrincipalId(principalNames[i]), docTypeName);
        	workflowDocument.setTitle(titles[i]);
        	workflowDocument.setApplicationDocumentId(appDocIds[i]);
        	workflowDocument.route("routing this document.");
        	docIds[i] = workflowDocument.getDocumentId();
        	if (approverNames[i] != null) {
        		workflowDocument.switchPrincipal(getPrincipalId(approverNames[i]));
        		workflowDocument.approve("approving this document.");
        	}
    	}
        String principalId = getPrincipalId("bmcgough");
        DocumentLookupCriteria.Builder criteria = null;
        DocumentLookupResults results = null;

        /**
         * BEGIN - commenting out until we can resolve issues with person service not returning proper persons based on wildcards and various things
         */
        // Test the wildcards on the initiator attribute.
//        String[] searchStrings = {"!quickstart", "!rkirkend!bmcgough", "!quickstart&&!rkirkend", "!admin", "user1", "quickstart|bmcgough",
//        		"admin|rkirkend", ">bmcgough", ">=rkirkend", "<bmcgough", "<=quickstart", ">bmcgough&&<=rkirkend", "<rkirkend&&!bmcgough",
//        		"?mc?oug?", "*t", "*i?k*", "*", "!quick*", "!b???????!?kirk???", "!*g*&&!*k*", ">bmc?ough", "<=quick*", "quickstart..rkirkend"};
//        int[] expectedResults = {2, 1, 1, 3, 0, 2, 1, 2, 1, 0, 2, 2, 1, 1, 1, 2, 3, 2, 1, 0, 2, 1, 2/*1*/};
//        for (int i = 0; i < searchStrings.length; i++) {
//        	criteria = DocumentLookupCriteria.Builder.create();
//        	criteria.setInitiatorPrincipalName(searchStrings[i]);
//        	results = docSearchService.lookupDocuments(principalId, criteria.build());
//        	assertEquals("Initiator search at index " + i + " retrieved the wrong number of documents.", expectedResults[i], results.getLookupResults().size());
//        }

        // Test the wildcards on the approver attribute.
//        searchStrings = new String[] {"jhopf","!jhopf", ">jhopf", "<jjopf", ">=quickstart", "<=jhopf", "jhope..jhopg", "?hopf", "*i*", "!*f", "j*"};
//        expectedResults = new int[] {1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 1};
//        for (int i = 0; i < searchStrings.length; i++) {
//        	criteria = DocumentLookupCriteria.Builder.create();
//        	criteria.setApproverPrincipalName(searchStrings[i]);
//        	results = docSearchService.lookupDocuments(principalId, criteria.build());
//        	assertEquals("Approver search at index " + i + " retrieved the wrong number of documents.", expectedResults[i], results.getLookupResults().size());
//        }

        // Test the wildcards on the viewer attribute.
//        searchStrings = new String[] {"jhopf","!jhopf", ">jhopf", "<jjopf", ">=quickstart", "<=jhopf", "jhope..jhopg", "?hopf", "*i*", "!*f", "j*"};
//        expectedResults = new int[] {3, 0, 0, 3, 0, 3, 3, 3, 0, 0, 3};
//        for (int i = 0; i < searchStrings.length; i++) {
//        	criteria = DocumentLookupCriteria.Builder.create();
//        	criteria.setViewerPrincipalName(searchStrings[i]);
//        	results = docSearchService.lookupDocuments(principalId, criteria.build());
//        	if(expectedResults[i] !=  results.getLookupResults().size()){
//        		assertEquals("Viewer search at index " + i + " retrieved the wrong number of documents.", expectedResults[i], results.getLookupResults().size());
//        	}
//        }

        /**
         * END
         */

        // Test the wildcards on the document/notification ID attribute. The string wildcards should work, since the doc ID is not a string.
        String[] searchStrings = new String[] {"!"+docIds[0], docIds[1]+"|"+docIds[2], "<="+docIds[1], ">="+docIds[2], "<"+docIds[0]+"&&>"+docIds[2],
        		">"+docIds[1], "<"+docIds[2]+"&&!"+docIds[0], docIds[0]+".."+docIds[2], "?"+docIds[1]+"*", "?"+docIds[1].substring(1)+"*", "?9*7"};
        int[] expectedResults = new int[] {2, 2, 2, 1, 0, 1, 1, 3, 0, 1, 0};
        for (int i = 0; i < searchStrings.length; i++) {
        	criteria = DocumentLookupCriteria.Builder.create();
        	criteria.setDocumentId(searchStrings[i]);
        	results = docSearchService.lookupDocuments(principalId, criteria.build());
        	assertEquals("Doc ID search at index " + i + " retrieved the wrong number of documents.", expectedResults[i], results.getLookupResults().size());
        }

        // Test the wildcards on the application document/notification ID attribute. The string wildcards should work, since the app doc ID is a string.
        searchStrings = new String[] {"6543", "5432|4321", ">4321", "<=5432", ">=6543", "<3210", "!3210", "!5432", "!4321!5432", ">4321&&!6543",
        		"*5?3*", "*", "?3?1", "!*43*", "!???2", ">43*1", "<=5432&&!?32?", "5432..6543"};
        expectedResults = new int[] {1, 2, 2, 2, 1, 0, 3, 2, 1, 1, 2, 3, 1, 0, 2, 3, 1, 2/*1*/};
        for (int i = 0; i < searchStrings.length; i++) {
        	criteria = DocumentLookupCriteria.Builder.create();
        	criteria.setApplicationDocumentId(searchStrings[i]);
        	results = docSearchService.lookupDocuments(principalId, criteria.build());
        	if(expectedResults[i] !=  results.getLookupResults().size()){
        		assertEquals("App doc ID search at index " + i + " retrieved the wrong number of documents.", expectedResults[i], results.getLookupResults().size());
        	}
        }

        // Test the wildcards on the title attribute.
        searchStrings = new String[] {"Some New Document", "Document Number 2|The New Doc", "!The New Doc", "!Some New Document!Document Number 2",
        		"!The New Doc&&!Some New Document", ">Document Number 2", "<=Some New Document", ">=The New Doc&&<Some New Document", ">A New Doc",
        		"<Some New Document|The New Doc", ">=Document Number 2&&!Some New Document", "*Docu??nt*", "*New*", "The ??? Doc", "*Doc*", "*Number*",
        		"Some New Document..The New Doc", "Document..The", "*New*&&!*Some*", "!The ??? Doc|!*New*"};
        expectedResults = new int[] {1, 2, 2, 1, 1, 2, 2, 0, 3, 2, 2, 2, 2, 1, 3, 1, 2/*1*/, 2, 1, 2};
        for (int i = 0; i < searchStrings.length; i++) {
        	criteria = DocumentLookupCriteria.Builder.create();
        	criteria.setTitle(searchStrings[i]);
        	results = docSearchService.lookupDocuments(principalId, criteria.build());
        	if(expectedResults[i] !=  results.getLookupResults().size()){
        		assertEquals("Doc title search at index " + i + " retrieved the wrong number of documents.", expectedResults[i], results.getLookupResults().size());
        	}
        }

    }
    
    private String getPrincipalId(String principalName) {
    	return KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(principalName).getPrincipalId();
    }
}
