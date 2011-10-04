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
package edu.sampleu.kew.krad.controller;

import edu.sampleu.kew.krad.KEWConstants;
import edu.sampleu.kew.krad.form.IngesterForm;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.core.api.impex.xml.CompositeXmlDocCollection;
import org.kuali.rice.core.api.impex.xml.FileXmlDocCollection;
import org.kuali.rice.core.api.impex.xml.XmlDoc;
import org.kuali.rice.core.api.impex.xml.XmlDocCollection;
import org.kuali.rice.core.api.impex.xml.ZipXmlDocCollection;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is a description of what this class does - Venkat don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@Controller
@RequestMapping(value = "/ingester")
public class IngesterController extends UifControllerBase {

    @Override
    protected Class<IngesterForm> formType() {
        return IngesterForm.class;
    }

	@Override
	@RequestMapping(params = "methodToCall=start")
	public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
			HttpServletRequest request, HttpServletResponse response) {

		IngesterForm ingesterForm = (IngesterForm)form;
		
//		checkAuthorization(form,"");
		return super.start(ingesterForm, result, request, response);
	}

	@RequestMapping(method = RequestMethod.POST, params = "methodToCall=upload")
	public ModelAndView upload(@ModelAttribute("KualiForm") IngesterForm ingesterForm, BindingResult result,
			HttpServletRequest request, HttpServletResponse response) {
		
		List<File> tempFiles = new ArrayList<File>();
		
		try {
			
			List<XmlDocCollection> collections = new ArrayList<XmlDocCollection>();
			
			for (MultipartFile file : ingesterForm.getFiles())
	        {
	            if (file == null || StringUtils.isBlank(file.getOriginalFilename())) {
					continue;
				}
	            
	            // ok, we have to copy it to *another* file because Struts doesn't give us a File
	            // reference (which itself is not a bad abstraction) and XmlDocs based on ZipFile
	            // can't be constructed without a file reference.
	            FileOutputStream fos = null;
	            File temp = null;
	            try{
	                temp = File.createTempFile("ingester", null);
	                tempFiles.add(temp);
	                fos = new FileOutputStream(temp);
	                fos.write(file.getBytes());
	            } catch (IOException ioe) {
	            	GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID,KEWConstants.ERROR_INGESTER_COPY_FILE , file.getOriginalFilename(), ExceptionUtils.getFullStackTrace(ioe));
	                continue;
	            } finally{
	                if (fos != null) {
						try{
						    fos.close();
						} catch (IOException ioe){
	//					    //LOG.error("Error closing temp file output stream: " + temp, ioe);
						}
					}
	            }
	            if (file.getOriginalFilename().toLowerCase().endsWith(".zip"))
	            {
	                try {
	                    collections.add(new ZipXmlDocCollection(temp));
	                } catch (IOException ioe) {
	                    GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_LOAD_FILE, file.getOriginalFilename());
	                }
	            } else if (file.getOriginalFilename().endsWith(".xml")) {
	                collections.add(new FileXmlDocCollection(temp, file.getOriginalFilename()));
	            } else {
	            	GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_EXTRANEOUS_FILE, file.getOriginalFilename());
	            }
	        }
	
	        if (collections.size() == 0) {
	            String message = "No valid files to ingest";
	            GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_NO_VALID_FILES);
	        } else {
	            // wrap in composite collection to make transactional
	            CompositeXmlDocCollection compositeCollection = new CompositeXmlDocCollection(collections);
	            int totalProcessed = 0;
	            List<XmlDocCollection> c = new ArrayList<XmlDocCollection>(1);
	            c.add(compositeCollection);
	            try {
	                Collection<XmlDocCollection> failed = CoreApiServiceLocator.getXmlIngesterService().ingest(c, GlobalVariables.getUserSession().getPrincipalId());
	                boolean txFailed = failed.size() > 0;
	                if (txFailed) {
	                	GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_FAILED);
	                }
	                for (XmlDocCollection collection1 : collections)
	                {
	                    List<? extends XmlDoc> docs = collection1.getXmlDocs();
	                    for (XmlDoc doc1 : docs)
	                    {
	                        if (doc1.isProcessed())
	                        {
	                            if (!txFailed)
	                            {
	                                totalProcessed++;
	                                GlobalVariables.getMessageMap().putInfoForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.INFO_INGESTER_SUCCESS, doc1.getName(),doc1.getProcessingMessage());
//	                                messages.add("Ingested xml doc: " + doc1.getName() + (doc1.getProcessingMessage() == null ? "" : "\n" + doc1.getProcessingMessage()));
	                            } else
	                            {GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_ROLLEDBACK, doc1.getName(),doc1.getProcessingMessage());
//	                                messages.add("Rolled back doc: " + doc1.getName() + (doc1.getProcessingMessage() == null ? "" : "\n" + doc1.getProcessingMessage()));
	                            }
	                        } else
	                        {GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_FAILED_XML, doc1.getName(),doc1.getProcessingMessage());
//	                            messages.add("Failed to ingest xml doc: " + doc1.getName() + (doc1.getProcessingMessage() == null ? "" : "\n" + doc1.getProcessingMessage()));
	                        }
	                    }
	                }
	            } catch (Exception e) {
//	                String message = "Error during ingest";
	                //LOG.error(message, e);
//	                messages.add(message + ": " + e  + ":\n" + ExceptionUtils.getFullStackTrace(e));
	                GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_DURING_INJECT, ExceptionUtils.getFullStackTrace(e));
	            }
	            if (totalProcessed == 0) {
//	                String message = "No xml docs ingested";
	                GlobalVariables.getMessageMap().putErrorForSectionId(KEWConstants.INGESTER_SECTION_ID, KEWConstants.ERROR_INGESTER_NO_XMLS);
	            }
	        }
	    } finally {
	        if (tempFiles.size() > 0) {
	            for (File tempFile : tempFiles)
	            {
	                if (!tempFile.delete())
	                {
	                    //LOG.warn("Error deleting temp file: " + tempFile);
	                }
	            }
	        }
	    }
		
//	    request.setAttribute("messages", messages);
	    return getUIFModelAndView(ingesterForm);
	}
	
	@RequestMapping(method = RequestMethod.POST, params = "methodToCall=close")
	public ModelAndView close(@ModelAttribute("KualiForm") IngesterForm ingesterForm, BindingResult result,
			HttpServletRequest request, HttpServletResponse response) {

		return null;
	}

}
