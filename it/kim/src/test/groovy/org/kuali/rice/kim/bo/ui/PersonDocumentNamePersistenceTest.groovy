package org.kuali.rice.kim.bo.ui

import org.junit.Test
import org.kuali.rice.kim.impl.identity.address.EntityAddressBo
import org.kuali.rice.kim.impl.identity.entity.EntityBo
import org.kuali.rice.kim.impl.identity.type.EntityTypeContactInfoBo
import org.kuali.rice.kim.test.BoPersistenceTest
import org.kuali.rice.kim.test.Factory
import org.kuali.rice.kim.impl.identity.name.EntityNameBo
import org.kuali.rice.kim.impl.identity.name.EntityNameTypeBo
import org.joda.time.DateTime

/**
 * Tests persisting PersonDocumentName object in order to verify ORM mappings
 */
class PersonDocumentNamePersistenceTest extends BoPersistenceTest {
    @Test
    void test_save_persondocumentname() {
        EntityBo entity = Factory.make(EntityBo)
        boService.save(entity)
        EntityNameBo name = Factory.make(EntityNameBo, entity: entity)

        PersonDocumentName pdn = new PersonDocumentName([
            entityId: entity.id,
            documentNumber: System.currentTimeMillis(),
            namePrefix: name.namePrefixUnmasked,
            firstName: name.firstNameUnmasked,
            middleName: name.middleNameUnmasked,
            lastName: name.lastNameUnmasked,
            nameSuffix: name.nameSuffixUnmasked,
            entityNameType: name.nameType,
            nameChangedDate: genDbTimestamp(),
            nameCode: name.nameType.code
        ])

        boService.save(pdn)

        assertRow(kimdoc_fields(pdn) + [
            ENTITY_NM_ID: pdn.entityNameId,
            PREFIX_NM: pdn.namePrefix,
            TITLE_NM: pdn.nameTitle,
            FIRST_NM: pdn.firstName,
            MIDDLE_NM: pdn.middleName,
            LAST_NM: pdn.lastName,
            SUFFIX_NM: pdn.nameSuffix,
            NOTE_MSG: pdn.noteMessage,
            NM_CHNG_DT: toDbTimestamp(pdn.nameChangedDate.time),
            NM_TYP_CD: pdn.entityNameType.code
        ],
        "KRIM_PND_NM_MT", "ENTITY_NM_ID")
    }
}