/*
 * Copyright 2011 The Kuali Foundation Licensed under the Educational Community
 * License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/ecl1.php Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.kuali.rice.krad.uif.service;

import java.util.Map;

/**
 * Provides evaluation of expression language statements against a given context
 *
 * <p>
 * Used within the UI framework to allow conditional logic to be configured through
 * the XML which can alter the values of component properties
 * </p>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface ExpressionEvaluatorService {

    /**
     * Evaluates any el expressions that are found as a string property value
     * for the object
     * 
     * <p>
     * Using reflection the properties for the object are retrieved and if of
     * <code>String</code> type the corresponding value is retrieved. If the
     * value is not empty and contains the el placeholder see
     * {@link #containsElPlaceholder(String)} then the expression is evaluated
     * using the given context object and parameters. The evaluated string is
     * then set as the new property value, or in the case of a template
     * (expression contained within a literal string), the expression part is
     * replaced in the property value.
     * </p>
     * 
     * <p>
     * In addition to evaluating any property expressions, any configured
     * <code>PropertyReplacer</code> for the object are also evaluated and if a
     * match occurs those property replacements are made
     * </p>
     * 
     * @param object
     *            - object whose properties should be checked for expressions
     *            and evaluated
     * @param contextObject
     *            - context object for the expression evaluations
     * @param evaluationParameters
     *            - map of parameters that may appear in expressions, the map
     *            key gives the parameter name that may appear in the
     *            expression, and the map value is the object that expression
     *            should evaluate against when that name is found
     */
    public void evaluateObjectExpressions(Object object, Object contextObject, Map<String, Object> evaluationParameters);

    /**
     * Evaluates the given expression template string against the context object
     * and map of parameters
     * 
     * <p>
     * If the template string contains one or more el placeholders (see
     * {@link #containsElPlaceholder(String)}), the expression contained within
     * the placeholder will be evaluated and the corresponding value will be
     * substituted back into the property value where the placeholder occurred.
     * If no placeholders are found, the string will be returned unchanged
     * </p>
     * 
     * @param contextObject
     *            - context object for the expression evaluations
     * @param evaluationParameters
     *            - map of parameters that may appear in expressions, the map
     *            key gives the parameter name that may appear in the
     *            expression, and the map value is the object that expression
     *            should evaluate against when that name is found
     * @param expressionTemplate
     *            - string that should be evaluated for el expressions
     * @return String formed by replacing any el expressions in the original
     *         expression template with their corresponding evaluation results
     */
    public String evaluateExpressionTemplate(Object contextObject, Map<String, Object> evaluationParameters,
            String expressionTemplate);

    /**
     * Evaluates the given el expression against the content object and
     * parameters, and returns the result of the evaluation
     * 
     * <p>
     * The given expression string is assumed to be one el expression and should
     * not contain the el placeholders. The returned result depends on the
     * evaluation and what type is returns, for instance a boolean will be
     * return for a boolean expression, or a string for string expression
     * </p>
     * 
     * @param contextObject
     *            - context object for the expression evaluations
     * @param evaluationParameters
     *            - map of parameters that may appear in expressions, the map
     *            key gives the parameter name that may appear in the
     *            expression, and the map value is the object that expression
     *            should evaluate against when that name is found
     * @param expression
     *            - el expression to evaluate
     * @return Object result of the expression evaluation
     */
    public Object evaluateExpression(Object contextObject, Map<String, Object> evaluationParameters, String expression);

    /**
     * Indicates whether or not the given string contains the el placholder
     * (begin and end delimiters)
     * 
     * @param value
     *            - String to check for contained placeholders
     * @return boolean true if the string contains one or more placeholders,
     *         false if it contains none
     * @see org.kuali.rice.krad.uif.UifConstants.EL_PLACEHOLDER_PREFIX
     * @see org.kuali.rice.krad.uif.UifConstants.EL_PLACEHOLDER_SUFFIX
     */
    public boolean containsElPlaceholder(String value);
}
