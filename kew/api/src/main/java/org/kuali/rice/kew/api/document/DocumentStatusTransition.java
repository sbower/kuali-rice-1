package org.kuali.rice.kew.api.document;

import java.io.Serializable;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.CoreConstants;
import org.kuali.rice.core.api.mo.AbstractDataTransferObject;
import org.kuali.rice.core.api.mo.ModelBuilder;
import org.w3c.dom.Element;

@XmlRootElement(name = DocumentStatusTransition.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = DocumentStatusTransition.Constants.TYPE_NAME, propOrder = {
    DocumentStatusTransition.Elements.ID,
    DocumentStatusTransition.Elements.DOCUMENT_ID,
    DocumentStatusTransition.Elements.OLD_APP_DOC_STATUS,
    DocumentStatusTransition.Elements.NEW_APP_DOC_STATUS,
    DocumentStatusTransition.Elements.STATUS_TRANSITION_DATE,
    CoreConstants.CommonElements.FUTURE_ELEMENTS
})
public final class DocumentStatusTransition
    extends AbstractDataTransferObject
    implements DocumentStatusTransitionContract
{

    @XmlElement(name = Elements.ID, required = false)
    private final String id;
    @XmlElement(name = Elements.DOCUMENT_ID, required = false)
    private final String documentId;
    @XmlElement(name = Elements.OLD_APP_DOC_STATUS, required = false)
    private final String oldStatus;
    @XmlElement(name = Elements.NEW_APP_DOC_STATUS, required = false)
    private final String newStatus;
    @XmlElement(name = Elements.STATUS_TRANSITION_DATE, required = false)
    private final DateTime statusTransitionDate;
    @SuppressWarnings("unused")
    @XmlAnyElement
    private final Collection<Element> _futureElements = null;

    /**
     * Private constructor used only by JAXB.
     * 
     */
    private DocumentStatusTransition() {
        this.id = null;
        this.documentId = null;
        this.oldStatus = null;
        this.newStatus = null;
        this.statusTransitionDate = null;
    }

    private DocumentStatusTransition(Builder builder) {
        this.id = builder.getId();
        this.documentId = builder.getDocumentId();
        this.oldStatus = builder.getOldStatus();
        this.newStatus = builder.getNewStatus();
        this.statusTransitionDate = builder.getStatusTransitionDate();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDocumentId() {
        return this.documentId;
    }

    @Override
    public String getOldStatus() {
        return this.oldStatus;
    }

    @Override
    public String getNewStatus() {
        return this.newStatus;
    }

    @Override
    public DateTime getStatusTransitionDate() {
        return this.statusTransitionDate;
    }


    /**
     * A builder which can be used to construct {@link DocumentStatusTransition} instances.  Enforces the constraints of the {@link DocumentStatusTransitionContract}.
     * 
     */
    public final static class Builder
        implements Serializable, ModelBuilder, DocumentStatusTransitionContract
    {

        private String id;
        private String documentId;
        private String oldStatus;
        private String newStatus;
        private DateTime statusTransitionDate;

        private Builder(String documentId, String oldStatus, String newStatus) {
            setDocumentId(documentId);
            setOldStatus(oldStatus);
            setNewStatus(newStatus);
        }

        public static Builder create(String documentId, String oldStatus, String newStatus) {
            return new Builder(documentId, oldStatus, newStatus);
        }

        public static Builder create(DocumentStatusTransitionContract contract) {
            if (contract == null) {
                throw new IllegalArgumentException("contract was null");
            }
            Builder builder = create(contract.getDocumentId(), contract.getOldStatus(), contract.getNewStatus());
            builder.setId(contract.getId());
            builder.setStatusTransitionDate(contract.getStatusTransitionDate());
            return builder;
        }

        public DocumentStatusTransition build() {
            return new DocumentStatusTransition(this);
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String getDocumentId() {
            return this.documentId;
        }

        @Override
        public String getOldStatus() {
            return this.oldStatus;
        }

        @Override
        public String getNewStatus() {
            return this.newStatus;
        }

        @Override
        public DateTime getStatusTransitionDate() {
            return this.statusTransitionDate;
        }

        public void setId(String id) {
            // TODO add validation of input value if required and throw IllegalArgumentException if needed
            this.id = id;
        }

        public void setDocumentId(String documentId) {
            // TODO add validation of input value if required and throw IllegalArgumentException if needed
            this.documentId = documentId;
        }

        public void setOldStatus(String oldStatus) {
            // TODO add validation of input value if required and throw IllegalArgumentException if needed
            this.oldStatus = oldStatus;
        }

        public void setNewStatus(String newStatus) {
            // TODO add validation of input value if required and throw IllegalArgumentException if needed
            this.newStatus = newStatus;
        }

        public void setStatusTransitionDate(DateTime statusTransitionDate) {
            // TODO add validation of input value if required and throw IllegalArgumentException if needed
            this.statusTransitionDate = statusTransitionDate;
        }

    }


    /**
     * Defines some internal constants used on this class.
     * 
     */
    static class Constants {

        final static String ROOT_ELEMENT_NAME = "documentStatusTransition";
        final static String TYPE_NAME = "DocumentStatusTransitionType";

    }


    /**
     * A private class which exposes constants which define the XML element names to use when this object is marshalled to XML.
     * 
     */
    static class Elements {

        final static String ID = "id";
        final static String DOCUMENT_ID = "documentId";
        final static String OLD_APP_DOC_STATUS = "oldStatus";
        final static String NEW_APP_DOC_STATUS = "newStatus";
        final static String STATUS_TRANSITION_DATE = "statusTransitionDate";

    }

}