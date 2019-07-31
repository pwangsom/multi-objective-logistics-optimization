package com.kmutt.sit.existing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.kmutt.sit.existing.evaluation.ExistingSolution;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobResult;
import com.kmutt.sit.utilities.JavaUtils;

import lombok.Setter;


@Controller
public class EvaluationManager {

	private static Logger logger = LoggerFactory.getLogger(EvaluationManager.class);
	
	@Setter
	private String jobId;
	
	@Autowired
	private EvaluationHelper evaluationHelper;
	
	private List<DhlRoute> vanList;
	private List<DhlRoute> bikeList;
	
	public EvaluationManager() {
		vanList = new ArrayList<DhlRoute>();
		bikeList = new ArrayList<DhlRoute>();
	}
	
	public void evaluate() {
		
        logger.info("EvaluationManager: Job ID: " + jobId + "\t start.....");
        
        prepareInformation();
        
        List<String> shipmentDateList = evaluationHelper.retrieveShipmentDateList();
        
        logger.debug("" + shipmentDateList);
        
        shipmentDateList.stream().forEach(day -> {
        	evaluateDailySolutionOfVan(day);
        	evaluateDailySolutionOfBike(day);
        });

        logger.info("EvaluationManager:: Job ID: " + jobId + "\t finished..");  
	}
	
	private void evaluateDailySolutionOfVan(String shipmentDate) {
        logger.info("evaluateDailySolutionOfVan: start....."); 
		
		List<DhlShipment> shipmentList = evaluationHelper.retrieveDailyShipmentForVan(shipmentDate);
		logger.debug("Date: " + shipmentDate + ", No Of Shipment: " + shipmentList.size());
		
		if(!shipmentList.isEmpty()) {
			ExistingSolution solution = new ExistingSolution(evaluationHelper, shipmentDate, shipmentList);
			solution.evaluate();
			
			LogisticsJobProblem problem = saveLogisticsJobProblem(shipmentDate, "Van", shipmentList, vanList);
			saveLogisticsJobResults(problem.getProblemId(), solution.getNoOfCar(), solution.getUtilization(), solution.getFamiliarity(), solution.getRouteList());
		}

        logger.info("evaluateDailySolutionOfVan: finished..");  		
	}
	
	private void evaluateDailySolutionOfBike(String shipmentDate) {
        logger.info("evaluateDailySolutionOfBike: start....."); 
		
		List<DhlShipment> shipmentList = evaluationHelper.retrieveDailyShipmentForBike(shipmentDate);
		logger.debug("Date: " + shipmentDate + ", No Of Shipment: " + shipmentList.size());
		
		if(!shipmentList.isEmpty()) {
			ExistingSolution solution = new ExistingSolution(evaluationHelper, shipmentDate, shipmentList);
			solution.evaluate();
			
			LogisticsJobProblem problem = saveLogisticsJobProblem(shipmentDate, "Bike", shipmentList, bikeList);
			saveLogisticsJobResults(problem.getProblemId(), solution.getNoOfCar(), solution.getUtilization(), solution.getFamiliarity(), solution.getRouteList());
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
		
		return evaluationHelper.saveLogisticsJobProblem(problem);
	}
	
	private void saveLogisticsJobResults(Integer problemId, Integer noOfCar, Double utilization, Double familiarity, List<DhlRoute> routes) {
		
		LogisticsJobResult result = new LogisticsJobResult();
		result.setProblemId(problemId);
		result.setSolutionIndex(0);
		

		String routeList = routes.stream().map(r -> r.getChromosomeId()).collect(Collectors.toList()).toString();
		result.setSolutionDetail(JavaUtils.removeStringOfList(routeList));
		result.setObjective1(BigDecimal.valueOf(noOfCar));
		result.setObjective2(BigDecimal.valueOf(utilization));
		result.setObjective3(BigDecimal.valueOf(familiarity));
		
		result.setNormalizedObjective1(BigDecimal.valueOf(0.0));
		result.setNormalizedObjective2(BigDecimal.valueOf(0.0));
		result.setNormalizedObjective3(BigDecimal.valueOf(0.0));
		
		evaluationHelper.saveLogisticsJobResult(result);
	}
	
	private void prepareInformation() {
		vanList = evaluationHelper.retrieveRoutesOfVan();
		bikeList = evaluationHelper.retrieveRoutesOfBike();
	}

}
