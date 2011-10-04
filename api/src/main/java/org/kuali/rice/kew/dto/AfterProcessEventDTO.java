/*
 * Copyright 2005-2009 The Kuali Foundation
 * 
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
package org.kuali.rice.kew.dto;

/**
 * Signal to the PostProcessor that the routeHeader has just completed processing
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class AfterProcessEventDTO extends DocumentEventDTO {

	private static final long serialVersionUID = 1462372563938714818L;
	
	private String nodeInstanceId;
	private boolean successfullyProcessed;

    public AfterProcessEventDTO() {
        super(AFTER_PROCESS);
    }

    public String getNodeInstanceId() {
        return this.nodeInstanceId;
    }

    public void setNodeInstanceId(String nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }

    public boolean isSuccessfullyProcessed() {
        return this.successfullyProcessed;
    }

    public void setSuccessfullyProcessed(boolean successfullyProcessed) {
        this.successfullyProcessed = successfullyProcessed;
    }
    
}
