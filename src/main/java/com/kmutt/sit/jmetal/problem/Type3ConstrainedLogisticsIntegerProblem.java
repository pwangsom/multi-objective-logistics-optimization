package com.kmutt.sit.jmetal.problem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public class Type3ConstrainedLogisticsIntegerProblem extends AbstractConstrainedLogisticsIntegerProblem {
	
	private Logger logger = LoggerFactory.getLogger(Type3ConstrainedLogisticsIntegerProblem.class);

	public Type3ConstrainedLogisticsIntegerProblem(NsgaIIIHelper helper) {	
		super(helper);
	    setNumberOfConstraints(2);
		setName("Type2ConstrainedLogisticsIntegerProblem");
	}

	@Override
	public void evaluateConstraints(IntegerSolution solution) {
		// TODO Auto-generated method stub
		logger.debug("");
		logger.debug("Start: Evaluate Constraints: -> " + utilizationType1 + ", " + familiarityType1);
		
	    double[] constraint = new double[this.getNumberOfConstraints()];

	    constraint[0] = utilizationType1;
	    constraint[1] = familiarityType1;

	    double overallConstraintViolation = 0.0;	    
	    int violatedConstraints = 0;
	    
		for (int i = 0; i < getNumberOfConstraints(); i++) {
			if (constraint[i] < 0.0) {
				overallConstraintViolation += constraint[i];
				violatedConstraints++;
			}
		}

	    overallConstraintViolationDegree.setAttribute(solution, overallConstraintViolation);
	    numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
	    
		utilizationType1 = 0.0;
		utilizationType2 = 0.0;
		familiarityType1 = 0.0;
	    
		logger.debug("");
		logger.debug("Finished: Evaluate Constraints");		
		
	}
	
}
