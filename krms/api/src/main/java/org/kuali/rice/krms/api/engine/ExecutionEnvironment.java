package org.kuali.rice.krms.api.engine;

import java.util.Map;
import java.util.Set;

/**
 * The ExecutionEnvironment manages contextual information which is made available to
 * different components of the rules engine during execution.  Facts can be retrieved
 * from and published to the environment.  It also provides a reference to the
 * {@link EngineResults} or tracking engine activity and returning values back to
 * the client of the rules engine.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public interface ExecutionEnvironment {
	
	/**
	 * Returns the selection criteria that was used to initialize the environment.
	 * 
	 * @return the selection criteria for this environment
	 */
	public SelectionCriteria getSelectionCriteria();
	
	/**
	 * Returns an immutable Map of facts available within this environment.
	 * 
	 * @return the facts in this environment
	 */
	public Map<Term, Object> getFacts();

	/**
	 * Publishes a new fact
	 * 
	 * @param factName name of the fact to publish
	 * @param factValue value of the fact to publish
	 * // TODO: we don't support updating facts, refactor this method
	 * @return true if an existing fact was updated, false if this was a new fact
	 */
	public boolean publishFact(Term factName, Object factValue);
	
	public void addTermResolver(TermResolver<?> termResolver);

	public <T> T resolveTerm(Term term, Object caller) throws TermResolutionException;
	
	public Set<Term> getTermsForCaller(Object caller);
	
	public ExecutionOptions getExecutionOptions();
	
	public EngineResults getEngineResults();
	
}
