package org.kuali.rice.kew.api.document.lookup;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.CoreConstants;
import org.kuali.rice.core.api.mo.AbstractDataTransferObject;
import org.kuali.rice.core.api.mo.ModelBuilder;
import org.kuali.rice.core.api.util.jaxb.DateTimeAdapter;
import org.kuali.rice.core.api.util.jaxb.MultiValuedStringMapAdapter;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.document.DocumentStatusCategory;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable data transfer object implementation of the {@link DocumentLookupCriteriaContract}.  Instances of this
 * class should be constructed using the nested {@link Builder} class.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@XmlRootElement(name = DocumentLookupCriteria.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = DocumentLookupCriteria.Constants.TYPE_NAME, propOrder = {
    DocumentLookupCriteria.Elements.DOCUMENT_ID,
    DocumentLookupCriteria.Elements.DOCUMENT_STATUSES,
    DocumentLookupCriteria.Elements.DOCUMENT_STATUS_CATEGORIES,
    DocumentLookupCriteria.Elements.TITLE,
    DocumentLookupCriteria.Elements.APPLICATION_DOCUMENT_ID,
    DocumentLookupCriteria.Elements.APPLICATION_DOCUMENT_STATUS,
    DocumentLookupCriteria.Elements.INITIATOR_PRINCIPAL_NAME,
    DocumentLookupCriteria.Elements.VIEWER_PRINCIPAL_NAME,
    DocumentLookupCriteria.Elements.VIEWER_GROUP_ID,
    DocumentLookupCriteria.Elements.APPROVER_PRINCIPAL_NAME,
    DocumentLookupCriteria.Elements.ROUTE_NODE_NAME,
    DocumentLookupCriteria.Elements.ROUTE_NODE_LOOKUP_LOGIC,
    DocumentLookupCriteria.Elements.DOCUMENT_TYPE_NAME,
    DocumentLookupCriteria.Elements.DATE_CREATED_FROM,
    DocumentLookupCriteria.Elements.DATE_CREATED_TO,
    DocumentLookupCriteria.Elements.DATE_LAST_MODIFIED_FROM,
    DocumentLookupCriteria.Elements.DATE_LAST_MODIFIED_TO,
    DocumentLookupCriteria.Elements.DATE_APPROVED_FROM,
    DocumentLookupCriteria.Elements.DATE_APPROVED_TO,
    DocumentLookupCriteria.Elements.DATE_FINALIZED_FROM,
    DocumentLookupCriteria.Elements.DATE_FINALIZED_TO,
    DocumentLookupCriteria.Elements.DATE_APPLICATION_DOCUMENT_STATUS_CHANGED_FROM,
    DocumentLookupCriteria.Elements.DATE_APPLICATION_DOCUMENT_STATUS_CHANGED_TO,
    DocumentLookupCriteria.Elements.DOCUMENT_ATTRIBUTE_VALUES,
    DocumentLookupCriteria.Elements.SAVE_NAME,
    DocumentLookupCriteria.Elements.START_AT_INDEX,
    DocumentLookupCriteria.Elements.MAX_RESULTS,
    CoreConstants.CommonElements.FUTURE_ELEMENTS
})
public final class DocumentLookupCriteria extends AbstractDataTransferObject implements DocumentLookupCriteriaContract {

    private static final long serialVersionUID = -221440103480740497L;
    
    @XmlElement(name = Elements.DOCUMENT_ID, required = false)
    private final String documentId;

    @XmlElementWrapper(name = Elements.DOCUMENT_STATUSES, required = false)
    @XmlElement(name = Elements.DOCUMENT_STATUS, required = false)
    private final List<DocumentStatus> documentStatuses;

    @XmlElementWrapper(name = Elements.DOCUMENT_STATUS_CATEGORIES, required = false)
    @XmlElement(name = Elements.DOCUMENT_STATUS_CATEGORY, required = false)
    private final List<DocumentStatusCategory> documentStatusCategories;

    @XmlElement(name = Elements.TITLE, required = false)
    private final String title;

    @XmlElement(name = Elements.APPLICATION_DOCUMENT_ID, required = false)
    private final String applicationDocumentId;

    @XmlElement(name = Elements.APPLICATION_DOCUMENT_STATUS, required = false)
    private final String applicationDocumentStatus;

    @XmlElement(name = Elements.INITIATOR_PRINCIPAL_NAME, required = false)
    private final String initiatorPrincipalName;

    @XmlElement(name = Elements.VIEWER_PRINCIPAL_NAME, required = false)
    private final String viewerPrincipalName;

    @XmlElement(name = Elements.VIEWER_GROUP_ID, required = false)
    private final String viewerGroupId;

    @XmlElement(name = Elements.APPROVER_PRINCIPAL_NAME, required = false)
    private final String approverPrincipalName;

    @XmlElement(name = Elements.ROUTE_NODE_NAME, required = false)
    private final String routeNodeName;

    @XmlElement(name = Elements.ROUTE_NODE_LOOKUP_LOGIC, required = false)
    private final RouteNodeLookupLogic routeNodeLookupLogic;

    @XmlElement(name = Elements.DOCUMENT_TYPE_NAME, required = false)
    private final String documentTypeName;

    @XmlElement(name = Elements.DATE_CREATED_FROM, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateCreatedFrom;

    @XmlElement(name = Elements.DATE_CREATED_TO, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateCreatedTo;

    @XmlElement(name = Elements.DATE_LAST_MODIFIED_FROM, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateLastModifiedFrom;

    @XmlElement(name = Elements.DATE_LAST_MODIFIED_TO, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateLastModifiedTo;

    @XmlElement(name = Elements.DATE_APPROVED_FROM, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateApprovedFrom;

    @XmlElement(name = Elements.DATE_APPROVED_TO, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateApprovedTo;

    @XmlElement(name = Elements.DATE_FINALIZED_FROM, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateFinalizedFrom;

    @XmlElement(name = Elements.DATE_FINALIZED_TO, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateFinalizedTo;

    @XmlElement(name = Elements.DATE_APPLICATION_DOCUMENT_STATUS_CHANGED_FROM, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateApplicationDocumentStatusChangedFrom;

    @XmlElement(name = Elements.DATE_APPLICATION_DOCUMENT_STATUS_CHANGED_TO, required = false)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime dateApplicationDocumentStatusChangedTo;

    @XmlElement(name = Elements.DOCUMENT_ATTRIBUTE_VALUES, required = false)
    @XmlJavaTypeAdapter(MultiValuedStringMapAdapter.class)
    private final Map<String, List<String>> documentAttributeValues;

    @XmlElement(name = Elements.SAVE_NAME, required = false)
    private final String saveName;

    @XmlElement(name = Elements.START_AT_INDEX, required = false)
    private final Integer startAtIndex;

    @XmlElement(name = Elements.MAX_RESULTS, required = false)
    private final Integer maxResults;
    
    @SuppressWarnings("unused")
    @XmlAnyElement
    private final Collection<Element> _futureElements = null;

    /**
     * Private constructor used only by JAXB.
     */
    private DocumentLookupCriteria() {
        this.documentId = null;
        this.documentStatuses = null;
        this.documentStatusCategories = null;
        this.title = null;
        this.applicationDocumentId = null;
        this.applicationDocumentStatus = null;
        this.initiatorPrincipalName = null;
        this.viewerPrincipalName = null;
        this.viewerGroupId = null;
        this.approverPrincipalName = null;
        this.routeNodeName = null;
        this.routeNodeLookupLogic = null;
        this.documentTypeName = null;
        this.dateCreatedFrom = null;
        this.dateCreatedTo = null;
        this.dateLastModifiedFrom = null;
        this.dateLastModifiedTo = null;
        this.dateApprovedFrom = null;
        this.dateApprovedTo = null;
        this.dateFinalizedFrom = null;
        this.dateFinalizedTo = null;
        this.dateApplicationDocumentStatusChangedFrom = null;
        this.dateApplicationDocumentStatusChangedTo = null;
        this.documentAttributeValues = null;
        this.saveName = null;
        this.startAtIndex = null;
        this.maxResults = null;
    }

    private DocumentLookupCriteria(Builder builder) {
        this.documentId = builder.getDocumentId();
        if (builder.getDocumentStatuses() == null) {
            this.documentStatuses = Collections.emptyList();
        } else {
            this.documentStatuses = Collections.unmodifiableList(new ArrayList<DocumentStatus>(builder.getDocumentStatuses()));
        }
        if (builder.getDocumentStatusCategories() == null) {
            this.documentStatusCategories = Collections.emptyList();
        } else {
            this.documentStatusCategories = Collections.unmodifiableList(new ArrayList<DocumentStatusCategory>(builder.getDocumentStatusCategories()));
        }
        this.title = builder.getTitle();
        this.applicationDocumentId = builder.getApplicationDocumentId();
        this.applicationDocumentStatus = builder.getApplicationDocumentStatus();
        this.initiatorPrincipalName = builder.getInitiatorPrincipalName();
        this.viewerPrincipalName = builder.getViewerPrincipalName();
        this.viewerGroupId = builder.getViewerGroupId();
        this.approverPrincipalName = builder.getApproverPrincipalName();
        this.routeNodeName = builder.getRouteNodeName();
        this.routeNodeLookupLogic = builder.getRouteNodeLookupLogic();
        this.documentTypeName = builder.getDocumentTypeName();
        this.dateCreatedFrom = builder.getDateCreatedFrom();
        this.dateCreatedTo = builder.getDateCreatedTo();
        this.dateLastModifiedFrom = builder.getDateLastModifiedFrom();
        this.dateLastModifiedTo = builder.getDateLastModifiedTo();
        this.dateApprovedFrom = builder.getDateApprovedFrom();
        this.dateApprovedTo = builder.getDateApprovedTo();
        this.dateFinalizedFrom = builder.getDateFinalizedFrom();
        this.dateFinalizedTo = builder.getDateFinalizedTo();
        this.dateApplicationDocumentStatusChangedFrom = builder.getDateApplicationDocumentStatusChangedFrom();
        this.dateApplicationDocumentStatusChangedTo = builder.getDateApplicationDocumentStatusChangedTo();
        if (builder.getDocumentAttributeValues() == null) {
            this.documentAttributeValues = Collections.emptyMap();
        } else {
            this.documentAttributeValues = Collections.unmodifiableMap(new HashMap<String, List<String>>(builder.getDocumentAttributeValues()));
        }
        this.saveName = builder.getSaveName();
        this.startAtIndex = builder.getStartAtIndex();
        this.maxResults = builder.getMaxResults();
    }

    @Override
    public String getDocumentId() {
        return this.documentId;
    }

    @Override
    public List<DocumentStatus> getDocumentStatuses() {
        return this.documentStatuses;
    }

    @Override
    public List<DocumentStatusCategory> getDocumentStatusCategories() {
        return this.documentStatusCategories;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getApplicationDocumentId() {
        return this.applicationDocumentId;
    }

    @Override
    public String getApplicationDocumentStatus() {
        return this.applicationDocumentStatus;
    }

    @Override
    public String getInitiatorPrincipalName() {
        return this.initiatorPrincipalName;
    }

    @Override
    public String getViewerPrincipalName() {
        return this.viewerPrincipalName;
    }

    @Override
    public String getViewerGroupId() {
        return this.viewerGroupId;
    }

    @Override
    public String getApproverPrincipalName() {
        return this.approverPrincipalName;
    }

    @Override
    public String getRouteNodeName() {
        return this.routeNodeName;
    }

    @Override
    public RouteNodeLookupLogic getRouteNodeLookupLogic() {
        return this.routeNodeLookupLogic;
    }

    @Override
    public String getDocumentTypeName() {
        return this.documentTypeName;
    }

    @Override
    public DateTime getDateCreatedFrom() {
        return this.dateCreatedFrom;
    }

    @Override
    public DateTime getDateCreatedTo() {
        return this.dateCreatedTo;
    }

    @Override
    public DateTime getDateLastModifiedFrom() {
        return this.dateLastModifiedFrom;
    }

    @Override
    public DateTime getDateLastModifiedTo() {
        return this.dateLastModifiedTo;
    }

    @Override
    public DateTime getDateApprovedFrom() {
        return this.dateApprovedFrom;
    }

    @Override
    public DateTime getDateApprovedTo() {
        return this.dateApprovedTo;
    }

    @Override
    public DateTime getDateFinalizedFrom() {
        return this.dateFinalizedFrom;
    }

    @Override
    public DateTime getDateFinalizedTo() {
        return this.dateFinalizedTo;
    }

    @Override
    public DateTime getDateApplicationDocumentStatusChangedFrom() {
        return dateApplicationDocumentStatusChangedFrom;
    }

    @Override
    public DateTime getDateApplicationDocumentStatusChangedTo() {
        return dateApplicationDocumentStatusChangedTo;
    }

    @Override
    public Map<String, List<String>> getDocumentAttributeValues() {
        return this.documentAttributeValues;
    }

    @Override
    public String getSaveName() {
        return saveName;
    }

    @Override
    public Integer getStartAtIndex() {
        return this.startAtIndex;
    }

    @Override
    public Integer getMaxResults() {
        return this.maxResults;
    }

    /**
     * A builder which can be used to construct {@link DocumentLookupCriteria} instances.  Enforces the constraints of
     * the {@link DocumentLookupCriteriaContract}.
     */
    public final static class Builder implements Serializable, ModelBuilder, DocumentLookupCriteriaContract {

        private String documentId;
        private List<DocumentStatus> documentStatuses;
        private List<DocumentStatusCategory> documentStatusCategories;
        private String title;
        private String applicationDocumentId;
        private String applicationDocumentStatus;
        private String initiatorPrincipalName;
        private String viewerPrincipalName;
        private String viewerGroupId;
        private String approverPrincipalName;
        private String routeNodeName;
        private RouteNodeLookupLogic routeNodeLookupLogic;
        private String documentTypeName;
        private DateTime dateCreatedFrom;
        private DateTime dateCreatedTo;
        private DateTime dateLastModifiedFrom;
        private DateTime dateLastModifiedTo;
        private DateTime dateApprovedFrom;
        private DateTime dateApprovedTo;
        private DateTime dateFinalizedFrom;
        private DateTime dateFinalizedTo;
        private DateTime dateApplicationDocumentStatusChangedFrom;
        private DateTime dateApplicationDocumentStatusChangedTo;
        private Map<String, List<String>> documentAttributeValues;
        private String saveName;
        private Integer startAtIndex;
        private Integer maxResults;

        private Builder() {
            setDocumentStatuses(new ArrayList<DocumentStatus>());
            setDocumentStatusCategories(new ArrayList<DocumentStatusCategory>());
            setDocumentAttributeValues(new HashMap<String, List<String>>());
        }

        /**
         * Creates an empty builder instance.  All collections on the new instance are initialized to empty collections.
         *
         * @return a new builder instance
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
        public static Builder create(DocumentLookupCriteriaContract contract) {
            if (contract == null) {
                throw new IllegalArgumentException("contract was null");
            }
            Builder builder = create();
            builder.setDocumentId(contract.getDocumentId());
            if (contract.getDocumentStatuses() != null) {
                builder.setDocumentStatuses(new ArrayList<DocumentStatus>(contract.getDocumentStatuses()));
            }
            if (contract.getDocumentStatusCategories() != null) {
                builder.setDocumentStatusCategories(new ArrayList<DocumentStatusCategory>(contract.getDocumentStatusCategories()));
            }
            builder.setTitle(contract.getTitle());
            builder.setApplicationDocumentId(contract.getApplicationDocumentId());
            builder.setApplicationDocumentStatus(contract.getApplicationDocumentStatus());
            builder.setInitiatorPrincipalName(contract.getInitiatorPrincipalName());
            builder.setViewerPrincipalName(contract.getViewerPrincipalName());
            builder.setViewerGroupId(contract.getViewerGroupId());
            builder.setApproverPrincipalName(contract.getApproverPrincipalName());
            builder.setRouteNodeName(contract.getRouteNodeName());
            builder.setRouteNodeLookupLogic(contract.getRouteNodeLookupLogic());
            builder.setDocumentTypeName(contract.getDocumentTypeName());
            builder.setDateCreatedFrom(contract.getDateCreatedFrom());
            builder.setDateCreatedTo(contract.getDateCreatedTo());
            builder.setDateLastModifiedFrom(contract.getDateLastModifiedFrom());
            builder.setDateLastModifiedTo(contract.getDateLastModifiedTo());
            builder.setDateApprovedFrom(contract.getDateApprovedFrom());
            builder.setDateApprovedTo(contract.getDateApprovedTo());
            builder.setDateFinalizedFrom(contract.getDateFinalizedFrom());
            builder.setDateFinalizedTo(contract.getDateFinalizedTo());
            builder.setDateApplicationDocumentStatusChangedFrom(contract.getDateApplicationDocumentStatusChangedFrom());
            builder.setDateApplicationDocumentStatusChangedTo(contract.getDateApplicationDocumentStatusChangedTo());
            if (contract.getDocumentAttributeValues() != null) {
                builder.setDocumentAttributeValues(new HashMap<String, List<String>>(contract.getDocumentAttributeValues()));
            }
            builder.setSaveName(contract.getSaveName());
            builder.setStartAtIndex(contract.getStartAtIndex());
            builder.setMaxResults(contract.getMaxResults());
            return builder;
        }

        @Override
        public DocumentLookupCriteria build() {
            return new DocumentLookupCriteria(this);
        }

        @Override
        public String getDocumentId() {
            return this.documentId;
        }

        @Override
        public List<DocumentStatus> getDocumentStatuses() {
            return this.documentStatuses;
        }

        @Override
        public List<DocumentStatusCategory> getDocumentStatusCategories() {
            return this.documentStatusCategories;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public String getApplicationDocumentId() {
            return this.applicationDocumentId;
        }

        @Override
        public String getApplicationDocumentStatus() {
            return this.applicationDocumentStatus;
        }

        @Override
        public String getInitiatorPrincipalName() {
            return this.initiatorPrincipalName;
        }

        @Override
        public String getViewerPrincipalName() {
            return this.viewerPrincipalName;
        }

        @Override
        public String getViewerGroupId() {
            return this.viewerGroupId;
        }

        @Override
        public String getApproverPrincipalName() {
            return this.approverPrincipalName;
        }

        @Override
        public String getRouteNodeName() {
            return this.routeNodeName;
        }

        @Override
        public RouteNodeLookupLogic getRouteNodeLookupLogic() {
            return this.routeNodeLookupLogic;
        }

        @Override
        public String getDocumentTypeName() {
            return this.documentTypeName;
        }

        @Override
        public DateTime getDateCreatedFrom() {
            return this.dateCreatedFrom;
        }

        @Override
        public DateTime getDateCreatedTo() {
            return this.dateCreatedTo;
        }

        @Override
        public DateTime getDateLastModifiedFrom() {
            return this.dateLastModifiedFrom;
        }

        @Override
        public DateTime getDateLastModifiedTo() {
            return this.dateLastModifiedTo;
        }

        @Override
        public DateTime getDateApprovedFrom() {
            return this.dateApprovedFrom;
        }

        @Override
        public DateTime getDateApprovedTo() {
            return this.dateApprovedTo;
        }

        @Override
        public DateTime getDateFinalizedFrom() {
            return this.dateFinalizedFrom;
        }

        @Override
        public DateTime getDateFinalizedTo() {
            return this.dateFinalizedTo;
        }

        @Override
        public DateTime getDateApplicationDocumentStatusChangedFrom() {
            return dateApplicationDocumentStatusChangedFrom;
        }

        @Override
        public DateTime getDateApplicationDocumentStatusChangedTo() {
            return dateApplicationDocumentStatusChangedTo;
        }

        @Override
        public Map<String, List<String>> getDocumentAttributeValues() {
            return this.documentAttributeValues;
        }

        @Override
        public String getSaveName() {
            return this.saveName;
        }

        @Override
        public Integer getStartAtIndex() {
            return this.startAtIndex;
        }

        @Override
        public Integer getMaxResults() {
            return this.maxResults;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public void setDocumentStatuses(List<DocumentStatus> documentStatuses) {
            this.documentStatuses = documentStatuses;
        }

        public void setDocumentStatusCategories(List<DocumentStatusCategory> documentStatusCategories) {
            this.documentStatusCategories = documentStatusCategories;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setApplicationDocumentId(String applicationDocumentId) {
            this.applicationDocumentId = applicationDocumentId;
        }

        public void setApplicationDocumentStatus(String applicationDocumentStatus) {
            this.applicationDocumentStatus = applicationDocumentStatus;
        }

        public void setInitiatorPrincipalName(String initiatorPrincipalName) {
            this.initiatorPrincipalName = initiatorPrincipalName;
        }

        public void setViewerPrincipalName(String viewerPrincipalName) {
            this.viewerPrincipalName = viewerPrincipalName;
        }

        public void setViewerGroupId(String viewerGroupId) {
            this.viewerGroupId = viewerGroupId;
        }

        public void setApproverPrincipalName(String approverPrincipalName) {
            this.approverPrincipalName = approverPrincipalName;
        }

        public void setRouteNodeName(String routeNodeName) {
            this.routeNodeName = routeNodeName;
        }

        public void setRouteNodeLookupLogic(RouteNodeLookupLogic routeNodeLookupLogic) {
            this.routeNodeLookupLogic = routeNodeLookupLogic;
        }

        public void setDocumentTypeName(String documentTypeName) {
            this.documentTypeName = documentTypeName;
        }

        public void setDateCreatedFrom(DateTime dateCreatedFrom) {
            this.dateCreatedFrom = dateCreatedFrom;
        }

        public void setDateCreatedTo(DateTime dateCreatedTo) {
            this.dateCreatedTo = dateCreatedTo;
        }

        public void setDateLastModifiedFrom(DateTime dateLastModifiedFrom) {
            this.dateLastModifiedFrom = dateLastModifiedFrom;
        }

        public void setDateLastModifiedTo(DateTime dateLastModifiedTo) {
            this.dateLastModifiedTo = dateLastModifiedTo;
        }

        public void setDateApprovedFrom(DateTime dateApprovedFrom) {
            this.dateApprovedFrom = dateApprovedFrom;
        }

        public void setDateApprovedTo(DateTime dateApprovedTo) {
            this.dateApprovedTo = dateApprovedTo;
        }

        public void setDateFinalizedFrom(DateTime dateFinalizedFrom) {
            this.dateFinalizedFrom = dateFinalizedFrom;
        }

        public void setDateFinalizedTo(DateTime dateFinalizedTo) {
            this.dateFinalizedTo = dateFinalizedTo;
        }

        public void setDateApplicationDocumentStatusChangedFrom(DateTime dateApplicationDocumentStatusChangedFrom) {
            this.dateApplicationDocumentStatusChangedFrom = dateApplicationDocumentStatusChangedFrom;
        }

        public void setDateApplicationDocumentStatusChangedTo(DateTime dateApplicationDocumentStatusChangedTo) {
            this.dateApplicationDocumentStatusChangedTo = dateApplicationDocumentStatusChangedTo;
        }

        public void setDocumentAttributeValues(Map<String, List<String>> documentAttributeValues) {
            this.documentAttributeValues = documentAttributeValues;
        }

        public void addDocumentAttributeValue(String name, String value) {
            if (StringUtils.isBlank(value)) {
                throw new IllegalArgumentException("value was null or blank");
            }
            List<String> values = getDocumentAttributeValues().get(name);
            if (values == null) {
                values = new ArrayList<String>();
                getDocumentAttributeValues().put(name, values);
            }
            values.add(value);
        }

        public void setSaveName(String saveName) {
            this.saveName = saveName;
        }

        public void setStartAtIndex(Integer startAtIndex) {
            this.startAtIndex = startAtIndex;
        }

        public void setMaxResults(Integer maxResults) {
            this.maxResults = maxResults;
        }

    }

    /**
     * Defines some internal constants used on this class.
     */
    static class Constants {
        final static String ROOT_ELEMENT_NAME = "documentLookupCriteria";
        final static String TYPE_NAME = "DocumentLookupCriteriaType";
    }

    /**
     * A private class which exposes constants which define the XML element names to use when this object is marshalled to XML.
     */
    static class Elements {
        final static String DOCUMENT_ID = "documentId";
        final static String DOCUMENT_STATUSES = "documentStatuses";
        final static String DOCUMENT_STATUS = "documentStatus";
        final static String DOCUMENT_STATUS_CATEGORIES = "documentStatusCategories";
        final static String DOCUMENT_STATUS_CATEGORY = "documentStatusCategory";
        final static String TITLE = "title";
        final static String APPLICATION_DOCUMENT_ID = "applicationDocumentId";
        final static String APPLICATION_DOCUMENT_STATUS = "applicationDocumentStatus";
        final static String INITIATOR_PRINCIPAL_NAME = "initiatorPrincipalName";
        final static String VIEWER_PRINCIPAL_NAME = "viewerPrincipalName";
        final static String VIEWER_GROUP_ID = "viewerGroupId";
        final static String APPROVER_PRINCIPAL_NAME = "approverPrincipalName";
        final static String ROUTE_NODE_NAME = "routeNodeName";
        final static String ROUTE_NODE_LOOKUP_LOGIC = "routeNodeLookupLogic";
        final static String DOCUMENT_TYPE_NAME = "documentTypeName";
        final static String DATE_CREATED_FROM = "dateCreatedFrom";
        final static String DATE_CREATED_TO = "dateCreatedTo";
        final static String DATE_LAST_MODIFIED_FROM = "dateLastModifiedFrom";
        final static String DATE_LAST_MODIFIED_TO = "dateLastModifiedTo";
        final static String DATE_APPROVED_FROM = "dateApprovedFrom";
        final static String DATE_APPROVED_TO = "dateApprovedTo";
        final static String DATE_FINALIZED_FROM = "dateFinalizedFrom";
        final static String DATE_FINALIZED_TO = "dateFinalizedTo";
        final static String DATE_APPLICATION_DOCUMENT_STATUS_CHANGED_FROM = "dateApplicationDocumentStatusChangedFrom";
        final static String DATE_APPLICATION_DOCUMENT_STATUS_CHANGED_TO = "dateApplicationDocumentStatusChangedTo";
        final static String DOCUMENT_ATTRIBUTE_VALUES = "documentAttributeValues";
        final static String SAVE_NAME = "saveName";
        final static String START_AT_INDEX = "startAtIndex";
        final static String MAX_RESULTS = "maxResults";
    }

}