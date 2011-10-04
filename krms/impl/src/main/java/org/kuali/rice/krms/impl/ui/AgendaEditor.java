/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krms.impl.ui;

import org.kuali.rice.core.api.util.tree.Node;
import org.kuali.rice.core.api.util.tree.Tree;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krms.impl.repository.AgendaBo;
import org.kuali.rice.krms.impl.repository.AgendaItemBo;
import org.kuali.rice.krms.impl.repository.ContextBo;

import java.util.List;

/**
 * synthetic (not directly persisted) BO for the KRMS agenda editor
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class AgendaEditor extends PersistableBusinessObjectBase {
	
	private static final long serialVersionUID = 1L;
	
	private ContextBo context;
	private AgendaBo agenda;
	private AgendaItemBo agendaItemLine;
    private String selectedAgendaItemId;
    private String cutAgendaItemId;
    private String selectedPropositionId;
    private String copyRuleName;

    public AgendaEditor() {
        agenda = new AgendaBo();
        // ToDo: Determine proper default values of agenda's typeId
        agenda.setTypeId("");
        agendaItemLine = new AgendaItemBo();
    }

	private void addAgendaItemAndChildren(Node<AgendaTreeNode, String> parent, AgendaItemBo agendaItem) {
	    Node<AgendaTreeNode, String> child = new Node<AgendaTreeNode, String>();
	    child.setNodeLabel(agendaItem.getRuleText());
	    child.setNodeType("agendaNode ruleNode");
	    child.setData(new AgendaTreeRuleNode(agendaItem));
	    parent.getChildren().add(child);
	    
	    // deal with peer nodes:
	    if (agendaItem.getAlways() != null) addAgendaItemAndChildren(parent, agendaItem.getAlways());
	    
	    // deal with child nodes:
	    if (agendaItem.getWhenTrue() != null) {
	        Node<AgendaTreeNode, String> whenTrue = new Node<AgendaTreeNode, String>();
	        whenTrue.setNodeLabel("When TRUE");
	        whenTrue.setNodeType("agendaNode logicNode whenTrueNode");
	        whenTrue.setData(new AgendaTreeLogicNode());
	        child.getChildren().add(whenTrue);
	        
	        addAgendaItemAndChildren(whenTrue, agendaItem.getWhenTrue());
	    }
        if (agendaItem.getWhenFalse() != null) {
            Node<AgendaTreeNode, String> whenFalse = new Node<AgendaTreeNode, String>();
            whenFalse.setNodeLabel("When FALSE");
            whenFalse.setNodeType("agendaNode logicNode whenFalseNode");
            whenFalse.setData(new AgendaTreeLogicNode());
            child.getChildren().add(whenFalse);
            
            addAgendaItemAndChildren(whenFalse, agendaItem.getWhenFalse());
        }
	}
	
    public Tree<? extends AgendaTreeNode, String> getAgendaRuleTree() {
        Tree<AgendaTreeNode, String> agendaTree = new Tree<AgendaTreeNode, String>();

        Node<AgendaTreeNode, String> rootNode = new Node<AgendaTreeNode, String>();
        agendaTree.setRootElement(rootNode);
        
        if (agenda != null) {
            String firstItemId = agenda.getFirstItemId();
            List<AgendaItemBo> items = agenda.getItems();
            AgendaItemBo firstItem = null;

            if (items != null && firstItemId != null) {
                for (AgendaItemBo item : items) {
                    if (firstItemId.equals(item.getId())) {
                        firstItem = item;
                        break;
                    }
                }
            }

            if (firstItem != null) addAgendaItemAndChildren(rootNode, firstItem);
        } 
        
        return agendaTree;
    }	
	
	/**
     * @return the agendaItemLine
     */
    public AgendaItemBo getAgendaItemLine() {
        return this.agendaItemLine;
    }

    /**
     * @param agendaItemLine the agendaItemLine to set
     */
    public void setAgendaItemLine(AgendaItemBo agendaItemLine) {
        this.agendaItemLine = agendaItemLine;
    }

    /**
     * @return the selectedAgendaItemId
     */
    public String getSelectedAgendaItemId() {
        return this.selectedAgendaItemId;
    }

    /**
     * @param selectedAgendaItemId the selectedAgendaItemId to set
     */
    public void setSelectedAgendaItemId(String selectedAgendaItemId) {
        this.selectedAgendaItemId = selectedAgendaItemId;
    }

    /**
     * @return the cutAgendaItemId
     */
    public String getCutAgendaItemId() {
        return this.cutAgendaItemId;
    }

    /**
     * @param cutAgendaItemId the cutAgendaItemId to set
     */
    public void setCutAgendaItemId(String cutAgendaItemId) {
        this.cutAgendaItemId = cutAgendaItemId;
    }

    /**
	 * @return the context
	 */
	public ContextBo getContext() {
		return this.context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(ContextBo context) {
		this.context = context;
	}

	/**
	 * @return the agenda
	 */
	public AgendaBo getAgenda() {
		return this.agenda;
	}

	/**
	 * @param agenda the agenda to set
	 */
	public void setAgenda(AgendaBo agenda) {
		this.agenda = agenda;
	}

    /**
     * @param copyRuleName the rule name from which to copy
     */
    public void setCopyRuleName(String copyRuleName) {
        this.copyRuleName = copyRuleName;
    }

    /**
     * @return the rule name from which to copy
     */
    public String getCopyRuleName() {
        return copyRuleName;
    }

    /**
     * @return the selectedPropositionId
     */
    public String getSelectedPropositionId() {
        return this.selectedPropositionId;
    }

    /**
     * @param selectedPropositionId the selectedPropositionId to set
     */
    public void setSelectedPropositionId(String selectedPropositionId) {
        this.selectedPropositionId = selectedPropositionId;
    }

    // Need to override this method since the actual persistable BO is wrapped inside dataObject.
    @Override
    public void refreshNonUpdateableReferences() {
        getPersistenceService().refreshAllNonUpdatingReferences(this.getAgenda());
    }

}
