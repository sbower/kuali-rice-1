/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.rice.core.web.parameter;

import org.kuali.rice.core.api.component.Component;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.core.impl.component.ComponentBo;
import org.kuali.rice.core.impl.parameter.ParameterBo;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.datadictionary.DataDictionaryException;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a description of what this class does - kellerj don't forget to fill
 * this in.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ParameterRule extends MaintenanceDocumentRuleBase {

	/**
	 * This overridden method ...
	 *
	 * @see org.kuali.rice.krad.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.krad.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
		boolean result = super.processCustomRouteDocumentBusinessRules( document );

		result &= checkAllowsMaintenanceEdit( document.getDocumentHeader().getWorkflowDocument()
				.getInitiatorPrincipalId(), (ParameterBo)getNewBo() );

		result &= checkComponent((ParameterBo) getNewBo());
		
		return result;
	}

	protected boolean checkAllowsMaintenanceEdit(String initiatorPrincipalId, ParameterBo newBO) {

		 boolean allowsEdit = false;
	        ParameterBo parm = newBO;
	        
	        Map<String, String> permissionDetails = new HashMap<String, String>();
	        permissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, parm.getNamespaceCode());
	        permissionDetails.put(KimConstants.AttributeConstants.COMPONENT_NAME, parm.getComponentCode());
	        permissionDetails.put(KimConstants.AttributeConstants.PARAMETER_NAME, parm.getName());
	        allowsEdit = KimApiServiceLocator.getPermissionService().isAuthorizedByTemplateName(
	        				GlobalVariables.getUserSession().getPerson().getPrincipalId(),
	        				KRADConstants.KRAD_NAMESPACE,
	        				KimConstants.PermissionTemplateNames.MAINTAIN_SYSTEM_PARAMETER,
	        				permissionDetails, Collections.<String, String>emptyMap());
	        if(!allowsEdit){
	        	putGlobalError(RiceKeyConstants.AUTHORIZATION_ERROR_PARAMETER);
	        }
	        return allowsEdit;
	}

    public boolean checkComponent(ParameterBo param) {
        String component = param.getComponentCode();
        String namespace = param.getNamespaceCode();
        boolean result = false;

        try {
            List<Component> dataDictionaryAndSpringComponents = KRADServiceLocatorWeb
                    .getRiceApplicationConfigurationMediationService().getNonDatabaseComponents();
            for (Component pdt : dataDictionaryAndSpringComponents) {
                if (namespace.equals(pdt.getNamespaceCode()) && component.equals(pdt.getCode())) {
                    result = true;
                    break;
                }
            }

            if (!result) {
                Map<String, String> primaryKeys = new HashMap<String, String>(2);
                primaryKeys.put("namespaceCode", namespace);
                primaryKeys.put("code", component);
                result = ObjectUtils.isNotNull(getBoService().findByPrimaryKey(ComponentBo.class, primaryKeys));
            }

            if (!result) {
                putFieldError("code", "error.document.parameter.detailType.invalid", component);
            }

            return result;
        }
        catch (DataDictionaryException ex) {
            throw new RuntimeException("Problem parsing data dictionary during full load required for rule validation: " + ex.getMessage(), ex);
        }
    }
}

