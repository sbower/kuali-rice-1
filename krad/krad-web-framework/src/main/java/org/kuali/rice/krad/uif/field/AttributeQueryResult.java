package org.kuali.rice.krad.uif.field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object that is returned for Ajax attribute queries and exposed
 * as JSON
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class AttributeQueryResult implements Serializable {
    private static final long serialVersionUID = -6688384365943881516L;

    private String resultMessage;
    private String resultMessageStyleClasses;

    private Map<String, String> resultFieldData;
    private List<String> resultData;

    public AttributeQueryResult() {
        resultFieldData = new HashMap<String, String>();
        resultData = new ArrayList<String>();
    }

    /**
     * Message text that should display (if non empty) with the results.
     * Can be used to given messages such as data not found
     *
     * @return String text to display with results
     */
    public String getResultMessage() {
        return resultMessage;
    }

    /**
     * Setter for the result message text
     *
     * @param resultMessage
     */
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    /**
     * CSS Style classes that should be applied to the result message text
     *
     * @return String of CSS style classes
     */
    public String getResultMessageStyleClasses() {
        return resultMessageStyleClasses;
    }

    /**
     * Setter for the CSS style classes to use for the return message
     *
     * @param resultMessageStyleClasses
     */
    public void setResultMessageStyleClasses(String resultMessageStyleClasses) {
        this.resultMessageStyleClasses = resultMessageStyleClasses;
    }

    /**
     * Returns data for multiple fields as a Map where key is the field
     * name and map value is the field value
     *
     * @return Map<String, String> result field data
     */
    public Map<String, String> getResultFieldData() {
        return resultFieldData;
    }

    /**
     * Setter for the map field data
     *
     * @param resultFieldData
     */
    public void setResultFieldData(Map<String, String> resultFieldData) {
        this.resultFieldData = resultFieldData;
    }

    /**
     * Result data as a List of string objects for queries that
     * return single field multiple values
     *
     * @return List<String> result data
     */
    public List<String> getResultData() {
        return resultData;
    }

    /**
     * Setter for the attribute query result data
     *
     * @param resultData
     */
    public void setResultData(List<String> resultData) {
        this.resultData = resultData;
    }
}
