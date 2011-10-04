/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.ken.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.ken.bo.NotificationResponse;
import org.kuali.rice.ken.service.NotificationService;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.ksb.messaging.service.KSBXMLService;


/**
 * This class allows the NotificationService.sendNotification(XML) service 
 * to be invoked as a web service generically from the bus.
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SendNotificationServiceKewXmlImpl implements KSBXMLService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
	.getLogger(SendNotificationServiceKewXmlImpl.class);
    
    private final NotificationService notificationService;

    /**
     * Constructs a SendNotificationServiceKewXmlImpl instance.
     * @param notificationService
     */
    public SendNotificationServiceKewXmlImpl(NotificationService notificationService) {
	    this.notificationService = notificationService;
    }

    /**
     * Actually invokes the sendNotification() service method.  The KSB calls 
     * this.
     * @see org.kuali.rice.ksb.messaging.service.KSBXMLService#invoke(java.lang.String)
     */
    @Override
    public void invoke(String xml) {
    	if (StringUtils.isBlank(xml)) {
            throw new RiceIllegalArgumentException("xml is null or blank");
        }

        try {
    	   NotificationResponse response = notificationService.sendNotification(xml);
    	   LOG.info(response.getMessage());
    	} catch (Exception e) {
    	    throw new WorkflowRuntimeException(e);
    	}
    }
}
