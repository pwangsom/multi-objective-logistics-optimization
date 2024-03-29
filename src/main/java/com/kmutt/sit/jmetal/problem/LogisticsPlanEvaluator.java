package com.kmutt.sit.jmetal.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.solution.IntegerSolution;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;

import lombok.Getter;
import lombok.Setter;


public class LogisticsPlanEvaluator {

	private Logger logger = LoggerFactory.getLogger(LogisticsPlanEvaluator.class);
	
	private List<ChromosomeRepresentation> solutionList;
	private List<Integer> vehicleList;
	
	@Getter
	private Integer noOfCar = 0;
	@Getter
	private Integer utilization = 0;
	@Getter
	private Integer familiarity = 0;
	
	private IntegerSolution solution;
	private NsgaIIIHelper helper;
	
	
	public LogisticsPlanEvaluator(IntegerSolution solution, NsgaIIIHelper helper) {
		this.solutionList = new ArrayList<ChromosomeRepresentation>();
		this.vehicleList = new ArrayList<Integer>();
		this.solution = solution;
		this.helper = helper;
	}
	
	public void evaluate() {
		converToSolutionList();
		mapShipmentsToVehicle();
		
		if(logger.isDebugEnabled()) printSolutionList();
	}
	
	private void mapShipmentsToVehicle() {
		
		int[] util = {0};
		int[] score = {0};
		
		int[] eachVehicleUtil = {0};
		int[] eachVehicleScore = {0};
		
		vehicleList.stream().forEach(vid -> {
			
			eachVehicleUtil[0] = 0;
			eachVehicleScore[0] = 0;
			
			// Finding route coresponding with chromosome id
			DhlRoute route = helper.getRouteList().stream().filter(r -> r.getChromosomeId() == vid).collect(Collectors.toList()).get(0);
			
			List<ChromosomeRepresentation> findingIndexOfVehicleId = solutionList.stream().filter(item -> item.chromosomeValue == vid).collect(Collectors.toList());
			
			List<DhlShipment> shipmentOfEachVehicleIdList = new ArrayList<DhlShipment>();
			
			findingIndexOfVehicleId.stream().forEach(item -> {
				shipmentOfEachVehicleIdList.add(helper.getShipmentList().get(item.getChromosomeIndex()));
			});

			eachVehicleUtil[0] = determineUtilizationOfEachVehicle(route, shipmentOfEachVehicleIdList.size());
			eachVehicleScore[0] = determineEffortScoreOfEachVehicle(route, shipmentOfEachVehicleIdList);
			
			util[0] = util[0] + eachVehicleUtil[0];
			score[0] = score[0] + eachVehicleScore[0];
		});
		
		utilization = util[0];
		familiarity = score[0];
	}
	
	private Integer determineUtilizationOfEachVehicle(DhlRoute route, Integer noOfShipment) {
		Integer util = 0;
		
		Integer minUtil = (int) Math.round(route.getMaxUtilization() * helper.getMinUtilization());
		
		if(noOfShipment < minUtil) {
			util = minUtil - noOfShipment;
		} else if(noOfShipment > route.getMaxUtilization()) {
			util = (noOfShipment - route.getMaxUtilization()) * 2;
		}

		return util;
	}
	
	private Integer determineEffortScoreOfEachVehicle(DhlRoute route, List<DhlShipment> shipmentOfEachVehicleIdList) {
		int[] score = {0};
		
		// Accomulate effort score of each vehicle
		shipmentOfEachVehicleIdList.stream().forEach(shipment -> {
			if(shipment.getAreaCode() != 0) {
				score[0] = score[0] + helper.getScoreMapping().get(shipment.getAreaCode() + "-" + route.getRoute()); 
			} else {
				score[0] = score[0] + helper.getNotfoundScore();
			}
		});
		
		return score[0];
	}
	
	private void converToSolutionList() {
		for (int i = 0; i < solution.getNumberOfVariables(); i++) {
			ChromosomeRepresentation slot = new ChromosomeRepresentation();
			slot.setChromosomeIndex(i);
			slot.setChromosomeValue(solution.getVariableValue(i));
			slot.setShipment(helper.getShipmentList().get(i));
			slot.setRoute(helper.getRouteList().stream().filter(r -> r.getChromosomeId() == slot.getChromosomeValue()).collect(Collectors.toList()).get(0));
			
			solutionList.add(slot);			
		}
		
		vehicleList = solutionList.stream().map(item -> item.getChromosomeValue()).distinct().sorted().collect(Collectors.toList());
		noOfCar = vehicleList.size();
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
