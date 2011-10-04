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
package org.kuali.rice.krad.datadictionary.validation;

import junit.framework.AssertionFailedError;

public class UTF8ValidationTestUtils {
	static final String[] TEST_INPUTS = { "", "!!!", "[a-9]", "^A-Z", "abc", "a bc", "a_bc", "123", "12 3", "12_3", 
		"a1b2c3", "a1b2_c3", "a 1b2c3", "a 1b2_c3", "foo.bar", "foo.bar_baz", ".bar_foo baz", 
		"!\"#abs$%&'()*+,./:;<=abs>?@\\^_abs`{|}~-", "\u00E6\u00E6", "\t", "\u00E6 \u00E6"};

	public static final void assertPatternMatches(ValidationPattern pattern, boolean[] expectedValues) {
	
		if (expectedValues.length != TEST_INPUTS.length) {
			throw new AssertionFailedError("expectedValues length was " + expectedValues.length + ", expected TEST_INPUTS.length of " + TEST_INPUTS.length);
		}

		for (int i = 0; i < TEST_INPUTS.length; ++i) {
			String testInput = TEST_INPUTS[i];
			boolean expectedResult = expectedValues[i];

			boolean actualResult = pattern.matches(testInput);
			if (actualResult != expectedResult) {
				throw new AssertionFailedError("for input '" + testInput + "', expected " + expectedResult + " but got " + actualResult);
			}
		}
	}
}
