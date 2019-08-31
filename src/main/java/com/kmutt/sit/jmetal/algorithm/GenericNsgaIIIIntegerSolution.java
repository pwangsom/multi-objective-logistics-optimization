package com.kmutt.sit.jmetal.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

public abstract class GenericNsgaIIIIntegerSolution extends NSGAIII<IntegerSolution> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(FourEnsgaIIIIntegerSolution.class);
	protected NsgaIIIHelper helper;
	
	public GenericNsgaIIIIntegerSolution(NSGAIIIBuilder<IntegerSolution> builder, NsgaIIIHelper helper) {
		super(builder);
		// TODO Auto-generated constructor stub

		this.helper = helper;
		displayAlgorithmDetails();
	}
	
	@Override
	protected void initProgress() {
		iterations = 1;
	}
	
	@Override
	protected boolean isStoppingConditionReached() {
		printGenerationLog();
				
		return iterations >= maxIterations;
	}
	
	protected void printGenerationLog() {		
		String generationLog = String.format(getName() + ": Shipment Date %s -> Gen %d of %d", helper.getShipmentDate(), iterations, maxIterations);
		logger.info(generationLog);
	}
	
	protected void displayAlgorithmDetails() {		
		logger.debug("");
		logger.info("Algorithm: " + getName());
		logger.info("Population Size: " + maxPopulationSize);
		logger.info("Max Generation : " + maxIterations);
		logger.debug("");
	}

}
