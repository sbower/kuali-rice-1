/*
 * Copyright 2006-2011 The Kuali Foundation
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

package org.kuali.rice.kew.rule.bo;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.web.WebRuleUtils;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.web.ui.Section;
import org.kuali.rice.krad.bo.BusinessObject;

import java.util.List;
import java.util.Map;

/**
 * This is a description of what this class does - ewestfal don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class RuleBaseValuesInquirableImpl extends KualiInquirableImpl {

	public static final String DOCUMENT_ID = "documentId";

	@Override
	public Object retrieveDataObject(Map fieldValues){
		RuleBaseValues rule = (RuleBaseValues)super.retrieveDataObject(fieldValues);
		WebRuleUtils.populateRuleMaintenanceFields(rule);
		return rule;
    }
	
	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.krad.inquiry.Inquirable#getBusinessObject(java.util.Map)
	 */
	public BusinessObject getBusinessObject(Map fieldValues) {
		RuleBaseValues rule = (RuleBaseValues)super.getBusinessObject(fieldValues);
		WebRuleUtils.populateRuleMaintenanceFields(rule);
		return rule;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.krad.inquiry.Inquirable#getSections(org.kuali.rice.krad.bo.BusinessObject)
	 */
	public List getSections(BusinessObject bo) {
		List<Section> sections = super.getSections(bo);

		return WebRuleUtils.customizeSections((RuleBaseValues)bo, sections, false);

	}
	

    @Override
    public HtmlData getInquiryUrl(BusinessObject businessObject, String attributeName, boolean forceInquiry) {
    	
		if(DOCUMENT_ID.equals(attributeName)) {

			HtmlData.AnchorHtmlData link = new HtmlData.AnchorHtmlData();
			RuleBaseValues rule = (RuleBaseValues)businessObject;

			String documentId = rule.getDocumentId();
			link.setDisplayText(documentId+"");

			String href = ConfigContext.getCurrentContextConfig().getKEWBaseURL() + "/" +
			KEWConstants.DOC_HANDLER_REDIRECT_PAGE + "?" + KEWConstants.COMMAND_PARAMETER + "=" +
			KEWConstants.DOCSEARCH_COMMAND + "&" + KEWConstants.DOCUMENT_ID_PARAMETER + "=" + documentId;

			link.setHref(href);

			return link;
		}		
        return super.getInquiryUrl(businessObject, attributeName, forceInquiry);
    }

}
