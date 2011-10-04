/*
 * Copyright 2005-2007 The Kuali Foundation
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
package org.kuali.rice.krad.document;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.core.proxy.ProxyHelper;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.bo.DocumentAttachment;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.bo.GlobalBusinessObject;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.bo.PersistableAttachment;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.datadictionary.DocumentEntry;
import org.kuali.rice.krad.datadictionary.WorkflowAttributes;
import org.kuali.rice.krad.datadictionary.WorkflowProperties;
import org.kuali.rice.krad.exception.PessimisticLockingException;
import org.kuali.rice.krad.exception.ValidationException;
import org.kuali.rice.krad.maintenance.Maintainable;
import org.kuali.rice.krad.maintenance.MaintenanceUtils;
import org.kuali.rice.krad.rule.event.KualiDocumentEvent;
import org.kuali.rice.krad.rule.event.SaveDocumentEvent;
import org.kuali.rice.krad.service.DocumentDictionaryService;
import org.kuali.rice.krad.service.DocumentHeaderService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.service.MaintenanceDocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.NoteType;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.util.documentserializer.PropertySerializabilityEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Document class for all maintenance documents which wraps the maintenance object in
 * a <code>Maintainable</code> that is also used for various callbacks
 *
 * <p>
 * The maintenance xml structure will be: <maintainableDocumentContents maintainableImplClass="className">
 * <oldMaintainableObject>... </oldMaintainableObject> <newMaintainableObject>... </newMaintainableObject>
 * </maintainableDocumentContents> Maintenance Document
 * </p>
 */
@Entity
@Table(name = "KRNS_MAINT_DOC_T")
public class MaintenanceDocumentBase extends DocumentBase implements MaintenanceDocument, SessionDocument {
    private static final long serialVersionUID = -505085142412593305L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MaintenanceDocumentBase.class);

    public static final String MAINTAINABLE_IMPL_CLASS = "maintainableImplClass";
    public static final String OLD_MAINTAINABLE_TAG_NAME = "oldMaintainableObject";
    public static final String NEW_MAINTAINABLE_TAG_NAME = "newMaintainableObject";
    public static final String MAINTENANCE_ACTION_TAG_NAME = "maintenanceAction";
    public static final String NOTES_TAG_NAME = "notes";

    @Transient
    transient private static DocumentDictionaryService documentDictionaryService;
    @Transient
    transient private static MaintenanceDocumentService maintenanceDocumentService;
    @Transient
    transient private static DocumentHeaderService documentHeaderService;
    @Transient
    transient private static DocumentService documentService;

    @Transient
    protected Maintainable oldMaintainableObject;
    @Transient
    protected Maintainable newMaintainableObject;

    @Column(name = "DOC_CNTNT", length = 4096)
    protected String xmlDocumentContents;
    @Transient
    protected boolean fieldsClearedOnCopy;
    @Transient
    protected boolean displayTopicFieldInNotes = false;

    @Transient
    protected String attachmentPropertyName;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "DOC_HDR_ID", insertable = false, updatable = false)
    protected DocumentAttachment attachment;

    public String getAttachmentPropertyName() {
        return this.attachmentPropertyName;
    }

    public void setAttachmentPropertyName(String attachmentPropertyName) {
        this.attachmentPropertyName = attachmentPropertyName;
    }

    public MaintenanceDocumentBase() {
        super();
        fieldsClearedOnCopy = false;
    }

    /**
     * Initializies the maintainables.
     */
    public MaintenanceDocumentBase(String documentTypeName) {
        this();
        Class clazz = getDocumentDictionaryService().getMaintainableClass(documentTypeName);
        try {
            oldMaintainableObject = (Maintainable) clazz.newInstance();
            newMaintainableObject = (Maintainable) clazz.newInstance();

            // initialize maintainable with a data object
            Class<?> dataObjectClazz = getDocumentDictionaryService().getMaintenanceDataObjectClass(documentTypeName);
            oldMaintainableObject.setDataObject(dataObjectClazz.newInstance());
            oldMaintainableObject.setDataObjectClass(dataObjectClazz);
            newMaintainableObject.setDataObject(dataObjectClazz.newInstance());
            newMaintainableObject.setDataObjectClass(dataObjectClazz);
        } catch (InstantiationException e) {
            LOG.error("Unable to initialize maintainables of type " + clazz.getName());
            throw new RuntimeException("Unable to initialize maintainables of type " + clazz.getName());
        } catch (IllegalAccessException e) {
            LOG.error("Unable to initialize maintainables of type " + clazz.getName());
            throw new RuntimeException("Unable to initialize maintainables of type " + clazz.getName());
        }
    }

    /**
     * Builds out the document title for maintenance documents - this will get loaded into the flex doc and passed into
     * workflow. It will be searchable.
     */
    @Override
    public String getDocumentTitle() {
        String documentTitle = "";

        documentTitle = newMaintainableObject.getDocumentTitle(this);
        if (StringUtils.isNotBlank(documentTitle)) {
            // if doc title has been overridden by maintainable, use it
            return documentTitle;
        }

        // TODO - build out with bo label once we get the data dictionary stuff in place
        // build out the right classname
        String className = newMaintainableObject.getDataObject().getClass().getName();
        String truncatedClassName = className.substring(className.lastIndexOf('.') + 1);
        if (isOldDataObjectInDocument()) {
            documentTitle = "Edit ";
        } else {
            documentTitle = "New ";
        }
        documentTitle += truncatedClassName + " - ";
        documentTitle += this.getDocumentHeader().getDocumentDescription() + " ";
        return documentTitle;
    }

    /**
     * @param xmlDocument
     * @return
     */
    protected boolean isOldMaintainableInDocument(Document xmlDocument) {
        boolean isOldMaintainableInExistence = false;
        if (xmlDocument.getElementsByTagName(OLD_MAINTAINABLE_TAG_NAME).getLength() > 0) {
            isOldMaintainableInExistence = true;
        }
        return isOldMaintainableInExistence;
    }

    /**
     * Checks old maintainable bo has key values
     */
    public boolean isOldDataObjectInDocument() {
        boolean isOldBusinessObjectInExistence = false;
        if (oldMaintainableObject == null || oldMaintainableObject.getDataObject() == null) {
            isOldBusinessObjectInExistence = false;
        } else {
            isOldBusinessObjectInExistence = oldMaintainableObject.isOldDataObjectInDocument();
        }
        return isOldBusinessObjectInExistence;
    }

    /**
     * This method is a simplified-naming wrapper around isOldDataObjectInDocument(), so that the method name
     * matches the functionality.
     */
    public boolean isNew() {
        return MaintenanceUtils.isMaintenanceDocumentCreatingNewRecord(newMaintainableObject.getMaintenanceAction());
    }

    /**
     * This method is a simplified-naming wrapper around isOldDataObjectInDocument(), so that the method name
     * matches the functionality.
     */
    public boolean isEdit() {
        if (KRADConstants.MAINTENANCE_EDIT_ACTION.equalsIgnoreCase(newMaintainableObject.getMaintenanceAction())) {
            return true;
        } else {
            return false;
        }
        // return isOldDataObjectInDocument();
    }

    public boolean isNewWithExisting() {
        if (KRADConstants.MAINTENANCE_NEWWITHEXISTING_ACTION
                .equalsIgnoreCase(newMaintainableObject.getMaintenanceAction())) {
            return true;
        } else {
            return false;
        }
    }

    public void populateMaintainablesFromXmlDocumentContents() {
        // get a hold of the parsed xml document, then read the classname,
        // then instantiate one to two instances depending on content
        // then populate those instances
        if (!StringUtils.isEmpty(xmlDocumentContents)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document xmlDocument = builder.parse(new InputSource(new StringReader(xmlDocumentContents)));
                String clazz = xmlDocument.getDocumentElement().getAttribute(MAINTAINABLE_IMPL_CLASS);
                if (isOldMaintainableInDocument(xmlDocument)) {
                    oldMaintainableObject = (Maintainable) Class.forName(clazz).newInstance();
                    Object dataObject = getDataObjectFromXML(OLD_MAINTAINABLE_TAG_NAME);

                    String oldMaintenanceAction = getMaintenanceAction(xmlDocument, OLD_MAINTAINABLE_TAG_NAME);
                    oldMaintainableObject.setMaintenanceAction(oldMaintenanceAction);

                    oldMaintainableObject.setDataObject(dataObject);
                    oldMaintainableObject.setDataObjectClass(dataObject.getClass());
                }
                newMaintainableObject = (Maintainable) Class.forName(clazz).newInstance();
                Object bo = getDataObjectFromXML(NEW_MAINTAINABLE_TAG_NAME);
                newMaintainableObject.setDataObject(bo);
                newMaintainableObject.setDataObjectClass(bo.getClass());

                String newMaintenanceAction = getMaintenanceAction(xmlDocument, NEW_MAINTAINABLE_TAG_NAME);
                newMaintainableObject.setMaintenanceAction(newMaintenanceAction);

                if (newMaintainableObject.isNotesEnabled()) {
                    List<Note> notes = getNotesFromXml(NOTES_TAG_NAME);
                    setNotes(notes);
                }
            } catch (ParserConfigurationException e) {
                LOG.error("Error while parsing document contents", e);
                throw new RuntimeException("Could not load document contents from xml", e);
            } catch (SAXException e) {
                LOG.error("Error while parsing document contents", e);
                throw new RuntimeException("Could not load document contents from xml", e);
            } catch (IOException e) {
                LOG.error("Error while parsing document contents", e);
                throw new RuntimeException("Could not load document contents from xml", e);
            } catch (InstantiationException e) {
                LOG.error("Error while parsing document contents", e);
                throw new RuntimeException("Could not load document contents from xml", e);
            } catch (IllegalAccessException e) {
                LOG.error("Error while parsing document contents", e);
                throw new RuntimeException("Could not load document contents from xml", e);
            } catch (ClassNotFoundException e) {
                LOG.error("Error while parsing document contents", e);
                throw new RuntimeException("Could not load document contents from xml", e);
            }
        }
    }

    /**
     * This method is a lame containment of ugly DOM walking code. This is ONLY necessary because of the version
     * conflicts between Xalan.jar in 2.6.x and 2.7. As soon as we can upgrade to 2.7, this will be switched to using
     * XPath, which is faster and much easier on the eyes.
     *
     * @param xmlDocument
     * @param oldOrNewElementName - String oldMaintainableObject or newMaintainableObject
     * @return the value of the element, or null if none was there
     */
    protected String getMaintenanceAction(Document xmlDocument, String oldOrNewElementName) {

        if (StringUtils.isBlank(oldOrNewElementName)) {
            throw new IllegalArgumentException("oldOrNewElementName may not be blank, null, or empty-string.");
        }

        String maintenanceAction = null;
        NodeList rootChildren = xmlDocument.getDocumentElement().getChildNodes();
        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node rootChild = rootChildren.item(i);
            if (oldOrNewElementName.equalsIgnoreCase(rootChild.getNodeName())) {
                NodeList maintChildren = rootChild.getChildNodes();
                for (int j = 0; j < maintChildren.getLength(); j++) {
                    Node maintChild = maintChildren.item(j);
                    if (MAINTENANCE_ACTION_TAG_NAME.equalsIgnoreCase(maintChild.getNodeName())) {
                        maintenanceAction = maintChild.getChildNodes().item(0).getNodeValue();
                    }
                }
            }
        }
        return maintenanceAction;
    }

    private List<Note> getNotesFromXml(String notesTagName) {
        String notesXml =
                StringUtils.substringBetween(xmlDocumentContents, "<" + notesTagName + ">", "</" + notesTagName + ">");
        if (StringUtils.isBlank(notesXml)) {
            return Collections.emptyList();
        }
        List<Note> notes = (List<Note>) KRADServiceLocator.getXmlObjectSerializerService().fromXml(notesXml);
        if (notes == null) {
            return Collections.emptyList();
        }
        return notes;
    }

    /**
     * Retrieves substring of document contents from maintainable tag name. Then use xml service to translate xml into
     * a
     * business object.
     */
    protected Object getDataObjectFromXML(String maintainableTagName) {
        String maintXml = StringUtils.substringBetween(xmlDocumentContents, "<" + maintainableTagName + ">",
                "</" + maintainableTagName + ">");
        Object businessObject = KRADServiceLocator.getXmlObjectSerializerService().fromXml(maintXml);
        return businessObject;
    }

    /**
     * Populates the xml document contents from the maintainables.
     *
     * @see org.kuali.rice.krad.document.MaintenanceDocument#populateXmlDocumentContentsFromMaintainables()
     */
    public void populateXmlDocumentContentsFromMaintainables() {
        StringBuilder docContentBuffer = new StringBuilder();
        docContentBuffer.append("<maintainableDocumentContents maintainableImplClass=\"")
                .append(newMaintainableObject.getClass().getName()).append("\">");

        // if business objects notes are enabled then we need to persist notes to the XML
        if (getNewMaintainableObject().isNotesEnabled()) {
            docContentBuffer.append("<" + NOTES_TAG_NAME + ">");
            // copy notes to a non-ojb Proxied ArrayList to get rid of the usage of those proxies
            // note: XmlObjectSerializerServiceImpl should be doing this for us but it does not
            // appear to be working (at least in this case) and the xml comes through
            // with the fully qualified ListProxyDefault class name from OJB embedded inside it.
            List<Note> noteList = new ArrayList<Note>();
            for (Note note : getNotes()) {
                noteList.add(note);
            }
            docContentBuffer.append(KRADServiceLocator.getXmlObjectSerializerService().toXml(noteList));
            docContentBuffer.append("</" + NOTES_TAG_NAME + ">");
        }
        if (oldMaintainableObject != null && oldMaintainableObject.getDataObject() != null) {
            // TODO: refactor this out into a method
            docContentBuffer.append("<" + OLD_MAINTAINABLE_TAG_NAME + ">");

            Object oldBo = oldMaintainableObject.getDataObject();

            // hack to resolve XStream not dealing well with Proxies
            if (oldBo instanceof PersistableBusinessObject) {
                ObjectUtils.materializeAllSubObjects((PersistableBusinessObject) oldBo);
            }

            docContentBuffer.append(KRADServiceLocator.getBusinessObjectSerializerService()
                    .serializeBusinessObjectToXml(oldBo));

            // add the maintainable's maintenanceAction
            docContentBuffer.append("<" + MAINTENANCE_ACTION_TAG_NAME + ">");
            docContentBuffer.append(oldMaintainableObject.getMaintenanceAction());
            docContentBuffer.append("</" + MAINTENANCE_ACTION_TAG_NAME + ">\n");

            docContentBuffer.append("</" + OLD_MAINTAINABLE_TAG_NAME + ">");
        }
        docContentBuffer.append("<" + NEW_MAINTAINABLE_TAG_NAME + ">");

        Object newBo = newMaintainableObject.getDataObject();

        if (newBo instanceof PersistableBusinessObject) {
            // hack to resolve XStream not dealing well with Proxies
            ObjectUtils.materializeAllSubObjects((PersistableBusinessObject) newBo);
        }

        docContentBuffer
                .append(KRADServiceLocator.getBusinessObjectSerializerService().serializeBusinessObjectToXml(newBo));

        // add the maintainable's maintenanceAction
        docContentBuffer.append("<" + MAINTENANCE_ACTION_TAG_NAME + ">");
        docContentBuffer.append(newMaintainableObject.getMaintenanceAction());
        docContentBuffer.append("</" + MAINTENANCE_ACTION_TAG_NAME + ">\n");

        docContentBuffer.append("</" + NEW_MAINTAINABLE_TAG_NAME + ">");
        docContentBuffer.append("</maintainableDocumentContents>");
        xmlDocumentContents = docContentBuffer.toString();
    }

    /**
     * @see org.kuali.rice.krad.document.DocumentBase#doRouteStatusChange(org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO)
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChangeDTO statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        getNewMaintainableObject().doRouteStatusChange(getDocumentHeader());
        // commit the changes to the Maintainable BusinessObject when it goes to Processed (ie, fully approved),
        // and also unlock it
        if (workflowDocument.isProcessed()) {
            String documentNumber = getDocumentHeader().getDocumentNumber();
            newMaintainableObject.setDocumentNumber(documentNumber);

            //Populate Attachment Property
            if (newMaintainableObject.getDataObject() instanceof PersistableAttachment) {
                populateAttachmentForBO();
            }

            newMaintainableObject.saveDataObject();

            if (!getDocumentService().saveDocumentNotes(this)) {
                throw new IllegalStateException(
                        "Failed to save document notes, this means that the note target was not ready for notes to be attached when it should have been.");
            }

            //Attachment should be deleted from Maintenance Document attachment table
            deleteDocumentAttachment();

            getMaintenanceDocumentService().deleteLocks(documentNumber);

            //for issue 3070, check if delete record
            if (this.checkAllowsRecordDeletion() && this.checkMaintenanceAction() &&
                    this.checkDeletePermission(newMaintainableObject.getDataObject()))
                newMaintainableObject.deleteDataObject();
        }

        // unlock the document when its canceled or disapproved
        if (workflowDocument.isCanceled() || workflowDocument.isDisapproved()) {
            //Attachment should be deleted from Maintenance Document attachment table
            deleteDocumentAttachment();

            String documentNumber = getDocumentHeader().getDocumentNumber();
            getMaintenanceDocumentService().deleteLocks(documentNumber);
        }
    }

    @Override
    /**
     * @see org.kuali.rice.krad.document.DocumentBase#getWorkflowEngineDocumentIdsToLock()
     */
    public List<Long> getWorkflowEngineDocumentIdsToLock() {
        if (newMaintainableObject != null) {
            return newMaintainableObject.getWorkflowEngineDocumentIdsToLock();
        }
        return Collections.emptyList();
    }

    /**
     * Pre-Save hook.
     *
     * @see org.kuali.rice.krad.document.Document#prepareForSave()
     */
    @Override
    public void prepareForSave() {
        if (newMaintainableObject != null) {
            newMaintainableObject.prepareForSave();
        }
    }

    /**
     * @see org.kuali.rice.krad.document.DocumentBase#processAfterRetrieve()
     */
    @Override
    public void processAfterRetrieve() {

        super.processAfterRetrieve();

        populateMaintainablesFromXmlDocumentContents();
        if (oldMaintainableObject != null) {
            oldMaintainableObject.setDocumentNumber(documentNumber);
        }
        if (newMaintainableObject != null) {
            newMaintainableObject.setDocumentNumber(documentNumber);
            newMaintainableObject.processAfterRetrieve();
            // If a maintenance lock exists, warn the user.
            checkForLockingDocument(false);
        }
    }

    /**
     * @return Returns the newMaintainableObject.
     */
    public Maintainable getNewMaintainableObject() {
        return newMaintainableObject;
    }

    /**
     * @param newMaintainableObject The newMaintainableObject to set.
     */
    public void setNewMaintainableObject(Maintainable newMaintainableObject) {
        this.newMaintainableObject = newMaintainableObject;
    }

    /**
     * @return Returns the oldMaintainableObject.
     */
    public Maintainable getOldMaintainableObject() {
        return oldMaintainableObject;
    }

    /**
     * @param oldMaintainableObject The oldMaintainableObject to set.
     */
    public void setOldMaintainableObject(Maintainable oldMaintainableObject) {
        this.oldMaintainableObject = oldMaintainableObject;
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        super.setDocumentNumber(documentNumber);

        // set the finDocNumber on the Maintainable
        oldMaintainableObject.setDocumentNumber(documentNumber);
        newMaintainableObject.setDocumentNumber(documentNumber);
    }

    /**
     * Gets the fieldsClearedOnCopy attribute.
     *
     * @return Returns the fieldsClearedOnCopy.
     */
    public final boolean isFieldsClearedOnCopy() {
        return fieldsClearedOnCopy;
    }

    /**
     * Sets the fieldsClearedOnCopy attribute value.
     *
     * @param fieldsClearedOnCopy The fieldsClearedOnCopy to set.
     */
    public final void setFieldsClearedOnCopy(boolean fieldsClearedOnCopy) {
        this.fieldsClearedOnCopy = fieldsClearedOnCopy;
    }

    /**
     * Gets the xmlDocumentContents attribute.
     *
     * @return Returns the xmlDocumentContents.
     */
    public String getXmlDocumentContents() {
        return xmlDocumentContents;
    }

    /**
     * Sets the xmlDocumentContents attribute value.
     *
     * @param xmlDocumentContents The xmlDocumentContents to set.
     */
    public void setXmlDocumentContents(String xmlDocumentContents) {
        this.xmlDocumentContents = xmlDocumentContents;
    }

    /**
     * @see org.kuali.rice.krad.document.Document#getAllowsCopy()
     */
    public boolean getAllowsCopy() {
        return getDocumentDictionaryService().getAllowsCopy(this);
    }

    /**
     * @see org.kuali.rice.krad.document.MaintenanceDocument#getDisplayTopicFieldInNotes()
     */
    public boolean getDisplayTopicFieldInNotes() {
        return displayTopicFieldInNotes;
    }

    /**
     * @see org.kuali.rice.krad.document.MaintenanceDocument#setDisplayTopicFieldInNotes(boolean)
     */
    public void setDisplayTopicFieldInNotes(boolean displayTopicFieldInNotes) {
        this.displayTopicFieldInNotes = displayTopicFieldInNotes;
    }

    @Override
    /**
     * Overridden to avoid serializing the xml twice, because of the xmlDocumentContents property of this object
     */
    public String serializeDocumentToXml() {
        String tempXmlDocumentContents = xmlDocumentContents;
        xmlDocumentContents = null;
        String xmlForWorkflow = super.serializeDocumentToXml();
        xmlDocumentContents = tempXmlDocumentContents;
        return xmlForWorkflow;
    }

    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        super.prepareForSave(event);

        populateDocumentAttachment();
        populateXmlDocumentContentsFromMaintainables();
    }

    /**
     * The attachment BO is proxied in OJB.  For some reason when an attachment does not yet exist,
     * refreshReferenceObject is not returning null and the proxy cannot be materialized. So, this method exists to
     * properly handle the proxied attachment BO.  This is a hack and should be removed post JPA migration.
     */
    protected void refreshAttachment() {
        if (ObjectUtils.isNull(attachment)) {
            this.refreshReferenceObject("attachment");
            final boolean isProxy = attachment != null && ProxyHelper.isProxy(attachment);
            if (isProxy && ProxyHelper.getRealObject(attachment) == null) {
                attachment = null;
            }
        }
    }

    protected void populateAttachmentForBO() {
        refreshAttachment();

        PersistableAttachment boAttachment = (PersistableAttachment) newMaintainableObject.getDataObject();

        if (attachment != null) {
            byte[] fileContents;
            fileContents = attachment.getAttachmentContent();
            if (fileContents.length > 0) {
                boAttachment.setAttachmentContent(fileContents);
                boAttachment.setFileName(attachment.getFileName());
                boAttachment.setContentType(attachment.getContentType());
            }
        }
    }

    public void populateDocumentAttachment() {
        // TODO: need to convert this from using struts form file
//        refreshAttachment();
//
//        if (fileAttachment != null && StringUtils.isNotEmpty(fileAttachment.getFileName())) {
//            //Populate DocumentAttachment BO
//            if (attachment == null) {
//                attachment = new DocumentAttachment();
//            }
//
//            byte[] fileContents;
//            try {
//                fileContents = fileAttachment.getFileData();
//                if (fileContents.length > 0) {
//                    attachment.setFileName(fileAttachment.getFileName());
//                    attachment.setContentType(fileAttachment.getContentType());
//                    attachment.setAttachmentContent(fileAttachment.getFileData());
//                    attachment.setDocumentNumber(getDocumentNumber());
//                }
//            } catch (FileNotFoundException e) {
//                LOG.error("Error while populating the Document Attachment", e);
//                throw new RuntimeException("Could not populate DocumentAttachment object", e);
//            } catch (IOException e) {
//                LOG.error("Error while populating the Document Attachment", e);
//                throw new RuntimeException("Could not populate DocumentAttachment object", e);
//            }
//        }
////        else if(attachment != null) {
////            //Attachment has been deleted - Need to delete the Attachment Reference Object
////            deleteAttachment();
////        }
    }

    public void deleteDocumentAttachment() {
        KRADServiceLocator.getBusinessObjectService().delete(attachment);
        attachment = null;
    }

    /**
     * Explicitly NOT calling super here.  This is a complete override of the validation rules behavior.
     *
     * @see org.kuali.rice.krad.document.DocumentBase#validateBusinessRules(org.kuali.rice.krad.rule.event.KualiDocumentEvent)
     */
    public void validateBusinessRules(KualiDocumentEvent event) {
        if (GlobalVariables.getMessageMap().hasErrors()) {
            logErrors();
            throw new ValidationException("errors occured before business rule");
        }

        // check for locking documents for MaintenanceDocuments
        if (this instanceof MaintenanceDocument) {
            checkForLockingDocument(true);
        }

        // Make sure the business object's version number matches that of the database's copy.
        if (newMaintainableObject != null) {
            if (KRADServiceLocator.getPersistenceStructureService()
                    .isPersistable(newMaintainableObject.getDataObject().getClass())) {
                PersistableBusinessObject pbObject = KRADServiceLocator.getBusinessObjectService()
                        .retrieve((PersistableBusinessObject) newMaintainableObject.getDataObject());
                Long pbObjectVerNbr = ObjectUtils.isNull(pbObject) ? null : pbObject.getVersionNumber();
                Long newObjectVerNbr = ((PersistableBusinessObject) newMaintainableObject.getDataObject()).getVersionNumber();
                if (pbObjectVerNbr != null && !(pbObjectVerNbr.equals(newObjectVerNbr))) {
                    GlobalVariables.getMessageMap()
                            .putError(KRADConstants.GLOBAL_ERRORS, RiceKeyConstants.ERROR_VERSION_MISMATCH);
                    throw new ValidationException(
                            "Version mismatch between the local business object and the database business object");
                }
            }
        }

        // perform validation against rules engine
        if (LOG.isInfoEnabled()) {
            LOG.info("invoking rules engine on document " + getDocumentNumber());
        }
        boolean isValid = true;
        isValid = KRADServiceLocatorWeb.getKualiRuleService().applyRules(event);

        // check to see if the br eval passed or failed
        if (!isValid) {
            logErrors();
            // TODO: better error handling at the lower level and a better error message are
            // needed here
            throw new ValidationException("business rule evaluation failed");
        } else if (GlobalVariables.getMessageMap().hasErrors()) {
            logErrors();
            if (event instanceof SaveDocumentEvent) {
                // for maintenance documents, we want to always actually do a save if the
                // user requests a save, even if there are validation or business rules
                // failures. this empty if does this, and allows the document to be saved,
                // even if there are failures.
                // BR or validation failures on a ROUTE even should always stop the route,
                // that has not changed
            } else {
                throw new ValidationException(
                        "Unreported errors occured during business rule evaluation (rule developer needs to put meaningful error messages into global ErrorMap)");
            }
        }
        LOG.debug("validation completed");
    }

    protected void checkForLockingDocument(boolean throwExceptionIfLocked) {
        MaintenanceUtils.checkForLockingDocument(this, throwExceptionIfLocked);
    }

    /**
     * this needs to happen after the document itself is saved, to preserve consistency of the ver_nbr and in the case
     * of initial save, because this can't be saved until the document is saved initially
     *
     * @see org.kuali.rice.krad.document.DocumentBase#postProcessSave(org.kuali.rice.krad.rule.event.KualiDocumentEvent)
     */
    @Override
    public void postProcessSave(KualiDocumentEvent event) {
        if (getNewMaintainableObject().getDataObject() instanceof PersistableBusinessObject) {
            PersistableBusinessObject bo = (PersistableBusinessObject) getNewMaintainableObject().getDataObject();
            if (bo instanceof GlobalBusinessObject) {
                KRADServiceLocator.getBusinessObjectService().save(bo);
            }
        }

        //currently only global documents could change the list of what they're affecting during routing,
        //so could restrict this to only happening with them, but who knows if that will change, so safest
        //to always do the delete and re-add...seems a bit inefficient though if nothing has changed, which is
        //most of the time...could also try to only add/update/delete what's changed, but this is easier
        if (!(event instanceof SaveDocumentEvent)) { //don't lock until they route
            getMaintenanceDocumentService().deleteLocks(this.getDocumentNumber());
            getMaintenanceDocumentService().storeLocks(this.getNewMaintainableObject().generateMaintenanceLocks());
        }
    }

    /**
     * @see org.kuali.rice.krad.document.DocumentBase#getDocumentBusinessObject()
     */
    @Override
    public Object getDocumentDataObject() {
        return getNewMaintainableObject().getDataObject();
    }

    /**
     * <p>The Note target for maintenance documents is determined by whether or not the underlying {@link Maintainable}
     * supports business object notes or not.  This is determined via a call to {@link
     * Maintainable#isBoNotesEnabled()}.
     * The note target is then derived as follows: <p/> <ul> <li>If the {@link Maintainable} supports business object
     * notes, delegate to {@link #getDocumentDataObject()}. <li>Otherwise, delegate to the default implementation of
     * getNoteTarget on the superclass which will effectively return a reference to the {@link DocumentHeader}. </ul>
     *
     * @see org.kuali.rice.krad.document.Document#getNoteTarget()
     */
    @Override
    public PersistableBusinessObject getNoteTarget() {
        if (getNewMaintainableObject() == null) {
            throw new IllegalStateException(
                    "Failed to acquire the note target.  The new maintainable object on this document is null.");
        }
        if (getNewMaintainableObject().isNotesEnabled()) {
            return (PersistableBusinessObject) getDocumentDataObject();
        }
        return super.getNoteTarget();
    }

    /**
     * The {@link NoteType} for maintenance documents is determined by whether or not the underlying {@link
     * Maintainable} supports business object notes or not.  This is determined via a call to {@link
     * Maintainable#isBoNotesEnabled()}.  The {@link NoteType} is then derived as follows: <p/> <ul> <li>If the {@link
     * Maintainable} supports business object notes, return {@link NoteType#BUSINESS_OBJECT}. <li>Otherwise, delegate
     * to
     * {@link DocumentBase#getNoteType()} </ul>
     *
     * @see org.kuali.rice.krad.document.Document#getNoteType()
     * @see org.kuali.rice.krad.document.Document#getNoteTarget()
     */
    @Override
    public NoteType getNoteType() {
        if (getNewMaintainableObject().isNotesEnabled()) {
            return NoteType.BUSINESS_OBJECT;
        }
        return super.getNoteType();
    }

    @Override
    public PropertySerializabilityEvaluator getDocumentPropertySerizabilityEvaluator() {
        String docTypeName = "";
        if (newMaintainableObject != null) {
            docTypeName = getDocumentDictionaryService()
                    .getMaintenanceDocumentTypeName(this.newMaintainableObject.getDataObjectClass());
        } else { // I don't know why we aren't just using the header in the first place
            // but, in the case where we can't get it in the way above, attempt to get
            // it off the workflow document header
            if (getDocumentHeader() != null && getDocumentHeader().getWorkflowDocument() != null) {
                docTypeName = getDocumentHeader().getWorkflowDocument().getDocumentTypeName();
            }
        }
        if (!StringUtils.isBlank(docTypeName)) {
            DocumentEntry documentEntry =
                    getDocumentDictionaryService().getMaintenanceDocumentEntry(docTypeName);
            if (documentEntry != null) {
                WorkflowProperties workflowProperties = documentEntry.getWorkflowProperties();
                WorkflowAttributes workflowAttributes = documentEntry.getWorkflowAttributes();
                return createPropertySerializabilityEvaluator(workflowProperties, workflowAttributes);
            } else {
                LOG.error("Unable to obtain DD DocumentEntry for document type: '" + docTypeName + "'");
            }
        } else {
            LOG.error("Unable to obtain document type name for this document: " + this);
        }
        LOG.error("Returning null for the PropertySerializabilityEvaluator");
        return null;
    }

    public DocumentAttachment getAttachment() {
        return this.attachment;
    }

    public void setAttachment(DocumentAttachment attachment) {
        this.attachment = attachment;
    }

    /**
     * This overridden method is used to delete the {@link DocumentHeader} object due to the system not being able to
     * manage the {@link DocumentHeader} object via mapping files
     *
     * @see org.kuali.rice.krad.bo.PersistableBusinessObjectBase#postRemove()
     */
    @Override
    protected void postRemove() {
        super.postRemove();
        getDocumentHeaderService().deleteDocumentHeader(getDocumentHeader());
    }

    /**
     * This overridden method is used to retrieve the {@link DocumentHeader} object due to the system not being able to
     * manage the {@link DocumentHeader} object via mapping files
     *
     * @see org.kuali.rice.krad.bo.PersistableBusinessObjectBase#postLoad()
     */
    @Override
    protected void postLoad() {
        super.postLoad();
        setDocumentHeader(getDocumentHeaderService().getDocumentHeaderById(getDocumentNumber()));
    }

    /**
     * This overridden method is used to insert the {@link DocumentHeader} object due to the system not being able to
     * manage the {@link DocumentHeader} object via mapping files
     *
     * @see org.kuali.rice.krad.bo.PersistableBusinessObjectBase#prePersist()
     */
    @Override
    protected void prePersist() {
        super.prePersist();
        getDocumentHeaderService().saveDocumentHeader(getDocumentHeader());
    }

    /**
     * This overridden method is used to save the {@link DocumentHeader} object due to the system not being able to
     * manage the {@link DocumentHeader} object via mapping files
     *
     * @see org.kuali.rice.krad.bo.PersistableBusinessObjectBase#preUpdate()
     */
    @Override
    protected void preUpdate() {
        super.preUpdate();
        getDocumentHeaderService().saveDocumentHeader(getDocumentHeader());
    }

    /**
     * This method to check whether the document class implements SessionDocument
     *
     * @return
     */
    public boolean isSessionDocument() {
        return SessionDocument.class.isAssignableFrom(this.getClass());
    }

    /**
     * Returns whether or not the new maintainable object supports custom lock descriptors. Will always return false if
     * the new maintainable is null.
     *
     * @see org.kuali.rice.krad.document.Document#useCustomLockDescriptors()
     * @see org.kuali.rice.krad.maintenance.Maintainable#useCustomLockDescriptors()
     */
    @Override
    public boolean useCustomLockDescriptors() {
        return (newMaintainableObject != null && newMaintainableObject.useCustomLockDescriptors());
    }

    /**
     * Returns the custom lock descriptor generated by the new maintainable object, if defined. Will throw a
     * PessimisticLockingException if the new maintainable is null.
     *
     * @see org.kuali.rice.krad.document.Document#getCustomLockDescriptor(org.kuali.rice.kim.api.identity.Person)
     * @see org.kuali.rice.krad.maintenance.Maintainable#getCustomLockDescriptor(org.kuali.rice.kim.api.identity.Person)
     */
    @Override
    public String getCustomLockDescriptor(Person user) {
        if (newMaintainableObject == null) {
            throw new PessimisticLockingException("Maintenance Document " + getDocumentNumber() +
                    " is using pessimistic locking with custom lock descriptors, but no new maintainable object has been defined");
        }
        return newMaintainableObject.getCustomLockDescriptor(user);
    }

    protected DocumentDictionaryService getDocumentDictionaryService() {
        if (documentDictionaryService == null) {
            documentDictionaryService = KRADServiceLocatorWeb.getDocumentDictionaryService();
        }
        return documentDictionaryService;
    }

    protected MaintenanceDocumentService getMaintenanceDocumentService() {
        if (maintenanceDocumentService == null) {
            maintenanceDocumentService = KRADServiceLocatorWeb.getMaintenanceDocumentService();
        }
        return maintenanceDocumentService;
    }

    protected DocumentHeaderService getDocumentHeaderService() {
        if (documentHeaderService == null) {
            documentHeaderService = KRADServiceLocatorWeb.getDocumentHeaderService();
        }
        return documentHeaderService;
    }

    protected DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = KRADServiceLocatorWeb.getDocumentService();
        }
        return documentService;
    }

    //for issue KULRice3070
    protected boolean checkAllowsRecordDeletion() {
        Boolean allowsRecordDeletion = KRADServiceLocatorWeb.getDocumentDictionaryService()
                .getAllowsRecordDeletion(this.getNewMaintainableObject().getDataObjectClass());
        if (allowsRecordDeletion != null) {
            return allowsRecordDeletion.booleanValue();
        } else {
            return false;
        }
    }

    //for KULRice3070
    protected boolean checkMaintenanceAction() {
        return this.getNewMaintainableObject().getMaintenanceAction().equals(KRADConstants.MAINTENANCE_DELETE_ACTION);
    }

    //for KULRice3070
    protected boolean checkDeletePermission(Object dataObject) {
        boolean allowsMaintain = false;

        String maintDocTypeName = KRADServiceLocatorWeb.getDocumentDictionaryService()
                .getMaintenanceDocumentTypeName(dataObject.getClass());

        if (StringUtils.isNotBlank(maintDocTypeName)) {
            allowsMaintain = KRADServiceLocatorWeb.getDataObjectAuthorizationService()
                    .canMaintain(dataObject, GlobalVariables.getUserSession().getPerson(), maintDocTypeName);
        }
        return allowsMaintain;
    }
}
