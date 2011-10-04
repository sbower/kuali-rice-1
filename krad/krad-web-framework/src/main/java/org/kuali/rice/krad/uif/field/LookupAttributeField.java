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
package org.kuali.rice.krad.uif.field;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.datadictionary.AttributeDefinition;
import org.kuali.rice.krad.uif.control.CheckboxControl;
import org.kuali.rice.krad.uif.control.Control;
import org.kuali.rice.krad.uif.control.RadioGroupControl;
import org.kuali.rice.krad.uif.control.TextAreaControl;
import org.kuali.rice.krad.uif.util.ComponentFactory;
import org.kuali.rice.krad.uif.util.ComponentUtils;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADPropertyConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom <code>AttributeField</code> for search fields within a lookup view
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LookupAttributeField extends AttributeField {
    private static final long serialVersionUID = -8294275596836322699L;

    protected boolean treatWildcardsAndOperatorsAsLiteral = false;

    public LookupAttributeField() {
        super();
    }

    /**
     * @return the treatWildcardsAndOperatorsAsLiteral
     */
    public boolean isTreatWildcardsAndOperatorsAsLiteral() {
        return this.treatWildcardsAndOperatorsAsLiteral;
    }

    /**
     * @param treatWildcardsAndOperatorsAsLiteral the treatWildcardsAndOperatorsAsLiteral to set
     */
    public void setTreatWildcardsAndOperatorsAsLiteral(boolean treatWildcardsAndOperatorsAsLiteral) {
        this.treatWildcardsAndOperatorsAsLiteral = treatWildcardsAndOperatorsAsLiteral;
    }

    /**
     * Override of AttributeField copy to setup properties necessary to make the field usable for inputting
     * search criteria
     *
     * @param attributeDefinition - AttributeDefinition instance the property values should be copied from
     * @see org.kuali.rice.krad.uif.field.AttributeField#copyFromAttributeDefinition(org.kuali.rice.krad.datadictionary.AttributeDefinition)
     */
    @Override
    public void copyFromAttributeDefinition(AttributeDefinition attributeDefinition) {
        // label
        if (StringUtils.isEmpty(getLabel())) {
            setLabel(attributeDefinition.getLabel());
        }

        // short label
        if (StringUtils.isEmpty(getShortLabel())) {
            setShortLabel(attributeDefinition.getShortLabel());
        }

        // security
        if (getAttributeSecurity() == null) {
            setAttributeSecurity(attributeDefinition.getAttributeSecurity());
        }

        // options
        if (getOptionsFinder() == null) {
            setOptionsFinder(attributeDefinition.getOptionsFinder());
        }

        // TODO: what about formatter?

        // use control from dictionary if not specified and convert for searching
        if (getControl() == null) {
            setControl(convertControlToLookupControl(attributeDefinition));
        }

        // overwrite maxLength to allow for wildcards and ranges
        setMaxLength(100);

        // set default value for active field to true
        if (StringUtils.isEmpty(getDefaultValue())) {
            if ((StringUtils.equals(getPropertyName(), KRADPropertyConstants.ACTIVE))) {
                setDefaultValue(KRADConstants.YES_INDICATOR_VALUE);
            }
        }

        /*
           * TODO delyea: FieldUtils.createAndPopulateFieldsForLookup used to allow for a set of property names to be passed in via the URL
           * parameters of the lookup url to set fields as 'read only'
           */

    }

    /**
     * If control definition is defined on the given attribute definition, converts to an appropriate control for
     * searching (if necessary) and returns a copy for setting on the field
     *
     * @param attributeDefinition - attribute definition instance to retrieve control from
     * @return Control instance or null if not found
     */
    protected static Control convertControlToLookupControl(AttributeDefinition attributeDefinition) {
        if (attributeDefinition.getControlField() == null) {
            return null;
        }

        Control newControl = null;

        // convert checkbox to radio with yes/no/both options
        if (CheckboxControl.class.isAssignableFrom(attributeDefinition.getControlField().getClass())) {
            newControl = ComponentFactory.getRadioGroupControlHorizontal();
            List<KeyValue> options = new ArrayList<KeyValue>();
            options.add(new ConcreteKeyValue("Y", "Yes"));
            options.add(new ConcreteKeyValue("N", "No"));
            options.add(new ConcreteKeyValue("", "Both"));

            ((RadioGroupControl) newControl).setOptions(options);
        }
        // text areas get converted to simple text inputs
        else if (TextAreaControl.class.isAssignableFrom(attributeDefinition.getControlField().getClass())) {
            newControl = ComponentFactory.getTextControl();
        } else {
            newControl = ComponentUtils.copy(attributeDefinition.getControlField(), "");
        }

        return newControl;
    }
}
