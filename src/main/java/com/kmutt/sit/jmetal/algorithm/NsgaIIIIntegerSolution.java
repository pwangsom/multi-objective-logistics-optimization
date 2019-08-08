package com.kmutt.sit.jmetal.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public abstract class NsgaIIIIntegerSolution extends NSGAIII<IntegerSolution> {

	private Logger logger = LoggerFactory.getLogger(NsgaIIIIntegerSolution.class);
	private NsgaIIIHelper helper;
	
	public NsgaIIIIntegerSolution(NSGAIIIBuilder<IntegerSolution> builder, NsgaIIIHelper helper) {
		super(builder);
		// TODO Auto-generated constructor stub

		this.helper = helper;
		
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
		
		String generationLog = String.format(getName() + ": Shipment Date %s -> Gen %d of %d", helper.getShipmentDate(), iterations, maxIterations);
		logger.info(generationLog);
				
		return iterations >= maxIterations;
	}
	
	@Override
	public String getName() {
		return "NSGA-III for Integer Solution";
	}

	@Override
	public String getDescription() {
		return "Nondominated Sorting Genetic Algorithm version III for Integer Solution";
	}

}
