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
package org.kuali.rice.krad.uif.view;

/**
 * Provides configuration for <code>View</code> instances that render an HTML
 * form
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class FormView extends View {
	private static final long serialVersionUID = -3291164284675273147L;

	private boolean renderForm;
	private boolean validateServerSide;
	private boolean validateClientSide;

	private String formPostUrl;

	public FormView() {
		renderForm = true;
		validateServerSide = true;
		validateClientSide = true;
		setValidateDirty(true);
	}

	/**
	 * Indicates whether a Form element should be rendered for the View. This is
	 * necessary for pages that need to submit data back to the server. Note
	 * that even if a page is read-only, a form element is generally needed for
	 * the navigation. Defaults to true
	 * 
	 * @return true if the form element should be rendered, false if it should
	 *         not be
	 */
	public boolean isRenderForm() {
		return this.renderForm;
	}

	/**
	 * Setter for the render form indicator
	 * 
	 * @param renderForm
	 */
	public void setRenderForm(boolean renderForm) {
		this.renderForm = renderForm;
	}

	/**
	 * Indicates whether to perform the validate model phase of the view
	 * lifecycle. This phase will validate the model against configured
	 * dictionary validations and report errors. Defaults to true
	 * 
	 * @return boolean true if model data should be validated, false if it
	 *         should not be
	 * @see
	 */
	public boolean isValidateServerSide() {
		return this.validateServerSide;
	}

	/**
	 * Setter for the validate server side indicator
	 * 
	 * @param validateServerSide
	 */
	public void setValidateServerSide(boolean validateServerSide) {
		this.validateServerSide = validateServerSide;
	}

	/**
	 * Indicates whether to perform on-the-fly validation on the client using js
	 * during user data entry. Defaults to true
	 * 
	 * @return the validateClientSide
	 */
	public boolean isValidateClientSide() {
		return validateClientSide;
	}

	/**
	 * Setter for the validate client side indicator
	 * 
	 * @param validateClientSide
	 */
	public void setValidateClientSide(boolean validateClientSide) {
		this.validateClientSide = validateClientSide;
	}

	/**
	 * Specifies the URL the view's form should post to
	 * 
	 * <p>
	 * Any valid form post URL (full or relative) can be specified. If left
	 * empty, the form will be posted to the same URL of the preceding request
	 * URL.
	 * </p>
	 * 
	 * @return String post URL
	 */
	public String getFormPostUrl() {
		return this.formPostUrl;
	}

	/**
	 * Setter for the form post URL
	 * 
	 * @param formPostUrl
	 */
	public void setFormPostUrl(String formPostUrl) {
		this.formPostUrl = formPostUrl;
	}

}
