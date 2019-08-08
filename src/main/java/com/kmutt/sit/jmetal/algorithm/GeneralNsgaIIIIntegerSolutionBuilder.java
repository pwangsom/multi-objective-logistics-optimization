package com.kmutt.sit.jmetal.algorithm;

import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

public class GeneralNsgaIIIIntegerSolutionBuilder {
	
	private NSGAIIIBuilder<IntegerSolution> builder;
	private NsgaIIIHelper helper;

	public GeneralNsgaIIIIntegerSolutionBuilder(Problem<IntegerSolution> problem, CrossoverOperator<IntegerSolution> crossover, MutationOperator<IntegerSolution> mutation,
			SelectionOperator<List<IntegerSolution>, IntegerSolution> selection, int maxIterations, NsgaIIIHelper helper) {
		
		builder = new NSGAIIIBuilder<IntegerSolution>(problem)
				.setCrossoverOperator(crossover)
				.setMutationOperator(mutation)
				.setSelectionOperator(selection)
				.setMaxIterations(maxIterations);
		
		this.helper = helper;
	}
	
	public NsgaIIIIntegerSolution buildNsgaIIIIntegerSolution() {
		
		NsgaIIIIntegerSolution algorithm = new NsgaIIIIntegerSolution(builder, helper) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};
		
		return algorithm;
	}
	
	public ModifiedNsgaIIIIntegerSolution buildModifiedNsgaIIIIntegerSolution() {
		
		ModifiedNsgaIIIIntegerSolution algorithm = new ModifiedNsgaIIIIntegerSolution(builder, helper) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};
		
		return algorithm;
	}
}
