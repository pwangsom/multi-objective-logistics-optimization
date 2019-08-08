package com.kmutt.sit.jmetal.algorithm;

import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.IntegerSolution;

public class GeneralNsgaIIIIntegerSolutionBuilder {
	
	private NSGAIIIBuilder<IntegerSolution> builder;

	public GeneralNsgaIIIIntegerSolutionBuilder(Problem<IntegerSolution> problem, CrossoverOperator<IntegerSolution> crossover, MutationOperator<IntegerSolution> mutation,
			SelectionOperator<List<IntegerSolution>, IntegerSolution> selection, int maxIterations) {
		
		builder = new NSGAIIIBuilder<IntegerSolution>(problem)
				.setCrossoverOperator(crossover)
				.setMutationOperator(mutation)
				.setSelectionOperator(selection)
				.setMaxIterations(maxIterations);
	}
	
	public NsgaIIIIntegerSolution buildNsgaIIIIntegerSolution() {
		
		NsgaIIIIntegerSolution algorithm = new NsgaIIIIntegerSolution(builder) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};
		
		return algorithm;
	}
	
	public ModifiedNsgaIIIIntegerSolution buildModifiedNsgaIIIIntegerSolution() {
		
		ModifiedNsgaIIIIntegerSolution algorithm = new ModifiedNsgaIIIIntegerSolution(builder) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};
		
		return algorithm;
	}
}
