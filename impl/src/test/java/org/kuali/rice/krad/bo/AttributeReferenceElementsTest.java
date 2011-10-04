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
package org.kuali.rice.krad.bo;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This is a description of what this class does - chang don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class AttributeReferenceElementsTest {

	AttributeReferenceElements dummyAttributeReferenceElement;
	@Before
	public void setUp() throws Exception {
		dummyAttributeReferenceElement = new AttributeReferenceElements();
	}

	/**
	 * This method ...
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		dummyAttributeReferenceElement = null;
	}

	@Test
	public void testInfoTextArea(){
		dummyAttributeReferenceElement.setInfoTextArea("dummyAttributeReferenceElement");
		assertEquals("Testing InfoTextArea in AttributeReferenceElements.","dummyAttributeReferenceElement",dummyAttributeReferenceElement.getInfoTextArea());
	}
	
	@Test
	public void testExtendedTextArea(){
		dummyAttributeReferenceElement.setExtendedTextArea("dummyAttributeReferenceElement");
		assertEquals("Testing ExtendedTextArea in AttributeReferenceElements","dummyAttributeReferenceElement",dummyAttributeReferenceElement.getExtendedTextArea());
	}
}
