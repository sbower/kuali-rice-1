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
 * See the License for the specific language governing delegations and
 * limitations under the License.
 */
package org.kuali.rice.kim.rule.event.ui;

import org.kuali.rice.kim.bo.ui.RoleDocumentDelegation;
import org.kuali.rice.kim.document.IdentityManagementRoleDocument;
import org.kuali.rice.kim.rule.ui.AddDelegationRule;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.rule.BusinessRule;
import org.kuali.rice.krad.rule.event.KualiDocumentEventBase;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * This is a description of what this class does - shyu don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class AddDelegationEvent extends KualiDocumentEventBase {
	private RoleDocumentDelegation delegation;

	public AddDelegationEvent(String errorPathPrefix, IdentityManagementRoleDocument document) {
        super("Adding Delegation to document " + getDocumentId(document), errorPathPrefix, document);
    }

    public AddDelegationEvent(String errorPathPrefix, Document document, RoleDocumentDelegation delegation) {
        this(errorPathPrefix, (IdentityManagementRoleDocument) document);
        this.delegation = (RoleDocumentDelegation) ObjectUtils.deepCopy(delegation);
    }

    public Class<? extends BusinessRule> getRuleInterfaceClass() {
        return AddDelegationRule.class;
    }

    public boolean invokeRuleMethod(BusinessRule rule) {
        return ((AddDelegationRule) rule).processAddDelegation(this);
    }

	/**
	 * @return the delegation
	 */
	public RoleDocumentDelegation getDelegation() {
		return this.delegation;
	}

	/**
	 * @param delegation the delegation to set
	 */
	public void setDelegation(RoleDocumentDelegation delegation) {
		this.delegation = delegation;
	}

}
