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
package org.kuali.rice.ksb.messaging;

import org.junit.Test;
import org.kuali.rice.core.api.util.io.SerializationUtils;

import javax.xml.namespace.QName;

import static org.junit.Assert.assertTrue;


public class QNameSerializationTest {
	
	@Test public void testQNameSerializaion() throws Exception {
		
		QName qname = new QName("hi", "HI");
		String qnameSerialized = SerializationUtils.serializeToBase64(qname);
		SerializationUtils.deserializeFromBase64(qnameSerialized);
		assertTrue(true);
	}
	
}
