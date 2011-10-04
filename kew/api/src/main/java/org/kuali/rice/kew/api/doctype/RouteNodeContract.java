/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
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
package org.kuali.rice.kew.api.doctype;

import java.util.List;

import org.kuali.rice.core.api.mo.common.Identifiable;
import org.kuali.rice.core.api.mo.common.Versioned;

/**
 * TODO ewestfal don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface RouteNodeContract extends Identifiable, Versioned {

    String getDocumentTypeId();
    
    String getName();
    
    String getRouteMethodName();
    
    String getRouteMethodCode();
    
    boolean isFinalApproval();
    
    boolean isMandatory();
    
    String getActivationType();
    
    String getExceptionGroupId();
    
    String getType();
    
    String getBranchName();
    
    String getNextDocumentStatus();
    
    List<? extends RouteNodeConfigurationParameterContract> getConfigurationParameters();
    
    List<String> getPreviousNodeIds();
    
    List<String> getNextNodeIds();
    
}