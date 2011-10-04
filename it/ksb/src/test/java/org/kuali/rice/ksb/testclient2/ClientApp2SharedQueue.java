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
package org.kuali.rice.ksb.testclient2;

import java.io.Serializable;

import org.kuali.rice.ksb.messaging.ClientAppServiceSharedPayloadObj;
import org.kuali.rice.ksb.messaging.service.KSBJavaService;



/**
 * A service that is registered as a queue for both the client apps.  Used to test queue 
 * call scenarios.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class ClientApp2SharedQueue implements KSBJavaService {
	
	
	public void invoke(Serializable payLoad) {

		ClientAppServiceSharedPayloadObj sharedPayload = (ClientAppServiceSharedPayloadObj) payLoad;
		if (sharedPayload.isThrowException()) {
			throw new RuntimeException("ClientAppSharedQueue throwing exception.");
		}
	}
}
