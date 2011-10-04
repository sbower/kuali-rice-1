/*
 * Copyright 2006-2011 The Kuali Foundation
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



package org.kuali.rice.kim.api.role

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import org.junit.Assert
import org.junit.Test
import org.kuali.rice.kim.api.common.delegate.DelegateType
import org.kuali.rice.kim.api.common.delegate.DelegateTypeTest

public class RoleMembershipTest {

    private final shouldFail = new GroovyTestCase().&shouldFail

    final static String ROLE_ID = "1"
    final static String ROLE_MEMBER_ID = "187"
    final static String EMBEDDED_ROLE_ID = "255"
    final static String MEMBER_ID = "42"
    final static String MEMBER_TYPE_CODE = "P"
    final static String ROLE_SORTING_CODE = "DESC"
    final static Map<String, String> QUALIFIER = [:]
    final static List<DelegateType.Builder> DELEGATES = [create_delegate_type()]

    private static DelegateType.Builder create_delegate_type() {
        DelegateType.Builder builder =  DelegateType.Builder.create(
            DelegateTypeTest.ROLE_ID,
            DelegateTypeTest.DELEGATION_ID,
            DelegateTypeTest.DELEGATION_TYPE_CODE,
            DelegateTypeTest.DELEGATE_MEMBERS)
        builder.kimTypeId = DelegateTypeTest.KIM_TYPE_ID
        builder.active = true


        return builder
    }


    final static String XML = """
    <roleMembership xmlns="http://rice.kuali.org/kim/v2_0">
        <roleId>${ROLE_ID}</roleId>
        <roleMemberId>${ROLE_MEMBER_ID}</roleMemberId>
        <embeddedRoleId>${EMBEDDED_ROLE_ID}</embeddedRoleId>
        <memberId>${MEMBER_ID}</memberId>
        <memberTypeCode>${MEMBER_TYPE_CODE}</memberTypeCode>
        <roleSortingCode>${ROLE_SORTING_CODE}</roleSortingCode>
        <qualifier></qualifier>
        <delegates>
            <roleId>${DelegateTypeTest.ROLE_ID}</roleId>
            <delegationId>${DelegateTypeTest.DELEGATION_ID}</delegationId>
            <delegationTypeCode>${DelegateTypeTest.DELEGATION_TYPE_CODE}</delegationTypeCode>
            <kimTypeId>${DelegateTypeTest.KIM_TYPE_ID}</kimTypeId>
            <members>
                <delegationMemberId>${DelegateTypeTest.DELEGATION_MEMBER_ID}</delegationMemberId>
                <delegationId>${DelegateTypeTest.DELEGATION_ID}</delegationId>
                <memberId>${DelegateTypeTest.MEMBER_ID}</memberId>
                <roleMemberId>${DelegateTypeTest.ROLE_MEMBER_ID}</roleMemberId>
                <typeCode>${DelegateTypeTest.DELEGATION_TYPE_CODE}</typeCode>
                <delegationTypeCode>${DelegateTypeTest.DELEGATION_TYPE_CODE}</delegationTypeCode>
                <memberTypeCode>${DelegateTypeTest.DELEGATION_TYPE_CODE}</memberTypeCode>
                <roleMemberId>${DelegateTypeTest.ROLE_MEMBER_ID}</roleMemberId>
                <versionNumber>${DelegateTypeTest.VERSION}</versionNumber>
                <active>true</active>
            </members>
            <active>${DelegateTypeTest.ACTIVE}</active>
        </delegates>
    </roleMembership>
    """

    @Test
    public void testXmlMarshalingAndUnMarshalling() {
        JAXBContext jc = JAXBContext.newInstance(RoleMembership.class)
        Marshaller marshaller = jc.createMarshaller()
        StringWriter sw = new StringWriter()

        RoleMembership.Builder builder = RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER)
        builder.embeddedRoleId = EMBEDDED_ROLE_ID
        builder.roleSortingCode = ROLE_SORTING_CODE
        builder.delegates = DELEGATES


        marshaller.marshal(builder.build(), sw)
        String xml = sw.toString()


        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Object actual = unmarshaller.unmarshal(new StringReader(xml))
        Object expected = unmarshaller.unmarshal(new StringReader(XML))
        Assert.assertEquals(expected, actual)
    }

    @Test
    public void testXmlUnmarshal() {
        JAXBContext jc = JAXBContext.newInstance(RoleMembership.class)
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        RoleMembership rm = (RoleMembership) unmarshaller.unmarshal(new StringReader(XML))
        Assert.assertEquals(ROLE_ID, rm.roleId)
        Assert.assertEquals(ROLE_MEMBER_ID, rm.roleMemberId)
        Assert.assertEquals(EMBEDDED_ROLE_ID, rm.embeddedRoleId)
        Assert.assertEquals(MEMBER_ID, rm.memberId)
        Assert.assertEquals(MEMBER_TYPE_CODE, rm.memberTypeCode)
        Assert.assertEquals(ROLE_SORTING_CODE,rm.roleSortingCode)
        Assert.assertEquals(QUALIFIER, rm.qualifier)
        Assert.assertEquals(DELEGATES.collect {delegateBuilder -> delegateBuilder.build()}, rm.delegates)
    }

    @Test
    void test_Builder() {
        RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER).build()
    }

    @Test
    void test_BuilderForContract() {
        RoleMembership rm = RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER).build()
        RoleMembership rm_from_contract = RoleMembership.Builder.create(rm).build()
        Assert.assertEquals(rm, rm_from_contract)
    }

    @Test
    void test_MemberId_Blank() {
        RoleMembership.Builder builder = RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER)
        shouldFail(IllegalArgumentException) {
            builder.memberId = ""
        }
    }

    @Test
    void test_MemberId_Null() {
        RoleMembership.Builder builder = RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER)
        shouldFail(IllegalArgumentException) {
            builder.memberId = null
        }
    }

    @Test
    void test_MemberTypeCode_Blank() {
        RoleMembership.Builder builder = RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER)
        shouldFail(IllegalArgumentException) {
            builder.memberTypeCode = ""
        }
    }

    @Test
    void test_MemberTypeCode_Null() {
        RoleMembership.Builder builder = RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER)
        shouldFail(IllegalArgumentException) {
            builder.memberTypeCode = null
        }
    }

    @Test
    void test_DelegatesImmutable() {
        RoleMembership.Builder builder = RoleMembership.Builder.create(ROLE_ID, ROLE_MEMBER_ID, MEMBER_ID, MEMBER_TYPE_CODE, QUALIFIER)
        builder.delegates = DELEGATES
        RoleMembership rm = builder.build()
        shouldFail(UnsupportedOperationException) {
            rm.delegates.add(create_delegate_type())
        }
    }
}
