package com.kmutt.sit.jmetal.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

@SuppressWarnings("serial")
public class EnsgaIIIIntegerSolution extends GenericNsgaIIIIntegerSolution {

	private Logger logger = LoggerFactory.getLogger(EnsgaIIIIntegerSolution.class);
	
	public EnsgaIIIIntegerSolution(NSGAIIIBuilder<IntegerSolution> builder, NsgaIIIHelper helper) {
		super(builder, helper);
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
