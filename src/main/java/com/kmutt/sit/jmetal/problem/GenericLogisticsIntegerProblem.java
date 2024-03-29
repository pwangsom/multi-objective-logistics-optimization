package com.kmutt.sit.jmetal.problem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public class GenericLogisticsIntegerProblem extends AbstractIntegerProblem {
	
	private Logger logger = LoggerFactory.getLogger(GenericLogisticsIntegerProblem.class);
	protected NsgaIIIHelper helper;
	
	final protected int NO_OBJECTIVES = 3;
	
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
		
		logger.debug(String.format("[No.Of Cars: %.0f, Utilization: %.4f, Fammilarity: %.4f]", 
				 solution.getObjective(0), solution.getObjective(1), solution.getObjective(2)));
		
		logger.debug("");
		logger.debug("Finished: Evaluate");
	}
}
