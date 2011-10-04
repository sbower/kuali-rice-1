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
package org.kuali.rice.krad.uif.layout;

import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.component.Configurable;
import org.kuali.rice.krad.uif.component.PropertyReplacer;
import org.kuali.rice.krad.uif.service.ViewHelperService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the rendering of <code>Component</code> instances within a
 * <code>Container</code>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface LayoutManager extends Configurable, Serializable {

	/**
	 * The unique id (within a given tree) for the layout manager instance
	 * 
	 * <p>
	 * The id is used to identify a <code>LayoutManager</code> instance within
	 * the tree and can be used by renderers
	 * </p>
	 * 
	 * @return String id
	 */
	public String getId();

	/**
	 * Sets the unique id (within a given tree) for the layout manager
	 * 
	 * @param id
	 *            - string to set as the layout manager id
	 */
	public void setId(String id);

	/**
	 * The path to the JSP file that should be called to invoke the layout
	 * manager
	 * 
	 * <p>
	 * The path should be relative to the web root. All layout manager templates
	 * receive the list of items of be placed, the configured layout manager,
	 * and the container to which the layout manager applies
	 * </p>
	 * 
	 * <p>
	 * e.g. '/krad/WEB-INF/jsp/tiles/boxLayout.jsp'
	 * </p>
	 * 
	 * @return String representing the template path
	 */
	public String getTemplate();

	/**
	 * Setter for the layout managers template
	 * 
	 * @param template
	 */
	public void setTemplate(String template);

	/**
	 * Should be called to initialize the layout manager
     *
	 * <p>
	 * This is where layout managers can set defaults and setup other necessary
	 * state. The initialize method should only be called once per layout
	 * manager lifecycle and is invoked within the initialize phase of the view
	 * lifecylce.
	 * </p>
	 * 
	 * @param view
	 *            - View instance the layout manager is a part of
     * @param model - the object instance containing the view data
	 * @param container
	 *            - Container the layout manager applies to
	 * @see ViewHelperService#performInitialization
	 */
	public void performInitialization(View view, Object model, Container container);

	/**
	 * Called after the initialize phase to perform conditional logic based on
	 * the model data
	 * 
	 * @param view
	 *            - view instance to which the layout manager belongs
	 * @param model
	 *            - Top level object containing the data (could be the form or a
	 *            top level business object, dto)
	 * @param container
	 *            - Container the layout manager applies to
	 */
	public void performApplyModel(View view, Object model, Container container);

	/**
	 * The last phase before the view is rendered. Here final preparations can
	 * be made based on the updated view state
	 * 
	 * 
	 * @param view
	 *            - view instance that should be finalized for rendering
	 * @param model
	 *            - top level object containing the data
	 * @param container
	 *            - Container the layout manager applies to
	 */
	public void performFinalize(View view, Object model, Container container);

	/**
	 * Determines what <code>Container</code> classes are supported by the
	 * <code>LayoutManager</code>
	 * 
	 * @return Class<? extends Container> container class supported
	 */
	public Class<? extends Container> getSupportedContainer();

	/**
	 * List of components that are contained within the layout manager
	 * <p>
	 * Used by <code>ViewHelperService</code> for the various lifecycle
	 * callbacks
	 * 
	 * @return List<Component> child components
	 */
	public List<Component> getNestedComponents();

	/**
	 * Used by the copy process to determine for which properties only the value
	 * reference should be copied (not a new copy instance). Subclasses can
	 * define the properties for which only the reference should be copied
	 * 
	 * @return Set<String> property names for which only the value reference
	 *         should be copied
	 * @see org.kuali.rice.krad.uif.util.ComponentUtils.copy(T)
	 */
	public Set<String> getPropertiesForReferenceCopy();

	/**
	 * CSS style string to be applied to the area (div) the layout manager
	 * generates for the items
	 * 
	 * <p>
	 * Note the styleClass/style configured on the <code>Container</code>
	 * applies to all the container content (header, body, footer), while the
	 * styleClass/style configured on the <code>LayoutManager</code> only
	 * applies to the div surrounding the items placed by the manager (the
	 * container's body)
	 * </p>
	 * 
	 * <p>
	 * Any style override or additions can be specified with this attribute.
	 * This is used by the renderer to set the style attribute on the
	 * corresponding element.
	 * </p>
	 * 
	 * <p>
	 * e.g. 'color: #000000;text-decoration: underline;'
	 * </p>
	 * 
	 * @return String css style string
	 */
	public String getStyle();

	/**
	 * Setter for the layout manager div style
	 * 
	 * @param style
	 */
	public void setStyle(String style);

	/**
	 * CSS style class(s) to be applied to the area (div) the layout manager
	 * generates for the items
	 * 
	 * <p>
	 * Note the styleClass/style configured on the <code>Container</code>
	 * applies to all the container content (header, body, footer), while the
	 * styleClass/style configured on the <code>LayoutManager</code> only
	 * applies to the div surrounding the items placed by the manager (the
	 * container's body)
	 * </p>
	 * 
	 * <p>
	 * Declares additional style classes for the div. Multiple classes are
	 * specified with a space delimiter. This is used by the renderer to set the
	 * class attribute on the corresponding element. The class(s) declared must
	 * be available in the common style sheets or the style sheets specified for
	 * the view
	 * </p>
	 * 
	 * <p>
	 * e.g. 'header left'
	 * </p>
	 * 
	 * @return List<String> css style classes to apply
	 */
	public List<String> getStyleClasses();

	/**
	 * Setter for the layout manager div style class
	 * 
	 * @param styleClass
	 */
	public void setStyleClasses(List<String> styleClasses);

	/**
	 * This method adds a single style class to the list of css style classes on this component
	 * 
	 * @param style
	 */
	public void addStyleClass(String styleClass);

	/**
	 * Context map for the layout manager
	 * 
	 * @return Map<String, Object> context
	 * @see org.kuali.rice.krad.uif.Component.getContext()
	 */
	public Map<String, Object> getContext();

	/**
	 * Setter for the context Map
	 * 
	 * @param context
	 * @see org.kuali.rice.krad.uif.Component.setElContext(Map<String, Object>)
	 */
	public void setContext(Map<String, Object> context);

	/**
	 * Places the given object into the context Map for the layout manager
	 * with the given name
	 * 
	 * @see org.kuali.rice.krad.uif.Component.pushObjectToContext(String,
	 *      Object)
	 */
	public void pushObjectToContext(String objectName, Object object);
	
	/**
	 * List of <code>PropertyReplacer</code> instances that will be
	 * evaluated during the view lifecycle to conditional set properties on the
	 * <code>LayoutManager</code> based on expression evaluations
	 * 
	 * @return List<PropertyReplacer> replacers to evaluate
	 */
	public List<PropertyReplacer> getPropertyReplacers();

	/**
	 * Setter for the layout managers property substitutions
	 * 
	 * @param propertyReplacers
	 */
	public void setPropertyReplacers(List<PropertyReplacer> propertyReplacers);

}
