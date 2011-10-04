package org.kuali.rice.krms.impl.repository

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase
import org.kuali.rice.krms.api.repository.category.CategoryDefinitionContract
import org.kuali.rice.krms.api.repository.category.CategoryDefinition

class CategoryBo extends PersistableBusinessObjectBase implements CategoryDefinitionContract {

    def String id
    def String name
    def String namespace

	/**
	* Converts a mutable bo to it's immutable counterpart
	* @param bo the mutable business object
	* @return the immutable object
	*/
    static CategoryDefinition to(CategoryBo bo) {
        if (bo == null) { return null }

        return org.kuali.rice.krms.api.repository.category.CategoryDefinition.Builder.create(bo).build()
    }

	/**
	* Converts a list of mutable bos to it's immutable counterpart
	* @param bos the list of mutable business objects
	* @return and immutable list containing the immutable objects
	*/
   static List<CategoryDefinition> to(List<CategoryBo> bos) {
	   if (bos == null) { return null }

	   List<CategoryDefinition> categories = new ArrayList<CategoryDefinition>();
	   for (CategoryBo p : bos){
		   categories.add(CategoryDefinition.Builder.create(p).build())
	   }
	   return Collections.unmodifiableList(categories)
   }

   /**
	* Converts a immutable object to it's mutable bo counterpart
	* @param im immutable object
	* @return the mutable bo
	*/
   static CategoryBo from(CategoryDefinition im) {
	   if (im == null) { return null }

	   CategoryBo bo = new CategoryBo()
	   bo.id = im.id
	   bo.name = im.name
	   bo.namespace = im.namespace
	   bo.versionNumber = im.versionNumber
	   return bo
   }

   static List<CategoryBo> from(List<CategoryDefinition> ims){
	   if (ims == null) {return null }

	   List<CategoryBo> bos = new ArrayList<CategoryBo>();
	   for (CategoryBo im : ims){
		   CategoryBo bo = from(im)
	   	   bos.add(bo);
	   }
	   return Collections.unmodifiableList(bos)
   }
}
