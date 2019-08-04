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
	protected Integer noOfCar = 0;
	@Getter
	protected Double utilization = 0.0;
	@Getter
	protected Double familiarity = 0.0;
	
	protected LogisticsOptimizationHelper logisticsHelper;
	protected String shipmentDate;
	protected List<DhlShipment> shipmentList = new ArrayList<DhlShipment>();
	@Getter
	protected List<DhlRoute> vehicleList = new ArrayList<DhlRoute>();
	
	public ExistingSolutionEvaluator(LogisticsOptimizationHelper helper, String shipmentDate, List<DhlShipment> shipmentList) {
		this.logisticsHelper = helper;
		this.shipmentDate = shipmentDate;
		this.shipmentList = shipmentList;
	}
	
	public void evaluate() {
        logger.info("evaluate: start....."); 

        List<String> routes = shipmentList.stream().map(s -> s.getPudRte()).distinct().collect(Collectors.toList());
        vehicleList = logisticsHelper.retrieveRoutesByRouteList(routes);
        noOfCar = routes.size();
        
        assessUtilizationFamiliarity();
        
        logger.info("evaluate: finished..");          
	}
	
	protected void assessUtilizationFamiliarity() {
        logger.debug("assessUtilizationFamiliarity: start....."); 
                
		Double[] accumulateUtil = {0.0};
		Double[] areaResponsiblity = {0.0};
		Double[] frequentHistory = {0.0};
		Double[] areaShipmentPortion = {0.0};
		
		vehicleList.stream().forEach(vid -> {
			
			Double[] utilizationEachVehicle = {0.0};
			Double[] areaResponsiblityEachVehicle = {0.0};
			Double[] frequentHistoryEachVehicle = {0.0};
			Double[] areaShipmentPortionEachVehicle = {0.0};
			
			List<DhlShipment> shipmentsOfEachVehicleId = shipmentList.stream().filter(s -> s.getPudRte().equalsIgnoreCase(vid.getRoute())).collect(Collectors.toList());
			
			Double actualShipments = Double.valueOf(shipmentsOfEachVehicleId.size());
			DhlRouteUtilization routeUtil = logisticsHelper.getRouteUtilizationMapping().get(vid.getRoute());
			
			utilizationEachVehicle[0] = calculateUtilizationOfEachVehicle(actualShipments, routeUtil.getAllAvg().doubleValue());	
			accumulateUtil[0] += utilizationEachVehicle[0];			
			
			shipmentsOfEachVehicleId.stream().forEach(s -> {
				
				List<DhlRoutePostcodeArea> ra = logisticsHelper.getRouteAreaList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vid.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
				List<DhlRouteAreaPortion> rap =  logisticsHelper.getRouteAreaPortionList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vid.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
				
				if(!ra.isEmpty()) areaResponsiblityEachVehicle[0] += 1.0;
				
				if(!rap.isEmpty())
					frequentHistoryEachVehicle[0] += rap.get(0).getAreaPortion().doubleValue();
				
			});
			
			String shipmentKeyList = shipmentsOfEachVehicleId.stream().map(s -> s.getShipmentKey()).collect(Collectors.toList()).toString();
			shipmentKeyList = shipmentKeyList.replace(" ", "").replace("[", "").replace("]", "");
			areaShipmentPortionEachVehicle[0] += logisticsHelper.getCalculateAreaPortion(shipmentDate, shipmentKeyList).doubleValue();
			
			areaResponsiblityEachVehicle[0] /= Double.valueOf(shipmentsOfEachVehicleId.size());
			frequentHistoryEachVehicle[0] /= Double.valueOf(shipmentsOfEachVehicleId.size());
			
			areaResponsiblity[0] += areaResponsiblityEachVehicle[0];
			frequentHistory[0] += frequentHistoryEachVehicle[0];
			areaShipmentPortion[0] += areaShipmentPortionEachVehicle[0];			

			logger.debug(String.format("%d, %s: having %d shipments -> %.4f, %.4f, %.4f, %.4f", vid.getChromosomeId(), vid.getRoute(),
										shipmentsOfEachVehicleId.size(), utilizationEachVehicle[0],
										areaResponsiblityEachVehicle[0],
										frequentHistoryEachVehicle[0],
										areaShipmentPortionEachVehicle[0]));
			
			logger.debug("");
		});
		
		utilization = accumulateUtil[0] / vehicleList.size();
		familiarity = areaResponsiblity[0] + frequentHistory[0] + areaShipmentPortion[0];

        logger.debug("assessUtilizationFamiliarity: finished..");     
	}
	
	protected Double calculateUtilizationOfEachVehicle(Double actualShipments, Double utilizedShipments) {
		return (1-(Math.abs(actualShipments-utilizedShipments)/utilizedShipments))*100;
	}

}
