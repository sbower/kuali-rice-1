package org.kuali.rice.krms.impl.repository

import org.kuali.rice.krad.bo.MutableInactivatable
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase
import org.kuali.rice.krms.api.repository.type.KrmsTypeDefinitionContract

public class KrmsTypeBo extends PersistableBusinessObjectBase implements MutableInactivatable, KrmsTypeDefinitionContract {

	def String id
	def String name
	def String namespace
	def String serviceName
	def boolean active
	def List<KrmsTypeAttributeBo> attributes
	/**
	* Converts a mutable bo to it's immutable counterpart
	* @param bo the mutable business object
	* @return the immutable object
	*/
	static org.kuali.rice.krms.api.repository.type.KrmsTypeDefinition to(KrmsTypeBo bo) {
		if (bo == null) { return null }
		return org.kuali.rice.krms.api.repository.type.KrmsTypeDefinition.Builder.create(bo).build();
	}

	/**
	 * Converts a immutable object to it's mutable bo counterpart
	 * @param im immutable object
	 * @return the mutable bo
	 */
	static KrmsTypeBo from(org.kuali.rice.krms.api.repository.type.KrmsTypeDefinition im) {
		if (im == null) { return null }

		KrmsTypeBo bo = new KrmsTypeBo()
		bo.id = im.id
		bo.name = im.name
		bo.namespace = im.namespace
		bo.serviceName = im.serviceName
		bo.active = (im.active == null) ? true : im.active;
		bo.attributes = new ArrayList<KrmsTypeAttributeBo>()
		for( attr in im.attributes ){
			bo.attributes.add(KrmsTypeAttributeBo.from(attr))
		}
		bo.versionNumber = im.versionNumber
		return bo
	}

} 