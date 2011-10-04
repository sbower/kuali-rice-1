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



package org.kuali.rice.core.api.parameter

import org.junit.Test
import org.kuali.rice.core.test.JAXBAssert

class ParameterTypeTest {
	private static final Long VERSION_NUMBER = new Integer(1);
	private static final String OBJECT_ID = UUID.randomUUID();
    private static final String XML = """
        <parameterType xmlns="http://rice.kuali.org/core/v2_0">
            <code>PC</code>
            <name>Config</name>
            <active>true</active>
            <versionNumber>${VERSION_NUMBER}</versionNumber>
            <objectId>${OBJECT_ID}</objectId>
        </parameterType>
    """

    private static final String PARAMETER_TYPE_CODE = "PC"

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_first_null() {
        ParameterType.Builder.create((String) null);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_first_empty() {
        ParameterType.Builder.create("");
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_first_whitespace() {
        ParameterType.Builder.create("  ");
    }

    @Test
    void test_create_only_required() {
        ParameterType.Builder.create(ParameterType.Builder.create(PARAMETER_TYPE_CODE)).build();
    }

    @Test
    void happy_path() {
        ParameterType.Builder.create(PARAMETER_TYPE_CODE);
    }

    @Test
	public void test_Xml_Marshal_Unmarshal() {
		JAXBAssert.assertEqualXmlMarshalUnmarshal(this.create(), XML, ParameterType.class)
	}

    private create() {
		return ParameterType.Builder.create(new ParameterTypeContract() {
				def String code ="PC"
				def String name = "Config"
				def boolean active = true
                def Long versionNumber = ParameterTypeTest.VERSION_NUMBER
				def String objectId = ParameterTypeTest.OBJECT_ID
			}).build()
	}
}
