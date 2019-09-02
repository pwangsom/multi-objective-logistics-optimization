package com.kmutt.sit.optimization;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.util.PointSolution;

import com.kmutt.sit.jmetal.front.ModifiedFrontNormalizer;
import com.kmutt.sit.jmetal.front.ParetoSet;
import com.kmutt.sit.jmetal.runner.LogisticsNsgaIIIIntegerRunner;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobProblemBenchmark;

@Controller
public class MultipleOptimizationManager extends OptimizationManager {
	
	private static Logger logger = LoggerFactory.getLogger(MultipleOptimizationManager.class);
	
	private List<String> algorithmList;
	private List<LogisticsJobProblemBenchmark> problemBenchmarkList;
	
	public MultipleOptimizationManager() {
		super();
	}
	
	public void retrieveExtraInformation() {		
		algorithmList = this.nsgaIIIHelper.getLogisticsHelper().getAlgorithmList();
		problemBenchmarkList = this.nsgaIIIHelper.getLogisticsHelper().getProblemBenchmarkList();
	}
	
	protected void execute(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList) {
		
		List<ParetoSet> allParetoSets = new ArrayList<ParetoSet>();
		List<IntegerSolution> dateParetoSolutions = new ArrayList<IntegerSolution>();
		
		for (String algorithm : algorithmList) {
			
			prepareNsgaIIIHelperBeforeRunningNsgaIII(vehicleType, shipmentList, routeList, algorithm);
			List<IntegerSolution> algorithmParetoSolutions = new ArrayList<IntegerSolution>();			
			int maxRun = nsgaIIIHelper.getMaxRun();
			
	        // Allocate each run
	        for(int i = 1; i <= maxRun; i++) {
	        	
	        	String runInfo = String.format("[Job ID: %s, Shipment Date: %s, Vehicle: %s, Algorithm: %s, Run: %d, Max Run: %d]", 
	        					jobId, nsgaIIIHelper.getShipmentDate(), nsgaIIIHelper.getVehicleType(), nsgaIIIHelper.getNsgaVersion(), i, maxRun);

	            logger.info(runInfo + ": Starting....");        	
	            nsgaIIIHelper.setCurrentRun(i);	       				
				List<IntegerSolution> runParetoSolutions = runAlgorithm();
				
				int algorithmSize = algorithmParetoSolutions.size();
				int dateSize = dateParetoSolutions.size();				
				algorithmParetoSolutions = mergParetoSolution(algorithmParetoSolutions, runParetoSolutions);
				dateParetoSolutions = mergParetoSolution(dateParetoSolutions, runParetoSolutions);				
				printNumberOfReferenceParetoFront(algorithmSize, dateSize, runParetoSolutions.size(), algorithmParetoSolutions.size(), dateParetoSolutions.size());
				
				if(nsgaIIIHelper.getLogisticsHelper().isOutputDatabaseEnabled()) {					
					LogisticsJobProblem problem = saveLogisticsJobProblem(runParetoSolutions);
					allParetoSets.add(OptimizationHelper.createParetoSet(nsgaIIIHelper.getShipmentDate(), vehicleType, algorithm, problem, i, runParetoSolutions));
				}
				 
	    		logger.debug(runInfo + ": Finished....");
	        }
	        
	        nsgaIIIHelper.setCurrentRun(0);
			if(nsgaIIIHelper.getLogisticsHelper().isOutputDatabaseEnabled()) {					
				LogisticsJobProblem problem = saveLogisticsJobProblem(algorithmParetoSolutions);
				allParetoSets.add(OptimizationHelper.createParetoSet(nsgaIIIHelper.getShipmentDate(), vehicleType, algorithm, problem, nsgaIIIHelper.getCurrentRun(), algorithmParetoSolutions));
			}
		}
		
        nsgaIIIHelper.setCurrentRun(99);
        nsgaIIIHelper.setNsgaVersion("all");
		if(nsgaIIIHelper.getLogisticsHelper().isOutputDatabaseEnabled()) {					
			LogisticsJobProblem problem = saveLogisticsJobProblem(dateParetoSolutions);
			allParetoSets.add(OptimizationHelper.createParetoSet(nsgaIIIHelper.getShipmentDate(), vehicleType, "all", problem, nsgaIIIHelper.getCurrentRun(), dateParetoSolutions));
		}
		
		computeHypervolume(allParetoSets);
		
		LogisticsJobProblemBenchmark benchmark = problemBenchmarkList.stream().filter(b -> 
										b.getShipmentDate().equalsIgnoreCase(nsgaIIIHelper.getShipmentDate())&& b.getVehicleType().equalsIgnoreCase(vehicleType)).findFirst().get();
		
		saveLogisticsJobResults(allParetoSets, benchmark);
	}
	
	protected void saveLogisticsJobResults(List<ParetoSet> allParetoSets, LogisticsJobProblemBenchmark benchmark) {
		int[] i = {1};
		
		allParetoSets.stream().forEach(set -> {
			logger.info(String.format("Saving result[%d]: Shimpemnt Date: %s, Vehicle: %s, Algorithm: %s, Run: %d", 
					i[0], set.getShipmentDate(), set.getVehicleType(), set.getAlgorithm(), set.getRun()));
			
			OptimizationHelper.saveLogisticsJobResults(nsgaIIIHelper, set.getProblem().getProblemId(), set.getSolutions(), set.getNormalizedSolutions(), benchmark);
			
			i[0]++;			
		});
	}
	
	protected void computeHypervolume(List<ParetoSet> allParetoSets) {
		
		logger.info(String.format("Compute Hypervolume: Shimpemnt Date: %s, Vehicle: %s Starting....", nsgaIIIHelper.getShipmentDate(), nsgaIIIHelper.getVehicleType()));
		
		ParetoSet refParetoSet = allParetoSets.stream().filter(m -> m.getRun() == 99).findFirst().get();
        Front referenceFront = new ArrayFront(refParetoSet.getSolutions());
        ModifiedFrontNormalizer frontNormalizer = new ModifiedFrontNormalizer(referenceFront);            
        Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
        
        allParetoSets.stream().forEach(set ->{
			Front normalizedFront = frontNormalizer.normalize(new ArrayFront(set.getSolutions()));			
			List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);
			Double normalHypervolume = new PISAHypervolume<PointSolution>(normalizedReferenceFront).evaluate(normalizedPopulation);
			Double hypervolume = new PISAHypervolume<IntegerSolution>(referenceFront).evaluate(set.getSolutions());
			
			set.setNormalizedSolutions(normalizedPopulation);
			set.getProblem().setHypervolume(BigDecimal.valueOf(hypervolume));
			set.getProblem().setNormalHypervolume(BigDecimal.valueOf(normalHypervolume));
			
			nsgaIIIHelper.getLogisticsHelper().updateLogisticsJobProblem(set.getProblem());        	
        });        
	}
	
	protected ParetoSet createParetoSet(String shipmentDate, String vehicleType, String algorithm, Integer problemId, Integer run) {
		ParetoSet set = new ParetoSet();
		
		return set;
	}
	
	protected LogisticsJobProblem saveLogisticsJobProblem(List<IntegerSolution> paretoSet) {
		// Insert table logistics_job_problem		
		LogisticsJobProblem problem = OptimizationHelper.saveLogisticsJobProblem(nsgaIIIHelper, paretoSet.size());        
        return problem;
	}
	
	protected void printNumberOfReferenceParetoFront(int algorithmSize, int dateSize, int runSize, int newAlgorithmSize, int newDateSize) {		
		logger.debug("");
		logger.info(String.format("Current Algorithm Pareto + Incoming -> New: %d + %d -> %d", algorithmSize, runSize, newAlgorithmSize));
		logger.info(String.format("Current Shipment Date Pareto + Incoming -> New: %d + %d -> %d", dateSize, runSize, newDateSize));
		
	}
	
	protected List<IntegerSolution> runAlgorithm(){		
		LogisticsNsgaIIIIntegerRunner runner = new LogisticsNsgaIIIIntegerRunner(nsgaIIIHelper);
		runner.setRunnerParameter();
		runner.execute();
		
		List<IntegerSolution> runParetoSolutions = runner.getSolutions();
		
		return runParetoSolutions;
	}
	
	protected List<IntegerSolution> mergParetoSolution(List<IntegerSolution> baseSolutions, List<IntegerSolution> newComingSolutions) {		
		baseSolutions.addAll(newComingSolutions);
		
		if(baseSolutions.size() > newComingSolutions.size()) {			
			baseSolutions = SolutionListUtils.getNondominatedSolutions(baseSolutions);
		}
		
		return baseSolutions;
	}
	
	
	protected void prepareNsgaIIIHelperBeforeRunningNsgaIII(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList, String algorithm) {
		nsgaIIIHelper.setNsgaVersion(algorithm);
		nsgaIIIHelper.setVehicleType(vehicleType);
		nsgaIIIHelper.setShipmentList(shipmentList);
		nsgaIIIHelper.setRouteList(routeList);
		nsgaIIIHelper.setFunFile(OptimizationHelper.getFileOutputName(nsgaIIIHelper, vehicleType.toLowerCase(), "fun"));
		nsgaIIIHelper.setVarFile(OptimizationHelper.getFileOutputName(nsgaIIIHelper, vehicleType.toLowerCase(), "var"));		
	}

}
