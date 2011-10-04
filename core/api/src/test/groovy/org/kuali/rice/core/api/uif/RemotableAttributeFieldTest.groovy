package org.kuali.rice.core.api.uif

import org.junit.Test
import static org.junit.Assert.*
import org.junit.Ignore
import org.kuali.rice.core.test.JAXBAssert;

class RemotableAttributeFieldTest {

        private static final String XML1 =
        """<attributeField xmlns="http://rice.kuali.org/core/v2_0">
            <name>a_name</name>
          </attributeField>""";

        private static final String XML2 =
        """<attributeField xmlns="http://rice.kuali.org/core/v2_0">
            <name>a_name</name>
            <dataType>STRING</dataType>
            <shortLabel>a_shortLabel</shortLabel>
            <longLabel>a_longLabel</longLabel>
            <helpSummary>a_helpSummary</helpSummary>
            <helpConstraint>a_helpConstraint</helpConstraint>
            <helpDescription>a_helpDescription</helpDescription>
            <forceUpperCase>true</forceUpperCase>
            <minLength>1</minLength>
            <maxLength>1</maxLength>
            <minValue>-10</minValue>
            <maxValue>99</maxValue>
            <regexConstraint>\\w</regexConstraint>
            <regexContraintMsg>a_regexContraintMsg</regexContraintMsg>
            <required>true</required>
            <defaultValues>
                <defaultValue>foo</defaultValue>
                <defaultValue>bar</defaultValue>
            </defaultValues>
            <select>
                <keyLabels>
                    <entry key="foo">foo</entry>
                    <entry key="bar">bar</entry>
                </keyLabels><groups/>
                <multiple>true</multiple>
            </select>
            <widgets>
                <quickFinder>
                    <baseLookupUrl>http://foo.bar</baseLookupUrl>
                    <dataObjectClass>FooBo</dataObjectClass>
                    <lookupParameters/>
                    <fieldConversions/>
                </quickFinder>
            </widgets>
          </attributeField>""";

    @Test
    void testHappyPath() {
        RemotableAttributeField o = create().build();
        assertNotNull(o);
    }

    @Test
    void testHappyPath2() {
        RemotableAttributeField.Builder o = create2();
        assertNotNull(o.build());
    }

    @Test
    void testOptional() {
        RemotableAttributeField.Builder o = create();
        o.dataType = null
        o.shortLabel = null
        o.helpSummary = null
        o.helpConstraint = null
        o.helpDescription = null
        o.minLength = null
        o.maxLength = null
        o.minValue = null
        o.maxValue = null
        o.regexConstraint = null
        o.regexContraintMsg = null
        o.defaultValues = null
        o.control = null
        o.widgets = null
        assertNotNull(o.build());
    }

    @Test
    void testOptional2() {
        RemotableAttributeField.Builder o = create();
        o.widgets = []
        o.defaultValues = []
        assertNotNull(o.build());
    }

    @Test(expected=IllegalArgumentException.class)
    void testNullName() {
        RemotableAttributeField.Builder o = RemotableAttributeField.Builder.create((String) null);

        assertNotNull(o.build());
    }

    @Test(expected=IllegalArgumentException.class)
    void testBlankName() {
        RemotableAttributeField.Builder o = RemotableAttributeField.Builder.create(" ");

        assertNotNull(o.build());
    }

    @Test
	void testJAXB1() {
		RemotableAttributeField o = create().build();
		JAXBAssert.assertEqualXmlMarshalUnmarshal(o, XML1, RemotableAttributeField.class);
	}

    @Test
	void testJAXB2() {
		RemotableAttributeField o = create2().build();
		JAXBAssert.assertEqualXmlMarshalUnmarshal(o, XML2, RemotableAttributeField.class);
	}

    private RemotableAttributeField.Builder create() {
		RemotableAttributeField.Builder o = RemotableAttributeField.Builder.create("a_name");
        return o
	}

    private RemotableAttributeField.Builder create2() {
		RemotableAttributeField.Builder o = RemotableAttributeField.Builder.create("a_name");
        o.dataType = DataType.STRING
        o.shortLabel = "a_shortLabel"
        o.longLabel = "a_longLabel"
        o.helpSummary = "a_helpSummary"
        o.helpConstraint = "a_helpConstraint"
        o.helpDescription = "a_helpDescription"
        o.forceUpperCase = true
        o.minLength = 1
        o.maxLength = 1
        o.minValue = -10
        o.maxValue = 99
        o.regexConstraint = "\\w"
        o.regexContraintMsg = "a_regexContraintMsg"
        o.required = true
        o.defaultValues = ["foo", "bar"]
        RemotableSelect.Builder b = RemotableSelect.Builder.create(["foo" : "foo", "bar" : "bar"]);
        b.multiple = true
        o.control = b
        o.widgets = [RemotableQuickFinder.Builder.create("http://foo.bar", "FooBo")]
        return o
	}
}
