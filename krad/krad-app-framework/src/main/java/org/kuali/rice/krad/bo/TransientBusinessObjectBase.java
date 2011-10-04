/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.krad.bo;


/**
 * Transient Business Object Base Business Object
 */
public abstract class TransientBusinessObjectBase extends BusinessObjectBase {
    //private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TransientBusinessObjectBase.class);

    /**
     * Constructs a TransientBusinessObjectBase.java.
     */
    public TransientBusinessObjectBase() {
        super();
    }

    public void refresh() {
        // do nothing, assume that a class will implement this method if necessary        
    }

}