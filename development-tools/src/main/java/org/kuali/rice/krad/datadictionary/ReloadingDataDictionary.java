/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.krad.datadictionary;

import no.geosoft.cc.io.FileListener;
import no.geosoft.cc.io.FileMonitor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.uif.util.UifBeanFactoryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Extends the DataDictionary to add reloading of changed dictionary files
 * without a restart of the web container
 * 
 * <p>
 * To use modify the "dataDictionaryService" spring definition
 * (KRADSpringBeans.xml) and change the constructor arg bean class from
 * "org.kuali.rice.krad.datadictionary.DataDictionary" to
 * "ReloadingDataDictionary"
 * </p>
 * 
 * <p>
 * NOTE: For Development Purposes Only!
 * </p>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ReloadingDataDictionary extends DataDictionary implements FileListener, URLMonitor.URLContentChangedListener {
	private static final Log LOG = LogFactory.getLog(DataDictionary.class);

	private static final String CLASS_DIR_CONFIG_PARM = "reload.data.dictionary.classes.dir";
	private static final String SOURCE_DIR_CONFIG_PARM = "reload.data.dictionary.source.dir";
	private static final String INTERVAL_CONFIG_PARM = "reload.data.dictionary.interval";

    private URLMonitor dictionaryUrlMonitor;


	public ReloadingDataDictionary() {
		super();
	}

	/**
	 * After dictionary has been loaded, determine the source files and add them
	 * to the monitor
	 * 
	 * @see org.kuali.rice.krad.datadictionary.DataDictionary#parseDataDictionaryConfigurationFiles(boolean)
	 */
	@Override
	public void parseDataDictionaryConfigurationFiles(boolean allowConcurrentValidation) {
		ConfigurationService configurationService = KRADServiceLocator.getKualiConfigurationService();

		// class directory part of the path that should be replaced
		String classesDir = configurationService.getPropertyValueAsString(CLASS_DIR_CONFIG_PARM);

		// source directory where dictionary files are found
		String sourceDir = configurationService.getPropertyValueAsString(SOURCE_DIR_CONFIG_PARM);

		// interval to poll for changes in milliseconds
		int reloadInterval = Integer.parseInt(configurationService.getPropertyValueAsString(INTERVAL_CONFIG_PARM));

		FileMonitor dictionaryFileMonitor = new FileMonitor(reloadInterval);

        dictionaryUrlMonitor = new URLMonitor(reloadInterval);
        dictionaryUrlMonitor.addListener(this);

		// need to copy the configFileLocations list here because it gets
		// cleared out after processing by super
		List<String> configLocations = new ArrayList<String>(configFileLocations);

		super.parseDataDictionaryConfigurationFiles(allowConcurrentValidation);
		for (String configLocation : configLocations) {
			Resource classFileResource = getFileResource(configLocation);
			try {
                if (classFileResource.getURI().toString().startsWith("jar:")) {
                    LOG.debug("Monitoring dictionary file at URI: " + classFileResource.getURI().toString());
                    dictionaryUrlMonitor.addURI(classFileResource.getURL());
                } else {
                    String filePathClassesDir = classFileResource.getFile().getAbsolutePath();
                    String sourceFilePath = StringUtils.replace(filePathClassesDir, classesDir, sourceDir);
                    File dictionaryFile = new File(filePathClassesDir);
                    if (dictionaryFile.exists()) {
                        LOG.debug("Monitoring dictionary file: " + dictionaryFile.getName());
                        dictionaryFileMonitor.addFile(dictionaryFile);
                    }
                }
			}
			catch (Exception e) {
				LOG.info("Exception in picking up dictionary file for monitoring:  " + e.getMessage(), e);
			}
		}

		// add the dictionary as a listener for file changes
		dictionaryFileMonitor.addListener(this);
	}

	/**
	 * Call back when a dictionary file is changed. Calls the spring bean reader
	 * to reload the file (which will override beans as necessary and destroy
	 * singletons) and runs the indexer
	 * 
	 * @see no.geosoft.cc.io.FileListener#fileChanged(java.io.File)
	 */
	@Override
	public void fileChanged(File file) {
		LOG.info("reloading dictionary configuration for " + file.getName());
		try {
			Resource resource = new FileSystemResource(file);
			xmlReader.loadBeanDefinitions(resource);

            UifBeanFactoryPostProcessor factoryPostProcessor = new UifBeanFactoryPostProcessor();
            factoryPostProcessor.postProcessBeanFactory(ddBeans);

			// re-index
			ddIndex.run();
		}
		catch (Exception e) {
			LOG.info("Exception in dictionary hot deploy: " + e.getMessage(), e);
		}
	}

    public void urlContentChanged(final URL url) {
        LOG.info("reloading dictionary configuration for " + url.toString());
        try {
            InputStream urlStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(urlStream);

            int originalValidationMode = xmlReader.getValidationMode();
            xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
            xmlReader.loadBeanDefinitions(resource);
            xmlReader.setValidationMode(originalValidationMode);

            UifBeanFactoryPostProcessor factoryPostProcessor = new UifBeanFactoryPostProcessor();
            factoryPostProcessor.postProcessBeanFactory(ddBeans);

            // re-index
            ddIndex.run();
        }
        catch (Exception e) {
            LOG.info("Exception in dictionary hot deploy: " + e.getMessage(), e);
        }
    }
}
