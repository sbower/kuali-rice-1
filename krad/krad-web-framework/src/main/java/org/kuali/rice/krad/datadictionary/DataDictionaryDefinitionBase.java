/*
 * Copyright 2005-2007 The Kuali Foundation
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

package org.kuali.rice.krad.datadictionary;

import org.springframework.beans.factory.InitializingBean;



/**
 * Common base class for DataDictionaryDefinition types.
 * 
 * 
 */
abstract public class DataDictionaryDefinitionBase implements DataDictionaryDefinition, InitializingBean {
    private static final long serialVersionUID = -2003626577498716712L;
    
	protected String id;

    public DataDictionaryDefinitionBase() {
    }

    public String getId() {
        return this.id;
    }

    /**
     * A unique identifier for this data dictionary element.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {}
}
