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

package org.kuali.rice.ksb.messaging.service.impl;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.CoreConfigHelper;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.util.RiceUtilities;
import org.kuali.rice.ksb.api.bus.ServiceConfiguration;
import org.kuali.rice.ksb.api.messaging.AsynchronousCall;
import org.kuali.rice.ksb.messaging.PersistedMessageBO;
import org.kuali.rice.ksb.messaging.PersistedMessagePayload;
import org.kuali.rice.ksb.messaging.dao.MessageQueueDAO;
import org.kuali.rice.ksb.messaging.service.MessageQueueService;
import org.kuali.rice.ksb.util.KSBConstants;

import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class MessageQueueServiceImpl implements MessageQueueService {


    private static final Logger LOG = Logger.getLogger(MessageQueueServiceImpl.class);
    private MessageQueueDAO messageQueueDao;

    public void delete(PersistedMessageBO routeQueue) {
        if (Boolean.valueOf(ConfigContext.getCurrentContextConfig().getProperty(KSBConstants.Config.MESSAGE_PERSISTENCE))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Message Persistence is on.  Deleting stored message" + routeQueue);
            }
            this.getMessageQueueDao().remove(routeQueue);
        }
    }

    public void save(PersistedMessageBO routeQueue) {
        if (Boolean.valueOf(ConfigContext.getCurrentContextConfig().getProperty(KSBConstants.Config.MESSAGE_PERSISTENCE))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Persisting Message " + routeQueue);
            }
            this.getMessageQueueDao().save(routeQueue);
        }
    }

    public List<PersistedMessageBO> findAll() {
        return this.getMessageQueueDao().findAll();
    }

    public List<PersistedMessageBO> findAll(int maxRows) {
        return this.getMessageQueueDao().findAll(maxRows);
    }

    public PersistedMessageBO findByRouteQueueId(Long routeQueueId) {
        return getMessageQueueDao().findByRouteQueueId(routeQueueId);
    }

    public PersistedMessagePayload findByPersistedMessageByRouteQueueId(Long routeQueueId) {
        return messageQueueDao.findByPersistedMessageByRouteQueueId(routeQueueId);
    }

    public List<PersistedMessageBO> getNextDocuments(Integer maxDocuments) {
        return this.getMessageQueueDao().getNextDocuments(maxDocuments);
    }

    public MessageQueueDAO getMessageQueueDao() {
        return this.messageQueueDao;
    }

    public void setMessageQueueDao(MessageQueueDAO queueDAO) {
        this.messageQueueDao = queueDAO;
    }

    public List<PersistedMessageBO> findByServiceName(QName serviceName, String methodName) {
        return getMessageQueueDao().findByServiceName(serviceName, methodName);
    }

    public List<PersistedMessageBO> findByValues(Map<String, String> criteriaValues, int maxRows) {
        return getMessageQueueDao().findByValues(criteriaValues, maxRows);
    }

    public Integer getMaxRetryAttempts() {
        return new Integer(ConfigContext.getCurrentContextConfig().getProperty(KSBConstants.Config.ROUTE_QUEUE_MAX_RETRY_ATTEMPTS_KEY));
    }

    public PersistedMessageBO getMessage(ServiceConfiguration serviceConfiguration, AsynchronousCall methodCall) {
        PersistedMessageBO message = new PersistedMessageBO();
        message.setPayload(new PersistedMessagePayload(methodCall, message));
        message.setIpNumber(RiceUtilities.getIpNumber());
        message.setServiceName(serviceConfiguration.getServiceName().toString());
        message.setQueueDate(new Timestamp(System.currentTimeMillis()));
        if (serviceConfiguration.getPriority() == null) {
            message.setQueuePriority(KSBConstants.ROUTE_QUEUE_DEFAULT_PRIORITY);
        } else {
            message.setQueuePriority(serviceConfiguration.getPriority());
        }
        message.setQueueStatus(KSBConstants.ROUTE_QUEUE_QUEUED);
        message.setRetryCount(0);
        if (serviceConfiguration.getMillisToLive() > 0) {
            message.setExpirationDate(new Timestamp(System.currentTimeMillis() + serviceConfiguration.getMillisToLive()));
        }
        message.setApplicationId(CoreConfigHelper.getApplicationId());
        message.setMethodName(methodCall.getMethodName());
        return message;
    }
}