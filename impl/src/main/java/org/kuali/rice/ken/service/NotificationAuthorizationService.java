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
package org.kuali.rice.ken.service;

import org.kuali.rice.ken.bo.NotificationChannel;
import org.kuali.rice.ken.bo.NotificationProducer;

/**
 * The NotificationAuthorizationService class is responsible for housing permissions and authorization 
 * related services.
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface NotificationAuthorizationService {
    /**
     * This service method validates that the specified producer has the permissions to send a notification 
     * on for a specific NotificationChannel.
     * @param producer
     * @param channel
     * @return boolean - true if authorized, false if not
     */
    public boolean isProducerAuthorizedToSendNotificationForChannel(NotificationProducer producer, NotificationChannel channel);
    
    /**
     * This method checks to see if the user is authorized as an administrator in the system.
     * @param userId
     * @return boolean - true if authorized, false if not
     */
    public boolean isUserAdministrator(String userId);
}
