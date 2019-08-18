package com.kmutt.sit.jmetal.problem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.solutionattribute.SolutionAttribute;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.utilities.JavaUtils;

@SuppressWarnings("serial")
public class GenericLogisticsIntegerProblem extends AbstractIntegerProblem implements SolutionAttribute<IntegerSolution, ExtremeSolution> {
	
	private Logger logger = LoggerFactory.getLogger(GenericLogisticsIntegerProblem.class);
	protected NsgaIIIHelper helper;
	
	final protected int NO_OBJECTIVES = 3;
	final protected int NO_EXTREME_SOLUTIONS = 4;
	
	public GenericLogisticsIntegerProblem(NsgaIIIHelper helper) {
		
		setNumberOfObjectives(NO_OBJECTIVES);
		setName("LogisticsIntegerProblem");
		
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
		
	}

	@Override
	public void evaluate(IntegerSolution solution) {
		// TODO Auto-generated method stub
		logger.debug("");
		logger.debug("Start: Evaluate");
		
		GeneratedSolutionEvaluator evaluator = new GeneratedSolutionEvaluator(solution, helper);
		evaluator.evaluate();
		
		solution.setObjective(0, evaluator.getNoOfCar());
		solution.setObjective(1, evaluator.getUtilization() * -1);
		solution.setObjective(2, evaluator.getFamiliarity() * -1);
		
		displayExtremeSolutions(solution);
		
		logger.debug(String.format("[No.Of Cars: %.0f, Utilization: %.4f, Fammilarity: %.4f]", 
				 solution.getObjective(0), solution.getObjective(1), solution.getObjective(2)));
		
		logger.debug("");
		logger.debug("Finished: Evaluate");
	}
	
	protected void displayExtremeSolutions(IntegerSolution solution) {
	    if(!JavaUtils.isNull(getAttribute(solution)) && getAttribute(solution).getExtremeId() < NO_EXTREME_SOLUTIONS) {
			logger.info(String.format("[Extreme: %d, No.Of Cars: %.0f, Utilization: %.4f, Fammilarity: %.4f]", 
					getAttribute(solution).getExtremeId(), solution.getObjective(0), solution.getObjective(1), solution.getObjective(2)));
			logger.debug("");
	    }
	}

	@Override
	public void setAttribute(IntegerSolution solution, ExtremeSolution value) {
		// TODO Auto-generated method stub
		solution.setAttribute(getAttributeIdentifier(), value);
	}

	@Override
	public ExtremeSolution getAttribute(IntegerSolution solution) {
		// TODO Auto-generated method stub
		return (ExtremeSolution) solution.getAttribute(getAttributeIdentifier());
	}

	@Override
	public Object getAttributeIdentifier() {
		// TODO Auto-generated method stub
		return this.getClass();
	}

}
