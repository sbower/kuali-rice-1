/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.rice.krad.uif.view;

import org.kuali.rice.krad.uif.UifConstants.ViewType;

/**
 * Type of <code>View</code> that provides a read-only display of a record of
 * data (object instance)
 * 
 * <p>
 * The <code>InquiryView</code> provides the interface for the Inquiry
 * framework. It works with the <code>Inquirable</code> service and inquiry
 * controller. The view does render a form to support the configuration of
 * actions to perform operations on the data.
 * </p>
 * 
 * <p>
 * Inquiry views are primarily configured by the object class they are
 * associated with. This provides the default dictionary information for the
 * fields. If more than one inquiry view is needed for the same object class,
 * the view name can be used to further identify an unique view
 * </p>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class InquiryView extends FormView {
    private static final long serialVersionUID = 716926008488403616L;

    private Class<?> dataObjectClassName;

    public InquiryView() {
        super();

        setViewTypeName(ViewType.INQUIRY);
        setValidateDirty(false);
        setTranslateCodes(true);
    }

    /**
     * The following initialization is performed:
     *
     * <ul>
     * <li>Set the abstractTypeClasses map for the inquiry object path</li>
     * </ul>
     *
     * @see org.kuali.rice.krad.uif.container.ContainerBase#performInitialization(org.kuali.rice.krad.uif.view.View, java.lang.Object)
     */
    @Override
    public void performInitialization(View view, Object model) {
        super.performInitialization(view, model);

        getAbstractTypeClasses().put(getDefaultBindingObjectPath(), getDataObjectClassName());
    }

    /**
     * Class name for the object the inquiry applies to
     * 
     * <p>
     * The object class name is used to pick up a dictionary entry which will
     * feed the attribute field definitions and other configuration. In addition
     * it is used to configure the <code>Inquirable</code> which will carry out
     * the inquiry action
     * </p>
     * 
     * @return Class<?> inquiry object class
     */
    public Class<?> getDataObjectClassName() {
        return this.dataObjectClassName;
    }

    /**
     * Setter for the object class name
     * 
     * @param dataObjectClassName
     */
    public void setDataObjectClassName(Class<?> dataObjectClassName) {
        this.dataObjectClassName = dataObjectClassName;
    }

}
