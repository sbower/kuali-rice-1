/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.ksb.impl.registry;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * This is a description of what this class does - ewestfal don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public interface ServiceRegistryDao {

	ServiceInfoBo getServiceInfo(String serviceId);
	
	List<ServiceInfoBo> getOnlineServiceInfosByName(QName serviceName);
	
	List<ServiceInfoBo> getAllOnlineServiceInfos();
	
	List<ServiceInfoBo> getAllServiceInfos();
	
	List<ServiceInfoBo> getAllServiceInfosForInstance(String instanceId);
	
	ServiceDescriptorBo getServiceDescriptor(String serviceDescriptorId);
	
	ServiceDescriptorBo saveServiceDescriptor(ServiceDescriptorBo serviceDescriptor);
	
	ServiceInfoBo saveServiceInfo(ServiceInfoBo serviceInfo);
	
	void removeServiceInfo(String serviceId);
	
	void removeServiceDescriptor(String serviceDescriptorId);
	
	boolean updateStatus(String serviceId, String statusCode);
	
	void updateStatusForInstanceId(String instanceId, String statusCode);
	
}
