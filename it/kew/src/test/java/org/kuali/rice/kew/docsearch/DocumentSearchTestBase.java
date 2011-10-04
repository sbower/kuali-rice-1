/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kew.docsearch;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.kuali.rice.kew.api.document.lookup.DocumentLookupCriteria;
import org.kuali.rice.kew.docsearch.xml.StandardGenericXMLSearchableAttribute;
import org.kuali.rice.kew.test.KEWTestCase;

/**
 * This is a base class used for document search unit test classes to consolidate some helper methods
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@Ignore("This class is a helper class only with no specific tests within")
public class DocumentSearchTestBase extends KEWTestCase {

    /**
     * This method is used by tests that use xml attributes
     *
     * @param name - name of the attribute to retrieve
     * @return
     */
    protected StandardGenericXMLSearchableAttribute getAttribute(String name) {
        String attName = name;
        if (attName == null) {
            attName = "XMLSearchableAttribute";
        }
        StandardGenericXMLSearchableAttribute attribute = new StandardGenericXMLSearchableAttribute();
        return attribute;
    }

    protected void addSearchableAttribute(DocumentLookupCriteria.Builder criteria, String name, String value) {
        criteria.addDocumentAttributeValue(name, value);
    }

    protected void addSearchableAttribute(DocumentLookupCriteria.Builder criteria, String name, String[] values) {
        for (String value : values) {
            addSearchableAttribute(criteria, name, value);
        }
    }

    protected void addSearchableAttributeRange(DocumentLookupCriteria.Builder criteria, String name, String lowerBound, String upperBound, boolean upperBoundInclusive) {
        StringBuilder value = new StringBuilder();
        if (StringUtils.isNotBlank(lowerBound) && StringUtils.isNotBlank(upperBound)) {
            value.append(lowerBound);
            if (upperBoundInclusive) {
                value.append("..");
            } else {
                value.append("...");
            }
            value.append(upperBound);
        } else if (StringUtils.isBlank(upperBound)) {
            value.append(">= ").append(lowerBound);
        } else {
            value.append("<");
            if (upperBoundInclusive) {
                value.append("=");
            }
            value.append(" ").append(upperBound);
        }
        addSearchableAttribute(criteria, name, value.toString());
    }

//    protected SearchAttributeCriteriaComponent createSearchAttributeCriteriaComponent(String key,String value,DocumentType docType) {
//        return createSearchAttributeCriteriaComponent(key, value, null, docType);
//    }
//
//    protected SearchAttributeCriteriaComponent createSearchAttributeCriteriaComponent(String key,String[] values,DocumentType docType) {
//        return createSearchAttributeCriteriaComponent(key, values, null, docType);
//    }
//
//    protected SearchAttributeCriteriaComponent createSearchAttributeCriteriaComponent(String key,String value,Boolean isLowerBoundValue,DocumentType docType) {
//        String formKey = (isLowerBoundValue == null) ? key : ((isLowerBoundValue != null && isLowerBoundValue.booleanValue()) ? KEWConstants.SearchableAttributeConstants.RANGE_LOWER_BOUND_PROPERTY_PREFIX + key : KEWConstants.SearchableAttributeConstants.RANGE_UPPER_BOUND_PROPERTY_PREFIX + key);
//        String savedKey = key;
//        SearchAttributeCriteriaComponent sacc = new SearchAttributeCriteriaComponent(formKey,value,savedKey);
//        applyAttributeFieldToComponent(sacc, docType, formKey);
//        return sacc;
//    }
//
//    protected SearchAttributeCriteriaComponent createSearchAttributeCriteriaComponent(String key,String[] values,Boolean isLowerBoundValue,DocumentType docType) {
//        String formKey = (isLowerBoundValue == null) ? key : ((isLowerBoundValue != null && isLowerBoundValue.booleanValue()) ? KEWConstants.SearchableAttributeConstants.RANGE_LOWER_BOUND_PROPERTY_PREFIX + key : KEWConstants.SearchableAttributeConstants.RANGE_UPPER_BOUND_PROPERTY_PREFIX + key);
//        String savedKey = key;
//        SearchAttributeCriteriaComponent sacc = new SearchAttributeCriteriaComponent(formKey,null,savedKey);
//        sacc.setValues(Arrays.asList(values));
//        applyAttributeFieldToComponent(sacc, docType, formKey);
//        return sacc;
//    }
//
//    private void applyAttributeFieldToComponent(SearchAttributeCriteriaComponent sacc, DocumentType docType, String formKey) {
//        RemotableAttributeField field = getFieldByFormKey(docType, formKey);
//        if (field != null) {
//            sacc.setSearchableAttributeValue(DocSearchUtils.getSearchableAttributeValueByDataTypeString(field.getDataType()));
//            boolean isRange = field.getAttributeLookupSettings() != null && field.getAttributeLookupSettings().isRanged();
//            sacc.setRangeSearch(isRange);
//            if (field.getAttributeLookupSettings().isCaseSensitive() != null) {
//                sacc.setCaseSensitive(field.getAttributeLookupSettings().isCaseSensitive());
//            }
//            if (isRange) {
//                if (field.getAttributeLookupSettings().getLowerBoundName().equals(formKey)) {
//                    sacc.setSearchInclusive(field.getAttributeLookupSettings().isLowerBoundInclusive());
//                } else if (field.getAttributeLookupSettings().getUpperBoundName().equals(formKey)) {
//                    sacc.setSearchInclusive(field.getAttributeLookupSettings().isUpperBoundInclusive());
//                } else {
//                    throw new IllegalStateException("Encountered an invalid ranged attribute field definition.");
//                }
//            }
//            boolean canHoldMultipleValues = field.getControl() instanceof Select &&
//                    ((Select) field.getControl()).isMultiple();
//            sacc.setCanHoldMultipleValues(canHoldMultipleValues);
//        }
//    }
//
//    private RemotableAttributeField getFieldByFormKey(DocumentType docType, String formKey) {
//        if (docType == null) {
//            return null;
//        }
//        for (DocumentType.ExtensionHolder<SearchableAttribute> holder : docType.loadSearchableAttributes()) {
//            for (RemotableAttributeField field : holder.getExtension().getSearchFields(holder.getExtensionDefinition(),
//                    docType.getName())) {
//                if (field.getName().equals(formKey)) {
//                    return field;
//                } else if (field.getAttributeLookupSettings() != null) {
//                    if (field.getName().equals(field.getAttributeLookupSettings().getLowerBoundName()) ||
//                            field.getName().equals(field.getAttributeLookupSettings().getUpperBoundName())) {
//                        return field;
//                    }
//                }
//            }
//        }
//        return null;
//    }

}
