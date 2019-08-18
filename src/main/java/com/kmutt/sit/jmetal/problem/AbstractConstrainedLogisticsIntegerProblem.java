package com.kmutt.sit.jmetal.problem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public abstract class AbstractConstrainedLogisticsIntegerProblem extends GenericLogisticsIntegerProblem implements ConstrainedProblem<IntegerSolution> {


	private Logger logger = LoggerFactory.getLogger(AbstractConstrainedLogisticsIntegerProblem.class);	

	protected int no_constraints = 1;	
	protected double utilizationConstraintViolation = 0.0;

	public OverallConstraintViolation<IntegerSolution> overallConstraintViolationDegree;
	public NumberOfViolatedConstraints<IntegerSolution> numberOfViolatedConstraints;
	
	public AbstractConstrainedLogisticsIntegerProblem(NsgaIIIHelper helper) {
		
		super(helper);
		
	    setNumberOfConstraints(no_constraints);
		setName("GenericLogisticsIntegerProblem");

	    overallConstraintViolationDegree = new OverallConstraintViolation<IntegerSolution>();
	    numberOfViolatedConstraints = new NumberOfViolatedConstraints<IntegerSolution>();
	}
	
	@Override
	public void evaluate(IntegerSolution solution) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		logger.debug("");
		logger.debug("Start: Evaluate");
		
		utilizationConstraintViolation = 0.0;
		
		GeneratedSolutionEvaluator evaluator = new GeneratedSolutionEvaluator(solution, helper);
		evaluator.evaluate();
		
		solution.setObjective(0, evaluator.getNoOfCar());
		solution.setObjective(1, evaluator.getUtilization() * -1);
		solution.setObjective(2, evaluator.getFamiliarity() * -1);
		
		utilizationConstraintViolation = evaluator.getUtilizationConstraintScore();
		
		logger.debug(String.format("[No.Of Cars: %.0f, Utilization: %.4f, Fammilarity: %.4f, Constraints Value: %.2f]", 
				solution.getObjective(0), solution.getObjective(1), solution.getObjective(2), utilizationConstraintViolation));
		
		logger.debug("");
		logger.debug("Finished: Evaluate");
	}
}
