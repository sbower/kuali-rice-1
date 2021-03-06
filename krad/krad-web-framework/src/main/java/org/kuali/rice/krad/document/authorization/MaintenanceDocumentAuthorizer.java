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
package org.kuali.rice.krad.document.authorization;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.bo.authorization.InquiryOrMaintenanceDocumentAuthorizer;
import org.kuali.rice.krad.document.MaintenanceDocument;

import java.util.Set;

public interface MaintenanceDocumentAuthorizer extends DocumentAuthorizer, InquiryOrMaintenanceDocumentAuthorizer {

	public boolean canCreate(Class boClass, Person user);

	public boolean canMaintain(Object dataObject, Person user);

	public boolean canCreateOrMaintain(MaintenanceDocument maintenanceDocument,
			Person user);

	public Set<String> getSecurePotentiallyReadOnlySectionIds();
}
