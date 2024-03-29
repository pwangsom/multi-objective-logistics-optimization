package com.kmutt.sit.jmetal.runner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.IntegerSBXCrossover;
import org.uma.jmetal.operator.impl.mutation.IntegerPolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.kmutt.sit.jmetal.algorithm.GenericNsgaIIIIntegerSolutionBuilder;
import com.kmutt.sit.jmetal.algorithm.GenericNsgaIIIntegerBuilder;
import com.kmutt.sit.jmetal.problem.Type1ContrainedLogisticsIntegerProblem;
import com.kmutt.sit.jmetal.problem.GenericLogisticsIntegerProblem;
import com.kmutt.sit.jmetal.problem.Type2ConstrainedLogisticsIntegerProblem;
import com.kmutt.sit.jmetal.problem.Type3ConstrainedLogisticsIntegerProblem;
import com.kmutt.sit.utilities.JavaUtils;

import lombok.Getter;

public class LogisticsAlgorithmIntegerSolutionRunner extends AbstractAlgorithmRunner {

	private Logger logger = LoggerFactory.getLogger(LogisticsAlgorithmIntegerSolutionRunner.class);
	
	private Problem<IntegerSolution> problem;
	private Algorithm<List<IntegerSolution>> algorithm;
	private CrossoverOperator<IntegerSolution> crossover;
	private MutationOperator<IntegerSolution> mutation;
	private SelectionOperator<List<IntegerSolution>, IntegerSolution> selection;
	private AlgorithmRunner algorithmRunner;
	
	private int maxIteration = 300;
	@SuppressWarnings("unused")
	private String referenceParetoFront = "src/main/resources/NBI_3_12.pf";
	
	private NsgaIIIHelper helper;
	
	@Getter
	private List<IntegerSolution> solutions;
	
	public LogisticsAlgorithmIntegerSolutionRunner(NsgaIIIHelper helper) {
		this.helper = helper;
	}
	
	public void setRunnerParameter() {
		
		if(helper.getLogisticsHelper().isProblemConstraintEnabled()) {
			if(helper.getLogisticsHelper().getProblemConstraintType() == 2) {
				problem = new Type2ConstrainedLogisticsIntegerProblem(this.helper);				
			} else if(helper.getLogisticsHelper().getProblemConstraintType() == 3) {
				problem = new Type3ConstrainedLogisticsIntegerProblem(this.helper);					
			} else {
				problem = new Type1ContrainedLogisticsIntegerProblem(this.helper);		
			}
		} else {
			problem = new GenericLogisticsIntegerProblem(this.helper);
		}
		
	    double crossoverProbability = 0.9 ;
	    double crossoverDistributionIndex = 30.0 ;
		crossover = new IntegerSBXCrossover(crossoverProbability, crossoverDistributionIndex);

	    double mutationProbability = 1.0 / problem.getNumberOfVariables() ;
	    double mutationDistributionIndex = 20.0 ;
		mutation = new IntegerPolynomialMutation(mutationProbability, mutationDistributionIndex);

		selection = new BinaryTournamentSelection<IntegerSolution>();
		maxIteration = this.helper.getMaxIteration();
		
		if(!JavaUtils.isNull(this.helper.getReferenceFile())) referenceParetoFront = this.helper.getReferenceFile();
	}
	
	public void execute() {
		
		/*
		 * algorithm = new NSGAIIIBuilder<>(problem) .setCrossoverOperator(crossover)
		 * .setMutationOperator(mutation) .setSelectionOperator(selection)
		 * .setMaxIterations(this.maxIteration) .build() ;
		 */
	    
		if(helper.getNsgaVersion().equalsIgnoreCase("mnsgaiii")) {			
			 algorithm = new GenericNsgaIIIIntegerSolutionBuilder(problem, crossover, mutation, selection, maxIteration, helper)
						.buildModifiedNsgaIIIIntegerSolution();
		} else if(helper.getNsgaVersion().equalsIgnoreCase("ensgaiii3")) {
			algorithm = new GenericNsgaIIIIntegerSolutionBuilder(problem, crossover, mutation, selection, maxIteration, helper)
					.buildThreeEnsgaIIIIntegerSolution();
		} else if(helper.getNsgaVersion().equalsIgnoreCase("ensgaiii4")) {
			algorithm = new GenericNsgaIIIIntegerSolutionBuilder(problem, crossover, mutation, selection, maxIteration, helper)
					.buildFourEnsgaIIIIntegerSolution();
		} else if(helper.getNsgaVersion().equalsIgnoreCase("nsgaii")) {
			algorithm = new GenericNsgaIIIntegerBuilder(problem, crossover, mutation, selection, maxIteration, 92, helper)
					.buildNsgaIIIntegerSolution();
		} else {			
			 algorithm = new GenericNsgaIIIIntegerSolutionBuilder(problem, crossover, mutation, selection, maxIteration, helper)
						.buildNsgaIIIIntegerSolution();			
		}
	    

	    algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
	    
	    solutions = algorithm.getResult();
	    long computingTime = algorithmRunner.getComputingTime();

		logger.info("Total execution time: " + computingTime + "ms");
	    
	    if(helper.getLogisticsHelper().isOutputFileEnabled()) {
	    	printOutputFiles();
			logger.info("Objectives values have been written to file " + helper.getFunFile());
			logger.info("Variables values have been written to file " + helper.getVarFile());
	    }	
		
	}
		
	private void printOutputFiles() {
	    
	    new SolutionListOutput(solutions)
	        .setSeparator(",")
	        .setVarFileOutputContext(new DefaultFileOutputContext(helper.getVarFile()))
	        .setFunFileOutputContext(new DefaultFileOutputContext(helper.getFunFile()))
	        .print() ;
		
	}

}
