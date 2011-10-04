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
package org.kuali.rice.krad.service;

import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.document.authorization.DocumentAuthorizer;
import org.kuali.rice.krad.document.authorization.DocumentPresentationController;

/**
 * This class is a utility service intended to help retrieve objects related to particular documents.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public interface DocumentHelperService {

    public DocumentPresentationController getDocumentPresentationController(String documentType);
    
    public DocumentPresentationController getDocumentPresentationController(Document document);
    
    public DocumentAuthorizer getDocumentAuthorizer(String documentType);
    
    public DocumentAuthorizer getDocumentAuthorizer(Document document);
    
}
