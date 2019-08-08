package com.kmutt.sit.optimization;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.point.util.PointSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.LogisticsJob;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
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
		problem.setOptionalParameter(nsgaIIIHelper.getObjectiveVersion() + "_max" + nsgaIIIHelper.getMaxIteration());
		
		return nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobProblem(problem);
	}	
	
	public static void saveLogisticsJob(NsgaIIIHelper nsgaIIIHelper) {		
		
		LogisticsJob job = new LogisticsJob();
		job.setJobId(nsgaIIIHelper.getJobId());
		job.setVehicleConfig(nsgaIIIHelper.getLogisticsHelper().getVehicleTypes());
		job.setMaxRun(nsgaIIIHelper.getMaxRun());
		job.setMaxIteration(nsgaIIIHelper.getMaxIteration());
		
		nsgaIIIHelper.getLogisticsHelper().saveLogisticsJob(job);		
	}
	
	public static String getFileOutputName(NsgaIIIHelper nsgaIIIHelper, String vehicleType, String fileType) {
		return nsgaIIIHelper.getLogisticsHelper().getOutputPath() + "/" + nsgaIIIHelper.getJobId() 
				+ "-" + nsgaIIIHelper.getShipmentDate() + "-" + vehicleType + "-" + fileType + ".csv";
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
	
	private static IntegerSolution[] getBestEachObjective(List<IntegerSolution> paretoSet) {
		
		IntegerSolution[] results = new IntegerSolution[4];
		
		Comparator<IntegerSolution> byFirtObjective = new Comparator<IntegerSolution>() {
			@Override
			public int compare(IntegerSolution s1, IntegerSolution s2) {
				return Double.compare(s1.getObjective(0), s2.getObjective(0));
			}			
		};
		
		Comparator<IntegerSolution> bySecondObjective = new Comparator<IntegerSolution>() {
			@Override
			public int compare(IntegerSolution s1, IntegerSolution s2) {
				return Double.compare(s1.getObjective(1), s2.getObjective(2));
			}			
		};
		
		Comparator<IntegerSolution> byThirdObjective = new Comparator<IntegerSolution>() {
			@Override
			public int compare(IntegerSolution s1, IntegerSolution s2) {
				return Double.compare(s1.getObjective(2), s2.getObjective(2));
			}			
		};
		
		paretoSet.sort(byThirdObjective.thenComparing(bySecondObjective).thenComparing(byFirtObjective));
		results[3] = paretoSet.get(0);

		paretoSet.sort(bySecondObjective.thenComparing(byThirdObjective).thenComparing(byFirtObjective));
		results[2] = paretoSet.get(0);
		
		paretoSet.sort(byFirtObjective.thenComparing(byThirdObjective).thenComparing(bySecondObjective));
		results[1] = paretoSet.get(0);
		
		paretoSet.sort(byFirtObjective.thenComparing(bySecondObjective).thenComparing(byThirdObjective));
		results[0] = paretoSet.get(0);
		
		return results;
	}
	
	private static String getSolutionString(IntegerSolution solution) {
		
		List<Integer> list = new ArrayList<Integer>();
		
		for (int i = 0; i < solution.getNumberOfVariables(); i++) {			
			list.add(solution.getVariableValue(i));			
		}
		
		return list.toString();
	}	

}
