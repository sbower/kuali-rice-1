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

package org.kuali.rice.core.framework.component

import org.kuali.rice.core.api.component.Component
import org.kuali.rice.core.api.component.ComponentContract
import org.kuali.rice.krad.bo.ExternalizableBusinessObject
import org.kuali.rice.krad.bo.MutableInactivatable

//@ToString
class ComponentEbo implements ComponentContract, MutableInactivatable, ExternalizableBusinessObject {

    private static final long serialVersionUID = 1L;

    def String namespaceCode
	def String code
	def String name
	def boolean active = true
	def boolean virtual
    def Long versionNumber
	def String objectId

    /**
     * Converts a mutable ebo to its immutable counterpart
     * @param bo the mutable business object
     * @return the immutable object
     */
    static Component to(ComponentEbo bo) {
        if (bo == null) {
            return null
        }

        return Component.Builder.create(bo).build();
    }

    /**
     * Converts a immutable object to its mutable counterpart
     * @param im immutable object
     * @return the mutable bo
     */
    static ComponentEbo from(Component im) {
        if (im == null) {
            return null
        }

        ComponentEbo bo = new ComponentEbo()
        bo.code = im.code
        bo.name = im.name
        bo.active = im.active
        bo.namespaceCode = im.namespaceCode
        bo.virtual = im.virtual
		bo.objectId = im.objectId
        return bo;
    }

    @Override
    void refresh() { }
}
