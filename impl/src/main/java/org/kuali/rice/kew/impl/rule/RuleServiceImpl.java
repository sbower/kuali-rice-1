/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.impl.rule;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.criteria.CriteriaLookupService;
import org.kuali.rice.core.api.criteria.GenericQueryResults;
import org.kuali.rice.core.api.criteria.LookupCustomizer;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.exception.RiceIllegalStateException;
import org.kuali.rice.kew.api.rule.Rule;
import org.kuali.rice.kew.api.rule.RuleDelegation;
import org.kuali.rice.kew.api.rule.RuleQueryResults;
import org.kuali.rice.kew.api.rule.RuleReportCriteria;
import org.kuali.rice.kew.api.rule.RuleResponsibility;
import org.kuali.rice.kew.api.rule.RuleService;
import org.kuali.rice.kew.api.rule.RuleTemplate;
import org.kuali.rice.kew.api.rule.RuleTemplateQueryResults;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.RuleDelegationBo;
import org.kuali.rice.kew.rule.RuleResponsibilityBo;
import org.kuali.rice.kew.rule.bo.RuleTemplateBo;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.impl.common.attribute.AttributeTransform;
import org.kuali.rice.krad.service.BusinessObjectService;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class RuleServiceImpl implements RuleService {
    private static final Logger LOG = Logger.getLogger(RuleServiceImpl.class);
    //private RuleDAO ruleDAO;
    private BusinessObjectService businessObjectService;
    private CriteriaLookupService criteriaLookupService;

    @Override
    public Rule getRule(String id) throws RiceIllegalArgumentException, RiceIllegalStateException{
        incomingParamCheck("id", id);
        Map<String, String> criteria = Collections.singletonMap("id", id);
        RuleBaseValues rbv = this.businessObjectService.findByPrimaryKey(RuleBaseValues.class, criteria);
        if (rbv == null) {
            throw new RiceIllegalStateException("Rule with specified id: " + id + " does not exist");
        }
        return RuleBaseValues.to(rbv);
    }

    @Override
    public Rule getRuleByName(String name) {
        incomingParamCheck("name", name);
        Map<String, Object> criteria = new HashMap<String, Object>(2);
        criteria.put("name", name);
        criteria.put("currentInd", Boolean.TRUE);
        RuleBaseValues rbv = this.businessObjectService.findByPrimaryKey(RuleBaseValues.class, criteria);
        if (rbv == null) {
            throw new RiceIllegalStateException("Rule with specified name: " + name + " does not exist");
        }
        return RuleBaseValues.to(rbv);
    }

    @Override
    public List<Rule> getRulesByTemplateId(
            @WebParam(name = "templateId") String templateId) throws RiceIllegalArgumentException {
        incomingParamCheck("templateId", templateId);
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("ruleTemplateId", templateId);
        criteria.put("currentInd", Boolean.TRUE);
        Collection<RuleBaseValues> ruleValues = this.businessObjectService.findMatching(RuleBaseValues.class, criteria);

        final List<Rule> rules = new ArrayList<Rule>();
        for (RuleBaseValues bo : ruleValues) {
            rules.add(Rule.Builder.create(bo).build());
        }
        return rules;
    }

    @Override
    public RuleQueryResults findRules(QueryByCriteria queryByCriteria) {
        if (queryByCriteria == null) {
            throw new RiceIllegalArgumentException("queryByCriteria is null");
        }

        LookupCustomizer.Builder<RuleBaseValues> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        GenericQueryResults<RuleBaseValues> results = criteriaLookupService.lookup(RuleBaseValues.class, queryByCriteria, lc.build());

        RuleQueryResults.Builder builder = RuleQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<Rule.Builder> ims = new ArrayList<Rule.Builder>();
        for (RuleBaseValues bo : results.getResults()) {
            ims.add(Rule.Builder.create(RuleBaseValues.to(bo)));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public List<Rule> ruleReport(RuleReportCriteria ruleReportCriteria) {
        incomingParamCheck(ruleReportCriteria, "ruleReportCriteria");
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Executing rule report [responsibleUser=" + ruleReportCriteria.getResponsiblePrincipalId() + ", responsibleWorkgroup=" +
                    ruleReportCriteria.getResponsibleGroupId() + "]");
        }
        Collection<RuleBaseValues> rulesFound = KEWServiceLocator.getRuleService().searchByTemplate(
                ruleReportCriteria.getDocumentTypeName(), ruleReportCriteria.getRuleTemplateName(),
                ruleReportCriteria.getRuleDescription(), ruleReportCriteria.getResponsibleGroupId(),
                ruleReportCriteria.getResponsiblePrincipalId(), Boolean.valueOf(ruleReportCriteria.isConsiderGroupMembership()),
                Boolean.valueOf(ruleReportCriteria.isIncludeDelegations()), Boolean.valueOf(ruleReportCriteria.isActive()), ruleReportCriteria.getRuleExtensions(),
                ruleReportCriteria.getActionRequestCodes());
        List<org.kuali.rice.kew.api.rule.Rule> returnableRules = new ArrayList<Rule>(rulesFound.size());
        for (RuleBaseValues rule : rulesFound) {
            returnableRules.add(RuleBaseValues.to(rule));
        }
        return returnableRules;
    }

    @Override
    public RuleTemplate getRuleTemplate(@WebParam(name = "id") String id) {
        incomingParamCheck("id", id);
        Map<String, String> criteria = Collections.singletonMap("id", id);
        RuleTemplateBo template = this.businessObjectService.findByPrimaryKey(RuleTemplateBo.class, criteria);
        if (template == null) {
            throw new RiceIllegalStateException("RuleTemplate with specified id: " + id + " does not exist");
        }
        return RuleTemplateBo.to(template);
    }

    @Override
    public RuleTemplate getRuleTemplateByName(@WebParam(name = "name") String name) {
        incomingParamCheck("name", name);
        Map<String, Object> criteria = new HashMap<String, Object>(2);
        criteria.put("name", name);
        RuleTemplateBo template = this.businessObjectService.findByPrimaryKey(RuleTemplateBo.class, criteria);
        if (template == null) {
            throw new RiceIllegalStateException("RuleTemplate with specified name: " + name + " does not exist");
        }
        return RuleTemplateBo.to(template);
    }

    @Override
    public RuleTemplateQueryResults findRuleTemplates(
            @WebParam(name = "query") QueryByCriteria queryByCriteria) throws RiceIllegalArgumentException {
        if (queryByCriteria == null) {
            throw new RiceIllegalArgumentException("queryByCriteria is null");
        }

        LookupCustomizer.Builder<RuleTemplateBo> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        GenericQueryResults<RuleTemplateBo> results = criteriaLookupService.lookup(RuleTemplateBo.class, queryByCriteria, lc.build());

        RuleTemplateQueryResults.Builder builder = RuleTemplateQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<RuleTemplate.Builder> ims = new ArrayList<RuleTemplate.Builder>();
        for (RuleTemplateBo bo : results.getResults()) {
            ims.add(RuleTemplate.Builder.create(RuleTemplateBo.to(bo)));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public RuleResponsibility getRuleResponsibility(String responsibilityId) {
        incomingParamCheck("responsibilityId", responsibilityId);
        Map<String, String> criteria = Collections.singletonMap("responsibilityId", responsibilityId);
        RuleResponsibilityBo responsibility = this.businessObjectService.findByPrimaryKey(RuleResponsibilityBo.class, criteria);
        if (responsibility == null) {
            throw new RiceIllegalStateException("RuleResponsibility with specified id: " + responsibilityId + " does not exist");
        }
        return RuleResponsibilityBo.to(responsibility);
    }

    @Override
    public List<RuleDelegation> getRuleDelegationsByResponsibiltityId(
            @WebParam(name = "id") String id) throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck("id", id);
        Map<String, Object> criteria = new HashMap<String, Object>(2);
    	criteria.put("responsibilityId", id);
    	criteria.put("delegationRule.currentInd", Boolean.TRUE);
    	Collection<RuleDelegationBo> delegations = this.businessObjectService.findMatching(RuleDelegationBo.class,
                criteria);
        List<RuleDelegation> ruleDelegations = new ArrayList<RuleDelegation>();
        if (CollectionUtils.isNotEmpty(delegations)) {
            for (RuleDelegationBo bo : delegations) {
                ruleDelegations.add(RuleDelegationBo.to(bo));
            }
        }

    	return ruleDelegations;

    }

    private void incomingParamCheck(Object object, String name) {
        if (object == null) {
            throw new RiceIllegalArgumentException(name + " was null");
        } else if (object instanceof String
                && StringUtils.isBlank((String) object)) {
            throw new RiceIllegalArgumentException(name + " was blank");
        }
    }

    public BusinessObjectService getBusinessObjectService() {
        return this.businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public CriteriaLookupService getCriteriaLookupService() {
        return this.criteriaLookupService;
    }

    public void setCriteriaLookupService(CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }
}
