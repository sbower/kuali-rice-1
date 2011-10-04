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
package org.kuali.rice.ksb.impl.registry;

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.Version

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.kuali.rice.core.api.mo.ModelObjectBasic
import org.kuali.rice.ksb.api.registry.ServiceDescriptor
import org.kuali.rice.ksb.api.registry.ServiceDescriptorContract

/**
 * TODO... 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@Entity
@Table(name="KRSB_SVC_DSCRPTR_T")
public class ServiceDescriptorBo implements ServiceDescriptorContract, ModelObjectBasic {

	@Id
	@GeneratedValue(generator="KRSB_SVC_DSCRPTR_S")
	@GenericGenerator(name="KRSB_FLT_SVC_DEF_S",strategy="org.hibernate.id.enhanced.SequenceStyleGenerator",parameters=[
			@Parameter(name="sequence_name", value="KRSB_SVC_DSCRPTR_S"),
			@Parameter(name="value_column", value="id")
	])
	@Column(name="SVC_DSCRPTR_ID")
	String id
	
	@Lob
	@Column(name="DSCRPTR", length=4000)
	String descriptor
	
	@Version
	@Column(name="VER_NBR")
	Long versionNumber
	
	static ServiceDescriptor to(ServiceDescriptorBo bo) {
		if (bo == null) {
			return null
		}
		return ServiceDescriptor.Builder.create(bo).build();
	}
	
	static ServiceDescriptorBo from(ServiceDescriptor im) {
		if (im == null) {
			return null
		}

		ServiceDescriptorBo bo = new ServiceDescriptorBo()
		bo.id = im.id
		bo.descriptor = im.descriptor
		bo.versionNumber = im.versionNumber

		return bo
	}

}
