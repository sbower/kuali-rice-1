/*
 * Copyright 2006-2007 The Kuali Foundation
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

package org.kuali.rice.krad.datadictionary;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.krad.datadictionary.exception.AttributeValidationException;

/**
                    The primitiveAttribute element identifies one pair of
                    corresponding fields in the primary business object and
                    the related business object.

                    JSTL: primitiveAttribute is a Map which is accessed by the
                    sequential key of "0", "1", etc.  Each entry contains the following
                    keys:
                        * sourceName (String)
                        * targetName (String)
                    The value corresponding to the sourceName key is the attribute name defined
                    for the primary business object.
                    The value corresponding to the targetName key is the attribute name for
                    the object being referenced by objectAttributeName.
 */
public class PrimitiveAttributeDefinition extends DataDictionaryDefinitionBase {
    private static final long serialVersionUID = -715128943756700821L;
    
	protected String sourceName;
    protected String targetName;

    public PrimitiveAttributeDefinition() {}


    /**
     * @return sourceName
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * sourceName is the name of the POJO property of the business object
     * 
     * @throws IllegalArgumentException if the given sourceName is blank
     */
    public void setSourceName(String sourceName) {
        if (StringUtils.isBlank(sourceName)) {
            throw new IllegalArgumentException("invalid (blank) sourceName");
        }

        this.sourceName = sourceName;
    }


    /**
     * @return targetName
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * targetName is the name of attribute that corresponds to the sourceName in the looked up BO
     * 
     * @throws IllegalArgumentException if the given targetName is blank
     */
    public void setTargetName(String targetName) {
        if (StringUtils.isBlank(targetName)) {
            throw new IllegalArgumentException("invalid (blank) targetName");
        }

        this.targetName = targetName;
    }


    /**
     * Directly validate simple fields.
     * 
     * @see org.kuali.rice.krad.datadictionary.DataDictionaryDefinition#completeValidation(java.lang.Class, java.lang.Object)
     */
    public void completeValidation(Class rootBusinessObjectClass, Class otherBusinessObjectClass) {
        if (!DataDictionary.isPropertyOf(rootBusinessObjectClass, sourceName)) {
            throw new AttributeValidationException("unable to find attribute '" + sourceName + "' in relationship class '" + rootBusinessObjectClass + "' (" + "" + ")");
        }
        if (!DataDictionary.isPropertyOf(otherBusinessObjectClass, targetName)) {
            throw new AttributeValidationException("unable to find attribute '" + targetName + "' in related class '" + otherBusinessObjectClass.getName() + "' (" + "" + ")");
        }

        Class sourceClass = DataDictionary.getAttributeClass(rootBusinessObjectClass, sourceName);
        Class targetClass = DataDictionary.getAttributeClass(otherBusinessObjectClass, targetName);
        if ((null == sourceClass && null != targetClass) || (null != sourceClass && null == targetClass) || !StringUtils.equals(sourceClass.getName(), targetClass.getName())) {            
        	String sourceClassName = rootBusinessObjectClass.getName();
            String targetClassName = otherBusinessObjectClass.getName();
            String sourcePath = sourceClassName + "." + sourceName;
            String targetPath = targetClassName + "." + targetName;
            
            // Just a temp hack to ignore null Person objects
            if ((sourcePath != null && !StringUtils.contains(sourcePath, ".principalId")) && (targetPath != null && !StringUtils.contains(targetPath, ".principalId"))) {
            	throw new AttributeValidationException("source attribute '" + sourcePath + "' (" + sourceClass + ") and target attribute '" + targetPath + "' (" + targetClass + ") are of differing types (" + "" + ")");
            }
        }
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PrimitiveAttributeDefinition (" + getSourceName()+","+getTargetName()+")";
    }
}
