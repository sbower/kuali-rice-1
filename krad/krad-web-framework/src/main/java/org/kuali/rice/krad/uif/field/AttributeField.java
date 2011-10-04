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
package org.kuali.rice.krad.uif.field;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.core.web.format.Formatter;
import org.kuali.rice.krad.bo.DataObjectRelationship;
import org.kuali.rice.krad.bo.KualiCode;
import org.kuali.rice.krad.datadictionary.AttributeDefinition;
import org.kuali.rice.krad.datadictionary.AttributeSecurity;
import org.kuali.rice.krad.datadictionary.validation.constraint.CaseConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.MustOccurConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.PrerequisiteConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.SimpleConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.ValidCharactersConstraint;
import org.kuali.rice.krad.keyvalues.KeyValuesFinder;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinder;
import org.kuali.rice.krad.uif.view.FormView;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.control.Control;
import org.kuali.rice.krad.uif.control.MultiValueControlBase;
import org.kuali.rice.krad.uif.component.BindingInfo;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.component.DataBinding;
import org.kuali.rice.krad.uif.util.ClientValidationUtils;
import org.kuali.rice.krad.uif.util.ComponentUtils;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.util.ViewModelUtils;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.rice.krad.uif.widget.DirectInquiry;
import org.kuali.rice.krad.uif.widget.Inquiry;
import org.kuali.rice.krad.uif.widget.QuickFinder;
import org.kuali.rice.krad.uif.widget.Suggest;
import org.kuali.rice.krad.util.KRADPropertyConstants;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.valuefinder.ValueFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Field that encapsulates data input/output captured by an attribute within the
 * application
 *
 * <p>                                                                                                                                    R
 * The <code>AttributField</code> provides the majority of the data input/output
 * for the screen. Through these fields the model can be displayed and updated.
 * For data input, the field contains a <code>Control</code> instance will
 * render an HTML control element(s). The attribute field also contains a
 * <code>LabelField</code>, summary, and widgets such as a quickfinder (for
 * looking up values) and inquiry (for getting more information on the value).
 * <code>AttributeField</code> instances can have associated messages (errors)
 * due to invalid input or business rule failures. Security can also be
 * configured to restrict who may view the fields value.
 * </p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class AttributeField extends FieldBase implements DataBinding {
    private static final long serialVersionUID = -3703656713706343840L;

    // value props
    private String defaultValue;
    private Class<? extends ValueFinder> defaultValueFinderClass;

    // constraint variables
    private String customValidatorClass;
    private ValidCharactersConstraint validCharactersConstraint;
    private CaseConstraint caseConstraint;
    private List<PrerequisiteConstraint> dependencyConstraints;
    private List<MustOccurConstraint> mustOccurConstraints;
    private SimpleConstraint simpleConstraint;

    private Formatter formatter;
    private KeyValuesFinder optionsFinder;

    // binding
    private String propertyName;
    private BindingInfo bindingInfo;

    private String dictionaryAttributeName;
    private String dictionaryObjectEntry;

    // display props
    private Control control;

    private String errorMessagePlacement;
    private ErrorsField errorsField;

    // messages
    private String constraintText;
    private String instructionalText;

    private MessageField instructionalMessageField;
    private MessageField constraintMessageField;

    private String helpSummary;
    private String helpDescription;

    private AttributeSecurity attributeSecurity;

    // Alternate and additional display properties
    protected String alternateDisplayPropertyName;
    protected String additionalDisplayPropertyName;

    private String alternateDisplayValue;
    private String additionalDisplayValue;

    private List<String> informationalDisplayPropertyNames;
    private List<String> hiddenPropertyNames;

    private AttributeQuery fieldAttributeQuery;

    private boolean escapeHtmlInPropertyValue;

    private boolean performUppercase;

    // widgets
    private Inquiry fieldInquiry;
    private QuickFinder fieldLookup;
    private DirectInquiry fieldDirectInquiry;
    private Suggest fieldSuggest;

    public AttributeField() {
        super();

        simpleConstraint = new SimpleConstraint();
        informationalDisplayPropertyNames = new ArrayList<String>();
        hiddenPropertyNames = new ArrayList<String>();
    }

    /**
     * The following initialization is performed:
     *
     * <ul>
     * <li>Set defaults for binding</li>
     * <li>Default the model path if not set</li>
     * </ul>
     *
     * @see org.kuali.rice.krad.uif.component.ComponentBase#performInitialization(org.kuali.rice.krad.uif.view.View, java.lang.Object)
     */
    @Override
    public void performInitialization(View view, Object model) {
        super.performInitialization(view, model);

        if (bindingInfo != null) {
            bindingInfo.setDefaults(view, getPropertyName());
        }
    }

    /**
     * The following actions are performed:
     *
     * <ul>
     * <li>Set the ids for the various attribute components</li>
     * <li>Sets up the client side validation for constraints on this field. In
     * addition, it sets up the messages applied to this field</li>
     * </ul>
     *
     * @see org.kuali.rice.krad.uif.component.ComponentBase#performFinalize(org.kuali.rice.krad.uif.view.View,
     *      java.lang.Object, org.kuali.rice.krad.uif.component.Component)
     */
    @Override
    public void performFinalize(View view, Object model, Component parent) {
        super.performFinalize(view, model, parent);

        setupIds();

        // Sets message
        if (StringUtils.isNotBlank(instructionalText)) {
            instructionalMessageField.setMessageText(instructionalText);
        }

        // Sets constraints
        if (StringUtils.isNotBlank(constraintText)) {
            constraintMessageField.setMessageText(constraintText);
        }

        // Additional and Alternate display value
        setAlternateAndAdditionalDisplayValue(view, model);

        // adjust paths on informational property names
        List<String> informationalPropertyPaths = new ArrayList<String>();
        for (String infoPropertyName : getInformationalDisplayPropertyNames()) {
            String infoPropertyPath = getBindingInfo().getPropertyAdjustedBindingPath(infoPropertyName);
            informationalPropertyPaths.add(infoPropertyPath);
        }
        this.informationalDisplayPropertyNames = informationalPropertyPaths;

        // adjust the path for hidden fields
        // TODO: should this check the view#readOnly?
        List<String> hiddenPropertyPaths = new ArrayList<String>();
        for (String hiddenPropertyName : getHiddenPropertyNames()) {
            String hiddenPropertyPath = getBindingInfo().getPropertyAdjustedBindingPath(hiddenPropertyName);
            hiddenPropertyPaths.add(hiddenPropertyPath);
        }
        this.hiddenPropertyNames = hiddenPropertyPaths;

        // if read only or the control is not set no need to set client side
        // validation
        if (isReadOnly() || getControl() == null) {
            return;
        }

        setupFieldQuery();

        // invoke options finder if options not configured on the control
        if ((optionsFinder != null) && (control != null) && control instanceof MultiValueControlBase) {
            MultiValueControlBase multiValueControl = (MultiValueControlBase) control;
            if ((multiValueControl.getOptions() == null) || multiValueControl.getOptions().isEmpty()) {
                List<KeyValue> options = new ArrayList<KeyValue>();
                if (optionsFinder instanceof UifKeyValuesFinder) {
                    options = ((UifKeyValuesFinder) optionsFinder).getKeyValues((ViewModel) model);
                }
                else {
                    options = optionsFinder.getKeyValues();
                }

                multiValueControl.setOptions(options);
            }
        }

        if (view instanceof FormView && ((FormView) view).isValidateClientSide()) {
            ClientValidationUtils.processAndApplyConstraints(this, view);
        }
    }

    /**
     * Performs setup of the field attribute query and informational display properties. Paths
     * are adjusted to match the binding for the this field, and the necessary onblur script for
     * triggering the query client side is constructed
     */
    protected void setupFieldQuery() {
        if (getFieldAttributeQuery() != null) {
            // adjust paths on query mappings
            getFieldAttributeQuery().updateQueryFieldMapping(getBindingInfo());
            getFieldAttributeQuery().updateReturnFieldMapping(getBindingInfo());
            getFieldAttributeQuery().updateQueryMethodArgumentFieldList(getBindingInfo());

            // build onblur script for field query
            String script = "executeFieldQuery('" + getControl().getId() + "',";
            script += "'" + getId() + "'," + getFieldAttributeQuery().getQueryFieldMappingJsString() + ",";
            script += getFieldAttributeQuery().getQueryMethodArgumentFieldsJsString() + ",";
            script += getFieldAttributeQuery().getReturnFieldMappingJsString() + ");";

            if (StringUtils.isNotBlank(getControl().getOnBlurScript())) {
                script = getControl().getOnBlurScript() + script;
            }
            getControl().setOnBlurScript(script);
        }
    }

    /**
     * Sets alternate and additional property value for this field.
     *
     * <p>
     * If <code>AttributeSecurity</code> present in this field, make sure the current user has permission to view the
     * field value. If user doesn't have permission to view the value, mask the value as configured and set it
     * as alternate value for display. If security doesn't exists for this field but
     * <code>alternateDisplayPropertyName</code> present, get its value and format it based on that
     * fields formatting and set for display.
     * </p>
     *
     * <p>
     * For additional display value, if <code>AttributeSecurity</code> not present, sets the value if
     * <code>additionalDisplayPropertyName</code> present. If not present, check whether this field is a
     * <code>KualiCode</code> and get the relationship configured in the datadictionary file and set the name
     * additional display value which will be displayed along with the code. If additional display property not
     * present,
     * check whether this field is has <code>MultiValueControlBase</code>. If yes, get the Label for the value and
     * set it as additional display value.
     * </p>
     *
     * @param view - the current view instance
     * @param model - model instance
     */
    protected void setAlternateAndAdditionalDisplayValue(View view, Object model) {
        boolean additionalValueSet = false;
        boolean alternateValueSet = false;

        // check whether field value needs to be masked, and if so apply masking as alternateDisplayValue
        if (getAttributeSecurity() != null) {
            //TODO: Check authorization
            // if (attributeSecurity.isMask() && !boAuthzService.canFullyUnmaskField(user,dataObjectClass, field.getPropertyName(), null)) {
            Object fieldValue = ObjectPropertyUtils.getPropertyValue(model, getBindingInfo().getBindingPath());
            alternateDisplayValue = getSecuredFieldValue(attributeSecurity, fieldValue);

            setReadOnly(true);
            return;
        }

        // if not read only, return without trying to set alternate and additional values
        if (!isReadOnly()) {
            return;
        }

        // if field is not secure, check for alternate and additional display properties
        if (StringUtils.isNotBlank(getAlternateDisplayPropertyName())) {
            String alternateDisplayPropertyPath =
                    getBindingInfo().getPropertyAdjustedBindingPath(getAlternateDisplayPropertyName());

            Object alternateFieldValue = ObjectPropertyUtils.getPropertyValue(model, alternateDisplayPropertyPath);
            if (alternateFieldValue != null) {
                // TODO: format by type
                alternateDisplayValue = alternateFieldValue.toString();
                alternateValueSet = true;
            }
        }

        // perform automatic translation for code references if enabled on view
        if (StringUtils.isBlank(getAdditionalDisplayPropertyName()) && view.isTranslateCodes()) {
            // check for any relationship present for this field and it's of type KualiCode
            Class<?> parentObjectClass = ViewModelUtils.getParentObjectClassForMetadata(view, model, this);
            DataObjectRelationship relationship = KRADServiceLocatorWeb.getDataObjectMetaDataService()
                    .getDataObjectRelationship(null, parentObjectClass, getBindingInfo().getBindingName(), "", true,
                            false, false);

            if (relationship != null && getPropertyName().startsWith(relationship.getParentAttributeName()) &&
                    KualiCode.class.isAssignableFrom(relationship.getRelatedClass())) {
                additionalDisplayPropertyName =
                        relationship.getParentAttributeName() + "." + KRADPropertyConstants.NAME;
            }
        }

        // now check for an additional display property and if set get the value
        if (StringUtils.isNotBlank(getAdditionalDisplayPropertyName())) {
            String additionalDisplayPropertyPath =
                    getBindingInfo().getPropertyAdjustedBindingPath(getAdditionalDisplayPropertyName());

            Object additionalFieldValue = ObjectPropertyUtils.getPropertyValue(model, additionalDisplayPropertyPath);
            if (additionalFieldValue != null) {
                // TODO: format by type
                additionalDisplayValue = additionalFieldValue.toString();
                additionalValueSet = true;
            }
        }

        // if alternateValue and additionalValue not set yet check whether the field has multi value control
        if (!alternateValueSet && !additionalValueSet &&
                (control != null && control instanceof MultiValueControlBase)) {
            MultiValueControlBase multiValueControl = (MultiValueControlBase) control;
            if (multiValueControl.getOptions() != null) {
                for (KeyValue keyValue : multiValueControl.getOptions()) {
                    Object fieldValue = ObjectPropertyUtils.getPropertyValue(model, getBindingInfo().getBindingPath());

                    if (StringUtils.equals((String) fieldValue, keyValue.getKey())) {
                        alternateDisplayValue = keyValue.getValue();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Helper method which returns the masked value based on the <code>AttributeSecurity</code>
     *
     * @param attributeSecurity attribute security to check
     * @param fieldValue field value
     * @return masked value
     */
    private String getSecuredFieldValue(AttributeSecurity attributeSecurity, Object fieldValue) {
        if (attributeSecurity.isMask()) {
            return attributeSecurity.getMaskFormatter().maskValue(fieldValue);
        } else if (getAttributeSecurity().isPartialMask()) {
            return attributeSecurity.getPartialMaskFormatter().maskValue(fieldValue);
        } else {
            throw new RuntimeException("Encountered unsupported Attribute Security..");
        }
    }

    /**
     * Sets the ids on all components the attribute field uses so they will all
     * contain this attribute's id in their ids. This is useful for jQuery
     * manipulation.
     */
    private void setupIds() {
        // update ids so they all match the attribute
        if (getControl() != null) {
            getControl().setId(getId());
        }

        setNestedComponentIdAndSuffix(getErrorsField(), UifConstants.IdSuffixes.ERRORS);
        setNestedComponentIdAndSuffix(getLabelField(), UifConstants.IdSuffixes.LABEL);
        setNestedComponentIdAndSuffix(getInstructionalMessageField(), UifConstants.IdSuffixes.INSTRUCTIONAL);
        setNestedComponentIdAndSuffix(getConstraintMessageField(), UifConstants.IdSuffixes.CONSTRAINT);
        setNestedComponentIdAndSuffix(getFieldLookup(), UifConstants.IdSuffixes.QUICK_FINDER);
        setNestedComponentIdAndSuffix(getFieldDirectInquiry(), UifConstants.IdSuffixes.DIRECT_INQUIRY);
        setNestedComponentIdAndSuffix(getFieldSuggest(), UifConstants.IdSuffixes.SUGGEST);

        setId(getId() + UifConstants.IdSuffixes.ATTRIBUTE);
    }

    /**
     * Helper method for suffixing the ids of the fields nested components
     *
     * @param component - component to adjust id for
     * @param suffix - suffix to append to id
     */
    private void setNestedComponentIdAndSuffix(Component component, String suffix) {
        if (component != null) {
            String fieldId = getId();
            fieldId += suffix;

            component.setId(fieldId);
        }
    }

    /**
     * Defaults the properties of the <code>AttributeField</code> to the
     * corresponding properties of its <code>AttributeDefinition</code>
     * retrieved from the dictionary (if such an entry exists). If the field
     * already contains a value for a property, the definitions value is not
     * used.
     *
     * @param attributeDefinition - AttributeDefinition instance the property values should be
     * copied from
     */
    public void copyFromAttributeDefinition(AttributeDefinition attributeDefinition) {
        // label
        if (StringUtils.isEmpty(getLabel())) {
            setLabel(attributeDefinition.getLabel());
        }

        // short label
        if (StringUtils.isEmpty(getShortLabel())) {
            setShortLabel(attributeDefinition.getShortLabel());
        }

        // max length
        if (getMaxLength() == null) {
            setMaxLength(attributeDefinition.getMaxLength());
        }

        // max length
        if (getMinLength() == null) {
            setMinLength(attributeDefinition.getMinLength());
        }

        // valid characters
        if (getValidCharactersConstraint() == null) {
            setValidCharactersConstraint(attributeDefinition.getValidCharactersConstraint());
        }

        if (getCaseConstraint() == null) {
            setCaseConstraint(attributeDefinition.getCaseConstraint());
        }

        if (getDependencyConstraints() == null) {
            setDependencyConstraints(attributeDefinition.getPrerequisiteConstraints());
        }

        if (getMustOccurConstraints() == null) {
            setMustOccurConstraints(attributeDefinition.getMustOccurConstraints());
        }

        // required
        if (getRequired() == null) {
            setRequired(attributeDefinition.isRequired());

            //if still null, default to false
            if (getRequired() == null) {
                setRequired(false);
            }
        }

        // control
        if ((getControl() == null) && (attributeDefinition.getControlField() != null)) {
            setControl(ComponentUtils.copy(attributeDefinition.getControlField()));
        }

        // summary
        if (StringUtils.isEmpty(getHelpSummary())) {
            setHelpSummary(attributeDefinition.getSummary());
        }

        // description
        if (StringUtils.isEmpty(getHelpDescription())) {
            setHelpDescription(attributeDefinition.getDescription());
        }

        // security
        if (getAttributeSecurity() == null) {
            attributeSecurity = attributeDefinition.getAttributeSecurity();
        }

        // constraint
        if (StringUtils.isEmpty(getConstraintText())) {
            setConstraintText(attributeDefinition.getConstraintText());
            getConstraintMessageField().setMessageText(attributeDefinition.getConstraintText());
        }

        // options
        if (getOptionsFinder() == null) {
            setOptionsFinder(attributeDefinition.getOptionsFinder());
        }

        //alternate property name and binding object
        if (getAlternateDisplayPropertyName() == null &&
                StringUtils.isNotBlank(attributeDefinition.getAlternateDisplayAttributeName())) {
            setAlternateDisplayPropertyName(attributeDefinition.getAlternateDisplayAttributeName());
        }

        //additional property display name and binding object
        if (getAdditionalDisplayPropertyName() == null &&
                StringUtils.isNotBlank(attributeDefinition.getAdditionalDisplayAttributeName())) {
            setAdditionalDisplayPropertyName(attributeDefinition.getAdditionalDisplayAttributeName());
        }

        if (getFormatter() == null && StringUtils.isNotBlank(attributeDefinition.getFormatterClass())) {
            Class clazz = null;
            try {
                clazz = Class.forName(attributeDefinition.getFormatterClass());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to get class from name: " + attributeDefinition.getFormatterClass(),
                        e);
            }

            Formatter formatter = (Formatter) ObjectUtils.newInstance(clazz);
            setFormatter(formatter);
        }
    }

    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#getNestedComponents()
     */
    @Override
    public List<Component> getNestedComponents() {
        List<Component> components = super.getNestedComponents();

        components.add(control);
        components.add(errorsField);
        components.add(fieldLookup);
        components.add(fieldInquiry);
        components.add(fieldDirectInquiry);
        components.add(fieldSuggest);

        return components;
    }

    /**
     * @see org.kuali.rice.krad.uif.component.DataBinding#getPropertyName()
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * Setter for the component's property name
     *
     * @param propertyName
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Default value for the model property the field points to
     *
     * <p>
     * When a new <code>View</code> instance is requested, the corresponding
     * model will be newly created. During this initialization process the value
     * for the model property will be set to the given default value (if set)
     * </p>
     *
     * @return String default value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Setter for the fields default value
     *
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Gives Class that should be invoked to produce the default value for the
     * field
     *
     * @return Class<? extends ValueFinder> default value finder class
     */
    public Class<? extends ValueFinder> getDefaultValueFinderClass() {
        return this.defaultValueFinderClass;
    }

    /**
     * Setter for the default value finder class
     *
     * @param defaultValueFinderClass
     */
    public void setDefaultValueFinderClass(Class<? extends ValueFinder> defaultValueFinderClass) {
        this.defaultValueFinderClass = defaultValueFinderClass;
    }

    /**
     * <code>Formatter</code> instance that should be used when displaying and
     * accepting the field's value in the user interface
     *
     * <p>
     * Formatters can provide conversion between datatypes in addition to
     * special string formatting such as currency display
     * </p>
     *
     * @return Formatter instance
     * @see org.kuali.rice.core.web.format.Formatter
     */
    public Formatter getFormatter() {
        return this.formatter;
    }

    /**
     * Setter for the field's formatter
     *
     * @param formatter
     */
    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    /**
     * @see org.kuali.rice.krad.uif.component.DataBinding#getBindingInfo()
     */
    public BindingInfo getBindingInfo() {
        return this.bindingInfo;
    }

    /**
     * Setter for the field's binding info
     *
     * @param bindingInfo
     */
    public void setBindingInfo(BindingInfo bindingInfo) {
        this.bindingInfo = bindingInfo;
    }

    /**
     * <code>Control</code> instance that should be used to input data for the
     * field
     *
     * <p>
     * When the field is editable, the control will be rendered so the user can
     * input a value(s). Controls typically are part of a Form and render
     * standard HTML control elements such as text input, select, and checkbox
     * </p>
     *
     * @return Control instance
     */
    public Control getControl() {
        return this.control;
    }

    /**
     * Setter for the field's control
     *
     * @param control
     */
    public void setControl(Control control) {
        this.control = control;
    }

    public String getErrorMessagePlacement() {
        return this.errorMessagePlacement;
    }

    public void setErrorMessagePlacement(String errorMessagePlacement) {
        this.errorMessagePlacement = errorMessagePlacement;
    }

    /**
     * Field that contains the messages (errors) for the attribute field. The
     * <code>ErrorsField</code> holds configuration on associated messages along
     * with information on rendering the messages in the user interface
     *
     * @return ErrorsField instance
     */
    public ErrorsField getErrorsField() {
        return this.errorsField;
    }

    /**
     * Setter for the attribute field's errors field
     *
     * @param errorsField
     */
    public void setErrorsField(ErrorsField errorsField) {
        this.errorsField = errorsField;
    }

    /**
     * Name of the attribute within the data dictionary the attribute field is
     * associated with
     *
     * <p>
     * During the initialize phase for the <code>View</code>, properties for
     * attribute fields are defaulted from a corresponding
     * <code>AttributeDefinition</code> in the data dictionary. Based on the
     * propertyName and parent object class the framework attempts will
     * determine the attribute definition that is associated with the field and
     * set this property. However this property can also be set in the fields
     * configuration to use another dictionary attribute.
     * </p>
     * <p>
     * The attribute name is used along with the dictionary object entry to find
     * the <code>AttributeDefinition</code>
     * </p>
     *
     * @return String attribute name
     */
    public String getDictionaryAttributeName() {
        return this.dictionaryAttributeName;
    }

    /**
     * Setter for the dictionary attribute name
     *
     * @param dictionaryAttributeName
     */
    public void setDictionaryAttributeName(String dictionaryAttributeName) {
        this.dictionaryAttributeName = dictionaryAttributeName;
    }

    /**
     * Object entry name in the data dictionary the associated attribute is
     * apart of
     *
     * <p>
     * During the initialize phase for the <code>View</code>, properties for
     * attribute fields are defaulted from a corresponding
     * <code>AttributeDefinition</code> in the data dictionary. Based on the
     * parent object class the framework will determine the object entry for the
     * associated attribute. However the object entry can be set in the field's
     * configuration to use another object entry for the attribute
     * </p>
     *
     * <p>
     * The attribute name is used along with the dictionary object entry to find
     * the <code>AttributeDefinition</code>
     * </p>
     *
     * @return
     */
    public String getDictionaryObjectEntry() {
        return this.dictionaryObjectEntry;
    }

    /**
     * Setter for the dictionary object entry
     *
     * @param dictionaryObjectEntry
     */
    public void setDictionaryObjectEntry(String dictionaryObjectEntry) {
        this.dictionaryObjectEntry = dictionaryObjectEntry;
    }

    /**
     * Instance of <code>KeyValluesFinder</code> that should be invoked to
     * provide a List of values the field can have. Generally used to provide
     * the options for a multi-value control or to validate the submitted field
     * value
     *
     * @return KeyValuesFinder instance
     */
    public KeyValuesFinder getOptionsFinder() {
        return this.optionsFinder;
    }

    /**
     * Setter for the field's KeyValuesFinder instance
     *
     * @param optionsFinder
     */
    public void setOptionsFinder(KeyValuesFinder optionsFinder) {
        this.optionsFinder = optionsFinder;
    }

    /**
     * Setter that takes in the class name for the options finder and creates a
     * new instance to use as the finder for the attribute field
     *
     * @param optionsFinderClass
     */
    public void setOptionsFinderClass(Class<? extends KeyValuesFinder> optionsFinderClass) {
        this.optionsFinder = ObjectUtils.newInstance(optionsFinderClass);
    }

    /**
     * Text explaining how to use the field, including things like what values should be selected
     * in certain cases and so on (instructions)
     *
     * @return String instructional message
     */
    public String getInstructionalText() {
        return this.instructionalText;
    }

    /**
     * Setter for the instructional message
     *
     * @param instructionalText
     */
    public void setInstructionalText(String instructionalText) {
        this.instructionalText = instructionalText;
    }

    /**
     * Summary help text for the field
     *
     * @return String summary help text
     */
    public String getHelpSummary() {
        return helpSummary;
    }

    /**
     * Setter for the summary help text
     *
     * @param helpSummary
     */
    public void setHelpSummary(String helpSummary) {
        this.helpSummary = helpSummary;
    }

    /**
     * Full help information text for the field
     *
     * @return String help description text
     */
    public String getHelpDescription() {
        return this.helpDescription;
    }

    /**
     * Setter for the help description text
     *
     * @param helpDescription
     */
    public void setHelpDescription(String helpDescription) {
        this.helpDescription = helpDescription;
    }

    /**
     * Holds security configuration for the attribute field. This triggers
     * corresponding permission checks in KIM and can result in an update to the
     * field state (such as read-only or hidden) and masking of the value
     *
     * @return AttributeSecurity instance configured for field or Null if no
     *         restrictions are defined
     */
    public AttributeSecurity getAttributeSecurity() {
        return this.attributeSecurity;
    }

    /**
     * Setter for the AttributeSecurity instance that defines restrictions for
     * the field
     *
     * @param attributeSecurity
     */
    public void setAttributeSecurity(AttributeSecurity attributeSecurity) {
        this.attributeSecurity = attributeSecurity;
    }

    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#getSupportsOnLoad()
     */
    @Override
    public boolean getSupportsOnLoad() {
        return true;
    }

    /**
     * Lookup finder widget for the field
     *
     * <p>
     * The quickfinder widget places a small icon next to the field that allows
     * the user to bring up a search screen for finding valid field values. The
     * <code>Widget</code> instance can be configured to point to a certain
     * <code>LookupView</code>, or the framework will attempt to associate the
     * field with a lookup based on its metadata (in particular its
     * relationships in the model)
     * </p>
     *
     * @return QuickFinder lookup widget
     */
    public QuickFinder getFieldLookup() {
        return this.fieldLookup;
    }

    /**
     * Setter for the lookup widget
     *
     * @param fieldLookup
     */
    public void setFieldLookup(QuickFinder fieldLookup) {
        this.fieldLookup = fieldLookup;
    }

    /**
     * Inquiry widget for the field
     *
     * <p>
     * The inquiry widget will render a link for the field value when read-only
     * that points to the associated inquiry view for the field. The inquiry can
     * be configured to point to a certain <code>InquiryView</code>, or the
     * framework will attempt to associate the field with a inquiry based on its
     * metadata (in particular its relationships in the model)
     * </p>
     *
     * @return Inquiry field inquiry
     */
    public Inquiry getFieldInquiry() {
        return this.fieldInquiry;
    }

    /**
     * Setter for the inquiry widget
     *
     * @param fieldInquiry
     */
    public void setFieldInquiry(Inquiry fieldInquiry) {
        this.fieldInquiry = fieldInquiry;
    }

    /**
     * Suggest box widget for the attribute field
     *
     * <p>
     * If enabled (by render flag), as the user inputs data into the
     * fields control a dynamic query is performed to provide the user
     * suggestions on values which they can then select
     * </p>
     *
     * <p>
     * Note the Suggest widget is only valid when using a standard TextControl
     * </p>
     *
     * @return Suggest instance
     */
    public Suggest getFieldSuggest() {
        return fieldSuggest;
    }

    /**
     * Setter for the fields Suggest widget
     *
     * @param fieldSuggest
     */
    public void setFieldSuggest(Suggest fieldSuggest) {
        this.fieldSuggest = fieldSuggest;
    }

    /**
     * Message field that displays instructional text
     *
     * <p>
     * This message field can be configured to for adjusting how the instructional text will display. Generally
     * the styleClasses property will be of most interest
     * </p>
     *
     * @return MessageField instructional message field
     */
    public MessageField getInstructionalMessageField() {
        return this.instructionalMessageField;
    }

    /**
     * Setter for the instructional text message field
     *
     * <p>
     * Note this is the setter for the field that will render the instructional text. The actual text can be
     * set on the field but can also be set using {@link #setInstructionalText(String)}
     * </p>
     *
     * @param instructionalMessageField
     */
    public void setInstructionalMessageField(MessageField instructionalMessageField) {
        this.instructionalMessageField = instructionalMessageField;
    }

    /**
     * Text that display a restriction on the value a field can hold
     *
     * <p>
     * For example when the value must be a valid format (phone number, email), certain length, min/max value and
     * so on this text can be used to indicate the constraint to the user. Generally displays with the control so
     * it is visible when the user tabs to the field
     * </p>
     *
     * @return String text to display for the constraint message
     */
    public String getConstraintText() {
        return this.constraintText;
    }

    /**
     * Setter for the constraint message text
     *
     * @param constraintText
     */
    public void setConstraintText(String constraintText) {
        this.constraintText = constraintText;
    }

    /**
     * Message field that displays constraint text
     *
     * <p>
     * This message field can be configured to for adjusting how the constrain text will display. Generally
     * the styleClasses property will be of most interest
     * </p>
     *
     * @return MessageField constraint message field
     */
    public MessageField getConstraintMessageField() {
        return this.constraintMessageField;
    }

    /**
     * Setter for the constraint text message field
     *
     * <p>
     * Note this is the setter for the field that will render the constraint text. The actual text can be
     * set on the field but can also be set using {@link #setConstraintText(String)}
     * </p>
     *
     * @param constraintMessageField
     */
    public void setConstraintMessageField(MessageField constraintMessageField) {
        this.constraintMessageField = constraintMessageField;
    }

    /**
     * Valid character constraint that defines regular expressions for the valid
     * characters for this field
     *
     * @return the validCharactersConstraint
     */
    public ValidCharactersConstraint getValidCharactersConstraint() {
        return this.validCharactersConstraint;
    }

    /**
     * @param validCharactersConstraint the validCharactersConstraint to set
     */
    public void setValidCharactersConstraint(ValidCharactersConstraint validCharactersConstraint) {
        this.validCharactersConstraint = validCharactersConstraint;
    }

    /**
     * @return the caseConstraint
     */
    public CaseConstraint getCaseConstraint() {
        return this.caseConstraint;
    }

    /**
     * @param caseConstraint the caseConstraint to set
     */
    public void setCaseConstraint(CaseConstraint caseConstraint) {
        this.caseConstraint = caseConstraint;
    }

    /**
     * @return the dependencyConstraints
     */
    public List<PrerequisiteConstraint> getDependencyConstraints() {
        return this.dependencyConstraints;
    }

    /**
     * @param dependencyConstraints the dependencyConstraints to set
     */
    public void setDependencyConstraints(List<PrerequisiteConstraint> dependencyConstraints) {
        this.dependencyConstraints = dependencyConstraints;
    }

    /**
     * @return the mustOccurConstraints
     */
    public List<MustOccurConstraint> getMustOccurConstraints() {
        return this.mustOccurConstraints;
    }

    /**
     * @param mustOccurConstraints the mustOccurConstraints to set
     */
    public void setMustOccurConstraints(List<MustOccurConstraint> mustOccurConstraints) {
        this.mustOccurConstraints = mustOccurConstraints;
    }

    /**
     * A simple constraint which store the values for required, min/max length,
     * and min/max value
     *
     * @return the simpleConstraint
     */
    public SimpleConstraint getSimpleConstraint() {
        return this.simpleConstraint;
    }

    /**
     * When a simple constraint is set on this object ALL simple validation
     * constraints set directly will be overridden - recommended to use this or
     * the other gets/sets for defining simple constraints, not both
     *
     * @param simpleConstraint the simpleConstraint to set
     */
    public void setSimpleConstraint(SimpleConstraint simpleConstraint) {
        this.simpleConstraint = simpleConstraint;
    }

    /**
     * Maximum number of the characters the attribute value is allowed to have.
     * Used to set the maxLength for supporting controls. Note this can be
     * smaller or longer than the actual control size
     *
     * @return Integer max length
     */
    public Integer getMaxLength() {
        return simpleConstraint.getMaxLength();
    }

    /**
     * Setter for attributes max length
     *
     * @param maxLength
     */
    public void setMaxLength(Integer maxLength) {
        simpleConstraint.setMaxLength(maxLength);
    }

    /**
     * @return the minLength
     */
    public Integer getMinLength() {
        return simpleConstraint.getMinLength();
    }

    /**
     * @param minLength the minLength to set
     */
    public void setMinLength(Integer minLength) {
        simpleConstraint.setMinLength(minLength);
    }

    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.simpleConstraint.getRequired();
    }

    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#setRequired(java.lang.Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.simpleConstraint.setRequired(required);
    }

    /**
     * The exclusiveMin element determines the minimum allowable value for data
     * entry editing purposes. Value can be an integer or decimal value such as
     * -.001 or 99.
     */
    public String getExclusiveMin() {
        return simpleConstraint.getExclusiveMin();
    }

    /**
     * @param exclusiveMin the minValue to set
     */
    public void setExclusiveMin(String exclusiveMin) {
        simpleConstraint.setExclusiveMin(exclusiveMin);
    }

    /**
     * The inclusiveMax element determines the maximum allowable value for data
     * entry editing purposes. Value can be an integer or decimal value such as
     * -.001 or 99.
     */
    public String getInclusiveMax() {
        return simpleConstraint.getInclusiveMax();
    }

    /**
     * @param inclusiveMax the maxValue to set
     */
    public void setInclusiveMax(String inclusiveMax) {
        simpleConstraint.setInclusiveMax(inclusiveMax);
    }

    /**
     * Additional display attribute name, which will be displayed next to the actual field value
     * when the field is readonly with hypen inbetween like PropertyValue - AdditionalPropertyValue
     *
     * @param additionalDisplayPropertyName - Name of the additional display property
     */
    public void setAdditionalDisplayPropertyName(String additionalDisplayPropertyName) {
        this.additionalDisplayPropertyName = additionalDisplayPropertyName;
    }

    /**
     * Returns the additional display attribute name to be displayed when the field is readonly
     *
     * @return Additional Display Attribute Name
     */
    public String getAdditionalDisplayPropertyName() {
        return this.additionalDisplayPropertyName;
    }

    /**
     * Sets the alternate display attribute name to be displayed when the field is readonly.
     * This properties value will be displayed instead of actual fields value when the field is readonly.
     *
     * @param alternateDisplayPropertyName - alternate display property name
     */
    public void setAlternateDisplayPropertyName(String alternateDisplayPropertyName) {
        this.alternateDisplayPropertyName = alternateDisplayPropertyName;
    }

    /**
     * Returns the alternate display attribute name to be displayed when the field is readonly.
     *
     * @return alternate Display Property Name
     */
    public String getAlternateDisplayPropertyName() {
        return this.alternateDisplayPropertyName;
    }

    /**
     * Returns the alternate display value
     *
     * @return the alternate display value set for this field
     */
    public String getAlternateDisplayValue() {
        return alternateDisplayValue;
    }

    /**
     * Returns the additional display value.
     *
     * @return the additional display value set for this field
     */
    public String getAdditionalDisplayValue() {
        return additionalDisplayValue;
    }

    /**
     * Setter for the direct inquiry widget
     *
     * @param fieldDirectInquiry - field DirectInquiry to set
     */
    public void setFieldDirectInquiry(DirectInquiry fieldDirectInquiry) {
        this.fieldDirectInquiry = fieldDirectInquiry;
    }

    /**
     * DirectInquiry widget for the field
     *
     * <p>
     * The direct inquiry widget will render a button for the field value when
     * that field is editable. It points to the associated inquiry view for the
     * field. The inquiry can be configured to point to a certain
     * <code>InquiryView</code>, or the framework will attempt to associate the
     * field with a inquiry based on its metadata (in particular its
     * relationships in the model)
     * </p>
     *
     * @return the <code>DirectInquiry</code> field DirectInquiry
     */
    public DirectInquiry getFieldDirectInquiry() {
        return fieldDirectInquiry;
    }

    /**
     * List of property names whose values should be displayed read-only under this field
     *
     * <p>
     * In the attribute field template for each information property name given its values is
     * outputted read-only. Informational property values can also be updated dynamically with
     * the use of field attribute query
     * </p>
     *
     * <p>
     * Simple property names can be given if the property has the same binding parent as this
     * field, in which case the binding path will be adjusted by the framework. If the property
     * names starts with org.kuali.rice.krad.uif.UifConstants#NO_BIND_ADJUST_PREFIX, no binding
     * prefix will be added.
     * </p>
     *
     * @return List<String> informational property names
     */
    public List<String> getInformationalDisplayPropertyNames() {
        return informationalDisplayPropertyNames;
    }

    /**
     * Setter for the list of informational property names
     *
     * @param informationalDisplayPropertyNames
     */
    public void setInformationalDisplayPropertyNames(List<String> informationalDisplayPropertyNames) {
        this.informationalDisplayPropertyNames = informationalDisplayPropertyNames;
    }

    /**
     * Allows specifying hidden property names without having to specify as a
     * field in the group config (that might impact layout)
     *
     * @return List<String> hidden property names
     */
    public List<String> getHiddenPropertyNames() {
        return hiddenPropertyNames;
    }

    /**
     * Setter for the hidden property names
     *
     * @param hiddenPropertyNames
     */
    public void setHiddenPropertyNames(List<String> hiddenPropertyNames) {
        this.hiddenPropertyNames = hiddenPropertyNames;
    }

    /**
     * Attribute query instance configured for this field to dynamically pull information back for
     * updates other fields or providing messages
     *
     * <p>
     * If field attribute query is not null, associated event script will be generated to trigger the
     * query from the UI. This will invoke the <code>AttributeQueryService</code> to
     * execute the query and return an instance of <code>AttributeQueryResult</code> that is then
     * read by the script to update the UI. Typically used to update informational property values or
     * other field values
     * </p>
     *
     * @return AttributeQuery instance
     */
    public AttributeQuery getFieldAttributeQuery() {
        return fieldAttributeQuery;
    }

    /**
     * Setter for this fields query
     *
     * @param fieldAttributeQuery
     */
    public void setFieldAttributeQuery(AttributeQuery fieldAttributeQuery) {
        this.fieldAttributeQuery = fieldAttributeQuery;
    }

    /**
     * Sets HTML escaping for this property value. HTML escaping will be handled in alternate and additional fields
     * also.
     */
    public void setEscapeHtmlInPropertyValue(boolean escapeHtmlInPropertyValue) {
        this.escapeHtmlInPropertyValue = escapeHtmlInPropertyValue;
    }

    /**
     * Returns true if HTML escape allowed for this field
     *
     * @return true if escaping allowed
     */
    public boolean isEscapeHtmlInPropertyValue() {
        return this.escapeHtmlInPropertyValue;
    }

    /**
     * Perform uppercase flag for this field to force input to uppercase.
     *
     * <p>
     * It this flag is set to true the 'text-transform' style on the field will be set to 'uppercase'
     * which will automatically change any text input into the field to uppercase.
     * </p>
     *
     * @return performUppercase flag
     */
    public boolean isPerformUppercase() {
        return performUppercase;
    }

    /**
     * Setter for this fields performUppercase flag
     *
     * @param performUppercase flag
     */
    public void setPerformUppercase(boolean performUppercase) {
        this.performUppercase = performUppercase;
    }
}
