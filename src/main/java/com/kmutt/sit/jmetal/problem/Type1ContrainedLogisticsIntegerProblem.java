package com.kmutt.sit.jmetal.problem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public class Type1ContrainedLogisticsIntegerProblem extends AbstractConstrainedLogisticsIntegerProblem {
	
	private Logger logger = LoggerFactory.getLogger(Type1ContrainedLogisticsIntegerProblem.class);

	public Type1ContrainedLogisticsIntegerProblem(NsgaIIIHelper helper) {
		super(helper);
	    setNumberOfConstraints(2);
		setName("Type1ContrainedLogisticsIntegerProblem");
	}

	@Override
	public void evaluateConstraints(IntegerSolution solution) {
		// TODO Auto-generated method stub
		logger.debug("");
		logger.debug("Start: Evaluate Constraints: -> " + utilizationConstraintViolation);
		
	    double[] constraint = new double[this.getNumberOfConstraints()];

	    constraint[0] = utilizationConstraintViolation;

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
	    	    
	    utilizationConstraintViolation = 0.0;
	    	    
		logger.debug("");
		logger.debug("Finished: Evaluate Constraints");		
		
	}
}
