package com.kmutt.sit.jmetal.problem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.solutionattribute.SolutionAttribute;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public class ExtraLogisticsIntegerProblem extends AbstractIntegerProblem implements ConstrainedProblem<IntegerSolution>, SolutionAttribute<IntegerSolution, Integer> {


	private Logger logger = LoggerFactory.getLogger(ExtraLogisticsIntegerProblem.class);	
	protected NsgaIIIHelper helper;
	
	final protected int NO_OBJECTIVES = 3;
	final protected int NO_CONSTRAINTS = 1;
	
	public ExtraLogisticsIntegerProblem(NsgaIIIHelper helper) {
		
		setNumberOfObjectives(NO_OBJECTIVES);
	    setNumberOfConstraints(NO_CONSTRAINTS);
		setName("GenericLogisticsIntegerProblem");
		
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
		
	}

	@Override
	public void setAttribute(IntegerSolution solution, Integer value) {
		// TODO Auto-generated method stub
		solution.setAttribute(getAttributeIdentifier(), value);
	}

	@Override
	public Integer getAttribute(IntegerSolution solution) {
		// TODO Auto-generated method stub
		return (Integer) solution.getAttribute(getAttributeIdentifier());
	}

	@Override
	public Object getAttributeIdentifier() {
		// TODO Auto-generated method stub
		return this.getClass();
	}

	@Override
	public void evaluateConstraints(IntegerSolution solution) {
		// TODO Auto-generated method stub
		
	}

}
