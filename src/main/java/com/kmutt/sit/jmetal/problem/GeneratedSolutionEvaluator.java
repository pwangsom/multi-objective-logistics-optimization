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
        
        if(logger.isTraceEnabled()) printSolutionList();
        
        logger.debug("evaluate: finished..");          
	}
	
	@Override
	protected List<DhlShipment> getShipmentsOfEachVehicle(DhlRoute vehicle) {
        
		List<ChromosomeRepresentation> findingShipmentIndexOfVehicleId = solutionList.stream().filter(item -> item.chromosomeValue == vehicle.getChromosomeId()).collect(Collectors.toList());
		List<DhlShipment> shipmentsOfEachVehicle = findingShipmentIndexOfVehicleId.stream().map(c -> c.getShipment()).collect(Collectors.toList());
				
        logger.trace("getShipmentsOfEachVehicle: " + vehicle.getRoute() + " having " + findingShipmentIndexOfVehicleId.size() + " shipments"); 
        logger.trace(findingShipmentIndexOfVehicleId.stream().map(m -> m.getChromosomeIndex()).collect(Collectors.toList()).toString());
        logger.trace(shipmentsOfEachVehicle.stream().map(m -> m.getShipmentKey()).collect(Collectors.toList()).toString());
		
		return shipmentsOfEachVehicle;	
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
			
			
			logger.trace(log);
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
