package com.kmutt.sit.optimization;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.util.PointSolution;

import com.kmutt.sit.jmetal.front.ExtendedFrontNormalizer;
import com.kmutt.sit.jmetal.runner.LogisticsNsgaIIIIntegerRunner;
import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJob;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobResult;
import com.kmutt.sit.jpa.entities.LogisticsJobResultDetail;
import com.kmutt.sit.utilities.JavaUtils;

import lombok.Setter;


@Controller
public class OptimizationManager {	

	private static Logger logger = LoggerFactory.getLogger(OptimizationManager.class);
	
	@Setter
	private String jobId;
	
	@Autowired
	private NsgaIIIHelper nsgaIIIHelper;
	
	private List<DhlRoute> vanList;
	private List<DhlRoute> bikeList;
	private Map<String, Integer> scoreMapping;
	
	public OptimizationManager() {
		vanList = new ArrayList<DhlRoute>();
		bikeList = new ArrayList<DhlRoute>();
		scoreMapping = new HashMap<String, Integer>();
	}
	
	public void opitmize() {
		
        logger.info("OptimizationManager: Job ID: " + jobId + "\t start....."); 
                
        prepareInformation();
        
        // Insert table logistics_job
        if(nsgaIIIHelper.getLogisticsHelper().isOutputDatabaseEnabled()) {    
            saveLogisticsJob();
        }        
        
        List<String> shipmentDateList = nsgaIIIHelper.getLogisticsHelper().retrieveShipmentDateList();
        
        logger.info(shipmentDateList.toString());
                
        // Operate shipments by date
        shipmentDateList.stream().forEach(date ->{
        	nsgaIIIHelper.setShipmentDate(date);        	

        	// There are two types of shipments per day; shipment for van and bike.
        	// Allocation shipment for van
        	if(nsgaIIIHelper.getLogisticsHelper().getVehicleTypes().contains("Van")) {
            	allocateDailyShipmentForVan(date);
        	}
        	
        	// Allocation shipment for bike
        	if(nsgaIIIHelper.getLogisticsHelper().getVehicleTypes().contains("Bike")) {
            	allocateDailyShipmentForBike(date);        		
        	}
        });

        logger.info("OptimizationManager:: Job ID: " + jobId + "\t finished..");  
	}
	
	private void allocateDailyShipmentForVan(String shipmentDate) {
        logger.info("allocateDailyShipmentForVan: start....."); 
		
		List<DhlShipment> shipmentList = nsgaIIIHelper.getLogisticsHelper().retrieveValidDailyShipmentForVan(shipmentDate)
				.stream().sorted(Comparator.comparingInt(DhlShipment::getShipmentKey)).collect(Collectors.toList());
		
		if(shipmentList.isEmpty()) {
			printShipmentEmpty(shipmentDate, "Van");			
		} else {
			runNsgaIII("Van", shipmentList, vanList);
		}

        logger.info("allocateDailyShipmentForVan: finished..");  		
	}
	
	private void allocateDailyShipmentForBike(String shipmentDate) {
        logger.info("allocateDailyShipmentForBike: start....."); 

		List<DhlShipment> shipmentList = nsgaIIIHelper.getLogisticsHelper().retrieveDailyValidShipmentForBike(shipmentDate)
											.stream().sorted(Comparator.comparingInt(DhlShipment::getShipmentKey)).collect(Collectors.toList());
		
		if(shipmentList.isEmpty()) {
			printShipmentEmpty(shipmentDate, "Bike");
		} else {
			runNsgaIII("Bike", shipmentList, bikeList);
		}

        logger.info("allocateDailyShipmentForBike: finished..");  
	}
	
	private void printShipmentEmpty(String shipmentDate, String vehicleType) {
		logger.warn("There is no shipment on {" + shipmentDate + "} for {" + vehicleType + "}.");
	}
	
	private void runNsgaIII(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList) {
		
		prepareNsgaIIIHelperBeforeRunningNsgaIII(vehicleType, shipmentList, routeList);
		
		if(logger.isDebugEnabled()) previewLogisticsOperate();
		
        List<IntegerSolution> solutions = new ArrayList<IntegerSolution>();
		
        // Allocate each run
        for(int i = 1; i <= nsgaIIIHelper.getMaxRun(); i++) {
        	
        	String runInfo = String.format("[Job ID: %s, Shipment Date: %s, Vehicle: %s, Run No: %d, Max Run: %d]", 
        					jobId, nsgaIIIHelper.getShipmentDate(), nsgaIIIHelper.getVehicleType(), i, nsgaIIIHelper.getMaxRun());

            logger.info(runInfo + ": Starting....");
        	
            nsgaIIIHelper.setCurrentRun(i);
        	
    		LogisticsNsgaIIIIntegerRunner runner = new LogisticsNsgaIIIIntegerRunner(nsgaIIIHelper);
    		runner.setRunnerParameter();
    		runner.execute();
    		
    		solutions.addAll(runner.getSolutions());

    		logger.debug(runInfo + ": Finished....");
        }
        
        List<IntegerSolution> paretoSet = SolutionListUtils.getNondominatedSolutions(solutions);
        Front referenceFront = new ArrayFront(paretoSet);
        ExtendedFrontNormalizer frontNormalizer = new ExtendedFrontNormalizer(referenceFront);            
        @SuppressWarnings("unchecked")
		List<PointSolution> normalizedParetoSet = (List<PointSolution>) frontNormalizer.normalize(paretoSet);
		
		// Insert table logistics_job_problem        
        if(nsgaIIIHelper.getLogisticsHelper().isOutputDatabaseEnabled()) {
            LogisticsJobProblem problem = saveLogisticsJobProblem(paretoSet.size());
            logger.info("Logistics Job Problem is saved...");
            saveLogisticsJobResults(problem.getProblemId(), paretoSet, normalizedParetoSet);        	
        }
	}
	
	private void prepareNsgaIIIHelperBeforeRunningNsgaIII(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList) {
		nsgaIIIHelper.setVehicleType(vehicleType);
		nsgaIIIHelper.setShipmentList(shipmentList);
		nsgaIIIHelper.setRouteList(routeList);
		nsgaIIIHelper.setFunFile(getFileOutputName(vehicleType.toLowerCase(), "fun"));
		nsgaIIIHelper.setVarFile(getFileOutputName(vehicleType.toLowerCase(), "var"));		
	}
	
	private String getFileOutputName(String vehicleType, String fileType) {
		return nsgaIIIHelper.getLogisticsHelper().getOutputPath() + "/" + nsgaIIIHelper.getJobId() 
				+ "-" + nsgaIIIHelper.getShipmentDate() + "-" + vehicleType + "-" + fileType + ".csv";
	}
	
	private void saveLogisticsJobResults(Integer problemId, List<IntegerSolution> paretoSet, List<PointSolution> normalizedParetoSet) {
		
		List<LogisticsJobResult> results = new ArrayList<LogisticsJobResult>();
		// List<LogisticsJobResultDetail> resultDetail = new ArrayList<LogisticsJobResultDetail>();
		
		
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
        logger.info("Logistics Job Results are saved...");
        
		// nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobResultDetail(resultDetail);
        // logger.info("Logistics Job Result Details are saved...");
	}
	
	private List<LogisticsJobResultDetail> getLogisticsJobResultDetail(Integer problemId, Integer solutionIndex, IntegerSolution solution) {
		
		List<LogisticsJobResultDetail> details = new ArrayList<LogisticsJobResultDetail>();
		
		for (int[] i = {0}; i[0] < solution.getNumberOfVariables(); i[0]++) {
			
			LogisticsJobResultDetail detail = new LogisticsJobResultDetail();
			detail.setProblemId(problemId);
			detail.setSolutionIndex(solutionIndex);
			detail.setShipmentDate(nsgaIIIHelper.getShipmentDate());
			detail.setVehicleType(nsgaIIIHelper.getVehicleType());
			
			DhlRoute route = nsgaIIIHelper.getRouteList().stream().filter(r -> r.getChromosomeId() == solution.getVariableValue(i[0])).collect(Collectors.toList()).get(0);
			detail.setChromosomeId(route.getChromosomeId());
			detail.setRoute(route.getRoute());
			
			detail.setShipmentKey(nsgaIIIHelper.getShipmentList().get(i[0]).getShipmentKey());
			
			details.add(detail);			
		}
		
		return details;
	}
	
	private LogisticsJobProblem saveLogisticsJobProblem(Integer noOfSolutions) {
		
		LogisticsJobProblem problem = new LogisticsJobProblem();
		problem.setJobId(jobId);
		problem.setShipmentDate(nsgaIIIHelper.getShipmentDate());
		problem.setVehicleType(nsgaIIIHelper.getVehicleType());
		
		String shipmentList = nsgaIIIHelper.getShipmentList().stream().map(s -> s.getShipmentKey()).collect(Collectors.toList()).toString();
		String routeList = nsgaIIIHelper.getRouteList().stream().map(r -> r.getChromosomeId()).collect(Collectors.toList()).toString();
		
		problem.setShipmentList(JavaUtils.removeStringOfList(shipmentList));
		problem.setRouteList(JavaUtils.removeStringOfList(routeList));
		problem.setNoOfSolutions(noOfSolutions);
		problem.setSolutionType("generated");
		problem.setAlgorithm("nsgaiii");
		problem.setOptionalParameter("version1");
		
        logger.info("saveLogisticsJob: finished..");  
		
		return nsgaIIIHelper.getLogisticsHelper().saveLogisticsJobProblem(problem);
	}
	
	private void saveLogisticsJob() {		
		
		LogisticsJob job = new LogisticsJob();
		job.setJobId(nsgaIIIHelper.getJobId());
		job.setVehicleConfig(nsgaIIIHelper.getLogisticsHelper().getVehicleTypes());
		job.setMaxRun(nsgaIIIHelper.getMaxRun());
		job.setMaxIteration(nsgaIIIHelper.getMaxIteration());
		
		nsgaIIIHelper.getLogisticsHelper().saveLogisticsJob(job);		

        logger.info("Logistics Job is saved...");  
	}
	
	private String getSolutionString(IntegerSolution solution) {
		
		List<Integer> list = new ArrayList<Integer>();
		
		for (int i = 0; i < solution.getNumberOfVariables(); i++) {			
			list.add(solution.getVariableValue(i));			
		}
		
		return list.toString();
	}
		
	private void previewLogisticsOperate() {		
		logger.debug("Job ID: " + nsgaIIIHelper.getJobId());
		logger.debug("Shipment Date: " + nsgaIIIHelper.getShipmentDate());
		logger.debug("Vehicle Type: " + nsgaIIIHelper.getVehicleType());
		logger.debug("No. of Shipments: " + nsgaIIIHelper.getShipmentList().size());
		logger.debug("No. of Available Routes: " + nsgaIIIHelper.getRouteList().size());
	}
	
	private void prepareInformation() {		

		scoreMapping = nsgaIIIHelper.getLogisticsHelper().retrieveAreaRouteScoreMap();
		nsgaIIIHelper.setJobId(jobId);
		nsgaIIIHelper.setScoreMapping(scoreMapping);
		
		vanList = nsgaIIIHelper.getLogisticsHelper().retrieveRoutesOfVan();
		bikeList = nsgaIIIHelper.getLogisticsHelper().retrieveRoutesOfBike();
		
		if(logger.isDebugEnabled()) {
			logger.debug("No. of Score Mapping: " + scoreMapping.size()); 

			logger.debug(""); 
			logger.debug("List of vans"); 
			vanList.stream().forEach(van -> {
				logger.debug(van.toString());
			});
			
			logger.debug(""); 
			logger.debug("List of bikes"); 
			bikeList.stream().forEach(bike -> {
				logger.debug(bike.toString());
			});
		}
	}
}
