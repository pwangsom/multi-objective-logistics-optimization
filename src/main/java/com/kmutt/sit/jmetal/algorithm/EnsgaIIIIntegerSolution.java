package com.kmutt.sit.jmetal.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public class EnsgaIIIIntegerSolution extends GenericNsgaIIIIntegerSolution {

	private Logger logger = LoggerFactory.getLogger(EnsgaIIIIntegerSolution.class);
	
	public EnsgaIIIIntegerSolution(NSGAIIIBuilder<IntegerSolution> builder, NsgaIIIHelper helper) {
		super(builder, helper);
	}
	
	@Override
	protected List<IntegerSolution> createInitialPopulation() {
		
		List<IntegerSolution> population = new ArrayList<>(getMaxPopulationSize());		
		
		population.add(getCostExtremeSolution()); // index 0
		population.add(getMakespanExtremeSolution()); // index 1
		population.add(getThirdExtremeSolution()); // index 2

		int startIdx = 3;
		
		for (int i = startIdx; i < getMaxPopulationSize(); i++) {
			IntegerSolution newIndividual = getProblem().createSolution();
			population.add(newIndividual);
		}
		
		return population;
	}
	
	protected IntegerSolution getCostExtremeSolution() {
		IntegerSolution extreme = getProblem().createSolution();
		Integer value = this.config.getProviderModel().getCostExtremeType();
		
		for(int i = 0; i < getProblem().getNumberOfVariables(); i++) {
			extreme.setVariableValue(i, value);
		}
		
		return extreme;
	}
	
	protected IntegerSolution getMakespanExtremeSolution() {
		IntegerSolution extreme = getProblem().createSolution();
		Integer value = this.config.getProviderModel().getMakespanExtremeType();
		
		for(int i = 0; i < extreme.getNumberOfVariables(); i++) {
			extreme.setVariableValue(i, value);
			value += this.config.getProviderModel().getNoOfTypes();
		}
		
		return extreme;
	}
	
	protected IntegerSolution getThirdExtremeSolution() {
		// TODO Auto-generated method stub
		IntegerSolution extreme = getProblem().createSolution();
		JMetalRandom randomGenerator = JMetalRandom.getInstance();
		Integer value = randomGenerator.nextInt(extreme.getLowerBound(0), extreme.getUpperBound(0));
				
		for(int i = 0; i < extreme.getNumberOfVariables(); i++) {
			extreme.setVariableValue(i, value);
		}
		
		return extreme;
	}
	
	@Override
	public String getName() {
		return "E-NSGA-III for Integer Solution";
	}

	@Override
	public String getDescription() {
		return "Extreme Nondominated Sorting Genetic Algorithm version III for Integer Solution";
	}
}
