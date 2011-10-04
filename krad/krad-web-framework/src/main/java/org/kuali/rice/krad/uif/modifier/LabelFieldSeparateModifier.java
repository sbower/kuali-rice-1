/*
 * Copyright 2011 The Kuali Foundation
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
package org.kuali.rice.krad.uif.modifier;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.field.Field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pulls <code>LabelField</code> instances out of a contained field so they will
 * be placed separately in the <code>LayoutManager</code>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LabelFieldSeparateModifier extends ComponentModifierBase {
	private static final long serialVersionUID = -4304947796868636298L;

	public LabelFieldSeparateModifier() {
		super();
	}

	/**
	 * Iterates through the <code>Group</code> items and if the label field is
	 * not null and should be rendered, adds it to the new field list
	 * immediately before the <code>Field</code> item the label applies to.
	 * Finally the new list of components is set on the group
	 * 
	 * @see org.kuali.rice.krad.uif.modifier.ComponentModifier#performModification(org.kuali.rice.krad.uif.view.View,
	 *      java.lang.Object, org.kuali.rice.krad.uif.component.Component)
	 */
	@Override
	public void performModification(View view, Object model, Component component) {
		if ((component != null) && !(component instanceof Group)) {
			throw new IllegalArgumentException("Compare field initializer only support Group components, found type: "
					+ component.getClass());
		}

		if (component == null) {
			return;
		}

		// list that will be built
		List<Component> groupFields = new ArrayList<Component>();

		Group group = (Group) component;
		for (Component item : group.getItems()) {
			if (item instanceof Field) {
				Field field = (Field) item;

				// pull out label field
				if (field.getLabelField() != null && field.getLabelField().isRender()) {
				    field.getLabelField().addStyleClass("displayWith-" + field.getFactoryId());
                    if (!field.isRender() && StringUtils.isBlank(field.getProgressiveRender())) {
                       field.getLabelField().setRender(false);
                    }
                    else if(!field.isRender() && StringUtils.isNotBlank(field.getProgressiveRender())){
                       field.getLabelField().setRender(true);
                       String prefixStyle = "";
                       if(StringUtils.isNotBlank(field.getLabelField().getStyle())){
                           prefixStyle = field.getLabelField().getStyle();
                       }
                       field.getLabelField().setStyle(prefixStyle + ";" + "display: none;");
                    }

					groupFields.add(field.getLabelField());

					// set boolean to indicate label field should not be
					// rendered with the attribute
					field.setLabelFieldRendered(true);
				}
			}

			groupFields.add(item);
		}

		// update group
		group.setItems(groupFields);
	}

	/**
	 * @see org.kuali.rice.krad.uif.modifier.ComponentModifier#getSupportedComponents()
	 */
	@Override
	public Set<Class<? extends Component>> getSupportedComponents() {
		Set<Class<? extends Component>> components = new HashSet<Class<? extends Component>>();
		components.add(Group.class);

		return components;
	}

}
