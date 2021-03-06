/*
 * Copyright 2005-2008 The Kuali Foundation
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
package org.kuali.rice.krad.datadictionary.exception;

import org.kuali.rice.krad.datadictionary.DataDictionaryException;

/**
 * Exception thrown when an error occurs validating the data parsed from a DictionaryEntry's XML file.
 * 
 * 
 */
public class CompletionException extends DataDictionaryException {

    private static final long serialVersionUID = 5657585994350063964L;

    /**
     * @param message
     */
    public CompletionException(String message) {
        super(message);
    }

    /**
     * @param message
     */
    public CompletionException(String message, Throwable t) {
        super(message, t);
    }
}
