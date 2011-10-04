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
package org.kuali.rice.krad.datadictionary.validation.processor;

import java.util.List;

import org.kuali.rice.core.api.uif.DataType;
import org.kuali.rice.krad.datadictionary.DataDictionaryEntry;
import org.kuali.rice.krad.datadictionary.exception.AttributeValidationException;
import org.kuali.rice.krad.datadictionary.validation.AttributeValueReader;
import org.kuali.rice.krad.datadictionary.validation.DictionaryObjectAttributeValueReader;
import org.kuali.rice.krad.datadictionary.validation.ValidationUtils;
import org.kuali.rice.krad.datadictionary.validation.capability.Constrainable;
import org.kuali.rice.krad.datadictionary.validation.capability.HierarchicallyConstrainable;
import org.kuali.rice.krad.datadictionary.validation.constraint.CaseConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.Constraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.DataTypeConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.WhenConstraint;
import org.kuali.rice.krad.datadictionary.validation.result.DictionaryValidationResult;
import org.kuali.rice.krad.datadictionary.validation.result.ProcessorResult;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;

/**
 * This object processes 'case constraints', which are constraints that are imposed only in specific cases, for example, when a value is
 * equal to some constant, or greater than some limit. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org) 
 */
public class CaseConstraintProcessor extends MandatoryElementConstraintProcessor<CaseConstraint> {

	private static final String CONSTRAINT_NAME = "case constraint";

	/**
	 * @see org.kuali.rice.krad.datadictionary.validation.processor.ConstraintProcessor#process(DictionaryValidationResult, Object, org.kuali.rice.krad.datadictionary.validation.capability.Constrainable, org.kuali.rice.krad.datadictionary.validation.AttributeValueReader)
	 */
	@Override
	public ProcessorResult process(DictionaryValidationResult result, Object value, CaseConstraint caseConstraint, AttributeValueReader attributeValueReader) throws AttributeValidationException {

		// Don't process this constraint if it's null
        if (null == caseConstraint) {
            return new ProcessorResult(result.addNoConstraint(attributeValueReader, CONSTRAINT_NAME));
        }
        AttributeValueReader constraintAttributeReader = ((DictionaryObjectAttributeValueReader)attributeValueReader).clone();
        //AttributeValueReader constraintAttributeReader = attributeValueReader;
        
        
        String operator = (ValidationUtils.hasText(caseConstraint.getOperator())) ? caseConstraint.getOperator() : "EQUALS";
        AttributeValueReader fieldPathReader = (ValidationUtils.hasText(caseConstraint.getFieldPath())) ? getChildAttributeValueReader(caseConstraint.getFieldPath(), attributeValueReader) : attributeValueReader;

        Constrainable caseField = (null != fieldPathReader) ? fieldPathReader.getDefinition(fieldPathReader.getAttributeName()) : null;
        Object fieldValue = (null != fieldPathReader) ? fieldPathReader.getValue(fieldPathReader.getAttributeName()) : value;
        DataType fieldDataType = (null != caseField && caseField instanceof DataTypeConstraint) ? ((DataTypeConstraint)caseField).getDataType() : null;

        // Default to a string comparison
        if (fieldDataType == null)
        	fieldDataType = DataType.STRING;
        
        // If fieldValue is null then skip Case check
        if (null == fieldValue) {
        	// FIXME: not sure if the definition and attribute value reader should change under this case
            return new ProcessorResult(result.addSkipped(attributeValueReader, CONSTRAINT_NAME), caseField, fieldPathReader);
        }

        // Extract value for field Key
        for (WhenConstraint wc : caseConstraint.getWhenConstraint()) {
            
            if (ValidationUtils.hasText(wc.getValuePath())){
                Object whenValue = null;
                AttributeValueReader whenValueReader = getChildAttributeValueReader(wc.getValuePath(), attributeValueReader);
                whenValue = whenValueReader.getValue(whenValueReader.getAttributeName());
                if (ValidationUtils.compareValues(fieldValue, whenValue, fieldDataType, operator, caseConstraint.isCaseSensitive(), dateTimeService) && null != wc.getConstraint()) {                    
                    return new ProcessorResult(result.addSuccess(attributeValueReader, CONSTRAINT_NAME), null, constraintAttributeReader, wc.getConstraint());
                }
            } else {
            	List<Object> whenValueList = wc.getValues();
    
            	for (Object whenValue : whenValueList) {
            		if (ValidationUtils.compareValues(fieldValue, whenValue, fieldDataType, operator, caseConstraint.isCaseSensitive(), dateTimeService) && null != wc.getConstraint()) {
            			return new ProcessorResult(result.addSuccess(attributeValueReader, CONSTRAINT_NAME), null, constraintAttributeReader, wc.getConstraint());
            		}
            	}
            }
        }

        // Assuming that not finding any case constraints is equivalent to 'skipping' the constraint
        return new ProcessorResult(result.addSkipped(attributeValueReader, CONSTRAINT_NAME));
	}
	
	@Override 
	public String getName() {
		return CONSTRAINT_NAME;
	}
	
	/**
	 * @see org.kuali.rice.krad.datadictionary.validation.processor.ConstraintProcessor#getConstraintType()
	 */
	@Override
	public Class<? extends Constraint> getConstraintType() {
		return CaseConstraint.class;
	}
	
	private AttributeValueReader getChildAttributeValueReader(String key, AttributeValueReader attributeValueReader) throws AttributeValidationException {
		String[] lookupPathTokens = ValidationUtils.getPathTokens(key);
		
		AttributeValueReader localAttributeValueReader = attributeValueReader;
		for(int i = 0; i < lookupPathTokens.length; i++) {
			for (Constrainable definition : localAttributeValueReader.getDefinitions()) {
				String attributeName = definition.getName();
				if (attributeName.equals(lookupPathTokens[i])) {
					if(i==lookupPathTokens.length-1){
						localAttributeValueReader.setAttributeName(attributeName);
						return localAttributeValueReader;
					}
					if (definition instanceof HierarchicallyConstrainable) {
						String childEntryName = ((HierarchicallyConstrainable)definition).getChildEntryName();
						DataDictionaryEntry entry = KRADServiceLocatorWeb.getDataDictionaryService().getDataDictionary().getDictionaryObjectEntry(childEntryName);
						Object value = attributeValueReader.getValue(attributeName);
						attributeValueReader.setAttributeName(attributeName);
						String attributePath = attributeValueReader.getPath();
						localAttributeValueReader = new DictionaryObjectAttributeValueReader(value, childEntryName, entry, attributePath);
					} 
					break;
				}
			}
		 }
		return null;
	}
	
}
