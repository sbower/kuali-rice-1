/*
 * Copyright 2006-2008 The Kuali Foundation
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

public class ObjectNotABusinessObjectRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 6279456922662880686L;

    public ObjectNotABusinessObjectRuntimeException() {
        super();
    }

    public ObjectNotABusinessObjectRuntimeException(String message) {
        super(message);
    }

    public ObjectNotABusinessObjectRuntimeException(Throwable cause) {
        super(cause);
    }

    public ObjectNotABusinessObjectRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
