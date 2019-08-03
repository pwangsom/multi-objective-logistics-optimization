package com.kmutt.sit.existing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlRouteAreaPortion;
import com.kmutt.sit.jpa.entities.DhlRoutePostcodeArea;
import com.kmutt.sit.jpa.entities.DhlRouteUtilization;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.utilities.LogisticsOptimizationHelper;

import lombok.Getter;

public class ExistingSolutionEvaluator {	

	private Logger logger = LoggerFactory.getLogger(ExistingSolutionEvaluator.class);
	
	@Getter
	private Integer noOfCar = 0;
	@Getter
	private Double utilization = 0.0;
	@Getter
	private Double familiarity = 0.0;
	
	private LogisticsOptimizationHelper helper;
	private String shipmentDate;
	private List<DhlShipment> shipmentList = new ArrayList<DhlShipment>();
	@Getter
	private List<DhlRoute> vehicleList = new ArrayList<DhlRoute>();
	
	public ExistingSolutionEvaluator(LogisticsOptimizationHelper helper, String shipmentDate, List<DhlShipment> shipmentList) {
		this.helper = helper;
		this.shipmentDate = shipmentDate;
		this.shipmentList = shipmentList;
	}
	
	public void evaluate() {
        logger.info("evaluate: start....."); 

        List<String> routes = shipmentList.stream().map(s -> s.getPudRte()).distinct().collect(Collectors.toList());
        vehicleList = helper.retrieveRoutesByRouteList(routes);
        noOfCar = routes.size();
        
        assessUtilizationFamiliarity();
        
        logger.info("evaluate: finished..");          
	}
	
	private void assessUtilizationFamiliarity() {
        logger.debug("assessUtilizationFamiliarity: start....."); 
                
		Double[] accumulateUtil = {0.0};
		Double[] areaResponsiblity = {0.0};
		Double[] frequentHistory = {0.0};
		Double[] areaShipmentPortion = {0.0};
		
		vehicleList.stream().forEach(vid -> {
			
			List<DhlShipment> shipmentsOfEachVehicleId = shipmentList.stream().filter(s -> s.getPudRte().equalsIgnoreCase(vid.getRoute())).collect(Collectors.toList());
			
			Double actualShipments = Double.valueOf(shipmentsOfEachVehicleId.size());
			DhlRouteUtilization routeUtil = helper.getRouteUtilizationMapping().get(vid.getRoute());
			
			accumulateUtil[0] += calculateUtilizationOfEachVehicle(actualShipments, routeUtil.getAllAvg().doubleValue());
			

			Double[] accumulateAreaResponsiblity = {0.0};
			Double[] accumulateFrequentHistory = {0.0};
			Double[] accumulateAreaShipmentPortion = {0.0};
			
			
			shipmentsOfEachVehicleId.stream().forEach(s -> {
				
				List<DhlRoutePostcodeArea> ra = helper.getRouteAreaList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vid.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
				List<DhlRouteAreaPortion> rap =  helper.getRouteAreaPortionList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vid.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
				
				if(!ra.isEmpty()) accumulateAreaResponsiblity[0] += 1.0;
				
				if(!rap.isEmpty())
					accumulateFrequentHistory[0] += rap.get(0).getAreaPortion().doubleValue();
				
			});
			
			String shipmentKeyList = shipmentsOfEachVehicleId.stream().map(s -> s.getShipmentKey()).collect(Collectors.toList()).toString();
			shipmentKeyList = shipmentKeyList.replace(" ", "").replace("[", "").replace("]", "");
			accumulateAreaShipmentPortion[0] += helper.getCalculateAreaPortion(shipmentDate, shipmentKeyList).doubleValue();
			
			areaResponsiblity[0] += accumulateAreaResponsiblity[0] / shipmentsOfEachVehicleId.size();
			frequentHistory[0] += accumulateFrequentHistory[0] / shipmentsOfEachVehicleId.size();
			areaShipmentPortion[0] += accumulateAreaShipmentPortion[0];
		});
		
		utilization = accumulateUtil[0] / vehicleList.size();
		familiarity = areaResponsiblity[0] + frequentHistory[0] + areaShipmentPortion[0];

        logger.debug("assessUtilizationFamiliarity: finished..");     
	}
	
	private Double calculateUtilizationOfEachVehicle(Double actualShipments, Double utilizedShipments) {
		return (1-(Math.abs(actualShipments-utilizedShipments)/utilizedShipments))*100;
	}

}
