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
package org.kuali.rice.krad.exception;

/**
 * This class represents an exception that is thrown when the persistence layer attempts to manipulate a class which has not been
 * marked as persistable.
 * 
 * 
 */

public class ClassNotPersistableException extends RuntimeException {

    private static final long serialVersionUID = 6240754565898373530L;

    /**
     * @param message
     */
    public ClassNotPersistableException(String message) {
        super(message);
    }

    /**
     * @param message
     */
    public ClassNotPersistableException(String message, Throwable t) {
        super(message, t);
    }
}