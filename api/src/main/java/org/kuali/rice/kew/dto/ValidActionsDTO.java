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

import java.io.Serializable;

/**
 * A transport object containing information about actions
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ValidActionsDTO implements Serializable {

    private static final long serialVersionUID = 8629234634226269476L;

    private String[] validActionCodesAllowed;

    public ValidActionsDTO() {}

    public String[] getValidActionCodesAllowed() {
        return validActionCodesAllowed;
    }

    public void setValidActionCodesAllowed(String[] validActionCodesAllowed) {
        this.validActionCodesAllowed = validActionCodesAllowed;
    }

    public void addValidActionsAllowed(String actionTakenCodeAllowed) {
        if (getValidActionCodesAllowed() == null) {
            setValidActionCodesAllowed(new String[0]);
        }
        String[] newValidActionsAllowed = new String[getValidActionCodesAllowed().length+1];
        System.arraycopy(getValidActionCodesAllowed(), 0, newValidActionsAllowed, 0, getValidActionCodesAllowed().length);
        newValidActionsAllowed[getValidActionCodesAllowed().length] = actionTakenCodeAllowed;
        setValidActionCodesAllowed(newValidActionsAllowed);
    }

    public boolean contains(String actionTakenCode) {
    	for (int index = 0; index < validActionCodesAllowed.length; index++) {
    		if (actionTakenCode.equals(validActionCodesAllowed[index])) {
    			return true;
    		}
    	}
    	return false;
    }

}
