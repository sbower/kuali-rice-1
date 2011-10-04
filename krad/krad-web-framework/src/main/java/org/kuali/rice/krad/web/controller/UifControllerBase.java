/*
 * Copyright 2007 The Kuali Foundation Licensed under the Educational Community
 * License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/ecl1.php Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.kuali.rice.krad.web.controller;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.exception.RiceRuntimeException;
import org.kuali.rice.core.web.format.BooleanFormatter;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.bo.ExternalizableBusinessObject;
import org.kuali.rice.krad.exception.AuthorizationException;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.service.ModuleService;
import org.kuali.rice.krad.service.SessionDocumentService;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.UifPropertyPaths;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.field.AttributeQueryResult;
import org.kuali.rice.krad.uif.service.ViewService;
import org.kuali.rice.krad.uif.util.ComponentFactory;
import org.kuali.rice.krad.uif.util.LookupInquiryUtils;
import org.kuali.rice.krad.uif.util.UifWebUtils;
import org.kuali.rice.krad.uif.view.History;
import org.kuali.rice.krad.uif.view.HistoryEntry;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.util.UrlFactory;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Base controller class for views within the KRAD User Interface Framework
 *
 * Provides common methods such as:
 *
 * <ul>
 * <li>Authorization methods such as method to call check</li>
 * <li>Preparing the View instance and setup in the returned
 * <code>ModelAndView</code></li>
 * </ul>
 *
 * All subclass controller methods after processing should call one of the
 * #getUIFModelAndView methods to setup the <code>View</code> and return the
 * <code>ModelAndView</code> instance.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class UifControllerBase {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(UifControllerBase.class);

    protected static final String REDIRECT_PREFIX = "redirect:";

    /**
     * Create/obtain the model(form) object before it is passed
     * to the Binder/BeanWrapper. This method is not intended to be overridden
     * by client applications as it handles framework setup and session
     * maintenance. Clients should override createIntialForm() instead when they
     * need custom form initialization.
     */
    @ModelAttribute(value = "KualiForm")
    public UifFormBase initForm(HttpServletRequest request) {
        UifFormBase form = null;
        String formKeyParam = request.getParameter(UifParameters.FORM_KEY);
        String documentNumber = request.getParameter(KRADConstants.DOCUMENT_DOCUMENT_NUMBER);

        if (StringUtils.isNotBlank(formKeyParam)) {
            form = (UifFormBase) request.getSession().getAttribute(formKeyParam);

            // retrieve from db if form not in session
            if (form == null) {
                UserSession userSession = (UserSession) request.getSession().getAttribute(
                        KRADConstants.USER_SESSION_KEY);
                form = getSessionDocumentService().getDocumentForm(documentNumber, formKeyParam, userSession,
                        request.getRemoteAddr());
            }
        } else {
            form = createInitialForm();
        }

        return form;
    }

    /**
     * Called to create a new model(form) object when
     * necessary. This usually occurs on the initial request in a conversation
     * (when the model is not present in the session). This method must be
     * overridden when extending a controller and using a different form type
     * than the superclass.
     */
    protected abstract Class<? extends UifFormBase> formType();

    /**
     * Called to create a new model(form) object when
     * necessary. This usually occurs on the initial request in a conversation
     * (when the model is not present in the session).
     */
    protected UifFormBase createInitialForm() {
        try {
            return formType().newInstance();
        } catch (InstantiationException e) {
            throw new RiceRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RiceRuntimeException(e);
        }
    }

    private Set<String> methodToCallsToNotCheckAuthorization = new HashSet<String>();

    {
        methodToCallsToNotCheckAuthorization.add("performLookup");
        methodToCallsToNotCheckAuthorization.add("performQuestion");
        methodToCallsToNotCheckAuthorization.add("performQuestionWithInput");
        methodToCallsToNotCheckAuthorization.add("performQuestionWithInputAgainBecauseOfErrors");
        methodToCallsToNotCheckAuthorization.add("performQuestionWithoutInput");
        methodToCallsToNotCheckAuthorization.add("performWorkgroupLookup");
    }

    /**
     * Use to add a methodToCall to the a list which will not have authorization
     * checks. This assumes that the call will be redirected (as in the case of
     * a lookup) that will perform the authorization.
     */
    protected final void addMethodToCallToUncheckedList(String methodToCall) {
        methodToCallsToNotCheckAuthorization.add(methodToCall);
    }

    /**
     * Returns an immutable Set of methodToCall parameters that should not be
     * checked for authorization.
     */
    public Set<String> getMethodToCallsToNotCheckAuthorization() {
        return Collections.unmodifiableSet(methodToCallsToNotCheckAuthorization);
    }

    /**
     * Override this method to provide controller class-level access controls to
     * the application.
     */
    public void checkAuthorization(UifFormBase form, String methodToCall) throws AuthorizationException {
        String principalId = GlobalVariables.getUserSession().getPrincipalId();
        Map<String, String> roleQualifier = new HashMap<String, String>(getRoleQualification(form, methodToCall));
        Map<String, String> permissionDetails = KRADUtils.getNamespaceAndActionClass(this.getClass());

        if (!KimApiServiceLocator.getPermissionService().isAuthorizedByTemplateName(principalId,
                KRADConstants.KRAD_NAMESPACE, KimConstants.PermissionTemplateNames.USE_SCREEN, permissionDetails,
                roleQualifier)) {
            throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(),
                    methodToCall, this.getClass().getSimpleName());
        }
    }

    /**
     * Override this method to add data from the form for role qualification in
     * the authorization check
     */
    protected Map<String, String> getRoleQualification(UifFormBase form, String methodToCall) {
        return new HashMap<String, String>();
    }

    /**
     * Initial method called when requesting a new view instance which forwards
     * the view for rendering
     */
    @RequestMapping(method = RequestMethod.GET, params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        return getUIFModelAndView(form);
    }

    /**
     * Called by the add line action for a new collection line. Method
     * determines which collection the add action was selected for and invokes
     * the view helper service to add the line
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addLine")
    public ModelAndView addLine(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        String selectedCollectionPath = uifForm.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        if (StringUtils.isBlank(selectedCollectionPath)) {
            throw new RuntimeException("Selected collection was not set for add line action, cannot add new line");
        }

        View view = uifForm.getPreviousView();
        view.getViewHelperService().processCollectionAddLine(view, uifForm, selectedCollectionPath);

        return updateComponent(uifForm, result, request, response);
    }

    /**
     * Called by the delete line action for a model collection. Method
     * determines which collection the action was selected for and the line
     * index that should be removed, then invokes the view helper service to
     * process the action
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteLine")
    public ModelAndView deleteLine(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        String selectedCollectionPath = uifForm.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        if (StringUtils.isBlank(selectedCollectionPath)) {
            throw new RuntimeException("Selected collection was not set for delete line action, cannot delete line");
        }

        int selectedLineIndex = -1;
        String selectedLine = uifForm.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        if (StringUtils.isNotBlank(selectedLine)) {
            selectedLineIndex = Integer.parseInt(selectedLine);
        }

        if (selectedLineIndex == -1) {
            throw new RuntimeException("Selected line index was not set for delete line action, cannot delete line");
        }

        View view = uifForm.getPreviousView();
        view.getViewHelperService().processCollectionDeleteLine(view, uifForm, selectedCollectionPath,
                selectedLineIndex);

        return updateComponent(uifForm, result, request, response);
    }

    /**
     * Invoked to toggle the show inactive indicator on the selected collection group and then
     * rerun the component lifecycle and rendering based on the updated indicator and form data
     *
     * @param request - request object that should contain the request component id (for the collection group)
     * and the show inactive indicator value
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=toggleInactiveRecordDisplay")
    public ModelAndView toggleInactiveRecordDisplay(@ModelAttribute("KualiForm") UifFormBase uifForm,
            BindingResult result, HttpServletRequest request, HttpServletResponse response) {
        String collectionGroupId = request.getParameter(UifParameters.REQUESTED_COMPONENT_ID);
        if (StringUtils.isBlank(collectionGroupId)) {
            throw new RuntimeException(
                    "Collection group id to update for inactive record display not found in request");
        }

        String showInactiveStr = request.getParameter(UifParameters.SHOW_INACTIVE_RECORDS);
        Boolean showInactive = false;
        if (StringUtils.isNotBlank(showInactiveStr)) {
            // TODO: should use property editors once we have util class
            showInactive = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(showInactiveStr);
        } else {
            throw new RuntimeException("Show inactive records flag not found in request");
        }

        CollectionGroup collectionGroup = (CollectionGroup) ComponentFactory.getComponentById(uifForm,
                collectionGroupId);

        // update inactive flag on group
        collectionGroup.setShowInactive(showInactive);

        // run lifecycle and update in view
        uifForm.getView().getViewHelperService().performComponentLifecycle(uifForm, collectionGroup, collectionGroupId);
        uifForm.getView().getViewIndex().indexComponent(collectionGroup);

        return UifWebUtils.getComponentModelAndView(collectionGroup, uifForm);
    }

    /**
     * Just returns as if return with no value was selected.
     */
    @RequestMapping(params = "methodToCall=cancel")
    public ModelAndView cancel(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        return close(form, result, request, response);
    }

    /**
     * Just returns as if return with no value was selected.
     */
    @RequestMapping(params = "methodToCall=close")
    public ModelAndView close(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        Properties props = new Properties();
        props.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.REFRESH);
        if (StringUtils.isNotBlank(form.getReturnFormKey())) {
            props.put(UifParameters.FORM_KEY, form.getReturnFormKey());
        }

        // TODO this needs setup for lightbox and possible home location
        // property
        String returnUrl = form.getReturnLocation();
        if (StringUtils.isBlank(returnUrl)) {
            returnUrl = ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY);
        }

        return performRedirect(form, returnUrl, props);
    }

    /**
     * Invoked to navigate back one page in history..
     *
     * @param form - form object that should contain the history object
     */
    @RequestMapping(params = "methodToCall=returnToPrevious")
    public ModelAndView returnToPrevious(@ModelAttribute("KualiForm") UifFormBase form) {

        return returnToHistory(form, false);
    }

    /**
     * Invoked to navigate back to the first page in history.
     *
     * @param form - form object that should contain the history object
     */
    @RequestMapping(params = "methodToCall=returnToHub")
    public ModelAndView returnToHub(@ModelAttribute("KualiForm") UifFormBase form) {

        return returnToHistory(form, true);
    }

    /**
     * Invoked to navigate back to a history entry. The homeFlag will determine whether navigation
     * will be back to the first or last history entry.
     *
     * @param form - form object that should contain the history object
     * @param homeFlag - if true will navigate back to first entry else will navigate to last entry
     * in the history
     */
    public ModelAndView returnToHistory(UifFormBase form, boolean homeFlag) {
        // Get the history from the form
        History hist = form.getFormHistory();
        List<HistoryEntry> histEntries = hist.getHistoryEntries();

        // Get the history page url. Default to the application url if there is no history.
        String histUrl = null;
        if (histEntries.isEmpty()) {
            histUrl = ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY);
        } else {
            // For home get the first entry, for previous get the last entry.
            // Remove history up to where page is opened
            if (homeFlag) {
                histUrl = histEntries.get(0).getUrl();
                histEntries.clear();
            } else {
                histUrl = histEntries.get(histEntries.size() - 1).getUrl();
                histEntries.remove(histEntries.size() - 1);
                hist.setCurrent(null);
            }
        }

        // Add the refresh call
        Properties props = new Properties();
        props.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.REFRESH);

        return performRedirect(form, histUrl, props);
    }

    /**
     * Handles menu navigation between view pages
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=navigate")
    public ModelAndView navigate(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        String pageId = form.getActionParamaterValue(UifParameters.NAVIGATE_TO_PAGE_ID);

        // only refreshing page
        form.setRenderFullView(false);

        return getUIFModelAndView(form, pageId);
    }

    @RequestMapping(params = "methodToCall=refresh")
    public ModelAndView refresh(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // TODO: this code still needs to handle reference refreshes
        String refreshCallerType = "";
        if (request.getParameterMap().containsKey(KRADConstants.REFRESH_CALLER_TYPE)) {
            refreshCallerType = request.getParameter(KRADConstants.REFRESH_CALLER_TYPE);
        }

        // process multi-value lookup returns
        if (StringUtils.equals(refreshCallerType, UifConstants.RefreshCallerTypes.MULTI_VALUE_LOOKUP)) {
            String lookupCollectionName = "";
            if (request.getParameterMap().containsKey(UifParameters.LOOKUP_COLLECTION_NAME)) {
                lookupCollectionName = request.getParameter(UifParameters.LOOKUP_COLLECTION_NAME);
            }

            if (StringUtils.isBlank(lookupCollectionName)) {
                throw new RuntimeException(
                        "Lookup collection name is required for processing multi-value lookup results");
            }

            String selectedLineValues = "";
            if (request.getParameterMap().containsKey(UifParameters.SELECTED_LINE_VALUES)) {
                selectedLineValues = request.getParameter(UifParameters.SELECTED_LINE_VALUES);
            }

            // invoked view helper to populate the collection from lookup results
            form.getPreviousView().getViewHelperService().processMultipleValueLookupResults(form.getPreviousView(), form,
                    lookupCollectionName, selectedLineValues);
        }

        form.setRenderFullView(true);

        return getUIFModelAndView(form);
    }

    /**
     * Updates the current component by retrieving a fresh copy from the dictionary,
     * running its component lifecycle, and returning it
     *
     * @param request - the request must contain reqComponentId that specifies the component to retrieve
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=updateComponent")
    public ModelAndView updateComponent(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        String requestedComponentId = request.getParameter(UifParameters.REQUESTED_COMPONENT_ID);
        if (StringUtils.isBlank(requestedComponentId)) {
            throw new RuntimeException("Requested component id for update not found in request");
        }

        Component comp = ComponentFactory.getComponentByIdWithLifecycle(form, requestedComponentId);

        return UifWebUtils.getComponentModelAndView(comp, form);
    }

    /**
     * Builds up a URL to the lookup view based on the given post action
     * parameters and redirects
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=performLookup")
    public ModelAndView performLookup(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        Properties lookupParameters = form.getActionParametersAsProperties();

        String lookupObjectClassName = (String) lookupParameters.get(UifParameters.DATA_OBJECT_CLASS_NAME);
        Class<?> lookupObjectClass = null;
        try {
            lookupObjectClass = Class.forName(lookupObjectClassName);
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to get class for name: " + lookupObjectClassName);
            throw new RuntimeException("Unable to get class for name: " + lookupObjectClassName, e);
        }

        // get form values for the lookup parameter fields
        String lookupParameterString = (String) lookupParameters.get(UifParameters.LOOKUP_PARAMETERS);
        if (lookupParameterString != null) {
            Map<String, String> lookupParameterFields = KRADUtils.getMapFromParameterString(lookupParameterString);
            for (Entry<String, String> lookupParameter : lookupParameterFields.entrySet()) {
                String lookupParameterValue = LookupInquiryUtils.retrieveLookupParameterValue(form, request,
                        lookupObjectClass, lookupParameter.getValue(), lookupParameter.getKey());

                if (StringUtils.isNotBlank(lookupParameterValue)) {
                    lookupParameters.put(UifPropertyPaths.CRITERIA_FIELDS + "['" + lookupParameter.getValue() + "']",
                            lookupParameterValue);
                }
            }
        }

        // TODO: lookup anchors and doc number?

        String baseLookupUrl = (String) lookupParameters.get(UifParameters.BASE_LOOKUP_URL);
        lookupParameters.remove(UifParameters.BASE_LOOKUP_URL);

        // set lookup method to call
        lookupParameters.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.START);
        String autoSearchString = (String) lookupParameters.get(UifParameters.AUTO_SEARCH);
        if (Boolean.parseBoolean(autoSearchString)) {
            lookupParameters.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.SEARCH);
        }

        lookupParameters.put(UifParameters.RETURN_LOCATION, form.getFormPostUrl());
        lookupParameters.put(UifParameters.RETURN_FORM_KEY, form.getFormKey());

        // special check for external object classes
        if (lookupObjectClass != null) {
            ModuleService responsibleModuleService =
                    KRADServiceLocatorWeb.getKualiModuleService().getResponsibleModuleService(lookupObjectClass);
            if (responsibleModuleService != null && responsibleModuleService.isExternalizable(lookupObjectClass)) {
                Class<? extends ExternalizableBusinessObject> implLookupObjectClass =
                        responsibleModuleService.getExternalizableBusinessObjectImplementation(
                                lookupObjectClass.asSubclass(ExternalizableBusinessObject.class));

                if (implLookupObjectClass != null) {
                    lookupParameters.put(UifParameters.DATA_OBJECT_CLASS_NAME, implLookupObjectClass.getName());
                } else {
                    throw new RuntimeException(
                            "Unable to find implementation class for EBO: " + lookupObjectClass.getName());
                }

                // TODO: should call module service to get full URL, but right now it is coded to direct to the KNS lookups
                //                Map<String, String> parameterMap = new HashMap<String, String>();
                //                Enumeration<Object> e = lookupParameters.keys();
                //                while (e.hasMoreElements()) {
                //                    String paramName = (String) e.nextElement();
                //                    parameterMap.put(paramName, lookupParameters.getProperty(paramName));
                //                }
                //
                //                String lookupUrl = responsibleModuleService.getExternalizableBusinessObjectLookupUrl(lookupObjectClass,
                //                        parameterMap);
                //                return performRedirect(form, lookupUrl, new Properties());
            }
        }

        return performRedirect(form, baseLookupUrl, lookupParameters);
    }

    /**
     * Invoked to provide the options for a suggest widget. The valid options are retrieved by the associated
     * <code>AttributeQuery</code> for the field containing the suggest widget. The controller method picks
     * out the query parameters from the request and calls <code>AttributeQueryService</code> to perform the
     * suggest query and prepare the result object that will be exposed with JSON
     */
    @RequestMapping(method = RequestMethod.GET, params = "methodToCall=performFieldSuggest")
    public
    @ResponseBody
    AttributeQueryResult performFieldSuggest(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        // retrieve query fields from request
        Map<String, String> queryParameters = new HashMap<String, String>();
        for (Object parameterName : request.getParameterMap().keySet()) {
            if (parameterName.toString().startsWith(UifParameters.QUERY_PARAMETER + ".")) {
                String fieldName = StringUtils.substringAfter(parameterName.toString(),
                        UifParameters.QUERY_PARAMETER + ".");
                String fieldValue = request.getParameter(parameterName.toString());
                queryParameters.put(fieldName, fieldValue);
            }
        }

        // retrieve id for field to perform query for
        String queryFieldId = request.getParameter(UifParameters.QUERY_FIELD_ID);
        if (StringUtils.isBlank(queryFieldId)) {
            throw new RuntimeException("Unable to find id for field to perform query on under request parameter name: "
                    + UifParameters.QUERY_FIELD_ID);
        }

        // get the field term to match
        String queryTerm = request.getParameter(UifParameters.QUERY_TERM);
        if (StringUtils.isBlank(queryTerm)) {
            throw new RuntimeException(
                    "Unable to find id for query term value for attribute query on under request parameter name: "
                            + UifParameters.QUERY_TERM);
        }

        // invoke attribute query service to perform the query
        AttributeQueryResult queryResult = KRADServiceLocatorWeb.getAttributeQueryService().performFieldSuggestQuery(
                form.getView(), queryFieldId, queryTerm, queryParameters);

        return queryResult;
    }

    /**
     * Invoked to execute the <code>AttributeQuery</code> associated with a field given the query parameters
     * found in the request. This controller method picks out the query parameters from the request and calls
     * <code>AttributeQueryService</code> to perform the field query and prepare the result object
     * that will be exposed with JSON. The result is then used to update field values in the UI with client
     * script.
     */
    @RequestMapping(method = RequestMethod.GET, params = "methodToCall=performFieldQuery")
    public
    @ResponseBody
    AttributeQueryResult performFieldQuery(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        // retrieve query fields from request
        Map<String, String> queryParameters = new HashMap<String, String>();
        for (Object parameterName : request.getParameterMap().keySet()) {
            if (parameterName.toString().startsWith(UifParameters.QUERY_PARAMETER + ".")) {
                String fieldName = StringUtils.substringAfter(parameterName.toString(),
                        UifParameters.QUERY_PARAMETER + ".");
                String fieldValue = request.getParameter(parameterName.toString());
                queryParameters.put(fieldName, fieldValue);
            }
        }

        // retrieve id for field to perform query for
        String queryFieldId = request.getParameter(UifParameters.QUERY_FIELD_ID);
        if (StringUtils.isBlank(queryFieldId)) {
            throw new RuntimeException("Unable to find id for field to perform query on under request parameter name: "
                    + UifParameters.QUERY_FIELD_ID);
        }

        // invoke attribute query service to perform the query
        AttributeQueryResult queryResult = KRADServiceLocatorWeb.getAttributeQueryService().performFieldQuery(
                form.getView(), queryFieldId, queryParameters);

        return queryResult;
    }

    /**
     * Builds a <code>ModelAndView</code> instance configured to redirect to the
     * URL formed by joining the base URL with the given URL parameters
     *
     * @param form - current form instance
     * @param baseUrl - base url to redirect to
     * @param urlParameters - properties containing key/value pairs for the url parameters
     * @return ModelAndView configured to redirect to the given URL
     */
    protected ModelAndView performRedirect(UifFormBase form, String baseUrl, Properties urlParameters) {
        // On post redirects we need to make sure we are sending the history forward:
        urlParameters.setProperty(UifConstants.UrlParams.HISTORY, form.getFormHistory().getHistoryParameterString());

        // If this is an Light Box call only return the redirectURL view with the URL
        // set this is to avoid automatic redirect when using light boxes
        // TODO: add constant to UifParameters
        if (urlParameters.get("lightBoxCall") != null && urlParameters.get("lightBoxCall").equals("true")) {
            urlParameters.remove("lightBoxCall");
            String redirectUrl = UrlFactory.parameterizeUrl(baseUrl, urlParameters);

            ModelAndView modelAndView = new ModelAndView("redirectURL");
            modelAndView.addObject("redirectUrl", redirectUrl);
            return modelAndView;
        }

        String redirectUrl = UrlFactory.parameterizeUrl(baseUrl, urlParameters);
        ModelAndView modelAndView = new ModelAndView(REDIRECT_PREFIX + redirectUrl);

        return modelAndView;
    }

    protected ModelAndView getUIFModelAndView(UifFormBase form) {
        return getUIFModelAndView(form, form.getPageId());
    }

    /**
     * Configures the <code>ModelAndView</code> instance containing the form
     * data and pointing to the UIF generic spring view
     *
     * @param form - Form instance containing the model data
     * @param pageId - Id of the page within the view that should be rendered, can
     * be left blank in which the current or default page is rendered
     * @return ModelAndView object with the contained form
     */
    protected ModelAndView getUIFModelAndView(UifFormBase form, String pageId) {
        return UifWebUtils.getUIFModelAndView(form, pageId);
    }

    protected ViewService getViewService() {
        return KRADServiceLocatorWeb.getViewService();
    }

    public SessionDocumentService getSessionDocumentService() {
        return KRADServiceLocatorWeb.getSessionDocumentService();
    }

}
