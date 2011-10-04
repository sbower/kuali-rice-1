package org.kuali.rice.kim.impl.identity.residency

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase
import org.kuali.rice.kim.api.identity.residency.EntityResidencyContract
import javax.persistence.Id
import javax.persistence.GeneratedValue
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import javax.persistence.Column
import org.kuali.rice.kim.api.identity.residency.EntityResidency

class EntityResidencyBo extends PersistableBusinessObjectBase implements EntityResidencyContract {
    private static final long serialVersionUID = 6577601907062646926L;

	@Id
	@GeneratedValue(generator="KRIM_ENTITY_RESIDENCY_ID_S")
	@GenericGenerator(name="KRIM_ENTITY_RESIDENCY_ID_S",strategy="org.kuali.rice.core.jpa.spring.RiceNumericStringSequenceStyleGenerator",parameters=[
			@Parameter(name="sequence_name",value="KRIM_ENTITY_RESIDENCY_ID_S"),
			@Parameter(name="value_column",value="id")
		])
	@Column(name = "ID")
	String id;

	@Column(name = "ENTITY_ID")
	String entityId;
	
	@Column(name = "DETERMINATION_METHOD")
	String determinationMethod;
	
	@Column(name = "IN_STATE")
    String inState;
    
  /*
   * Converts a mutable EntityResidencyBo to an immutable EntityResidency representation.
   * @param bo
   * @return an immutable EntityResidency
   */
  static EntityResidency to(EntityResidencyBo bo) {
    if (bo == null) { return null }
    return EntityResidency.Builder.create(bo).build()
  }

  /**
   * Creates a EntityResidencyBo business object from an immutable representation of a EntityResidency.
   * @param an immutable EntityResidency
   * @return a EntityResidencyBo
   */
  static EntityResidencyBo from(EntityResidency immutable) {
    if (immutable == null) {return null}

    EntityResidencyBo bo = new EntityResidencyBo()
    bo.entityId = immutable.entityId
    bo.id = immutable.id
    bo.determinationMethod = immutable.determinationMethod
    bo.inState = immutable.inState
    bo.versionNumber = immutable.versionNumber
    bo.objectId = immutable.objectId

    return bo;
  }
}
