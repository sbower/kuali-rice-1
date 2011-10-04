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
package org.kuali.rice.kew.actionlist;

import org.junit.Test;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionlist.service.ActionListService;
import org.kuali.rice.kew.actionrequest.Recipient;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.WorkflowDocumentFactory;
import org.kuali.rice.kew.api.action.DelegationType;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.test.KEWTestCase;
import org.kuali.rice.kew.test.TestUtilities;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.WebFriendlyRecipient;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ActionListTest extends KEWTestCase {

    private static final String[] AUTHENTICATION_IDS = { "ewestfal", "rkirkend", "jhopf", "bmcgough" };
    private static final String[] WORKGROUP_IDS = { "1", "2", "3", "4" };

    private DocumentRouteHeaderValue routeHeader1;
    private DocumentRouteHeaderValue routeHeader2;
    private DocumentRouteHeaderValue routeHeader3;
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();

    protected void loadTestData() throws Exception {
        loadXmlFile("ActionListConfig.xml");
    }

    private void setUpOldSchool() throws Exception {
        super.setUpAfterDataLoad();
        List<ActionItem> actionItems1 = new ArrayList<ActionItem>();
        List<ActionItem> actionItems2 = new ArrayList<ActionItem>();
        List<ActionItem> actionItems3 = new ArrayList<ActionItem>();
        routeHeader1 = generateDocRouteHeader();
        routeHeader2 = generateDocRouteHeader();
        routeHeader3 = generateDocRouteHeader();
        
        getRouteHeaderService().saveRouteHeader(routeHeader1);
        getRouteHeaderService().saveRouteHeader(routeHeader2);
        getRouteHeaderService().saveRouteHeader(routeHeader3);

        for (int i = 0; i < AUTHENTICATION_IDS.length; i++) {
            actionItems1.add(generateActionItem(routeHeader1, "K", AUTHENTICATION_IDS[i], null));
            actionItems2.add(generateActionItem(routeHeader2, "A", AUTHENTICATION_IDS[i], null));
        }
        for (int i = 0; i < WORKGROUP_IDS.length; i++) {
            actionItems3.add(generateActionItem(routeHeader3, "A", AUTHENTICATION_IDS[i], WORKGROUP_IDS[i]));
        }
        
        actionItems.addAll(actionItems1);
        actionItems.addAll(actionItems2);
        actionItems.addAll(actionItems3);
        for (Iterator<ActionItem> iterator = actionItems.iterator(); iterator.hasNext();) {
            ActionItem actionItem = iterator.next();
            getActionListService().saveActionItem(actionItem);
        }
    }

    @Test public void testRouteHeaderDelete() throws Exception {
    	setUpOldSchool();
        Collection<ActionItem> actionItems = getActionListService().findByDocumentId(routeHeader1.getDocumentId());
        assertEquals("Route header " + routeHeader1.getDocumentId() + " should have action items.", AUTHENTICATION_IDS.length, actionItems.size());
        getActionListService().deleteByDocumentId(routeHeader1.getDocumentId());
        actionItems = getActionListService().findByDocumentId(routeHeader1.getDocumentId());
        assertEquals("There should be no remaining action items for route header " + routeHeader1.getDocumentId(), 0, actionItems.size());
        actionItems = getActionListService().findByDocumentId(routeHeader2.getDocumentId());
        assertEquals("Route header " + routeHeader2.getDocumentId() + " should have action items.", AUTHENTICATION_IDS.length, actionItems.size());
    }

    @Test public void testActionListCount() throws Exception {
    	setUpOldSchool();
        TransactionTemplate transactionTemplate = getTransactionTemplate();
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
            	return TestUtilities.getJdbcTemplate().execute(new StatementCallback() {
            		public Object doInStatement(Statement stmt) {
                        try {
                            Connection conn = stmt.getConnection();
                            PreparedStatement ps = conn.prepareStatement("select distinct PRNCPL_ID from krew_actn_itm_t");
                            ResultSet rs = ps.executeQuery();
                            int emplIdCnt = 0;
                            int loopCnt = 0;
                            //do first 5 for time sake
                            while (rs.next() && ++loopCnt < 6) {
                                String workflowId = rs.getString(1);
                                PreparedStatement ps1 = conn.prepareStatement("select count(*) from krew_actn_itm_t where PRNCPL_ID = ?");
                                ps1.setString(1, workflowId);
                                ResultSet rsWorkflowIdCnt = ps1.executeQuery();
                                if (rsWorkflowIdCnt.next()) {
                                    emplIdCnt = rsWorkflowIdCnt.getInt(1);
                                } else {
                                    throw new Exception("WorkflowId " + workflowId + " didn't return a count.  Test SQL invalid.");
                                }
                                Collection<ActionItem> actionList = getActionListService().findByPrincipalId(workflowId);
                                assertEquals("ActionItemService returned incorrect number of ActionItems for user " + workflowId + " ActionList", emplIdCnt, actionList.size());
                                ps1.close();
                                rsWorkflowIdCnt.close();
                            }
                            rs.close();
                            ps.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return null;
                    }
                });
            }
        });
    }

    /**
     * Tests that the user's secondary action list works appropriately.  Also checks that if a user
     * is their own secondary delegate, their request shows up in their main action list rather than
     * their secondary list.
     */
    @Test public void testSecondaryActionList() throws Exception {
    	WorkflowDocument document = WorkflowDocumentFactory.createDocument(getPrincipalIdForName("jhopf"), "ActionListDocumentType");
    	document.route("");

    	// at this point the document should be routed to the following people
    	// 1) approve to bmcgough with primary delegate of rkirkend and secondary delegates of ewestfal and bmcgough (himself)
    	// 2) approve to jitrue with a secondary delegate of jitrue (himself)
    	// 3) acknowledge to user1
    	// 4) approve to NonSIT workgroup (which should include user1)

    	// now lets verify that everyone's action lists look correct

    	String bmcgoughPrincipalId = getPrincipalIdForName("bmcgough");
    	String rkirkendPrincipalId = getPrincipalIdForName("rkirkend");
    	String ewestfalPrincipalId = getPrincipalIdForName("ewestfal");
    	String jitruePrincipalId = getPrincipalIdForName("jitrue");
    	String user1PrincipalId = getPrincipalIdForName("user1");

    	Group NonSIT = KimApiServiceLocator.getGroupService().getGroupByNameAndNamespaceCode(
                KimConstants.KIM_GROUP_WORKFLOW_NAMESPACE_CODE, "NonSIT");

    	ActionListFilter noFilter = new ActionListFilter();
    	ActionListFilter excludeSecondaryFilter = new ActionListFilter();
    	excludeSecondaryFilter.setDelegationType(DelegationType.SECONDARY.getCode());
    	excludeSecondaryFilter.setExcludeDelegationType(true);
    	ActionListFilter secondaryFilter = new ActionListFilter();
    	secondaryFilter.setDelegationType(DelegationType.SECONDARY.getCode());
    	Collection<ActionItem> actionItems = null;
    	ActionItem actionItem = null;

    	actionItems = getActionListService().getActionList(bmcgoughPrincipalId, excludeSecondaryFilter);
    	assertEquals("bmcgough should have 0 items in his primary action list.", 0, actionItems.size());
    	actionItems = getActionListService().getActionList(bmcgoughPrincipalId, secondaryFilter);
    	assertEquals("bmcgough should have 1 item in his secondary action list.", 1, actionItems.size());
        actionItem = actionItems.iterator().next();
        assertEquals("Should be an approve request.", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
        assertEquals("Should be a secondary delegation request.", DelegationType.SECONDARY.getCode(), actionItem.getDelegationType());
    	actionItem = actionItems.iterator().next();
    	assertEquals("Should be an approve request.", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
    	assertEquals("Should be a secondary delegation request.", DelegationType.SECONDARY.getCode(), actionItem.getDelegationType());
    	actionItems = getActionListService().getActionList(bmcgoughPrincipalId, noFilter);
    	assertEquals("bmcgough should have 1 item in his entire action list.", 1, actionItems.size());

    	actionItems = getActionListService().getActionList(rkirkendPrincipalId, excludeSecondaryFilter);
    	assertEquals("bmcgough should have 1 item in his primary action list.", 1, actionItems.size());

    	actionItems = getActionListService().getActionList(jitruePrincipalId, excludeSecondaryFilter);
    	assertEquals("jitrue should have 1 item in his primary action list.", 1, actionItems.size());

    	actionItems = getActionListService().getActionList(ewestfalPrincipalId, secondaryFilter);
    	assertEquals("ewestfal should have 1 item in his secondary action list.", 1, actionItems.size());

    	// check that user1's approve comes out as their action item from the action list
    	actionItems = getActionListService().getActionList(user1PrincipalId, noFilter);
    	assertEquals("user1 should have 1 item in his primary action list.", 1, actionItems.size());
    	actionItem = actionItems.iterator().next();
    	assertEquals("Should be an approve request.", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
    	assertEquals("Should be to a workgroup.", NonSIT.getId(), actionItem.getGroupId());
    	// check that user1 acknowledge shows up when filtering
    	ActionListFilter ackFilter = new ActionListFilter();
    	ackFilter.setActionRequestCd(KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ);
    	actionItems = getActionListService().getActionList(user1PrincipalId, ackFilter);
    	assertEquals("user1 should have 1 item in his primary action list.", 1, actionItems.size());
    	actionItem = (ActionItem)actionItems.iterator().next();
    	assertEquals("Should be an acknowledge request.", KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, actionItem.getActionRequestCd());
    	assertNull("Should not be to a workgroup.", actionItem.getGroupId());

    	// all members of NonSIT should have a single primary Approve Request
        List<String> memberPrincipalIds = KimApiServiceLocator.getGroupService().getMemberPrincipalIds(NonSIT.getId());
        for (String memberPrincipalId : memberPrincipalIds)
        {
            //will want to convert to Kim Principal
            actionItems = getActionListService().getActionList(memberPrincipalId, excludeSecondaryFilter);
            assertEquals("Workgroup Member " + memberPrincipalId + " should have 1 action item.", 1, actionItems.size());
            actionItem = (ActionItem) actionItems.iterator().next();
            assertEquals("Should be an approve request.", KEWConstants.ACTION_REQUEST_APPROVE_REQ, actionItem.getActionRequestCd());
            assertEquals("Should be to a workgroup.", NonSIT.getId(), actionItem.getGroupId());
        }

        document = WorkflowDocumentFactory.createDocument(getPrincipalIdForName("jhopf"), "ActionListDocumentType_PrimaryDelegate");
        document.route("");
        document = WorkflowDocumentFactory.createDocument(getPrincipalIdForName("jhopf"), "ActionListDocumentType_PrimaryDelegate2");
        document.route("");

        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, excludeSecondaryFilter);
        assertEquals("bmcgough should have 0 items in his primary action list.", 0, actionItems.size());
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, secondaryFilter);
        assertEquals("bmcgough should have 1 item in his secondary action list.", 3, actionItems.size());
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, new ActionListFilter());
        assertEquals("bmcgough should have 1 item in his entire action list.", 3, actionItems.size());

        ActionListFilter filter = null;
        // test a standard filter with no delegations
        filter = new ActionListFilter();
        filter.setDelegatorId(KEWConstants.DELEGATION_DEFAULT);
        filter.setPrimaryDelegateId(KEWConstants.PRIMARY_DELEGATION_DEFAULT);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough should have 0 items in his entire action list.", 0, actionItems.size());

        // test secondary delegation with all selected returns all
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.SECONDARY.getCode());
        filter.setDelegatorId(KEWConstants.ALL_CODE);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough has incorrect action list item count.", 3, actionItems.size());

        // test that primary delegation with none selected returns none
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.SECONDARY.getCode());
        filter.setDelegatorId(KEWConstants.DELEGATION_DEFAULT);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough has incorrect action list item count.", 0, actionItems.size());

        // test that primary delegation with single user ids works corectly
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.SECONDARY.getCode());
        filter.setDelegatorId(bmcgoughPrincipalId);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough has incorrect action list item count.", 3, actionItems.size());
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.SECONDARY.getCode());
        filter.setDelegatorId(bmcgoughPrincipalId);
        actionItems = getActionListService().getActionList(ewestfalPrincipalId, filter);
        assertEquals("ewestfal has incorrect action list item count.", 3, actionItems.size());
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.SECONDARY.getCode());
        filter.setDelegatorId(jitruePrincipalId);
        actionItems = getActionListService().getActionList(jitruePrincipalId, filter);
        assertEquals("jitrue has incorrect action list item count.", 3, actionItems.size());
    }

    /**
     * Tests that the user's secondary action list works appropriately.  Also checks that if a user
     * is their own secondary delegate, their request shows up in their main action list rather than
     * their secondary list.
     */
    @Test public void testPrimaryDelegationActionList() throws Exception {
    	WorkflowDocument document = WorkflowDocumentFactory.createDocument(getPrincipalIdForName("jhopf"), "ActionListDocumentType");
    	document.route("");

    	// at this point the document should be routed to the following people
    	// 1) approve to bmcgough with primary delegate of rkirkend and secondary delegates of ewestfal and bmcgough (himself)
    	// 2) approve to jitrue with a secondary delegate of jitrue (himself)
    	// 3) acknowledge to user1
    	// 4) approve to NonSIT workgroup (which should include user1)

    	// now lets verify that everyone's action lists look correct

    	String bmcgoughPrincipalId = getPrincipalIdForName("bmcgough");
    	String rkirkendPrincipalId = getPrincipalIdForName("rkirkend");
    	String delyeaPrincipalId = getPrincipalIdForName("delyea");
    	String temayPrincipalId = getPrincipalIdForName("temay");
    	String jhopfPrincipalId = getPrincipalIdForName("jhopf");

    	ActionListFilter showPrimaryFilter = new ActionListFilter();
    	showPrimaryFilter.setDelegationType(DelegationType.PRIMARY.getCode());
    	Collection<ActionItem> actionItems = null;
    	ActionItem actionItem = null;

    	// make sure showing primary delegations show primary delegated action items
    	actionItems = getActionListService().getActionList(bmcgoughPrincipalId, showPrimaryFilter);
    	assertEquals("bmcgough should have 1 item in his primary delegation action list.", 1, actionItems.size());
    	actionItems = getActionListService().getActionList(bmcgoughPrincipalId, new ActionListFilter());
    	assertEquals("bmcgough should have 1 item in his entire action list.", 1, actionItems.size());

    	document = WorkflowDocumentFactory.createDocument(jhopfPrincipalId, "ActionListDocumentType_PrimaryDelegate");
    	document.route("");
    	document = WorkflowDocumentFactory.createDocument(jhopfPrincipalId, "ActionListDocumentType_PrimaryDelegate2");
        document.route("");

        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, showPrimaryFilter);
        // should be 6 total action items but 3 distinct doc ids
        assertEquals("bmcgough should have 1 item in his primary delegation action list.", 3, actionItems.size());

        ActionListFilter filter = null;

        // test a standard filter with no delegations
        filter = new ActionListFilter();
        filter.setDelegatorId(KEWConstants.DELEGATION_DEFAULT);
        filter.setPrimaryDelegateId(KEWConstants.PRIMARY_DELEGATION_DEFAULT);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough should have 0 items in his entire action list.", 0, actionItems.size());

        // test primary delegation with all selected returns all
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.PRIMARY.getCode());
        filter.setPrimaryDelegateId(KEWConstants.ALL_CODE);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough should have 1 item in his entire action list.", 3, actionItems.size());

        // test that primary delegation with none selected returns none
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.PRIMARY.getCode());
        filter.setPrimaryDelegateId(KEWConstants.PRIMARY_DELEGATION_DEFAULT);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough should have 1 item in his entire action list.", 0, actionItems.size());

        // test that primary delegation with single user ids works corectly
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.PRIMARY.getCode());
        filter.setPrimaryDelegateId(rkirkendPrincipalId);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough should have 3 items in his entire action list.", 3, actionItems.size());
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.PRIMARY.getCode());
        filter.setPrimaryDelegateId(delyeaPrincipalId);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough should have 2 items in his entire action list.", 2, actionItems.size());
        filter = new ActionListFilter();
        filter.setDelegationType(DelegationType.PRIMARY.getCode());
        filter.setPrimaryDelegateId(temayPrincipalId);
        actionItems = getActionListService().getActionList(bmcgoughPrincipalId, filter);
        assertEquals("bmcgough should have 1 item in his entire action list.", 1, actionItems.size());

    }

    /**
     * Tests that the retrieval of primary and secondary delegation users is working correctly
     */
    @Test public void testGettingDelegationUsers() throws Exception {

        Person jhopf = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("jhopf");
        Person bmcgough = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("bmcgough");
    	WorkflowDocument document = WorkflowDocumentFactory.createDocument(getPrincipalIdForName("jhopf"), "ActionListDocumentType");
    	document.route("");
        document = WorkflowDocumentFactory.createDocument(getPrincipalIdForName("jhopf"), "ActionListDocumentType_PrimaryDelegate");
        document.route("");
        document = WorkflowDocumentFactory.createDocument(getPrincipalIdForName("jhopf"), "ActionListDocumentType_PrimaryDelegate2");
        document.route("");

        Collection<Recipient> recipients = getActionListService().findUserPrimaryDelegations(jhopf.getPrincipalId());
        assertEquals("Wrong size of users who were delegated to via Primary Delegation", 0, recipients.size());
    	recipients = getActionListService().findUserPrimaryDelegations(bmcgough.getPrincipalId());
    	assertEquals("Wrong size of users who were delegated to via Primary Delegation", 3, recipients.size());
    	String user1 = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("rkirkend").getPrincipalId();
    	String user2 = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("temay").getPrincipalId();
    	String user3 = KimApiServiceLocator.getPersonService().getPersonByPrincipalName("delyea").getPrincipalId();

    	boolean foundUser1 = false;
        boolean foundUser2 = false;
        boolean foundUser3 = false;
    	for (Recipient recipient : recipients) {
            if (user1.equals(((WebFriendlyRecipient)recipient).getRecipientId())) {
                foundUser1 = true;
            } else if (user2.equals(((WebFriendlyRecipient)recipient).getRecipientId())) {
                foundUser2 = true;
            } else if (user3.equals(((WebFriendlyRecipient)recipient).getRecipientId())) {
                foundUser3 = true;
            } else {
                fail("Found invalid recipient in list with display name '" + ((WebFriendlyRecipient)recipient).getDisplayName() + "'");
            }
        }
    	assertTrue("Should have found user " + user1, foundUser1);
        assertTrue("Should have found user " + user2, foundUser2);
        assertTrue("Should have found user " + user3, foundUser3);

    	recipients = getActionListService().findUserSecondaryDelegators(bmcgough.getPrincipalId());
    	assertEquals("Wrong size of users who were have delegated to given user via Secondary Delegation", 1, recipients.size());
    	WebFriendlyRecipient recipient = (WebFriendlyRecipient)recipients.iterator().next();
    	assertEquals("Wrong employee id of primary delegate", "bmcgough", getPrincipalNameForId(recipient.getRecipientId()));
    }

    private DocumentRouteHeaderValue generateDocRouteHeader() {
        DocumentRouteHeaderValue routeHeader = new DocumentRouteHeaderValue();
        routeHeader.setAppDocId("Test");
        routeHeader.setApprovedDate(null);
        routeHeader.setCreateDate(new Timestamp(new Date().getTime()));
        routeHeader.setDocContent("test");
        routeHeader.setDocRouteLevel(1);
        routeHeader.setDocRouteStatus(KEWConstants.ROUTE_HEADER_ENROUTE_CD);
        routeHeader.setDocTitle("Test");
        routeHeader.setDocumentTypeId("1");
        routeHeader.setDocVersion(KewApiConstants.DocumentContentVersions.CURRENT);
        routeHeader.setRouteStatusDate(new Timestamp(new Date().getTime()));
        routeHeader.setStatusModDate(new Timestamp(new Date().getTime()));
        routeHeader.setInitiatorWorkflowId("someone");
        return routeHeader;
    }

    private ActionItem generateActionItem(DocumentRouteHeaderValue routeHeader, String actionRequested, String authenticationId, String groupId) {
        ActionItem actionItem = new ActionItem();
        actionItem.setActionRequestCd(actionRequested);
        actionItem.setActionRequestId("1");
        actionItem.setPrincipalId(getPrincipalIdForName(authenticationId));
        actionItem.setDocumentId(routeHeader.getDocumentId());
        actionItem.setDateAssigned(new Timestamp(new Date().getTime()));
        actionItem.setDocHandlerURL("Unit testing");
        actionItem.setDocLabel("unit testing");
        actionItem.setDocTitle(routeHeader.getDocTitle());
        actionItem.setDocName("docname");
        actionItem.setGroupId(groupId);
//        actionItem.setResponsibilityId(new Long(-1));
        return actionItem;
    }

    private ActionListService getActionListService() {
        return KEWServiceLocator.getActionListService();
    }

    private RouteHeaderService getRouteHeaderService() {
        return KEWServiceLocator.getRouteHeaderService();
    }
}
