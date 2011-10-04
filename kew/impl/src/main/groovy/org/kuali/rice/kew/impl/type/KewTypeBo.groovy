package org.kuali.rice.kew.impl.type

import org.kuali.rice.kew.api.repository.type.KewTypeDefinitionContract
import org.kuali.rice.krad.bo.MutableInactivatable
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase

public class KewTypeBo extends PersistableBusinessObjectBase implements MutableInactivatable, KewTypeDefinitionContract {

	def String id
	def String name
	def String namespace
	def String serviceName
	def boolean active = true
	def List<KewTypeAttributeBo> attributes

    public List<KewTypeAttributeBo> getAttributes() {
        if (attributes == null) attributes = new ArrayList<KewTypeAttributeBo>();
        return attributes;
    }

    /**
    * Converts a mutable bo to it's immutable counterpart
    * @param bo the mutable business object
    * @return the immutable object
    */
    static org.kuali.rice.kew.api.repository.type.KewTypeDefinition to(KewTypeBo bo) {
        if (bo == null) { return null }
        return org.kuali.rice.kew.api.repository.type.KewTypeDefinition.Builder.create(bo).build();
    }
        
    /**
     * Converts a immutable object to it's mutable bo counterpart
     * @param im immutable object
     * @return the mutable bo
     */
    static KewTypeBo from(org.kuali.rice.kew.api.repository.type.KewTypeDefinition im) {
        if (im == null) { return null }

        KewTypeBo bo = new KewTypeBo()
        bo.id = im.id
        bo.name = im.name
        bo.namespace = im.namespace
        bo.serviceName = im.serviceName
        bo.active = (im.active == null) ? true : im.active;
        bo.attributes = new ArrayList<KewTypeAttributeBo>()
        for( attr in im.attributes ){
            bo.attributes.add(KewTypeAttributeBo.from(attr))
        }
        bo.versionNumber = im.versionNumber
        return bo
    }

} 