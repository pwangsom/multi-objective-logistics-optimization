package com.kmutt.sit.jmetal.problem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

public class LogisticsIntegerConstrainedProblemType1 extends AbstractIntegerProblem implements ConstrainedProblem<IntegerSolution> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OverallConstraintViolation<IntegerSolution> overallConstraintViolationDegree;
	public NumberOfViolatedConstraints<IntegerSolution> numberOfViolatedConstraints;
	
	private Logger logger = LoggerFactory.getLogger(LogisticsIntegerConstrainedProblemType1.class);
	private NsgaIIIHelper helper;
	
	final private int NO_OBJECTIVES = 3;
	final private int NO_CONSTRAINTS = 1;
	
	private double utilizationConstraintViolation = 0.0;

	public LogisticsIntegerConstrainedProblemType1(NsgaIIIHelper helper) {
		
		setNumberOfObjectives(NO_OBJECTIVES);
	    setNumberOfConstraints(NO_CONSTRAINTS);
		setName("LogisticsIntegerConstrainedProblem");
		
		this.helper = helper;
		
		// Length of chromosome is equal to a number of shipments
		setNumberOfVariables(this.helper.getShipmentList().size());

		List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
		List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());
		
		// Values in chromosome can be ranging from 0 to number of routes - 1
		int lower = 1;
		int upper = this.helper.getRouteList().size();

		for (int i = 0; i < getNumberOfVariables(); i++) {
			lowerLimit.add(lower);
			upperLimit.add(upper);
		}

		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);
		
	    overallConstraintViolationDegree = new OverallConstraintViolation<IntegerSolution>();
	    numberOfViolatedConstraints = new NumberOfViolatedConstraints<IntegerSolution>();
	}

	@Override
	public void evaluate(IntegerSolution solution) {
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
		
		logger.debug(String.format("[No.Of Cars: %d, Utilization: %.4f, Fammilarity: %.4f, Constraints Value: %.2f]", 
				evaluator.getNoOfCar(), evaluator.getUtilization(), evaluator.getFamiliarity(), utilizationConstraintViolation));
		
		logger.debug("");
		logger.debug("Finished: Evaluate");
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
