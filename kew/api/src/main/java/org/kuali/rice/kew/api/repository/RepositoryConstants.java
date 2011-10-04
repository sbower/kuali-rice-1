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

package org.kuali.rice.kew.api.repository;

import org.kuali.rice.core.api.CoreConstants;
import org.kuali.rice.kew.api.KewApiConstants;

public final class RepositoryConstants {
	public final static class Namespaces {
		
        /**
    	 * Namespace for the kew module which is compatible with Kuali Rice 2.0.x.
    	 */
    	public static final String REPOSITORY_NAMESPACE_2_0 = KewApiConstants.Namespaces.KEW_NAMESPACE_PREFIX + "/repository/" + CoreConstants.Versions.VERSION_2_0;
    	
    }
}
