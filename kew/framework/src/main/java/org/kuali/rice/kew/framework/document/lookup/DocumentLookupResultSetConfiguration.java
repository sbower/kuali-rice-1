package org.kuali.rice.kew.framework.document.lookup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.rice.core.api.CoreConstants;
import org.kuali.rice.core.api.mo.AbstractDataTransferObject;
import org.kuali.rice.core.api.mo.ModelBuilder;
import org.kuali.rice.core.api.mo.ModelObjectUtils;
import org.kuali.rice.core.api.uif.AttributeField;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.w3c.dom.Element;

/**
 * An immutable data transfer object implementation of the {@link DocumentLookupResultSetConfigurationContract}.
 * Instances of this class should be constructed using the nested {@link Builder} class.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@XmlRootElement(name = DocumentLookupResultSetConfiguration.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = DocumentLookupResultSetConfiguration.Constants.TYPE_NAME, propOrder = {
    DocumentLookupResultSetConfiguration.Elements.OVERRIDE_SEARCHABLE_ATTRIBUTES,
    DocumentLookupResultSetConfiguration.Elements.CUSTOM_FIELD_NAMES_TO_ADD,
    DocumentLookupResultSetConfiguration.Elements.STANDARD_RESULT_FIELDS_TO_REMOVE,
    DocumentLookupResultSetConfiguration.Elements.ADDITIONAL_ATTRIBUTE_FIELDS,
    CoreConstants.CommonElements.FUTURE_ELEMENTS
})
public final class DocumentLookupResultSetConfiguration extends AbstractDataTransferObject
        implements DocumentLookupResultSetConfigurationContract {

    @XmlElement(name = Elements.OVERRIDE_SEARCHABLE_ATTRIBUTES, required = true)
    private final boolean overrideSearchableAttributes;

    @XmlElementWrapper(name = Elements.CUSTOM_FIELD_NAMES_TO_ADD, required = false)
    @XmlElement(name = Elements.CUSTOM_FIELD_NAME_TO_ADD, required = false)
    private final List<String> customFieldNamesToAdd;

    @XmlElementWrapper(name = Elements.STANDARD_RESULT_FIELDS_TO_REMOVE, required = false)
    @XmlElement(name = Elements.STANDARD_RESULT_FIELD_TO_REMOVE, required = false)
    private final List<StandardResultField> standardResultFieldsToRemove;

    @XmlElementWrapper(name = Elements.ADDITIONAL_ATTRIBUTE_FIELDS, required = false)
    @XmlElement(name = Elements.ADDITIONAL_ATTRIBUTE_FIELD, required = false)
    private final List<RemotableAttributeField> additionalAttributeFields;
    
    @SuppressWarnings("unused")
    @XmlAnyElement
    private final Collection<Element> _futureElements = null;

    /**
     * Private constructor used only by JAXB.
     */
    @SuppressWarnings("unused")
    private DocumentLookupResultSetConfiguration() {
        this.overrideSearchableAttributes = false;
        this.customFieldNamesToAdd = null;
        this.standardResultFieldsToRemove = null;
        this.additionalAttributeFields = null;
    }

    private DocumentLookupResultSetConfiguration(Builder builder) {
        this.overrideSearchableAttributes = builder.isOverrideSearchableAttributes();
        this.customFieldNamesToAdd = ModelObjectUtils.createImmutableCopy(builder.getCustomFieldNamesToAdd());
        this.standardResultFieldsToRemove =
                ModelObjectUtils.createImmutableCopy(builder.getStandardResultFieldsToRemove());
        this.additionalAttributeFields = ModelObjectUtils.buildImmutableCopy(builder.getAdditionalAttributeFields());
    }

    @Override
    public boolean isOverrideSearchableAttributes() {
        return this.overrideSearchableAttributes;
    }

    @Override
    public List<String> getCustomFieldNamesToAdd() {
        return this.customFieldNamesToAdd;
    }

    @Override
    public List<StandardResultField> getStandardResultFieldsToRemove() {
        return this.standardResultFieldsToRemove;
    }

    @Override
    public List<RemotableAttributeField> getAdditionalAttributeFields() {
        return this.additionalAttributeFields;
    }

    /**
     * A builder which can be used to construct {@link DocumentLookupResultSetConfiguration} instances.  Enforces the
     * constraints of the {@link DocumentLookupResultSetConfigurationContract}.
     */
    public final static class Builder implements Serializable, ModelBuilder,
            DocumentLookupResultSetConfigurationContract {

        private boolean overrideSearchableAttributes;
        private List<String> customFieldNamesToAdd;
        private List<StandardResultField> standardResultFieldsToRemove;
        private List<RemotableAttributeField.Builder> additionalAttributeFields;

        private Builder() {
            setOverrideSearchableAttributes(false);
            setCustomFieldNamesToAdd(new ArrayList<String>());
            setStandardResultFieldsToRemove(new ArrayList<StandardResultField>());
            setAdditionalAttributeFields(new ArrayList<RemotableAttributeField.Builder>());
        }

        /**
         * Creates new empty builder instance.  The various lists on this builder are initialized to empty lists.  The
         * {@code overrideSearchableAttribute} boolean property is initialized to "false".
         *
         * @return a new empty builder instance
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Creates a new builder instance initialized with copies of the properties from the given contract.
         *
         * @param contract the contract from which to copy properties
         *
         * @return a builder instance initialized with properties from the given contract
         *
         * @throws IllegalArgumentException if the given contract is null
         */
        public static Builder create(DocumentLookupResultSetConfigurationContract contract) {
            if (contract == null) {
                throw new IllegalArgumentException("contract was null");
            }
            Builder builder = create();
            builder.setOverrideSearchableAttributes(contract.isOverrideSearchableAttributes());
            if (CollectionUtils.isNotEmpty(contract.getCustomFieldNamesToAdd())) {
                builder.setCustomFieldNamesToAdd(new ArrayList<String>(contract.getCustomFieldNamesToAdd()));
            }
            if (CollectionUtils.isNotEmpty(contract.getStandardResultFieldsToRemove())) {
                builder.setStandardResultFieldsToRemove(
                        new ArrayList<StandardResultField>(contract.getStandardResultFieldsToRemove()));
            }
            if (CollectionUtils.isNotEmpty(contract.getAdditionalAttributeFields())) {
                for (AttributeField attributeField : contract.getAdditionalAttributeFields()) {
                    builder.getAdditionalAttributeFields().add(RemotableAttributeField.Builder.create(attributeField));
                }
            }
            return builder;
        }

        @Override
        public DocumentLookupResultSetConfiguration build() {
            return new DocumentLookupResultSetConfiguration(this);
        }

        @Override
        public boolean isOverrideSearchableAttributes() {
            return this.overrideSearchableAttributes;
        }

        @Override
        public List<String> getCustomFieldNamesToAdd() {
            return this.customFieldNamesToAdd;
        }

        @Override
        public List<StandardResultField> getStandardResultFieldsToRemove() {
            return this.standardResultFieldsToRemove;
        }

        @Override
        public List<RemotableAttributeField.Builder> getAdditionalAttributeFields() {
            return this.additionalAttributeFields;
        }

        public void setOverrideSearchableAttributes(boolean overrideSearchableAttributes) {
            this.overrideSearchableAttributes = overrideSearchableAttributes;
        }

        public void setCustomFieldNamesToAdd(List<String> customFieldNamesToAdd) {
            this.customFieldNamesToAdd = customFieldNamesToAdd;
        }

        public void setStandardResultFieldsToRemove(List<StandardResultField> standardResultFieldsToRemove) {
            this.standardResultFieldsToRemove = standardResultFieldsToRemove;
        }

        public void setAdditionalAttributeFields(List<RemotableAttributeField.Builder> additionalAttributeFields) {
            this.additionalAttributeFields = additionalAttributeFields;
        }

    }

    /**
     * Defines some internal constants used on this class.
     */
    static class Constants {
        final static String ROOT_ELEMENT_NAME = "documentLookupResultSetConfiguration";
        final static String TYPE_NAME = "DocumentLookupResultSetConfigurationType";
    }

    /**
     * A private class which exposes constants which define the XML element names to use when this object is marshalled
     * to XML.
     */
    static class Elements {
        final static String OVERRIDE_SEARCHABLE_ATTRIBUTES = "overrideSearchableAttributes";
        final static String CUSTOM_FIELD_NAMES_TO_ADD = "customFieldNamesToAdd";
        final static String CUSTOM_FIELD_NAME_TO_ADD = "customFieldNameToAdd";
        final static String STANDARD_RESULT_FIELDS_TO_REMOVE = "standardResultFieldsToRemove";
        final static String STANDARD_RESULT_FIELD_TO_REMOVE = "standardResultFieldToRemove";
        final static String ADDITIONAL_ATTRIBUTE_FIELDS = "additionalAttributeFields";
        final static String ADDITIONAL_ATTRIBUTE_FIELD = "additionalAttributeField";
    }

}
