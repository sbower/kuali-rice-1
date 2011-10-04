/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
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
package org.kuali.rice.kew.api.document;

import java.util.Map;

import org.joda.time.DateTime;

public interface DocumentContract {
    
	String getDocumentId();

	DocumentStatus getStatus();

	DateTime getDateCreated();

	DateTime getDateLastModified();

	DateTime getDateApproved();

	DateTime getDateFinalized();

	String getTitle();

	String getApplicationDocumentId();

	String getInitiatorPrincipalId();

	String getRoutedByPrincipalId();

	String getDocumentTypeName();

	String getDocumentTypeId();

	String getDocumentHandlerUrl();

	String getApplicationDocumentStatus();

	DateTime getApplicationDocumentStatusDate();

	Map<String, String> getVariables();
	
}
