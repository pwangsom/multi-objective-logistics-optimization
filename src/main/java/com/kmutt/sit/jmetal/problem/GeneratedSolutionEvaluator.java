package com.kmutt.sit.jmetal.problem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.existing.ExistingSolutionEvaluator;
import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;

import lombok.Getter;
import lombok.Setter;

public class GeneratedSolutionEvaluator extends ExistingSolutionEvaluator {	

	private Logger logger = LoggerFactory.getLogger(ExistingSolutionEvaluator.class);
	
	private List<ChromosomeRepresentation> solutionList;
	private IntegerSolution solution;
	private NsgaIIIHelper nsgaIIIHelper;
	
	public GeneratedSolutionEvaluator(IntegerSolution solution, NsgaIIIHelper helper) {
		super(helper.getLogisticsHelper(), helper.getShipmentDate(), helper.getShipmentList());
		// TODO Auto-generated constructor stub
		this.solutionList = new ArrayList<ChromosomeRepresentation>();
		this.solution = solution;
		this.nsgaIIIHelper = helper;
	}
	
	@Override
	public void evaluate() {
        logger.debug("evaluate: start....."); 

        converToSolutionList();
        List<Integer> vehicleIntegerList = solutionList.stream().map(item -> item.getChromosomeValue()).distinct().sorted().collect(Collectors.toList());
		vehicleList = solutionList.stream().map(s -> s.getRoute()).distinct().sorted(Comparator.comparingInt(DhlRoute::getChromosomeId)).collect(Collectors.toList());		
		noOfCar = vehicleList.size();
		
		logger.debug("Integer list: " + vehicleIntegerList.size() + ", Route list " + vehicleList.size());
		logger.debug(vehicleIntegerList.toString());
		logger.debug(vehicleList.stream().map(r -> r.getChromosomeId()).collect(Collectors.toList()).toString());
        
		printProblemSize();
		
        assessUtilizationFamiliarity();
        
        if(logger.isDebugEnabled()) printSolutionList();
        
        logger.debug("evaluate: finished..");          
	}
	
	@Override
	protected void assessUtilizationFamiliarity() {
        logger.debug("assessUtilizationFamiliarity: start....."); 
                
		Double[] accumulateUtil = {0.0};
		Double[] areaResponsiblity = {0.0};
		Double[] frequentHistory = {0.0};
		// Double[] areaShipmentPortion = {0.0};
		
		vehicleList.stream().forEach(vid -> {			
			
			List<ChromosomeRepresentation> findingShipmentIndexOfVehicleId = solutionList.stream().filter(item -> item.chromosomeValue == vid.getChromosomeId()).collect(Collectors.toList());
			
			List<DhlShipment> shipmentsOfEachVehicleId = findingShipmentIndexOfVehicleId.stream().map(c -> c.getShipment()).collect(Collectors.toList());			
			
			Double[] results = computeUtilizationFamiliarityEachVehicle(vid, shipmentsOfEachVehicleId);
			
			Double[] utilizationEachVehicle = {0.0};
			Double[] areaResponsiblityEachVehicle = {0.0};
			Double[] frequentHistoryEachVehicle = {0.0};
			// Double[] areaShipmentPortionEachVehicle = {0.0};
			
			utilizationEachVehicle[0] = results[0];
			areaResponsiblityEachVehicle[0] = results[1];
			frequentHistoryEachVehicle[0] = results[2];
			// areaShipmentPortionEachVehicle[0] = results[3];
			
			areaResponsiblity[0] += areaResponsiblityEachVehicle[0];
			frequentHistory[0] += frequentHistoryEachVehicle[0];
			// areaShipmentPortion[0] += areaShipmentPortionEachVehicle[0];		
			
		});
		
		utilization = accumulateUtil[0] / Double.valueOf(vehicleList.size());
		familiarity = areaResponsiblity[0] + frequentHistory[0];

        logger.debug("assessUtilizationFamiliarity: finished..");     
	}
	
	private void converToSolutionList() {
		for (int i = 0; i < solution.getNumberOfVariables(); i++) {
			ChromosomeRepresentation slot = new ChromosomeRepresentation();
			slot.setChromosomeIndex(i);
			slot.setChromosomeValue(solution.getVariableValue(i));
			slot.setShipment(nsgaIIIHelper.getShipmentList().get(i));
			slot.setRoute(nsgaIIIHelper.getRouteList().stream().filter(r -> r.getChromosomeId() == slot.getChromosomeValue()).collect(Collectors.toList()).get(0));
			
			solutionList.add(slot);			
		}
	}
	
	private void printProblemSize() {
		logger.debug("Shipment size: " + solutionList.size() + ", Vehicle size: " + vehicleList.size());
	}
	
	
	private void printSolutionList() {
		solutionList.stream().forEach(s -> {
			
			String log = String.format("[index: %d, area: %d, id: %d, route: %s]", 
					s.getChromosomeIndex(), s.getShipment().getAreaCode(), s.getChromosomeValue(), s.getRoute().getRoute());
			
			
			logger.debug(log);
		});
	}
	
	@Getter
	@Setter
	public class ChromosomeRepresentation{
		private Integer chromosomeIndex;
		private Integer chromosomeValue;
		private DhlShipment shipment;
		private DhlRoute route;
	}
}
