package com.kmutt.sit.jmetal.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.ReferencePoint;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.solutionattribute.Ranking;

@SuppressWarnings("serial")
public class ModifiedNsgaIIIIntegerSolution extends NSGAIII<IntegerSolution> {
	
	private Logger logger = LoggerFactory.getLogger(ModifiedNsgaIIIIntegerSolution.class);
	
	public ModifiedNsgaIIIIntegerSolution(NSGAIIIBuilder<IntegerSolution> builder) {
		super(builder);
		// TODO Auto-generated constructor stub

		logger.debug("");
		logger.info("Algorithm: " + getName());
		logger.info("Population Size: " + maxPopulationSize);
		logger.info("Max Generation : " + maxIterations);
		logger.debug("");
	}
	
	@Override
	protected void initProgress() {
		iterations = 1;
	}
	
	@Override
	protected boolean isStoppingConditionReached() {
		
		String generationLog = String.format(getName() + ": Gen %d of %d", iterations, maxIterations);
		logger.info(generationLog);
				
		return iterations >= maxIterations;
	}
	
	@Override
	protected List<IntegerSolution> replacement(List<IntegerSolution> population,
			List<IntegerSolution> offspringPopulation) {

		List<IntegerSolution> jointPopulation = new ArrayList<IntegerSolution>();
		jointPopulation.addAll(population);
		jointPopulation.addAll(offspringPopulation);

		Ranking<IntegerSolution> ranking = computeRanking(jointPopulation);

		// List<Solution> pop = crowdingDistanceSelection(ranking);
		List<IntegerSolution> pop = new ArrayList<IntegerSolution>();
		List<List<IntegerSolution>> fronts = new ArrayList<>();
		int rankingIndex = 0;
		int candidateSolutions = 0;
		while (candidateSolutions < getMaxPopulationSize()) {
			fronts.add(ranking.getSubfront(rankingIndex));
			candidateSolutions += ranking.getSubfront(rankingIndex).size();
			if ((pop.size() + ranking.getSubfront(rankingIndex).size()) <= getMaxPopulationSize())
				addRankedSolutionsToPopulation(ranking, rankingIndex, pop);
			rankingIndex++;
		}

		// A copy of the reference list should be used as parameter of the environmental
		// selection
		ModifiedEnvironmentalSelection<IntegerSolution> selection = new ModifiedEnvironmentalSelection<>(fronts,
				getMaxPopulationSize(), getReferencePointsCopy(), getProblem().getNumberOfObjectives());

		pop = selection.execute(pop);

		return pop;
	}	
	  
	private List<ReferencePoint<IntegerSolution>> getReferencePointsCopy() {
		List<ReferencePoint<IntegerSolution>> copy = new ArrayList<>();
		for (ReferencePoint<IntegerSolution> r : this.referencePoints) {
			copy.add(new ReferencePoint<>(r));
		}
		return copy;
	}
	
	@Override
	public String getName() {
		return "M-NSGA-III for Integer Solution";
	}

	@Override
	public String getDescription() {
		return "Modified of Nondominated Sorting Genetic Algorithm version III for Integer Solution";
	}

}
