package com.kmutt.sit.optimization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.util.PointSolution;

import com.kmutt.sit.jmetal.front.ModifiedFrontNormalizer;
import com.kmutt.sit.jmetal.runner.LogisticsNsgaIIIIntegerRunner;
import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;

import lombok.Setter;


@Controller
public class OptimizationManager {	

	private static Logger logger = LoggerFactory.getLogger(OptimizationManager.class);
	
	@Setter
	protected String jobId;
	
	@Autowired
	protected NsgaIIIHelper nsgaIIIHelper;
	
	protected List<DhlRoute> vanList;
	protected List<DhlRoute> bikeList;
	
	public OptimizationManager() {
		vanList = new ArrayList<DhlRoute>();
		bikeList = new ArrayList<DhlRoute>();
	}
	
	public void opitmize() {
		
        logger.info("OptimizationManager: Job ID: " + jobId + "\t start....."); 
                
        prepareInformation();
        
        saveLogisticsJob();      
        
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

        logger.info("OptimizationManager: Job ID: " + jobId + "\t finished..");  
	}
	
	protected void saveLogisticsJob() {
        // Insert table logistics_job
        if(nsgaIIIHelper.getLogisticsHelper().isOutputDatabaseEnabled()) {    
        	OptimizationHelper.saveLogisticsJob(nsgaIIIHelper);
            logger.info("Logistics Job is saved...");  
        } 
	}
	
	protected void allocateDailyShipmentForVan(String shipmentDate) {
        logger.info("allocateDailyShipmentForVan: start....."); 
		
		List<DhlShipment> shipmentList = nsgaIIIHelper.getLogisticsHelper().retrieveValidDailyShipmentForVan(shipmentDate)
				.stream().sorted(Comparator.comparingInt(DhlShipment::getShipmentKey)).collect(Collectors.toList());
		
		if(shipmentList.isEmpty()) {
			printShipmentEmpty(shipmentDate, "Van");			
		} else {
			execute("Van", shipmentList, vanList);
		}

        logger.info("allocateDailyShipmentForVan: finished..");  		
	}
	
	protected void allocateDailyShipmentForBike(String shipmentDate) {
        logger.info("allocateDailyShipmentForBike: start....."); 

		List<DhlShipment> shipmentList = nsgaIIIHelper.getLogisticsHelper().retrieveDailyValidShipmentForBike(shipmentDate)
											.stream().sorted(Comparator.comparingInt(DhlShipment::getShipmentKey)).collect(Collectors.toList());
		
		if(shipmentList.isEmpty()) {
			printShipmentEmpty(shipmentDate, "Bike");
		} else {
			execute("Bike", shipmentList, bikeList);
		}

        logger.info("allocateDailyShipmentForBike: finished..");  
	}
	
	protected void printShipmentEmpty(String shipmentDate, String vehicleType) {
		logger.warn("There is no shipment on {" + shipmentDate + "} for {" + vehicleType + "}.");
	}
	
	protected void execute(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList) {
		
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
        ModifiedFrontNormalizer frontNormalizer = new ModifiedFrontNormalizer(referenceFront);            
        @SuppressWarnings("unchecked")
		List<PointSolution> normalizedParetoSet = (List<PointSolution>) frontNormalizer.normalize(paretoSet);
        
        saveLogisticsJobProblem(paretoSet, normalizedParetoSet);
	}
	
	protected void saveLogisticsJobProblem(List<IntegerSolution> paretoSet, List<PointSolution> normalizedParetoSet) {		
		// Insert table logistics_job_problem        
        if(nsgaIIIHelper.getLogisticsHelper().isOutputDatabaseEnabled()) {
        	nsgaIIIHelper.setCurrentRun(0);
            LogisticsJobProblem problem = OptimizationHelper.saveLogisticsJobProblem(nsgaIIIHelper, paretoSet.size());
            logger.info("Logistics Job Problem is saved...");
            OptimizationHelper.saveLogisticsJobResults(nsgaIIIHelper, problem.getProblemId(), paretoSet, normalizedParetoSet);
            logger.info("Logistics Job Results are saved...");        	
        }
	}
	
	protected void prepareNsgaIIIHelperBeforeRunningNsgaIII(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList) {
		nsgaIIIHelper.setVehicleType(vehicleType);
		nsgaIIIHelper.setShipmentList(shipmentList);
		nsgaIIIHelper.setRouteList(routeList);
		nsgaIIIHelper.setFunFile(OptimizationHelper.getFileOutputName(nsgaIIIHelper, vehicleType.toLowerCase(), "fun"));
		nsgaIIIHelper.setVarFile(OptimizationHelper.getFileOutputName(nsgaIIIHelper, vehicleType.toLowerCase(), "var"));		
	}
		
	protected void previewLogisticsOperate() {		
		logger.debug("Job ID: " + nsgaIIIHelper.getJobId());
		logger.debug("Shipment Date: " + nsgaIIIHelper.getShipmentDate());
		logger.debug("Vehicle Type: " + nsgaIIIHelper.getVehicleType());
		logger.debug("No. of Shipments: " + nsgaIIIHelper.getShipmentList().size());
		logger.debug("No. of Available Routes: " + nsgaIIIHelper.getRouteList().size());	
		
		logger.debug("Problem Constraint Enabled: " + nsgaIIIHelper.getLogisticsHelper().isProblemConstraintEnabled());
		logger.debug("Problem Constraint Type: " + nsgaIIIHelper.getLogisticsHelper().getProblemConstraintType());
		logger.debug("Problem Constraint Allowed: " + nsgaIIIHelper.getLogisticsHelper().getUtilizationConstraintRate());
		logger.debug("Van Utilization Threshold: " + nsgaIIIHelper.getLogisticsHelper().getVanUtilizationThreshold());
		logger.debug("Van Familiarity Threshold: " + nsgaIIIHelper.getLogisticsHelper().getVanFamiliarityThreshold());;
		logger.debug("Bike Utilization Threshold: " + nsgaIIIHelper.getLogisticsHelper().getBikeUtilizationThreshold());
		logger.debug("Bike Familiarity Threshold: " + nsgaIIIHelper.getLogisticsHelper().getBikeFamiliarityThreshold());
		logger.debug("Utilization Version: " + nsgaIIIHelper.getLogisticsHelper().getUtilizationVersion());
		logger.debug("Familiarity Version: " + nsgaIIIHelper.getLogisticsHelper().getFamiliarityVersion());
		logger.debug("Area Responsibility Rate: " + nsgaIIIHelper.getLogisticsHelper().getAreaResponsibilityRate());
		logger.debug("Area History Rate: " + nsgaIIIHelper.getLogisticsHelper().getAreaHistoryRate());
		logger.debug("Area Responsibility Portion: " + nsgaIIIHelper.getLogisticsHelper().getAreaResponsibilityPortion());
		
		logger.debug("");
	}
	
	protected void prepareInformation() {		

		nsgaIIIHelper.setJobId(jobId);
		
		vanList = nsgaIIIHelper.getLogisticsHelper().retrieveRoutesOfVan();
		bikeList = nsgaIIIHelper.getLogisticsHelper().retrieveRoutesOfBike();
		
		if(logger.isDebugEnabled()) {

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
