package com.kmutt.sit.jmetal.algorithm;

import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.comparator.DominanceComparator;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

public class GenericNsgaIIIntegerBuilder {
	
	private NSGAIIBuilder<IntegerSolution> builder;
	private Comparator<IntegerSolution> dominanceComparator;
	
	private NsgaIIIHelper helper;

	
	public GenericNsgaIIIntegerBuilder(Problem<IntegerSolution> problem, CrossoverOperator<IntegerSolution> crossover, MutationOperator<IntegerSolution> mutation,
			SelectionOperator<List<IntegerSolution>, IntegerSolution> selection, int maxIterations, int populationSize, NsgaIIIHelper helper) {
		
		builder = new NSGAIIBuilder<IntegerSolution>(problem, crossover, mutation)
		        .setSelectionOperator(selection)
		        .setMaxEvaluations(maxIterations)
		        .setPopulationSize(populationSize);
		
		dominanceComparator = new DominanceComparator<>();
		
		this.helper = helper;
	}
	
	public NsgaIIIntegerSolution buildNsgaIIIntegerSolution(){
		
		NsgaIIIntegerSolution algorithm = new NsgaIIIntegerSolution(builder, dominanceComparator, helper);
		
		return algorithm;
	}
}
