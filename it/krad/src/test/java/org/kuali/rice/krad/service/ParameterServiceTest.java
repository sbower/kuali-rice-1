/*
 * Copyright 2006-2011 The Kuali Foundation
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

import org.junit.Test;
import org.kuali.rice.core.api.parameter.Parameter;
import org.kuali.rice.core.framework.services.CoreFrameworkServiceLocator;
import org.kuali.test.KRADTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This class is used to test the {@link NoteService} implementation 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class ParameterServiceTest extends KRADTestCase {

    @Test
    public void testRetrieveParameter() throws Exception {
    	String namespaceCode = "KR-NS";
    	String parameterDetailTypeCode = "Lookup";
    	String parameterName = "RESULTS_LIMIT";
    	String parameterValue = "200";
    	
    	Parameter resultsLimitParam = CoreFrameworkServiceLocator.getParameterService().getParameter(namespaceCode, parameterDetailTypeCode, parameterName);
    	assertNotNull("RESULTS_LIMIT should be non-null", resultsLimitParam);
    	assertEquals(parameterValue, resultsLimitParam.getValue());
    	
    	String detailType = resultsLimitParam.getComponentCode();
    	assertNotNull("Should have a detail type: " + detailType);
    	
    }


    
}

