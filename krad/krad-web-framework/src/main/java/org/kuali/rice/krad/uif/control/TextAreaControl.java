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
package org.kuali.rice.krad.uif.control;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.field.AttributeField;

/**
 * Represents a HTML TextArea control. Generally used for values that are very
 * large (such as a description)
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class TextAreaControl extends ControlBase {
    private static final long serialVersionUID = -4664558047325456844L;

    private int rows;
    private int cols;
    private Integer maxLength;
    private Integer minLength;

    private boolean textExpand;
    private String watermarkText = StringUtils.EMPTY;

    public TextAreaControl() {
        super();
    }

    /**
     * The following actions are performed:
     *
     * <ul>
     * <li>Defaults maxLength, minLength (if not set) to maxLength of parent field</li>
     * </ul>
     *
     * @see org.kuali.rice.krad.uif.component.ComponentBase#performFinalize(org.kuali.rice.krad.uif.view.View,
     *      java.lang.Object, org.kuali.rice.krad.uif.component.Component)
     */
    @Override
    public void performFinalize(View view, Object model, Component parent) {
        super.performFinalize(view, model, parent);

        if (parent instanceof AttributeField) {
            AttributeField field = (AttributeField) parent;
            if (getMaxLength() == null) {
                setMaxLength(field.getMaxLength());
            }

            if (getMinLength() == null) {
                setMinLength(field.getMinLength());
            }
        }
    }

    /**
     * Number of rows the control should span (horizontal length)
     *
     * @return int number of rows
     */
    public int getRows() {
        return this.rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * Number of columns the control should span (vertical length)
     *
     * @return int number of columns
     */
    public int getCols() {
        return this.cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    /**
     * Maximum number of characters that can be inputted
     *
     * <p>If not set on control, max length of field will be used</p>
     *
     * @return int max number of characters
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Setter for the max number of input characters
     *
     * @param maxLength
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Minimum number of characters that can be inputted
     *
     * <p>If not set on control, min length of field will be used</p>
     *
     * @return int max number of characters
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Setter for the min number of input characters
     *
     * @param maxLength
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * @return the watermarkText
     */
    public String getWatermarkText() {
        return this.watermarkText;
    }

    /**
     * @param watermarkText the watermarkText to set
     */
    public void setWatermarkText(String watermarkText) {
        //to avoid users from putting in the same value as the watermark adding some spaces here
        //see watermark troubleshooting for more info
        if (StringUtils.isNotEmpty(watermarkText)) {
            watermarkText = watermarkText + "   ";
        }
        this.watermarkText = watermarkText;
    }

    /**
     * If set to true, this control will have a button which can be clicked to expand the text area through
     * a popup window so the user has more space to type and see the data they are entering in this text field
     *
     * @return the textExpand
     */
    public boolean isTextExpand() {
        return this.textExpand;
    }

    /**
     * @param textExpand the textExpand to set
     */
    public void setTextExpand(boolean textExpand) {
        this.textExpand = textExpand;
    }


}
