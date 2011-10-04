package org.kuali.rice.krms.framework.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kuali.rice.krms.api.engine.ExecutionEnvironment;
import org.kuali.rice.krms.api.engine.ResultEvent;
import org.kuali.rice.krms.api.engine.ExecutionFlag;
import org.kuali.rice.krms.api.repository.LogicalOperator;
import org.kuali.rice.krms.framework.engine.result.BasicResult;

public final class CompoundProposition implements Proposition {
	
    private static final ResultLogger LOG = ResultLogger.getInstance();
    
	private final LogicalOperator logicalOperator;
	private final List<Proposition> propositions;
	
	public CompoundProposition(LogicalOperator logicalOperator, List<Proposition> propositions) {
				
		if (propositions == null || propositions.isEmpty()) {
			throw new IllegalArgumentException("Propositions must be non-null and non-empty.");
		}
		if (logicalOperator == null) {
			throw new IllegalArgumentException("Logical operator must be non-null.");
		}
		this.logicalOperator = logicalOperator;
		this.propositions = new ArrayList<Proposition>(propositions);
	}
	
	@Override
	public PropositionResult evaluate(ExecutionEnvironment environment) {
		
		PropositionResult result = evaluateInner(environment);
		
		// handle compound proposition result logging
		if (LOG.isEnabled(environment)) { 
            LOG.logResult(new BasicResult(ResultEvent.PropositionEvaluated, this, environment, result.getResult()));
        }
		
		return result;
	}

    /**
     * This method handles the evaluation logic
     * 
     * @param environment
     * @return
     */
	
    private PropositionResult evaluateInner(ExecutionEnvironment environment) {
    	
    	boolean collatedResult;
    	boolean evaluateAll = environment.getExecutionOptions().getFlag(ExecutionFlag.EVALUATE_ALL_PROPOSITIONS);
    	
        if (logicalOperator == LogicalOperator.AND) {

            collatedResult = true;

			for (Proposition proposition : propositions) {
				
				PropositionResult singleResult = proposition.evaluate(environment);
				logPropositionResult(proposition, singleResult, environment);
								
				if (!singleResult.getResult()) {
					collatedResult = false;
					if(!evaluateAll) break;
				}
			}
			
			return new PropositionResult(collatedResult);
			
		} else if (logicalOperator == LogicalOperator.OR) {
			
		    collatedResult = false;
			
			for (Proposition proposition : propositions) {
				
			    PropositionResult singleResult = proposition.evaluate(environment);
				
				logPropositionResult(proposition, singleResult, environment);
				
				if (!singleResult.getResult()) {
					collatedResult = true;
					if(!evaluateAll) break;
				}
			}
			
			return new PropositionResult(collatedResult);
		}
		throw new IllegalStateException("Invalid logical operator: " + logicalOperator);
    }
    
    
    /*
     * only log if the proposition is not compound
     * and have the compound proposition log its own result
     */
    
    public void logPropositionResult(Proposition proposition, PropositionResult propositionResult, ExecutionEnvironment environment) {
    	    	
    	if(!proposition.isCompound()) {
            LOG.logResult(new BasicResult(propositionResult.getExecutionDetails(), ResultEvent.PropositionEvaluated, proposition, environment, propositionResult.getResult()));		
    	}
    	
    }
	

    @Override
    public List<Proposition> getChildren() {
        return Collections.unmodifiableList(propositions);
    }
    
    @Override
    public boolean isCompound() {
        return true;
    }

}
