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
import org.kuali.rice.krad.keyvalues.KeyValuesFinder;
import org.kuali.rice.krad.uif.view.ViewModel;

import java.util.List;

/**
 * Values finder that can taken the {@link org.kuali.rice.krad.uif.view.ViewModel} that provides data to the view
 * for conditionally setting the valid options
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface UifKeyValuesFinder extends KeyValuesFinder {

    /**
     * Builds a list of key values representations for valid value selections using the given view model
     * to retrieve values from other fields and conditionally building the options
     *
     * @return List of KeyValue objects
     */
    public List<KeyValue> getKeyValues(ViewModel model);
}
