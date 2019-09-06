package com.kmutt.sit.optimization;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.point.util.PointSolution;

import com.kmutt.sit.jmetal.front.ParetoSet;
import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.LogisticsJob;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobProblemBenchmark;
import com.kmutt.sit.jpa.entities.LogisticsJobResult;
import com.kmutt.sit.jpa.entities.LogisticsJobResultDetail;
import com.kmutt.sit.utilities.JavaUtils;

public class OptimizationHelper {	
	
	public static LogisticsJobProblem saveLogisticsJobProblem(NsgaIIIHelper nsgaIIIHelper, Integer noOfSolutions) {
		
		LogisticsJobProblem problem = new LogisticsJobProblem();
		problem.setJobId(nsgaIIIHelper.getJobId());
		problem.setShipmentDate(nsgaIIIHelper.getShipmentDate());
		problem.setVehicleType(nsgaIIIHelper.getVehicleType());
		
		String shipmentList = nsgaIIIHelper.getShipmentList().stream().map(s -> s.getShipmentKey()).collect(Collectors.toList()).toString();
		String routeList = nsgaIIIHelper.getRouteList().stream().map(r -> r.getChromosomeId()).collect(Collectors.toList()).toString();
		
		problem.setShipmentList(JavaUtils.removeStringOfList(shipmentList));
		problem.setRouteList(JavaUtils.removeStringOfList(routeList));
		problem.setNoOfSolutions(noOfSolutions);
		problem.setSolutionType("generated");
		problem.setAlgorithm(nsgaIIIHelper.getNsgaVersion());
		problem.setOptionalParameter(JavaUtils.getObjectiveVersionRate(nsgaIIIHelper.getLogisticsHelper()));
		problem.setRun(nsgaIIIHelper.getCurrentRun());
		
		return nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobProblem(problem);
	}	
	
	public static void saveLogisticsJob(NsgaIIIHelper nsgaIIIHelper) {		
		
		LogisticsJob job = new LogisticsJob();
		job.setJobId(nsgaIIIHelper.getJobId());
		job.setVehicleConfig(nsgaIIIHelper.getLogisticsHelper().getVehicleTypes());
		job.setMaxRun(nsgaIIIHelper.getMaxRun());
		job.setMaxIteration(nsgaIIIHelper.getMaxIteration());
		
		if(nsgaIIIHelper.getLogisticsHelper().isMultipleAlgorithmEnabled()) {
			job.setIsMultiple(1);
		} else {
			job.setIsMultiple(0);
		}
		
		nsgaIIIHelper.getLogisticsHelper().saveLogisticsJob(job);		
	}
	
	public static String getFileOutputName(NsgaIIIHelper nsgaIIIHelper, String vehicleType, String fileType) {
		return nsgaIIIHelper.getLogisticsHelper().getOutputPath() + "/" + nsgaIIIHelper.getJobId() 
				+ "-" + nsgaIIIHelper.getShipmentDate() + "-" + vehicleType + "-" + nsgaIIIHelper.getNsgaVersion()
				+ "-" + JavaUtils.getObjectiveVersionRate(nsgaIIIHelper.getLogisticsHelper())
				+ "-max" + nsgaIIIHelper.getMaxIteration() + "-" + fileType + ".csv";
	}
	
	public static ParetoSet createParetoSet(String shipmentDate, String vehicleType, String algorithm, LogisticsJobProblem problem, Integer run, List<IntegerSolution> solutions) {
		ParetoSet set = new ParetoSet();
		set.setShipmentDate(shipmentDate);
		set.setVehicleType(vehicleType);
		set.setAlgorithm(algorithm);
		set.setProblem(problem);
		set.setRun(run);
		set.setSolutions(solutions);
		
		return set;
	}	
	
	public static void saveLogisticsJobResults(NsgaIIIHelper nsgaIIIHelper, Integer problemId, List<IntegerSolution> paretoSet, List<PointSolution> normalizedParetoSet) {
		
		List<LogisticsJobResult> results = new ArrayList<LogisticsJobResult>();
		// Map<String, IntegerSolution> details = new HashMap<String, IntegerSolution>();
		// IntegerSolution[] bestCases = getBestEachObjective(paretoSet);
		
		IntStream.range(0, paretoSet.size()).forEach(i -> {
			LogisticsJobResult result = new LogisticsJobResult();
			result.setProblemId(problemId);
			result.setSolutionIndex(i);
			
			IntegerSolution paretoSolution = paretoSet.get(i);		
			String routeList = JavaUtils.removeStringOfList(getSolutionString(paretoSolution));
			result.setSolutionDetail(routeList);			
			result.setObjective1(BigDecimal.valueOf(paretoSolution.getObjective(0)));
			result.setObjective2(BigDecimal.valueOf(paretoSolution.getObjective(1)));
			result.setObjective3(BigDecimal.valueOf(paretoSolution.getObjective(2)));
			
			PointSolution normalizedParetoSolution = normalizedParetoSet.get(i);
			result.setNormalizedObjective1(BigDecimal.valueOf(normalizedParetoSolution.getObjective(0)));
			result.setNormalizedObjective2(BigDecimal.valueOf(normalizedParetoSolution.getObjective(1)));
			result.setNormalizedObjective3(BigDecimal.valueOf(normalizedParetoSolution.getObjective(2)));			

			results.add(result);
			// resultDetail.addAll(getLogisticsJobResultDetail(problemId, i, paretoSet.get(i)));
			
		});
		
		nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobResult(results);
        
		// nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobResultDetail(resultDetail);
        // logger.info("Logistics Job Result Details are saved...");
	}
	
	public static void saveLogisticsJobResults(NsgaIIIHelper nsgaIIIHelper, Integer problemId, List<IntegerSolution> paretoSet, List<PointSolution> normalizedParetoSet, LogisticsJobProblemBenchmark benchmark) {
		
		List<LogisticsJobResult> results = new ArrayList<LogisticsJobResult>();
		
		IntStream.range(0, paretoSet.size()).forEach(i -> {
			LogisticsJobResult result = new LogisticsJobResult();
			result.setProblemId(problemId);
			result.setSolutionIndex(i);
			
			IntegerSolution paretoSolution = paretoSet.get(i);		
			String routeList = JavaUtils.removeStringOfList(getSolutionString(paretoSolution));
			result.setSolutionDetail(routeList);			
			result.setObjective1(BigDecimal.valueOf(paretoSolution.getObjective(0)));
			result.setObjective2(BigDecimal.valueOf(paretoSolution.getObjective(1)));
			result.setObjective3(BigDecimal.valueOf(paretoSolution.getObjective(2)));
			
			PointSolution normalizedParetoSolution = normalizedParetoSet.get(i);
			result.setNormalizedObjective1(BigDecimal.valueOf(normalizedParetoSolution.getObjective(0)));
			result.setNormalizedObjective2(BigDecimal.valueOf(normalizedParetoSolution.getObjective(1)));
			result.setNormalizedObjective3(BigDecimal.valueOf(normalizedParetoSolution.getObjective(2)));			
			
			if(checkDominateBenchmark(result, benchmark) == 3) {
				result.setIsDominateBenchmark(1);
			} else {
				result.setIsDominateBenchmark(0);
			}

			results.add(result);
			
		});
		
		nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobResult(results);
	}
	
	
	public static void saveLogisticsJobResults(NsgaIIIHelper nsgaIIIHelper, List<ParetoSet> allParetoSets, LogisticsJobProblemBenchmark benchmark) {
		List<LogisticsJobResult> results = new ArrayList<LogisticsJobResult>();
		
		allParetoSets.stream().forEach(set -> {
			Integer problemId = set.getProblem().getProblemId();
			List<IntegerSolution> paretoSet = set.getSolutions();
			List<PointSolution> normalizedParetoSet = set.getNormalizedSolutions();
			
			IntStream.range(0, paretoSet.size()).forEach(i -> {
				LogisticsJobResult result = new LogisticsJobResult();
				result.setProblemId(problemId);
				result.setSolutionIndex(i);
				
				IntegerSolution paretoSolution = paretoSet.get(i);		
				String routeList = JavaUtils.removeStringOfList(getSolutionString(paretoSolution));
				result.setSolutionDetail(routeList);			
				result.setObjective1(BigDecimal.valueOf(paretoSolution.getObjective(0)));
				result.setObjective2(BigDecimal.valueOf(paretoSolution.getObjective(1)));
				result.setObjective3(BigDecimal.valueOf(paretoSolution.getObjective(2)));
				
				PointSolution normalizedParetoSolution = normalizedParetoSet.get(i);
				result.setNormalizedObjective1(BigDecimal.valueOf(normalizedParetoSolution.getObjective(0)));
				result.setNormalizedObjective2(BigDecimal.valueOf(normalizedParetoSolution.getObjective(1)));
				result.setNormalizedObjective3(BigDecimal.valueOf(normalizedParetoSolution.getObjective(2)));
				
				if(checkDominateBenchmark(result, benchmark) == 3) {
					result.setIsDominateBenchmark(1);
				} else {
					result.setIsDominateBenchmark(0);
				}

				results.add(result);
				
			});			
			
			
		});
		
		nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobResult(results);
	}
	
	private static int checkDominateBenchmark(LogisticsJobResult result, LogisticsJobProblemBenchmark benchmark) {
		int score = 0;
		
		if(result.getObjective1().compareTo(benchmark.getObjective1()) < 1) score++;
		if(result.getObjective2().compareTo(benchmark.getObjective2()) < 1) score++;
		if(result.getObjective3().compareTo(benchmark.getObjective3()) < 1) score++;
		
		return score;
	}
		
	@SuppressWarnings("unused")
	private static LogisticsJobResultDetail getLogisticsJobResultDetail(NsgaIIIHelper nsgaIIIHelper, Integer problemId, Integer solutionIndex, IntegerSolution solution, String solutionType) {
					
		LogisticsJobResultDetail detail = new LogisticsJobResultDetail();
		detail.setProblemId(problemId);
		detail.setSolutionIndex(solutionIndex);
		detail.setShipmentDate(nsgaIIIHelper.getShipmentDate());
		detail.setVehicleType(nsgaIIIHelper.getVehicleType());
		
		// DhlRoute route = nsgaIIIHelper.getRouteList().stream().filter(r -> r.getChromosomeId() == solution.getVariableValue(i[0])).collect(Collectors.toList()).get(0);
		// detail.setChromosomeId(route.getChromosomeId());
		// detail.setRoute(route.getRoute());
		
		return detail;
	}
	
	private static String getSolutionString(IntegerSolution solution) {
		
		List<Integer> list = new ArrayList<Integer>();
		
		for (int i = 0; i < solution.getNumberOfVariables(); i++) {			
			list.add(solution.getVariableValue(i));			
		}
		
		return list.toString();
	}	

}
