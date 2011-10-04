/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kim.bo.ui;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.kuali.rice.kim.document.IdentityManagementPersonDocument;

/**
 * This is a description of what this class does - shyu don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@MappedSuperclass
public class PersonDocumentBoDefaultBase extends KimDocumentBoActivatableEditableBase{
    @Type(type="yes_no")
	@Column(name="DFLT_IND")
	protected boolean dflt;
    @Transient
	protected IdentityManagementPersonDocument personDocument;
	
	/**
	 * @return the personDocument
	 */
	public IdentityManagementPersonDocument getPersonDocument() {
		return this.personDocument;
	}

	/**
	 * @param personDocument the personDocument to set
	 */
	public void setPersonDocument(IdentityManagementPersonDocument personDocument) {
		this.personDocument = personDocument;
	}

	public boolean isDflt() {
		return this.dflt;
	}

	public void setDflt(boolean dflt) {
		this.dflt = dflt;
	}

}
