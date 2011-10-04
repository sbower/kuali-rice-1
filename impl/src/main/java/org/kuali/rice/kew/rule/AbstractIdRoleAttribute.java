/*
 * Copyright 2007-2008 The Kuali Foundation
 *
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
package org.kuali.rice.kew.rule;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.identity.Id;
import org.kuali.rice.kew.routeheader.DocumentContent;
import org.kuali.rice.kew.rule.bo.RuleAttribute;
import org.kuali.rice.kew.rule.xmlrouting.GenericXMLRuleAttribute;
import org.kuali.rice.kew.rule.xmlrouting.XPathHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic Role Attribute superclass that can be used to route to an ID. Can
 * take as configuration the label to use for the element name in the XML. This
 * allows for re-use of this component in different contexts.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class AbstractIdRoleAttribute extends AbstractRoleAttribute
		implements GenericXMLRuleAttribute {

	private static final String XML_ELEMENT_LABEL = "xmlElementLabel";
	private static final String ROLE_NAME_LABEL = "roleNameLabel";

	private String idValue;
	private Map paramMap = new HashMap();
	private RuleAttribute ruleAttribute;

	protected abstract String getAttributeElementName();

	protected abstract Id resolveId(String id);

	protected abstract String getIdName();

	/**
	 * Returns qualified role names based on IDs in the XML. Each returned
	 * qualified Role contains a single ID.
	 * 
	 * @see org.kuali.rice.kew.rule.RoleAttribute#getQualifiedRoleNames(java.lang.String,
	 *      org.kuali.rice.kew.routeheader.DocumentContent)
	 */
	public List<String> getQualifiedRoleNames(String roleName,
			DocumentContent documentContent) {
		try {
			readConfiguration();
			String elementName = (String) getParamMap().get(XML_ELEMENT_LABEL);
			List<String> qualifiedRoleNames = new ArrayList<String>();
			XPath xPath = XPathHelper.newXPath();
			NodeList idNodes = (NodeList) xPath.evaluate("//"
					+ getAttributeElementName() + "/" + elementName,
					documentContent.getDocument(), XPathConstants.NODESET);
			for (int index = 0; index < idNodes.getLength(); index++) {
				Element idElement = (Element) idNodes.item(index);
				String id = idElement.getTextContent();
				qualifiedRoleNames.add(id);
			}
			return qualifiedRoleNames;
		} catch (XPathExpressionException e) {
			throw new WorkflowRuntimeException(
					"Failed to evaulate XPath expression to find ids.", e);
		}
	}

	/**
	 * Takes the given qualified role which contains an ID and returns a
	 * resolved role for the entity with that id.
	 * 
	 * @see org.kuali.rice.kew.rule.RoleAttribute#resolveQualifiedRole(org.kuali.rice.kew.engine.RouteContext,
	 *      java.lang.String, java.lang.String)
	 */
	public ResolvedQualifiedRole resolveQualifiedRole(
			RouteContext routeContext, String roleName, String qualifiedRole) {
		String roleNameLabel = (String) getParamMap().get(ROLE_NAME_LABEL);
		if (roleNameLabel == null) {
			readConfiguration();
			roleNameLabel = (String) getParamMap().get(ROLE_NAME_LABEL);
		}
		ResolvedQualifiedRole resolvedRole = new ResolvedQualifiedRole();
		resolvedRole.setQualifiedRoleLabel(roleNameLabel);
		resolvedRole.getRecipients().add(resolveId(qualifiedRole));
		return resolvedRole;
	}

	/**
	 * Generates XML containing the ID on this attribute.
	 * 
	 * @see org.kuali.rice.kew.rule.AbstractWorkflowAttribute#getDocContent()
	 */
	@Override
	public String getDocContent() {
		readConfiguration();
		if (!StringUtils.isBlank(getIdValue())) {
			String elementName = (String) getParamMap().get(XML_ELEMENT_LABEL);
			return "<" + getAttributeElementName() + "><" + elementName + ">"
					+ getIdValue() + "</" + elementName + "></"
					+ getAttributeElementName() + ">";
		}
		return "";
	}

	/**
	 * Reads any configured values in the XML of the RuleAttribute and adds them
	 * to the paramMap.
	 * 
	 */
	protected void readConfiguration() {
		String idInMap = (String) getParamMap().get(getIdName());
		if (getIdValue() == null) {
			setIdValue(idInMap);
		}
		if (getIdValue() != null) {
			getParamMap().put(getIdName(), getIdValue());
		}
		if (ruleAttribute != null) {
			String xmlConfigData = ruleAttribute.getXmlConfigData();
			if (!StringUtils.isBlank(xmlConfigData)) {
				XPath xPath = XPathHelper.newXPath();
				try {
					String xmlElementLabel = xPath.evaluate("/configuration/"
							+ XML_ELEMENT_LABEL, new InputSource(
							new StringReader(xmlConfigData)));
					String roleNameLabel = xPath.evaluate("/configuration/"
							+ ROLE_NAME_LABEL, new InputSource(
							new StringReader(xmlConfigData)));
					if (!StringUtils.isBlank(xmlElementLabel)) {
						getParamMap().put(XML_ELEMENT_LABEL, xmlElementLabel);
					}
					if (!StringUtils.isBlank(roleNameLabel)) {
						getParamMap().put(ROLE_NAME_LABEL, roleNameLabel);
					}

				} catch (XPathExpressionException e) {
					throw new WorkflowRuntimeException(
							"Failed to locate Rule Attribute configuration.");
				}
			}
		}
		// setup default values if none were defined in XML
		if (StringUtils.isBlank((String) getParamMap().get(XML_ELEMENT_LABEL))) {
			getParamMap().put(XML_ELEMENT_LABEL, getIdName());
		}
		if (getParamMap().get(ROLE_NAME_LABEL) == null) {
			getParamMap().put(ROLE_NAME_LABEL, "");
		}
	}

	public String getIdValue() {
		return this.idValue;
	}

	public void setIdValue(String idValue) {
		this.idValue = idValue;
	}

	public Map getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map paramMap) {
		this.paramMap = paramMap;
	}

	public void setRuleAttribute(RuleAttribute ruleAttribute) {
		this.ruleAttribute = ruleAttribute;
	}

}
