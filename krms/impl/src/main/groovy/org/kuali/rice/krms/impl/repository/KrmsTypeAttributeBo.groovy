package org.kuali.rice.krms.impl.repository

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase
import org.kuali.rice.krms.api.repository.type.KrmsTypeAttribute
import org.kuali.rice.krms.api.repository.type.KrmsTypeAttributeContract
import org.kuali.rice.krad.bo.MutableInactivatable

public class KrmsTypeAttributeBo extends PersistableBusinessObjectBase implements MutableInactivatable, KrmsTypeAttributeContract {

	def String id
	def String typeId
	def String attributeDefinitionId
	def Integer sequenceNumber
	def boolean active
	def KrmsAttributeDefinitionBo attributeDefinition

	/**
	 * Converts a mutable bo to it's immutable counterpart
	 * @param bo the mutable business object
	 * @return the immutable object
	 */
	static KrmsTypeAttribute to(KrmsTypeAttributeBo bo) {
		if (bo == null) { return null }
		return org.kuali.rice.krms.api.repository.type.KrmsTypeAttribute.Builder
			.create(bo).build()
	}

	/**
	 * Converts a immutable object to it's mutable bo counterpart
	 * @param im immutable object
	 * @return the mutable bo
	 */
	static KrmsTypeAttributeBo from(KrmsTypeAttribute im) {
		if (im == null) { return null }

		KrmsTypeAttributeBo bo = new KrmsTypeAttributeBo()
		bo.id = im.id
		bo.typeId = im.typeId
		bo.attributeDefinitionId = im.attributeDefinitionId
		bo.sequenceNumber = im.sequenceNumber
		bo.active = im.active
		bo.attributeDefinition = KrmsAttributeDefinitionBo.from(im.attributeDefinition)
		return bo
	}
	
	@Override
	KrmsAttributeDefinitionBo getAttributeDefinition() {
		return attributeDefinition
	}
	
}