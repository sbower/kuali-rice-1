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

package org.kuali.rice.ksb.messaging.config;

import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.kuali.rice.core.api.config.CoreConfigHelper;
import org.kuali.rice.core.api.config.module.RunMode;
import org.kuali.rice.core.api.config.property.Config;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.exception.RiceRuntimeException;
import org.kuali.rice.core.api.lifecycle.BaseLifecycle;
import org.kuali.rice.core.api.lifecycle.Lifecycle;
import org.kuali.rice.core.api.resourceloader.ResourceLoader;
import org.kuali.rice.core.api.util.ClassLoaderUtils;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.core.framework.persistence.jpa.OrmUtils;
import org.kuali.rice.core.impl.config.module.ModuleConfigurer;
import org.kuali.rice.core.impl.lifecycle.ServiceDelegatingLifecycle;
import org.kuali.rice.ksb.api.KsbApiConstants;
import org.kuali.rice.ksb.api.KsbApiServiceLocator;
import org.kuali.rice.ksb.api.bus.ServiceDefinition;
import org.kuali.rice.ksb.messaging.AlternateEndpoint;
import org.kuali.rice.ksb.messaging.AlternateEndpointLocation;
import org.kuali.rice.ksb.messaging.MessageFetcher;
import org.kuali.rice.ksb.messaging.resourceloader.KSBResourceLoaderFactory;
import org.kuali.rice.ksb.messaging.serviceconnectors.HttpInvokerConnector;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.kuali.rice.ksb.util.KSBConstants;
import org.quartz.Scheduler;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Used to configure the embedded workflow. This could be used to configure
 * embedded workflow programmatically but mostly this is a base class by which
 * to hang specific configuration behavior off of through subclassing
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 * 
 */
public class KSBConfigurer extends ModuleConfigurer {
	
	private static final String SERVICE_BUS_CLIENT_SPRING = "classpath:org/kuali/rice/ksb/config/KsbServiceBusClientSpringBeans.xml";
	private static final String MESSAGE_CLIENT_SPRING = "classpath:org/kuali/rice/ksb/config/KsbMessageClientSpringBeans.xml";
	private static final String OJB_MESSAGE_CLIENT_SPRING = "classpath:org/kuali/rice/ksb/config/KsbOjbMessageClientSpringBeans.xml";
	private static final String BAM_SPRING = "classpath:org/kuali/rice/ksb/config/KsbBamSpringBeans.xml";
	private static final String OJB_BAM_SPRING = "classpath:org/kuali/rice/ksb/config/KsbOjbBamSpringBeans.xml";
	private static final String MODULE_SPRING = "classpath:org/kuali/rice/ksb/config/KsbModuleConfigurationSpringBeans.xml";
	private static final String REGISTRY_SERVER_SPRING = "classpath:org/kuali/rice/ksb/config/KsbRegistryServerSpringBeans.xml";
	private static final String OJB_REGISTRY_SPRING = "classpath:org/kuali/rice/ksb/config/KsbOjbRegistrySpringBeans.xml";
	private static final String WEB_SPRING = "classpath:org/kuali/rice/ksb/config/KsbWebSpringBeans.xml";

	private List<ServiceDefinition> services = new ArrayList<ServiceDefinition>();
	
    private List<AlternateEndpointLocation> alternateEndpointLocations = new ArrayList<AlternateEndpointLocation>();

	private List<AlternateEndpoint> alternateEndpoints = new ArrayList<AlternateEndpoint>();

	private DataSource registryDataSource;

	private DataSource messageDataSource;
	
	private DataSource nonTransactionalMessageDataSource;
	
	private DataSource bamDataSource;

	private Scheduler exceptionMessagingScheduler;

	private PlatformTransactionManager platformTransactionManager;
	
	private List<Lifecycle> internalLifecycles;
	
	public KSBConfigurer() {
		super(KsbApiConstants.KSB_MODULE_NAME);
		setValidRunModes(Arrays.asList(RunMode.REMOTE, RunMode.LOCAL));
		this.internalLifecycles = new ArrayList<Lifecycle>();
	}
	
	@Override
	public void addAdditonalToConfig() {
		configureDataSource();
		configureScheduler();
		configurePlatformTransactionManager();
		configureAlternateEndpoints();
	}

	@Override
	public List<String> getPrimarySpringFiles(){
		final List<String> springFileLocations = new ArrayList<String>();
				
		boolean isJpa = OrmUtils.isJpaEnabled("rice.ksb");
		if (isJpa) {
			// TODO redo this once we're back to JPA
        	// springFileLocations.add("classpath:org/kuali/rice/ksb/config/KSBJPASpringBeans.xml");
        	throw new UnsupportedOperationException("JPA not currently supported for KSB");
		}
		
		springFileLocations.add(SERVICE_BUS_CLIENT_SPRING);
		
		if (isMessagePersistenceEnabled()) {
			springFileLocations.add(MESSAGE_CLIENT_SPRING);
			springFileLocations.add(OJB_MESSAGE_CLIENT_SPRING);
		}
        
        if (isBamEnabled()) {
        	springFileLocations.add(BAM_SPRING);
        	springFileLocations.add(OJB_BAM_SPRING);
        }
        
        if (getRunMode().equals( RunMode.LOCAL )) {
    		// TODO hack 'cause KSB used KNS - this needs to be fixed!!!
    		springFileLocations.add("classpath:org/kuali/rice/krad/config/KRADSpringBeans.xml");
            springFileLocations.add("classpath:org/kuali/rice/kns/config/KNSSpringBeans.xml");
        	springFileLocations.add(REGISTRY_SERVER_SPRING);
        	springFileLocations.add(OJB_REGISTRY_SPRING);
        	if (ConfigContext.getCurrentContextConfig().getBooleanProperty(KSBConstants.Config.LOAD_KRAD_MODULE_CONFIGURATION, false)) {
            	springFileLocations.add(MODULE_SPRING);
            	springFileLocations.add(WEB_SPRING);
            }
        }
        
        return springFileLocations;
	}
	
	/**
	 * Returns true - KSB UI should always be included.
	 * 
	 * @see org.kuali.rice.core.ConfigContext.getCurrentContextConfig().ModuleConfigurer#shouldRenderWebInterface()
	 */
	@Override
	public boolean shouldRenderWebInterface() {
		return true;
	}
	
	@Override
	public Collection<ResourceLoader> getResourceLoadersToRegister() throws Exception{
		ResourceLoader ksbRemoteResourceLoader = KSBResourceLoaderFactory.createRootKSBRemoteResourceLoader();
		ksbRemoteResourceLoader.start();
		return Collections.singletonList(ksbRemoteResourceLoader);
	}
	
	@Override
	public List<Lifecycle> loadLifecycles() throws Exception {
		List<Lifecycle> lifecycles = new LinkedList<Lifecycle>();
		// this validation of our service list needs to happen after we've
		// loaded our configs so it's a lifecycle
		lifecycles.add(new BaseLifecycle() {

			@Override
			public void start() throws Exception {
				// first check if we want to allow self-signed certificates for SSL communication
				if (Boolean.valueOf(ConfigContext.getCurrentContextConfig().getProperty(KSBConstants.Config.KSB_ALLOW_SELF_SIGNED_SSL)).booleanValue()) {
				    Protocol.registerProtocol("https", new Protocol("https",
					    (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), 443));
				}
				super.start();
			}
		});
		lifecycles.add(new ServiceDelegatingLifecycle(KSBConstants.ServiceNames.BUS_ADMIN_SERVICE));
		return lifecycles;
	}
	
	

    @Override
    public void doAdditonalConfigurerValidations() {
        for (final ServiceDefinition serviceDef : KSBConfigurer.this.services) {
			serviceDef.validate();
		}
    }

	@Override
	public void doAdditionalContextStartedLogic() {
		ServicePublisher servicePublisher = new ServicePublisher(getServices());
		Lifecycle serviceBus = new ServiceDelegatingLifecycle(KsbApiServiceLocator.SERVICE_BUS);
		Lifecycle threadPool = new ServiceDelegatingLifecycle(KSBConstants.ServiceNames.THREAD_POOL_SERVICE);
		Lifecycle scheduledThreadPool = new ServiceDelegatingLifecycle(KSBConstants.ServiceNames.SCHEDULED_THREAD_POOL_SERVICE);
		
		try {
			servicePublisher.start();
			internalLifecycles.add(servicePublisher);
			serviceBus.start();
			internalLifecycles.add(serviceBus);
			threadPool.start();
			internalLifecycles.add(threadPool);
			scheduledThreadPool.start();
			internalLifecycles.add(scheduledThreadPool);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			}
			throw new RiceRuntimeException("Failed to initialize KSB on context startup");
		}

		requeueMessages();
	}

	@Override
	protected void doAdditionalModuleStopLogic() throws Exception {
		for (int index = internalLifecycles.size() - 1; index >= 0; index--) {
			try {
				internalLifecycles.get(index).stop();
			} catch (Exception e) {
				LOG.error("Failed to properly execute shutdown logic.", e);
			}
		}
	}

	/**
     * Used to refresh the service registry after the Application Context is initialized.  This way any services that were exported on startup
     * will be available in the service registry once startup is complete.
     */
    private void requeueMessages() {
        LOG.info("Refreshing Service Registry to export services to the bus.");
        KsbApiServiceLocator.getServiceBus().synchronize();
        
		//automatically requeue documents sitting with status of 'R'
		MessageFetcher messageFetcher = new MessageFetcher((Integer) null);
		KSBServiceLocator.getThreadPool().execute(messageFetcher);
    }
    
    protected boolean isMessagePersistenceEnabled() {
    	return ConfigContext.getCurrentContextConfig().getBooleanProperty(KSBConstants.Config.MESSAGE_PERSISTENCE, true);
    }
    
    protected boolean isBamEnabled() {
    	return ConfigContext.getCurrentContextConfig().getBooleanProperty(Config.BAM_ENABLED, false);
    }

	protected void configureScheduler() {
		if (this.getExceptionMessagingScheduler() != null) {
			LOG.info("Configuring injected exception messaging Scheduler");
			ConfigContext.getCurrentContextConfig().putObject(KSBConstants.Config.INJECTED_EXCEPTION_MESSAGE_SCHEDULER_KEY, this.getExceptionMessagingScheduler());
		}
	}

	protected void configureDataSource() {
		if (isMessagePersistenceEnabled()) {
			if (getMessageDataSource() != null) {
				ConfigContext.getCurrentContextConfig().putObject(KSBConstants.Config.KSB_MESSAGE_DATASOURCE, getMessageDataSource());
			}
			if (getNonTransactionalMessageDataSource() != null) {
	            ConfigContext.getCurrentContextConfig().putObject(KSBConstants.Config.KSB_MESSAGE_NON_TRANSACTIONAL_DATASOURCE, getNonTransactionalMessageDataSource());
			}
		}
        if (getRunMode().equals(RunMode.LOCAL)) {
        	if (getRegistryDataSource() != null) {
                ConfigContext.getCurrentContextConfig().putObject(KSBConstants.Config.KSB_REGISTRY_DATASOURCE, getRegistryDataSource());
            }
        }
        if (isBamEnabled()) {
        	if (getBamDataSource() != null) {
        		ConfigContext.getCurrentContextConfig().putObject(KSBConstants.Config.KSB_BAM_DATASOURCE, getBamDataSource());
        	}
        }
    }

	protected void configurePlatformTransactionManager() {
		if (getPlatformTransactionManager() == null) {
			return;
		}
		ConfigContext.getCurrentContextConfig().putObject(RiceConstants.SPRING_TRANSACTION_MANAGER, getPlatformTransactionManager());
	}
	
	protected void configureAlternateEndpoints() {
		ConfigContext.getCurrentContextConfig().putObject(KSBConstants.Config.KSB_ALTERNATE_ENDPOINT_LOCATIONS, getAlternateEndpointLocations());
		ConfigContext.getCurrentContextConfig().putObject(KSBConstants.Config.KSB_ALTERNATE_ENDPOINTS, getAlternateEndpoints());
	}
	
	@Override
	public void doAdditionalContextStoppedLogic() {
		try {
			HttpInvokerConnector.shutdownIdleConnectionTimeout();
		} catch (Exception e) {
			LOG.error("Failed to shutdown idle connection timeout evictor thread.", e);
		}
	    cleanUpConfiguration();
	}
	
	/**
     * Because our configuration is global, shutting down Rice does not get rid of objects stored there.  For that reason
     * we need to manually clean these up.  This is most important in the case of the service bus because the configuration
     * is used to store services to be exported.  If we don't clean this up then a shutdown/startup within the same
     * class loading context causes the service list to be doubled and results in "multiple endpoint" error messages.
     *
     */
    protected void cleanUpConfiguration() {
        ConfigContext.getCurrentContextConfig().removeObject(KSBConstants.Config.KSB_ALTERNATE_ENDPOINTS);
    }

	public List<ServiceDefinition> getServices() {
		return this.services;
	}

	public void setServices(List<ServiceDefinition> javaServices) {
		this.services = javaServices;
	}

	public DataSource getMessageDataSource() {
		return this.messageDataSource;
	}

	public void setMessageDataSource(DataSource messageDataSource) {
		this.messageDataSource = messageDataSource;
	}

    public DataSource getNonTransactionalMessageDataSource() {
        return this.nonTransactionalMessageDataSource;
    }

    public void setNonTransactionalMessageDataSource(DataSource nonTransactionalMessageDataSource) {
        this.nonTransactionalMessageDataSource = nonTransactionalMessageDataSource;
    }

    public DataSource getRegistryDataSource() {
		return this.registryDataSource;
	}

	public void setRegistryDataSource(DataSource registryDataSource) {
		this.registryDataSource = registryDataSource;
	}
	
	public DataSource getBamDataSource() {
		return this.bamDataSource;
	}

	public void setBamDataSource(DataSource bamDataSource) {
		this.bamDataSource = bamDataSource;
	}

	public Scheduler getExceptionMessagingScheduler() {
		return this.exceptionMessagingScheduler;
	}

	public void setExceptionMessagingScheduler(Scheduler exceptionMessagingScheduler) {
		this.exceptionMessagingScheduler = exceptionMessagingScheduler;
	}

	public PlatformTransactionManager getPlatformTransactionManager() {
		return platformTransactionManager;
	}

	public void setPlatformTransactionManager(PlatformTransactionManager springTransactionManager) {
		this.platformTransactionManager = springTransactionManager;
	}

    public List<AlternateEndpointLocation> getAlternateEndpointLocations() {
	    return this.alternateEndpointLocations;
    }

    public void setAlternateEndpointLocations(List<AlternateEndpointLocation> alternateEndpointLocations) {
	    this.alternateEndpointLocations = alternateEndpointLocations;
	}

    public List<AlternateEndpoint> getAlternateEndpoints() {
        return this.alternateEndpoints;
    }

    public void setAlternateEndpoints(List<AlternateEndpoint> alternateEndpoints) {
        this.alternateEndpoints = alternateEndpoints;
    }
    
    private final class ServicePublisher extends BaseLifecycle {

    	private final List<ServiceDefinition> serviceDefinitions;
    	
    	ServicePublisher(List<ServiceDefinition> serviceDefinitions) {
    		this.serviceDefinitions = serviceDefinitions;
    	}
    	
		@Override
		public void start() throws Exception {
			if (serviceDefinitions != null && !serviceDefinitions.isEmpty()) {
				LOG.debug("Configuring " + serviceDefinitions.size() + " services for application id " + CoreConfigHelper.getApplicationId() + " using config for classloader " + ClassLoaderUtils.getDefaultClassLoader());
				KsbApiServiceLocator.getServiceBus().publishServices(serviceDefinitions, true);
				super.start();
			}
		}
    	
    }
    
}
