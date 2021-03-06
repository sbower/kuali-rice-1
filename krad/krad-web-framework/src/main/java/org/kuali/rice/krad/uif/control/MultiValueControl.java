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

import org.kuali.rice.core.api.util.KeyValue;

import java.util.List;

/**
 * Indicates <code>Control</code> types that can hold more than one value for selection
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface MultiValueControl {

    /**
     * <code>List</code> of values the control can accept. Each value consists
     * of a key and a label. The key is the what will be submitted back if the
     * user selects the choice, the label is what will be displayed to the user
     * for the choice.
     * <p>
     * <code>KeyLabelPair</code> instances are usually generated by the
     * <code>KeyValueFinder</code> associated with the <code>Field</code> for
     * which the control belongs
     * </p>
     *
     * @return List of KeyLabelPair instances
     */
    public List<KeyValue> getOptions();

    /**
     * Sets the List of <code>KeyValue</code> pairs that make up the options for the control
     *
     * @param options
     */
    public void setOptions(List<KeyValue> options);
}
