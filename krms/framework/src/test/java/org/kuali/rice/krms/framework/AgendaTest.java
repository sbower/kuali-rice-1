package org.kuali.rice.krms.framework;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.krms.api.engine.EngineResults;
import org.kuali.rice.krms.api.engine.ExecutionOptions;
import org.kuali.rice.krms.api.engine.ExecutionFlag;
import org.kuali.rice.krms.api.engine.SelectionCriteria;
import org.kuali.rice.krms.api.engine.Term;
import org.kuali.rice.krms.api.engine.TermResolver;
import org.kuali.rice.krms.api.engine.TermSpecification;
import org.kuali.rice.krms.framework.engine.Action;
import org.kuali.rice.krms.framework.engine.Agenda;
import org.kuali.rice.krms.framework.engine.AgendaTreeEntry;
import org.kuali.rice.krms.framework.engine.BasicAgenda;
import org.kuali.rice.krms.framework.engine.BasicAgendaTree;
import org.kuali.rice.krms.framework.engine.BasicAgendaTreeEntry;
import org.kuali.rice.krms.framework.engine.BasicContext;
import org.kuali.rice.krms.framework.engine.BasicRule;
import org.kuali.rice.krms.framework.engine.ComparableTermBasedProposition;
import org.kuali.rice.krms.framework.engine.ComparisonOperator;
import org.kuali.rice.krms.framework.engine.Context;
import org.kuali.rice.krms.framework.engine.ContextProvider;
import org.kuali.rice.krms.framework.engine.Proposition;
import org.kuali.rice.krms.framework.engine.ProviderBasedEngine;
import org.kuali.rice.krms.framework.engine.ResultLogger;
import org.kuali.rice.krms.framework.engine.Rule;
import org.kuali.rice.krms.framework.engine.TermResolutionEngineImpl;

public class AgendaTest {
	private static final ResultLogger LOG = ResultLogger.getInstance();

	@Before
	public void setUp() {
		ActionMock.resetActionsFired();
	}

	// totalCostTerm will resolve to the Integer value 5
	private Proposition trueProp = new ComparableTermBasedProposition(ComparisonOperator.GREATER_THAN, totalCostTerm, Integer.valueOf(1));
	private Proposition falseProp = new ComparableTermBasedProposition(ComparisonOperator.GREATER_THAN, totalCostTerm, Integer.valueOf(1000));
	
	@Test
	public void testAllRulesAgenda() {

		Rule rule1 = new BasicRule("r1", trueProp, Collections.<Action>singletonList(new ActionMock("a1")));
		Rule rule2 = new BasicRule("r2", falseProp, Collections.<Action>singletonList(new ActionMock("a2")));
		Rule rule3 = new BasicRule("r3", trueProp, Collections.<Action>singletonList(new ActionMock("a3")));
		
		AgendaTreeEntry entry1 = new BasicAgendaTreeEntry(rule1);
		AgendaTreeEntry entry2 = new BasicAgendaTreeEntry(rule2);
		AgendaTreeEntry entry3 = new BasicAgendaTreeEntry(rule3);
		BasicAgendaTree agendaTree = new BasicAgendaTree(entry1, entry2, entry3); 
		Agenda agenda = new BasicAgenda("test", new HashMap<String, String>(), agendaTree);
		
		execute(agenda);

		assertTrue(ActionMock.actionFired("a1"));
		assertFalse(ActionMock.actionFired("a2"));
		assertTrue(ActionMock.actionFired("a3"));
	}
	
	@Test
	public void testIfTrueSubAgenda() {

		Rule rule1 = new BasicRule("r1", trueProp, Collections.<Action>singletonList(new ActionMock("a1")));
		Rule rule2 = new BasicRule("r2", falseProp, Collections.<Action>singletonList(new ActionMock("a2")));
		Rule subRule1 = new BasicRule("r1s1", trueProp, Collections.<Action>singletonList(new ActionMock("a3")));
		
		BasicAgendaTree subAgendaTree1 = new BasicAgendaTree(new BasicAgendaTreeEntry(subRule1));
		BasicAgendaTree agendaTree1 = new BasicAgendaTree(new BasicAgendaTreeEntry(rule1, subAgendaTree1, null)); 
		Agenda agenda1 = new BasicAgenda("test", new HashMap<String, String>(), agendaTree1);
		
		execute(agenda1);

		assertTrue(ActionMock.actionFired("a1"));
		assertTrue(ActionMock.actionFired("a3"));
		
		// RESET
		ActionMock.resetActionsFired();
		
		BasicAgendaTree subAgendaTree2 = new BasicAgendaTree(new BasicAgendaTreeEntry(subRule1));
		BasicAgendaTree agendaTree2 = new BasicAgendaTree(new BasicAgendaTreeEntry(rule2, subAgendaTree2, null)); 
		Agenda agenda2 = new BasicAgenda("test", new HashMap<String, String>(), agendaTree2);
		
		execute(agenda2);

		assertFalse(ActionMock.actionFired("a2"));
		assertFalse(ActionMock.actionFired("a3"));
	}

	@Test
	public void testIfFalseSubAgenda() {

		Rule rule1 = new BasicRule("r1", trueProp, Collections.<Action>singletonList(new ActionMock("a1")));
		Rule rule2 = new BasicRule("r2", falseProp, Collections.<Action>singletonList(new ActionMock("a2")));
		Rule subRule1 = new BasicRule("r1s1", trueProp, Collections.<Action>singletonList(new ActionMock("a3")));
		
		BasicAgendaTree subAgendaTree1 = new BasicAgendaTree(new BasicAgendaTreeEntry(subRule1));
		BasicAgendaTree agendaTree1 = new BasicAgendaTree(new BasicAgendaTreeEntry(rule1, null, subAgendaTree1)); 
		Agenda agenda1 = new BasicAgenda("test", new HashMap<String, String>(), agendaTree1);
		
		execute(agenda1);

		assertTrue(ActionMock.actionFired("a1"));
		assertFalse(ActionMock.actionFired("a3"));
		
		// RESET
		ActionMock.resetActionsFired();
		
		BasicAgendaTree subAgendaTree2 = new BasicAgendaTree(new BasicAgendaTreeEntry(subRule1));
		BasicAgendaTree agendaTree2 = new BasicAgendaTree(new BasicAgendaTreeEntry(rule2, null, subAgendaTree2)); 
		Agenda agenda2 = new BasicAgenda("test", new HashMap<String, String>(), agendaTree2);
		
		execute(agenda2);

		assertFalse(ActionMock.actionFired("a2"));
		assertTrue(ActionMock.actionFired("a3"));
	}
	
	@Test
	public void testAfterAgenda() {

		Rule rule1 = new BasicRule("r1", trueProp, Collections.<Action>singletonList(new ActionMock("a1")));
		Rule rule2 = new BasicRule("r2", falseProp, Collections.<Action>singletonList(new ActionMock("a2")));
		Rule subRule1 = new BasicRule("r1s1", trueProp, Collections.<Action>singletonList(new ActionMock("a3")));
		
		BasicAgendaTree agendaTree1 = new BasicAgendaTree(new BasicAgendaTreeEntry(rule1), new BasicAgendaTreeEntry(subRule1)); 
		Agenda agenda1 = new BasicAgenda("test", new HashMap<String, String>(), agendaTree1);
		
		execute(agenda1);

		assertTrue(ActionMock.actionFired("a1"));
		assertTrue(ActionMock.actionFired("a3"));
		
		// RESET
		ActionMock.resetActionsFired();
		
		BasicAgendaTree agendaTree2 = new BasicAgendaTree(new BasicAgendaTreeEntry(rule2), new BasicAgendaTreeEntry(subRule1)); 
		Agenda agenda2 = new BasicAgenda("test", new HashMap<String, String>(), agendaTree2);
		
		execute(agenda2);

		assertFalse(ActionMock.actionFired("a2"));
		assertTrue(ActionMock.actionFired("a3"));
	}	
	
	/**
	 * @param agenda
	 */
	private void execute(Agenda agenda) {
		Map<String, String> contextQualifiers = new HashMap<String, String>();
		contextQualifiers.put("docTypeName", "Proposal");
		
		List<TermResolver<?>> testResolvers = new ArrayList<TermResolver<?>>();
		testResolvers.add(testResolver);
		
		Context context = new BasicContext(Arrays.asList(agenda), testResolvers);
		ContextProvider contextProvider = new ManualContextProvider(context);
		
		SelectionCriteria selectionCriteria = SelectionCriteria.createCriteria("test", null, contextQualifiers, Collections.EMPTY_MAP);
		
		ProviderBasedEngine engine = new ProviderBasedEngine();
		engine.setContextProvider(contextProvider);
		engine.setTermResolutionEngine(new TermResolutionEngineImpl());
		
		// Set execution options to log execution
		ExecutionOptions executionOptions = new ExecutionOptions().setFlag(ExecutionFlag.LOG_EXECUTION, true);
		
		LOG.init();
		EngineResults results = engine.execute(selectionCriteria, new HashMap<Term, Object>(), executionOptions);
		assertNotNull(results);
	}
	
	private static final Term totalCostTerm = new Term(new TermSpecification("totalCost","Integer"));
	
	private static final TermResolver<Integer> testResolver = new TermResolverMock<Integer>(totalCostTerm.getSpecification(), 10); 
}
