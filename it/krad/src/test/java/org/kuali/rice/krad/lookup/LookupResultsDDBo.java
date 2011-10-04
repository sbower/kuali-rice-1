/*
 * Copyright 2007-2010 The Kuali Foundation
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
package org.kuali.rice.krad.lookup;

import org.kuali.rice.krad.bo.TransientBusinessObjectBase;

/**
 * Mock business object to try out the LookupResults DD support strategy
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class LookupResultsDDBo extends TransientBusinessObjectBase {
	private String someValue;
	
	public LookupResultsDDBo(String value) {
		this.someValue = value;
	}

	/**
	 * @return the someValue
	 */
	public String getSomeValue() {
		return this.someValue;
	}
	
	/**
	 * @param someValue the someValue to set
	 */
	public void setSomeValue(String someValue) {
		this.someValue = someValue;
	}
}
