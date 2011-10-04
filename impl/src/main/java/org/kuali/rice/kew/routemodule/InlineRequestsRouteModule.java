/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
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
package org.kuali.rice.kew.routemodule;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.kuali.rice.core.api.impex.xml.XmlConstants;
import org.kuali.rice.core.api.reflect.ObjectDefinition;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.xml.XmlHelper;
import org.kuali.rice.kew.actionrequest.ActionRequestFactory;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.RuleExtension;
import org.kuali.rice.kew.rule.RuleResponsibilityBo;
import org.kuali.rice.kew.rule.WorkflowAttribute;
import org.kuali.rice.kew.rule.bo.RuleAttribute;
import org.kuali.rice.kew.rule.xmlrouting.GenericXMLRuleAttribute;
import org.kuali.rice.kew.rule.xmlrouting.XPathHelper;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.xml.RuleXmlParser;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * A RouteModule that generates requests for responsibilities statically defined
 * in the config block of the node.
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class InlineRequestsRouteModule extends FlexRMAdapter {
    private static final Logger LOG = Logger.getLogger(InlineRequestsRouteModule.class);

    /**
     * This overridden method is used to decipher the inline xpath and responsibilities of a route node definition and use
     * them to create action reqeusts
     * 
     * @see org.kuali.rice.kew.routemodule.FlexRMAdapter#findActionRequests(org.kuali.rice.kew.engine.RouteContext)
     */
    @Override
    public List<ActionRequestValue> findActionRequests(RouteContext context) throws Exception {
        // comment this out while implementing the meta-rules stuff
        // re-implement later
        List<ActionRequestValue> actionRequests = new ArrayList<ActionRequestValue>();
        RouteNodeInstance currentNode = context.getNodeInstance();
        String contentFragment = currentNode.getRouteNode().getContentFragment();
        // parse with JDOM to reuse RuleXmlParser
        Document doc = XmlHelper.trimSAXXml(new ByteArrayInputStream(contentFragment.getBytes()));
        Element root = doc.getRootElement();
        List<String> ruleAttributeNames = new ArrayList<String>();
        List<String> ruleAttributeClassNames = new ArrayList<String>();
        List<String> xpathExpressions = new ArrayList<String>();
        // get the list of ruleAttributes to use
        Element ruleAttributes = root.getChild("ruleAttributes");
        if (ruleAttributes != null) {
            for (Object o : ruleAttributes.getChildren("name")) {
                Element e = (Element) o;
                ruleAttributeNames.add(e.getText());
            }
            for (Object o : ruleAttributes.getChildren("className")) {
                Element e = (Element) o;
                ruleAttributeClassNames.add(e.getText());
            }
        }
        // get the list of xpath expressions to verify
        for (Object o: root.getChildren("match")) {
            Element e = (Element) o;
            xpathExpressions.add(e.getText());
        }
        if ( (ruleAttributeNames.isEmpty()) && (ruleAttributeClassNames.isEmpty()) && (xpathExpressions.isEmpty()) ) {
            throw new RuntimeException("Match xpath expression not specified (should be parse-time exception...)");
        }

        List<WorkflowAttribute> attributes = new ArrayList<WorkflowAttribute>();
        for (String attributeName : ruleAttributeNames) {
            attributes.add(getRuleAttributeByName(attributeName));
        }
        for (String attributeClassName : ruleAttributeClassNames) {
            attributes.add(getRuleAttributeByClassName(attributeClassName));
        }
        
        // at this point if we have no xpath expressions or attributes we cannot match
        if (attributes.isEmpty() && xpathExpressions.isEmpty()) {
            return actionRequests;
        }
        
        Boolean match = Boolean.TRUE;
        if (!xpathExpressions.isEmpty()) {
            XPath xpath = XPathHelper.newXPath();
            for (String xpathExpression : xpathExpressions) {
                match &= (Boolean) xpath.evaluate(xpathExpression, context.getDocumentContent().getDocument(), XPathConstants.BOOLEAN);
            }
        }
        for (WorkflowAttribute workflowAttribute : attributes) {
            // no rule extensions to pass in below because we have no rule... simple attribute matching only
            match &= workflowAttribute.isMatch(context.getDocumentContent(), new ArrayList<RuleExtension>());
        }
        
        if (match.booleanValue()) {
//            LOG.debug("Expression '" + xpathExpression + "' matched document '" + context.getDocumentContent().getDocContent() + "'");
        } else {
            // return an empty list because we didn't find a match using the given xpath
//            LOG.debug("Expression '" + xpathExpression + "' did NOT match document '" + context.getDocumentContent().getDocContent() + "'");
            return actionRequests;
        }

        List<org.kuali.rice.kew.api.rule.RuleResponsibility> responsibilities = new ArrayList<org.kuali.rice.kew.api.rule.RuleResponsibility>();
        RuleXmlParser parser = new RuleXmlParser();
        ActionRequestFactory arf = new ActionRequestFactory(context.getDocument(), currentNode);
        // this rule is only used to obtain description, forceAction flag, and the rulebasevalues id, which may be null
        RuleBaseValues fakeRule = new RuleBaseValues();
        fakeRule.setName("fakeRule");
        fakeRule.setActive(Boolean.TRUE);
        fakeRule.setCurrentInd(Boolean.TRUE);
        fakeRule.setDescription("a fake rule");
        fakeRule.setForceAction(Boolean.TRUE);
        fakeRule.setId(null);

        for (Object o: root.getChildren("responsibility", XmlConstants.RULE_NAMESPACE)) {
            Element e = (Element) o;
            RuleResponsibilityBo responsibility = parser.parseResponsibility(e, fakeRule);
            responsibility.setResponsibilityId(KEWConstants.MACHINE_GENERATED_RESPONSIBILITY_ID);
            responsibilities.add(org.kuali.rice.kew.api.rule.RuleResponsibility.Builder.create(responsibility).build());
        }
        if (responsibilities.isEmpty()) {
            throw new RuntimeException("No responsibilities found on node " + currentNode.getName());
        }

        makeActionRequests(arf, responsibilities, context, RuleBaseValues.to(fakeRule), context.getDocument(), null, null);
        actionRequests.addAll(arf.getRequestGraphs());
        return actionRequests;
    }
    
    @Override
    public String toString() {
        return "InlineRequestsRouteModule";
    }

    private WorkflowAttribute getRuleAttributeByName(String ruleAttributeName) {
        return materializeRuleAttribute(KEWServiceLocator.getRuleAttributeService().findByName(ruleAttributeName));
    }
    
    private WorkflowAttribute getRuleAttributeByClassName(String ruleAttributeClassName) {
        return materializeRuleAttribute(KEWServiceLocator.getRuleAttributeService().findByClassName(ruleAttributeClassName));
    }
    
    private WorkflowAttribute materializeRuleAttribute(RuleAttribute ruleAttribute) {
        if (ruleAttribute != null) {
            if (KEWConstants.RULE_ATTRIBUTE_TYPE.equals(ruleAttribute.getType())) {
                ObjectDefinition objDef = new ObjectDefinition(ruleAttribute.getResourceDescriptor(), ruleAttribute.getApplicationId());
                return (WorkflowAttribute) GlobalResourceLoader.getObject(objDef);
            } else if (KEWConstants.RULE_XML_ATTRIBUTE_TYPE.equals(ruleAttribute.getType())) {
                ObjectDefinition objDef = new ObjectDefinition(ruleAttribute.getResourceDescriptor(), ruleAttribute.getApplicationId());
                WorkflowAttribute workflowAttribute = (WorkflowAttribute) GlobalResourceLoader.getObject(objDef);
                //required to make it work because ruleAttribute XML is required to construct custom columns
                ((GenericXMLRuleAttribute) workflowAttribute).setRuleAttribute(ruleAttribute);
                return workflowAttribute;
            }
        }
        return null;
    }
    
}
