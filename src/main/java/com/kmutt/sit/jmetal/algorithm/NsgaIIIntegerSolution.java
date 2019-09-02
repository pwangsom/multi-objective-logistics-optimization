package com.kmutt.sit.jmetal.algorithm;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public class NsgaIIIntegerSolution extends NSGAII<IntegerSolution> {	

	private Logger logger = LoggerFactory.getLogger(NsgaIIIntegerSolution.class);
	protected NsgaIIIHelper helper;

	
	public NsgaIIIntegerSolution(NSGAIIBuilder<IntegerSolution> builder, Comparator<IntegerSolution> dominanceComparator, NsgaIIIHelper helper) {
		
		this(builder.getProblem(), builder.getMaxIterations(), builder.getPopulationSize(),
			 builder.getCrossoverOperator(), builder.getMutationOperator(), builder.getSelectionOperator(),
			 dominanceComparator, builder.getSolutionListEvaluator());
		// TODO Auto-generated constructor stub		

		this.helper = helper;
		displayAlgorithmDetails();
	}
	
	public NsgaIIIntegerSolution(Problem<IntegerSolution> problem, int maxEvaluations, int populationSize,
			CrossoverOperator<IntegerSolution> crossoverOperator, MutationOperator<IntegerSolution> mutationOperator,
			SelectionOperator<List<IntegerSolution>, IntegerSolution> selectionOperator,
			Comparator<IntegerSolution> dominanceComparator, SolutionListEvaluator<IntegerSolution> evaluator) {
		
		super(problem, maxEvaluations, populationSize, crossoverOperator, mutationOperator, selectionOperator,
				dominanceComparator, evaluator);
		// TODO Auto-generated constructor stub
		
	}
	
	@Override
	protected void initProgress() {
		evaluations = 1;
	}

	@Override
	protected void updateProgress() {
		evaluations++;
	}

	@Override
	protected boolean isStoppingConditionReached() {
		printGenerationLog();
		return evaluations >= maxEvaluations;
	}
	
	private void printGenerationLog() {		
		String generationLog = String.format(getName() + ": Shipment Date %s -> Gen %d of %d", helper.getShipmentDate(), evaluations, maxEvaluations);
		logger.info(generationLog);
	}
	
	private void displayAlgorithmDetails() {		
		logger.debug("");
		logger.info("Algorithm: " + getName());
		logger.info("Population Size: " + maxPopulationSize);
		logger.info("Max Generation : " + maxEvaluations);
		logger.debug("");
	}
	
	@Override
	public String getName() {
		return "NSGA-II for Integer Solution";
	}

	@Override
	public String getDescription() {
		return "Nondominated Sorting Genetic Algorithm version II for Integer Solution";
	}
}
