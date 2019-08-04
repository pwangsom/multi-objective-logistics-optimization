package com.kmutt.sit.existing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobResult;
import com.kmutt.sit.utilities.JavaUtils;
import com.kmutt.sit.utilities.LogisticsOptimizationHelper;

import lombok.Setter;


@Controller
public class ExistingSolutionManager {

	private static Logger logger = LoggerFactory.getLogger(ExistingSolutionManager.class);
	
	@Setter
	private String jobId;
	
	@Autowired
	private LogisticsOptimizationHelper logisticsHelper;
	
	private List<DhlRoute> vanList;
	private List<DhlRoute> bikeList;
	
	public ExistingSolutionManager() {
		vanList = new ArrayList<DhlRoute>();
		bikeList = new ArrayList<DhlRoute>();
	}
	
	public void evaluate() {
		
        logger.info("EvaluationManager: Job ID: " + jobId + "\t start.....");
        
        prepareInformation();
        
        List<String> shipmentDateList = logisticsHelper.retrieveShipmentDateAllList();
        
        logger.debug("" + shipmentDateList);
        
        shipmentDateList.stream().forEach(day -> {
        	
        	if(logisticsHelper.getVehicleTypes().contains("Van")) {
        		evaluateDailySolutionOfVan(day);
        	}
        	
        	// Allocation shipment for bike
        	if(logisticsHelper.getVehicleTypes().contains("Bike")) {
        		evaluateDailySolutionOfBike(day);        		
        	}
        	
        });

        logger.info("EvaluationManager:: Job ID: " + jobId + "\t finished..");  
	}
	
	private void evaluateDailySolutionOfVan(String shipmentDate) {
        logger.info("evaluateDailySolutionOfVan: start....."); 
		
		List<DhlShipment> shipmentList = logisticsHelper.retrieveValidDailyShipmentForVan(shipmentDate)
											.stream().sorted(Comparator.comparingInt(DhlShipment::getShipmentKey)).collect(Collectors.toList());
		logger.debug("Date: " + shipmentDate + ", No Of Shipment: " + shipmentList.size());
		
		if(!shipmentList.isEmpty()) {
			ExistingSolutionEvaluator solution = new ExistingSolutionEvaluator(logisticsHelper, shipmentDate, shipmentList);
			solution.evaluate();
			
			LogisticsJobProblem problem = saveLogisticsJobProblem(shipmentDate, "Van", shipmentList, vanList);
			saveLogisticsJobResults(problem.getProblemId(), solution.getNoOfCar(), solution.getUtilization(), solution.getFamiliarity(), vanList, shipmentList);
		}

        logger.info("evaluateDailySolutionOfVan: finished..");  		
	}
	
	private void evaluateDailySolutionOfBike(String shipmentDate) {
        logger.info("evaluateDailySolutionOfBike: start....."); 
		
		List<DhlShipment> shipmentList = logisticsHelper.retrieveDailyValidShipmentForBike(shipmentDate)
											.stream().sorted(Comparator.comparingInt(DhlShipment::getShipmentKey)).collect(Collectors.toList());
		logger.debug("Date: " + shipmentDate + ", No Of Shipment: " + shipmentList.size());
		
		if(!shipmentList.isEmpty()) {
			ExistingSolutionEvaluator solution = new ExistingSolutionEvaluator(logisticsHelper, shipmentDate, shipmentList);
			solution.evaluate();
			
			LogisticsJobProblem problem = saveLogisticsJobProblem(shipmentDate, "Bike", shipmentList, bikeList);
			saveLogisticsJobResults(problem.getProblemId(), solution.getNoOfCar(), solution.getUtilization(), solution.getFamiliarity(), bikeList, shipmentList);
		}

        logger.info("evaluateDailySolutionOfBike: finished..");  		
	}
	
	private LogisticsJobProblem saveLogisticsJobProblem(String shipmentDate, String vehicle, List<DhlShipment> shipments, List<DhlRoute> routes) {
		LogisticsJobProblem problem = new LogisticsJobProblem();
		problem.setJobId(jobId);
		problem.setShipmentDate(shipmentDate);
		problem.setVehicleType(vehicle);
		
		String shipmentList = shipments.stream().map(s -> s.getShipmentKey()).collect(Collectors.toList()).toString();
		String routeList = routes.stream().map(r -> r.getChromosomeId()).collect(Collectors.toList()).toString();
		
		problem.setShipmentList(JavaUtils.removeStringOfList(shipmentList));
		problem.setRouteList(JavaUtils.removeStringOfList(routeList));
		problem.setNoOfSolutions(1);
		problem.setSolutionType("existing");
		
		return logisticsHelper.saveLogisticsJobProblem(problem);
	}
	
	private void saveLogisticsJobResults(Integer problemId, Integer noOfCar, Double utilization, Double familiarity, List<DhlRoute> routes, List<DhlShipment> shipmentList) {
		
		LogisticsJobResult result = new LogisticsJobResult();
		result.setProblemId(problemId);
		result.setSolutionIndex(0);
		
		List<Integer> routeList = new ArrayList<Integer>();
		
		shipmentList.stream().forEach(s -> {
			DhlRoute route = routes.stream().filter(r -> r.getRoute().equalsIgnoreCase(s.getPudRte())).collect(Collectors.toList()).get(0);
			routeList.add(route.getChromosomeId());
		});
		
		result.setSolutionDetail(JavaUtils.removeStringOfList(routeList.toString()));
		
		result.setObjective1(BigDecimal.valueOf(noOfCar));
		result.setObjective2(BigDecimal.valueOf(utilization * -1));
		result.setObjective3(BigDecimal.valueOf(familiarity * -1));
		
		result.setNormalizedObjective1(BigDecimal.valueOf(0.0));
		result.setNormalizedObjective2(BigDecimal.valueOf(0.0));
		result.setNormalizedObjective3(BigDecimal.valueOf(0.0));
		
		logisticsHelper.saveLogisticsJobResult(result);
	}
	
	private void prepareInformation() {
		vanList = logisticsHelper.retrieveRoutesOfVan();
		bikeList = logisticsHelper.retrieveRoutesOfBike();
	}

}
