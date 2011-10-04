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
package org.kuali.rice.kew.actiontaken.dao.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.actiontaken.dao.ActionTakenDAO;
import org.springmodules.orm.ojb.support.PersistenceBrokerDaoSupport;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;


/**
 * OJB implementation of the {@link ActionTakenDAO}.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ActionTakenDAOOjbImpl extends PersistenceBrokerDaoSupport implements ActionTakenDAO {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ActionTakenDAOOjbImpl.class);

    public ActionTakenValue load(String id) {
        LOG.debug("Loading Action Taken for the given id " + id);
        Criteria crit = new Criteria();
        crit.addEqualTo("actionTakenId", id);
        return (ActionTakenValue) this.getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(ActionTakenValue.class, crit));
    }

    public void deleteActionTaken(ActionTakenValue actionTaken) {
        LOG.debug("deleting ActionTaken " + actionTaken.getActionTakenId());
        this.getPersistenceBrokerTemplate().delete(actionTaken);
    }

    public ActionTakenValue findByActionTakenId(String actionTakenId) {
        LOG.debug("finding Action Taken by actionTakenId " + actionTakenId);
        Criteria crit = new Criteria();
        crit.addEqualTo("actionTakenId", actionTakenId);
        crit.addEqualTo("currentIndicator", Boolean.TRUE);
        return (ActionTakenValue) this.getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(ActionTakenValue.class, crit));
    }

    public Collection<ActionTakenValue> findByDocIdAndAction(String documentId, String action) {
        LOG.debug("finding Action Taken by documentId " + documentId + " and action " + action);
        Criteria crit = new Criteria();
        crit.addEqualTo("documentId", documentId);
        crit.addEqualTo("actionTaken", action);
        crit.addEqualTo("currentIndicator", Boolean.TRUE);
        return (Collection<ActionTakenValue>) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ActionTakenValue.class, crit));
    }

    public Collection<ActionTakenValue> findByDocumentId(String documentId) {
        LOG.debug("finding Action Takens by documentId " + documentId);
        Criteria crit = new Criteria();
        crit.addEqualTo("documentId", documentId);
        crit.addEqualTo("currentIndicator", Boolean.TRUE);
        return (Collection<ActionTakenValue>) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ActionTakenValue.class, crit));
    }

    public List<ActionTakenValue> findByDocumentIdWorkflowId(String documentId, String principalId) {
        LOG.debug("finding Action Takens by documentId " + documentId + " and principalId" + principalId);
        Criteria crit = new Criteria();
        crit.addEqualTo("documentId", documentId);
        crit.addEqualTo("principalId", principalId);
        crit.addEqualTo("currentIndicator", Boolean.TRUE);
        return (List<ActionTakenValue>) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ActionTakenValue.class, crit));
    }

    public List findByDocumentIdIgnoreCurrentInd(String documentId) {
        LOG.debug("finding ActionsTaken ignoring currentInd by documentId:" + documentId);
        Criteria crit = new Criteria();
        crit.addEqualTo("documentId", documentId);
        return (List) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(ActionTakenValue.class, crit));
    }

    public void saveActionTaken(ActionTakenValue actionTaken) {
        LOG.debug("saving ActionTaken");
        checkNull(actionTaken.getDocumentId(), "Document ID");
        checkNull(actionTaken.getActionTaken(), "action taken code");
        checkNull(actionTaken.getDocVersion(), "doc version");
        checkNull(actionTaken.getPrincipal(), "user principalId");

        if (actionTaken.getActionDate() == null) {
            actionTaken.setActionDate(new Timestamp(System.currentTimeMillis()));
        }
        if (actionTaken.getCurrentIndicator() == null) {
            actionTaken.setCurrentIndicator(Boolean.TRUE);
        }
        LOG.debug("saving ActionTaken: routeHeader " + actionTaken.getDocumentId() +
                ", actionTaken " + actionTaken.getActionTaken() + ", principalId " + actionTaken.getPrincipalId());
        this.getPersistenceBrokerTemplate().store(actionTaken);
    }

    //TODO perhaps runtime isn't the best here, maybe a dao runtime exception
    private void checkNull(Object value, String valueName) throws RuntimeException {
        if (value == null) {
            throw new RuntimeException("Null value for " + valueName);
        }
    }

    public void deleteByDocumentId(String documentId){
	    Criteria crit = new Criteria();
	    crit.addEqualTo("documentId", documentId);
	    this.getPersistenceBrokerTemplate().deleteByQuery(new QueryByCriteria(ActionRequestValue.class, crit));
    }

    public boolean hasUserTakenAction(String principalId, String documentId) {
    	Criteria crit = new Criteria();
	    crit.addEqualTo("documentId", documentId);
	    crit.addEqualTo("principalId", principalId);
	    crit.addEqualTo("currentIndicator", Boolean.TRUE);
        int count = getPersistenceBrokerTemplate().getCount(new QueryByCriteria(ActionTakenValue.class, crit));
        return count > 0;
    }

}
