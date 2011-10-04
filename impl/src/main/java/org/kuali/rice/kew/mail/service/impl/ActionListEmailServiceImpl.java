/*
 * Copyright 2006-2011 The Kuali Foundation
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

package org.kuali.rice.kew.mail.service.impl;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.framework.services.CoreFrameworkServiceLocator;
import org.kuali.rice.core.mail.EmailBody;
import org.kuali.rice.core.mail.EmailFrom;
import org.kuali.rice.core.mail.EmailSubject;
import org.kuali.rice.core.mail.EmailTo;
import org.kuali.rice.core.mail.Mailer;
import org.kuali.rice.kew.actionlist.service.ActionListService;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.action.ActionItem;
import org.kuali.rice.kew.api.action.ActionRequest;
import org.kuali.rice.kew.api.action.DelegationType;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.dto.DTOConverter;
import org.kuali.rice.kew.dto.RouteHeaderDTO;
import org.kuali.rice.kew.mail.CustomEmailAttribute;
import org.kuali.rice.kew.mail.DailyEmailJob;
import org.kuali.rice.kew.mail.WeeklyEmailJob;
import org.kuali.rice.kew.mail.service.ActionListEmailService;
import org.kuali.rice.kew.preferences.Preferences;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.useroptions.UserOptions;
import org.kuali.rice.kew.useroptions.UserOptionsService;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * ActionListeEmailService which generates messages whose body and subject can be customized via KEW
 * configuration parameters, 'immediate.reminder.email.message' and
 * 'immediate.reminder.email.subject'. The immediate reminder email message key should specify a
 * MessageFormat string. See code for the parameters to this MessageFormat.
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ActionListEmailServiceImpl implements ActionListEmailService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(ActionListEmailServiceImpl.class);

    private static final String DEFAULT_EMAIL_FROM_ADDRESS = "admin@localhost";

    private static final String ACTION_LIST_REMINDER = "Action List Reminder";

    private static final String IMMEDIATE_REMINDER_EMAIL_MESSAGE_KEY = "immediate.reminder.email.message";

    private static final String IMMEDIATE_REMINDER_EMAIL_SUBJECT_KEY = "immediate.reminder.email.subject";

    private static final String DAILY_TRIGGER_NAME = "Daily Email Trigger";
    private static final String DAILY_JOB_NAME = "Daily Email";
    private static final String WEEKLY_TRIGGER_NAME = "Weekly Email Trigger";
    private static final String WEEKLY_JOB_NAME = "Weekly Email";

    private String deploymentEnvironment;

    private Mailer mailer;

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public String getDocumentTypeEmailAddress(DocumentType documentType) {
        String fromAddress = (documentType == null ? null : documentType
                .getNotificationFromAddress());
        if (org.apache.commons.lang.StringUtils.isEmpty(fromAddress)) {
            fromAddress = getApplicationEmailAddress();
        }
        return fromAddress;
    }

    public String getApplicationEmailAddress() {
        // first check the configured value
        String fromAddress = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                KEWConstants.KEW_NAMESPACE, KRADConstants.DetailTypes.MAILER_DETAIL_TYPE,
                KEWConstants.EMAIL_REMINDER_FROM_ADDRESS);
        // if there's no value configured, use the default
        if (org.apache.commons.lang.StringUtils.isEmpty(fromAddress)) {
            fromAddress = DEFAULT_EMAIL_FROM_ADDRESS;
        }
        return fromAddress;
    }

    protected String getHelpLink() {
        return getHelpLink(null);
    }

    protected String getHelpLink(DocumentType documentType) {
        return "For additional help, email " + "<mailto:"
                + getDocumentTypeEmailAddress(documentType) + ">";
    }

    public EmailSubject getEmailSubject() {
        String subject = ConfigContext.getCurrentContextConfig().getProperty(IMMEDIATE_REMINDER_EMAIL_SUBJECT_KEY);
        if (subject == null) {
            subject = ACTION_LIST_REMINDER;
        }
        return new EmailSubject(subject);
    }

    public EmailSubject getEmailSubject(String customSubject) {
        String subject = ConfigContext.getCurrentContextConfig().getProperty(IMMEDIATE_REMINDER_EMAIL_SUBJECT_KEY);
        if (subject == null) {
            subject = ACTION_LIST_REMINDER;
        }
        return new EmailSubject(subject + " " + customSubject);
    }

    protected EmailFrom getEmailFrom(DocumentType documentType) {
        return new EmailFrom(getDocumentTypeEmailAddress(documentType));
    }

    protected void sendEmail(Person user, EmailSubject subject,
            EmailBody body) {
        sendEmail(user, subject, body, null);
    }

    protected void sendEmail(Person user, EmailSubject subject,
            EmailBody body, DocumentType documentType) {
        try {
            if (isProduction()) {
                mailer.sendEmail(getEmailFrom(documentType),
                        new EmailTo(user.getEmailAddressUnmasked()),
                        subject,
                        body,
                        false);
            } else {
                mailer.sendEmail(
                        getEmailFrom(documentType),
                        new EmailTo(CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                                KEWConstants.KEW_NAMESPACE, KRADConstants.DetailTypes.ACTION_LIST_DETAIL_TYPE,
                                KEWConstants.ACTIONLIST_EMAIL_TEST_ADDRESS)),
                        subject,
                        body,
                        false);
            }
        } catch (Exception e) {
            LOG.error("Error sending Action List email.", e);
        }
    }

    public void sendImmediateReminder(ActionItem actionItem, Boolean skipOnApprovals) {
        if (actionItem == null) {
            LOG.warn("Request to send immediate reminder to recipient of a null action item... aborting.");
            return;
        }
        
        if (actionItem.getPrincipalId() == null) {
            LOG.warn("Request to send immediate reminder to null recipient of an action item... aborting.");
            return;
        }

        if (skipOnApprovals != null && skipOnApprovals.booleanValue()
                && actionItem.getActionRequestCd().equals(KEWConstants.ACTION_REQUEST_APPROVE_REQ)) {
            LOG.debug("As requested, skipping immediate reminder notification on action item approval for " + actionItem.getPrincipalId());
            return;
        }

        boolean shouldSendActionListEmailNotification = sendActionListEmailNotification();
        if (shouldSendActionListEmailNotification) {
            LOG.debug("sending immediate reminder");

            Person person = KimApiServiceLocator.getPersonService().getPerson(actionItem.getPrincipalId());
            
            DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(
                    actionItem.getDocumentId());
            StringBuffer emailBody = new StringBuffer(buildImmediateReminderBody(person, actionItem,
                    document.getDocumentType()));
            StringBuffer emailSubject = new StringBuffer();
            try {
                CustomEmailAttribute customEmailAttribute = document.getCustomEmailAttribute();
                if (customEmailAttribute != null) {
                    RouteHeaderDTO routeHeaderVO = DTOConverter
                            .convertRouteHeader(document,
                                    actionItem.getPrincipalId());
                    ActionRequestValue actionRequest = KEWServiceLocator
                            .getActionRequestService().findByActionRequestId(
                                    actionItem.getActionRequestId());
                    ActionRequest actionRequestVO = ActionRequestValue.to(actionRequest);
                    customEmailAttribute.setRouteHeaderVO(routeHeaderVO);
                    customEmailAttribute.setActionRequestVO(actionRequestVO);
                    String customBody = customEmailAttribute
                            .getCustomEmailBody();
                    if (!org.apache.commons.lang.StringUtils.isEmpty(customBody)) {
                        emailBody.append(customBody);
                    }
                    String customEmailSubject = customEmailAttribute
                            .getCustomEmailSubject();
                    if (!org.apache.commons.lang.StringUtils.isEmpty(customEmailSubject)) {
                        emailSubject.append(customEmailSubject);
                    }
                }
            } catch (Exception e) {
                LOG
                        .error(
                                "Error when checking for custom email body and subject.",
                                e);
            }
            LOG.debug("Sending email to " + person);
            sendEmail(person, getEmailSubject(emailSubject.toString()),
                    new EmailBody(emailBody.toString()), document
                            .getDocumentType());
        }

    }

    protected boolean isProduction() {
        return ConfigContext.getCurrentContextConfig().getProperty(KEWConstants.PROD_DEPLOYMENT_CODE)
                .equalsIgnoreCase(getDeploymentEnvironment());
    }

    public void sendDailyReminder() {
        if (sendActionListEmailNotification()) {
            Collection<Person> users = getUsersWithEmailSetting(KEWConstants.EMAIL_RMNDR_DAY_VAL);
            for (Iterator<Person> userIter = users.iterator(); userIter.hasNext();) {
                Person user = userIter.next();
                try {
                    Collection actionItems = getActionListService().getActionList(user.getPrincipalId(), null);
                    if (actionItems != null && actionItems.size() > 0) {
                        sendPeriodicReminder(user, actionItems,
                                KEWConstants.EMAIL_RMNDR_DAY_VAL);
                    }
                } catch (Exception e) {
                    LOG.error(
                            "Error sending daily action list reminder to user: "
                                    + user.getEmailAddressUnmasked(), e);
                }
            }
        }
        LOG.debug("Daily action list emails sent successful");
    }

    public void sendWeeklyReminder() {
        if (sendActionListEmailNotification()) {
            List<Person> users = getUsersWithEmailSetting(KEWConstants.EMAIL_RMNDR_WEEK_VAL);
            for (Iterator<Person> userIter = users.iterator(); userIter.hasNext();) {
                Person user = userIter.next();
                try {
                    Collection actionItems = getActionListService()
                            .getActionList(user.getPrincipalId(), null);
                    if (actionItems != null && actionItems.size() > 0) {
                        sendPeriodicReminder(user, actionItems,
                                KEWConstants.EMAIL_RMNDR_WEEK_VAL);
                    }
                } catch (Exception e) {
                    LOG.error(
                            "Error sending weekly action list reminder to user: "
                                    + user.getEmailAddressUnmasked(), e);
                }
            }
        }
        LOG.debug("Weekly action list emails sent successful");
    }

    protected void sendPeriodicReminder(Person user, Collection<ActionItem> actionItems, String emailSetting) {
        String emailBody = null;
        actionItems = filterActionItemsToNotify(user.getPrincipalId(), actionItems);
        // if there are no action items after being filtered, there's no
        // reason to send the email
        if (actionItems.isEmpty()) {
            return;
        }
        if (KEWConstants.EMAIL_RMNDR_DAY_VAL.equals(emailSetting)) {
            emailBody = buildDailyReminderBody(user, actionItems);
        } else if (KEWConstants.EMAIL_RMNDR_WEEK_VAL.equals(emailSetting)) {
            emailBody = buildWeeklyReminderBody(user, actionItems);
        }
        sendEmail(user, getEmailSubject(), new EmailBody(emailBody));
    }

    /**
     * Returns a filtered Collection of {@link ActionItem}s which are filtered according to the
     * user's preferences. If they have opted not to recieve secondary or primary delegation emails
     * then they will not be included.
     */
    protected Collection filterActionItemsToNotify(String principalId, Collection actionItems) {
        List filteredItems = new ArrayList();
        Preferences preferences = KEWServiceLocator.getPreferencesService().getPreferences(principalId);
        for (Iterator iterator = actionItems.iterator(); iterator.hasNext();) {
            ActionItem actionItem = (ActionItem) iterator.next();
            if (!actionItem.getPrincipalId().equals(principalId)) {
                LOG.warn("Encountered an ActionItem with an incorrect workflow ID.  Was " + actionItem.getPrincipalId()
                        +
                        " but expected " + principalId);
                continue;
            }
            boolean includeItem = true;
            if (DelegationType.PRIMARY.getCode().equals(actionItem.getDelegationType())) {
                includeItem = KEWConstants.PREFERENCES_YES_VAL.equals(preferences.getNotifyPrimaryDelegation());
            } else if (DelegationType.SECONDARY.getCode().equals(actionItem.getDelegationType())) {
                includeItem = KEWConstants.PREFERENCES_YES_VAL.equals(preferences.getNotifySecondaryDelegation());
            }
            if (includeItem) {
                filteredItems.add(actionItem);
            }
        }
        return filteredItems;
    }

    protected List<Person> getUsersWithEmailSetting(String setting) {
        List users = new ArrayList();
        Collection userOptions = getUserOptionsService().findByOptionValue(
                KEWConstants.EMAIL_RMNDR_KEY, setting);
        for (Iterator iter = userOptions.iterator(); iter.hasNext();) {
            String workflowId = ((UserOptions) iter.next()).getWorkflowId();
            try {

                users.add(KimApiServiceLocator.getPersonService().getPerson(workflowId));
            } catch (Exception e) {
                LOG.error("error retrieving workflow user with ID: "
                        + workflowId);
            }
        }
        return users;
    }

    /**
     * 0 = actionItem.getDocumentId() 1 =
     * actionItem.getRouteHeader().getInitiatorUser().getDisplayName() 2 =
     * actionItem.getRouteHeader().getDocumentType().getName() 3 = actionItem.getDocTitle() 4 =
     * docHandlerUrl 5 = getActionListUrl() 6 = getPreferencesUrl() 7 = getHelpLink(documentType)
     */
    private static final MessageFormat DEFAULT_IMMEDIATE_REMINDER = new MessageFormat(
            "Your Action List has an eDoc(electronic document) that needs your attention: \n\n"
                    +
                    "Document ID:\t{0,number,#}\n"
                    +
                    "Initiator:\t\t{1}\n"
                    +
                    "Type:\t\tAdd/Modify {2}\n"
                    +
                    "Title:\t\t{3}\n"
                    +
                    "\n\n"
                    +
                    "To respond to this eDoc: \n"
                    +
                    "\tGo to {4}\n\n"
                    +
                    "\tOr you may access the eDoc from your Action List: \n"
                    +
                    "\tGo to {5}, and then click on the numeric Document ID: {0,number,#} in the first column of the List. \n"
                    +
                    "\n\n\n" +
                    "To change how these email notifications are sent(daily, weekly or none): \n" +
                    "\tGo to {6}\n" +
                    "\n\n\n" +
                    "{7}\n\n\n"
            );

    /**
     * 0 = actionItem.getDocumentId() 1 =
     * actionItem.getRouteHeader().getInitiatorUser().getDisplayName() 2 =
     * actionItem.getRouteHeader().getDocumentType().getName() 3 = actionItem.getDocTitle() 4 =
     * getActionListUrl() 5 = getPreferencesUrl() 6 = getHelpLink(documentType)
     */
    private static final MessageFormat DEFAULT_IMMEDIATE_REMINDER_NO_DOC_HANDLER = new MessageFormat(
            "Your Action List has an eDoc(electronic document) that needs your attention: \n\n" +
                    "Document ID:\t{0,number,#}\n" +
                    "Initiator:\t\t{1}\n" +
                    "Type:\t\tAdd/Modify {2}\n" +
                    "Title:\t\t{3}\n" +
                    "\n\n" +
                    "To respond to this eDoc you may use your Action List: \n" +
                    "\tGo to {4}, and then take actions related to Document ID: {0,number,#}. \n" +
                    "\n\n\n" +
                    "To change how these email notifications are sent(daily, weekly or none): \n" +
                    "\tGo to {5}\n" +
                    "\n\n\n" +
                    "{6}\n\n\n"
            );

    public String buildImmediateReminderBody(Person person,
            ActionItem actionItem, DocumentType documentType) {
        String docHandlerUrl = documentType.getDocHandlerUrl();
        if (StringUtils.isNotBlank(docHandlerUrl)) {
            if (!docHandlerUrl.contains("?")) {
                docHandlerUrl += "?";
            } else {
                docHandlerUrl += "&";
            }
            docHandlerUrl += KEWConstants.DOCUMENT_ID_PARAMETER + "="
                    + actionItem.getDocumentId();
            docHandlerUrl += "&" + KEWConstants.COMMAND_PARAMETER + "="
                    + KEWConstants.ACTIONLIST_COMMAND;
        }
        StringBuffer sf = new StringBuffer();

        /*sf
        		.append("Your Action List has an eDoc(electronic document) that needs your attention: \n\n");
        sf.append("Document ID:\t" + actionItem.getDocumentId() + "\n");
        sf.append("Initiator:\t\t");
        try {
        	sf.append(actionItem.getRouteHeader().getInitiatorUser()
        			.getDisplayName()
        			+ "\n");
        } catch (Exception e) {
        	LOG.error("Error retrieving initiator for action item "
        			+ actionItem.getDocumentId());
        	sf.append("\n");
        }
        sf.append("Type:\t\t" + "Add/Modify "
        		+ actionItem.getRouteHeader().getDocumentType().getName()
        		+ "\n");
        sf.append("Title:\t\t" + actionItem.getDocTitle() + "\n");
        sf.append("\n\n");
        sf.append("To respond to this eDoc: \n");
        sf.append("\tGo to " + docHandlerUrl + "\n\n");
        sf.append("\tOr you may access the eDoc from your Action List: \n");
        sf.append("\tGo to " + getActionListUrl()
        		+ ", and then click on the numeric Document ID: "
        		+ actionItem.getDocumentId()
        		+ " in the first column of the List. \n");
        sf.append("\n\n\n");
        sf
        		.append("To change how these email notifications are sent(daily, weekly or none): \n");
        sf.append("\tGo to " + getPreferencesUrl() + "\n");
        sf.append("\n\n\n");
        sf.append(getHelpLink(documentType) + "\n\n\n");*/

        MessageFormat messageFormat = null;
        String stringMessageFormat = ConfigContext.getCurrentContextConfig().getProperty(
                IMMEDIATE_REMINDER_EMAIL_MESSAGE_KEY);
        LOG.debug("Immediate reminder email message from configuration (" + IMMEDIATE_REMINDER_EMAIL_MESSAGE_KEY
                + "): " + stringMessageFormat);
        if (stringMessageFormat == null) {
            messageFormat = DEFAULT_IMMEDIATE_REMINDER;
        } else {
            messageFormat = new MessageFormat(stringMessageFormat);
        }
        String initiatorUser = (person == null ? "" : person.getName());

        if (StringUtils.isNotBlank(docHandlerUrl)) {
            Object[] args = {actionItem.getDocumentId(),
                    initiatorUser,
                    documentType.getName(),
                    actionItem.getDocTitle(),
                    docHandlerUrl,
                    getActionListUrl(),
                    getPreferencesUrl(),
                    getHelpLink(documentType)
            };

            messageFormat.format(args, sf, new FieldPosition(0));

            LOG.debug("default immediate reminder: " + DEFAULT_IMMEDIATE_REMINDER.format(args));
        } else {
            Object[] args = {actionItem.getDocumentId(),
                    initiatorUser,
                    documentType.getName(),
                    actionItem.getDocTitle(),
                    getActionListUrl(),
                    getPreferencesUrl(),
                    getHelpLink(documentType)
            };

            messageFormat.format(args, sf, new FieldPosition(0));

            LOG.debug("default immediate reminder: " + DEFAULT_IMMEDIATE_REMINDER_NO_DOC_HANDLER.format(args));
        }
        LOG.debug("immediate reminder: " + sf);

        // for debugging purposes on the immediate reminder only
        if (!isProduction()) {
            try {
                sf.append("Action Item sent to " + actionItem.getPrincipalId());
                if (actionItem.getDelegationType() != null) {
                    sf.append(" for delegation type "
                            + actionItem.getDelegationType());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return sf.toString();
    }

    public String buildDailyReminderBody(Person user,
            Collection actionItems) {
        StringBuffer sf = new StringBuffer();
        sf.append(getDailyWeeklyMessageBody(actionItems));
        sf
                .append("To change how these email notifications are sent (immediately, weekly or none): \n");
        sf.append("\tGo to " + getPreferencesUrl() + "\n");
        // sf.append("\tSend as soon as you get an eDoc\n\t" +
        // getPreferencesUrl() + "\n\n");
        // sf.append("\tSend weekly\n\t" + getPreferencesUrl() + "\n\n");
        // sf.append("\tDo not send\n\t" + getPreferencesUrl() + "\n");
        sf.append("\n\n\n");
        sf.append(getHelpLink() + "\n\n\n");
        return sf.toString();
    }

    public String buildWeeklyReminderBody(Person user,
            Collection actionItems) {
        StringBuffer sf = new StringBuffer();
        sf.append(getDailyWeeklyMessageBody(actionItems));
        sf
                .append("To change how these email notifications are sent (immediately, daily or none): \n");
        sf.append("\tGo to " + getPreferencesUrl() + "\n");
        // sf.append("\tSend as soon as you get an eDoc\n\t" +
        // getPreferencesUrl() + "\n\n");
        // sf.append("\tSend daily\n\t" + getPreferencesUrl() + "\n\n");
        // sf.append("\tDo not send\n\t" + getPreferencesUrl() + "\n");
        sf.append("\n\n\n");
        sf.append(getHelpLink() + "\n\n\n");
        return sf.toString();
    }

    String getDailyWeeklyMessageBody(Collection actionItems) {
        StringBuffer sf = new StringBuffer();
        HashMap docTypes = getActionListItemsStat(actionItems);

        sf
                .append("Your Action List has "
                        + actionItems.size()
                        + " eDocs(electronic documents) that need your attention: \n\n");
        Iterator iter = docTypes.keySet().iterator();
        while (iter.hasNext()) {
            String docTypeName = (String) iter.next();
            sf.append("\t" + ((Integer) docTypes.get(docTypeName)).toString()
                    + "\t" + docTypeName + "\n");
        }
        sf.append("\n\n");
        sf.append("To respond to each of these eDocs: \n");
        sf
                .append("\tGo to "
                        + getActionListUrl()
                        + ", and then click on its numeric Document ID in the first column of the List.\n");
        sf.append("\n\n\n");
        return sf.toString();
    }

    private HashMap<String, Integer> getActionListItemsStat(Collection<ActionItem> actionItems) {
        HashMap<String, Integer> docTypes = new LinkedHashMap<String, Integer>();
        Map<String, DocumentRouteHeaderValue> routeHeaders = KEWServiceLocator.getRouteHeaderService()
                .getRouteHeadersForActionItems(actionItems);
        Iterator<ActionItem> iter = actionItems.iterator();

        while (iter.hasNext()) {
            String docTypeName = routeHeaders.get(iter.next().getDocumentId()).getDocumentType().getName();
            if (docTypes.containsKey(docTypeName)) {
                docTypes.put(docTypeName, new Integer(docTypes.get(docTypeName).intValue() + 1));
            } else {
                docTypes.put(docTypeName, new Integer(1));
            }
        }
        return docTypes;
    }

    protected boolean sendActionListEmailNotification() {
        if (LOG.isDebugEnabled())
            LOG.debug("actionlistsendconstant: "
                    + CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                            KEWConstants.KEW_NAMESPACE, KRADConstants.DetailTypes.ACTION_LIST_DETAIL_TYPE,
                            KEWConstants.ACTION_LIST_SEND_EMAIL_NOTIFICATION_IND));

        return KEWConstants.ACTION_LIST_SEND_EMAIL_NOTIFICATION_VALUE
                .equals(CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                        KEWConstants.KEW_NAMESPACE, KRADConstants.DetailTypes.ACTION_LIST_DETAIL_TYPE,
                        KEWConstants.ACTION_LIST_SEND_EMAIL_NOTIFICATION_IND));
    }

    public void scheduleBatchEmailReminders() throws Exception {
        String emailBatchGroup = "Email Batch";
        String dailyCron = ConfigContext.getCurrentContextConfig()
                .getProperty(KEWConstants.DAILY_EMAIL_CRON_EXPRESSION);
        if (!StringUtils.isBlank(dailyCron)) {
            LOG.info("Scheduling Daily Email batch with cron expression: " + dailyCron);
            CronTrigger dailyTrigger = new CronTrigger(DAILY_TRIGGER_NAME, emailBatchGroup, dailyCron);
            JobDetail dailyJobDetail = new JobDetail(DAILY_JOB_NAME, emailBatchGroup, DailyEmailJob.class);
            dailyTrigger.setJobName(dailyJobDetail.getName());
            dailyTrigger.setJobGroup(dailyJobDetail.getGroup());
            addJobToScheduler(dailyJobDetail);
            addTriggerToScheduler(dailyTrigger);
        } else {
            LOG.warn("No " + KEWConstants.DAILY_EMAIL_CRON_EXPRESSION
                    + " parameter was configured.  Daily Email batch was not scheduled!");
        }

        String weeklyCron = ConfigContext.getCurrentContextConfig().getProperty(
                KEWConstants.WEEKLY_EMAIL_CRON_EXPRESSION);
        if (!StringUtils.isBlank(weeklyCron)) {
            LOG.info("Scheduling Weekly Email batch with cron expression: " + weeklyCron);
            CronTrigger weeklyTrigger = new CronTrigger(WEEKLY_TRIGGER_NAME, emailBatchGroup, weeklyCron);
            JobDetail weeklyJobDetail = new JobDetail(WEEKLY_JOB_NAME, emailBatchGroup, WeeklyEmailJob.class);
            weeklyTrigger.setJobName(weeklyJobDetail.getName());
            weeklyTrigger.setJobGroup(weeklyJobDetail.getGroup());
            addJobToScheduler(weeklyJobDetail);
            addTriggerToScheduler(weeklyTrigger);
        } else {
            LOG.warn("No " + KEWConstants.WEEKLY_EMAIL_CRON_EXPRESSION
                    + " parameter was configured.  Weekly Email batch was not scheduled!");
        }
    }

    private void addJobToScheduler(JobDetail jobDetail) throws SchedulerException {
        getScheduler().addJob(jobDetail, true);
    }

    private void addTriggerToScheduler(Trigger trigger) throws SchedulerException {
        boolean triggerExists = (getScheduler().getTrigger(trigger.getName(), trigger.getGroup()) != null);
        if (!triggerExists) {
            try {
                getScheduler().scheduleJob(trigger);
            } catch (ObjectAlreadyExistsException ex) {
                getScheduler().rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
            }
        } else {
            getScheduler().rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
        }
    }

    private Scheduler getScheduler() {
        return KSBServiceLocator.getScheduler();
    }

    private UserOptionsService getUserOptionsService() {
        return (UserOptionsService) KEWServiceLocator
                .getUserOptionsService();
    }

    protected ActionListService getActionListService() {
        return (ActionListService) KEWServiceLocator.getActionListService();
    }

    public String getDeploymentEnvironment() {
        return deploymentEnvironment;
    }

    public void setDeploymentEnvironment(String deploymentEnvironment) {
        this.deploymentEnvironment = deploymentEnvironment;
    }

    protected String getActionListUrl() {
        return ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.WORKFLOW_URL_KEY)
                + "/" + "ActionList.do";
    }

    protected String getPreferencesUrl() {
        return ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.WORKFLOW_URL_KEY)
                + "/" + "Preferences.do";
    }
}
