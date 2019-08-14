package com.kmutt.sit.existing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	protected Map<String, Integer> routeActualShipments;
	protected Map<String, Integer> routeUtilizedShipments;
	
	@Getter
	protected Double utilizationConstraintValue = 0.0;
	@Getter
	protected Double familiarityConstraintValue = 0.0;
	
	public ExistingSolutionEvaluator(LogisticsOptimizationHelper helper, String shipmentDate, List<DhlShipment> shipmentList) {
		this.logisticsHelper = helper;
		this.shipmentDate = shipmentDate;
		this.shipmentList = shipmentList;
		this.routeActualShipments = new HashMap<String, Integer>();
		this.routeUtilizedShipments = new HashMap<String, Integer>();
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
                
		Double[] accumulateUtilization = {0.0};
		Double[] accumulateFamiliarity = {0.0};
		
		vehicleList.stream().forEach(vid -> {
			
			List<DhlShipment> shipmentsOfEachVehicle = getShipmentsOfEachVehicle(vid);
			routeActualShipments.put(vid.getRoute(), shipmentsOfEachVehicle.size());			
			
			Double[] results = computeUtilizationFamiliarityEachVehicle(vid, shipmentsOfEachVehicle);
			
			accumulateUtilization[0] += results[0];
			accumulateFamiliarity[0] += results[1];	

			logger.trace(String.format("%d, %s: having %d shipments -> %.4f, %.4f",
					vid.getChromosomeId(), vid.getRoute(),
					shipmentsOfEachVehicle.size(), results[0], results[1]));
			
			logger.trace("");
		});
		
		utilization = accumulateUtilization[0] / Double.valueOf(vehicleList.size());
		familiarity = accumulateFamiliarity[0] / Double.valueOf(vehicleList.size());		
		
		Double[] resultCons = evaluateConstraints(utilization, familiarity, vehicleList.get(0));
		
		utilizationConstraintValue = resultCons[0];
		familiarityConstraintValue = resultCons[1];

        logger.debug("assessUtilizationFamiliarity: finished..");     
	}
	
	protected Double[] evaluateConstraints(Double utilzation, Double familiarity, DhlRoute route) {
		Double[] results = {0.0, 0.0};
		
		Double utilizationThreshold = 0.0;
		Double familiarityThreshold = 0.0;
		
		if(route.getVehicleType().equalsIgnoreCase("Bike")) {
			utilizationThreshold = logisticsHelper.getBikeUtilizationThreshold();
			familiarityThreshold = logisticsHelper.getBikeFamiliarityThreshold();
		} else {
			utilizationThreshold = logisticsHelper.getVanUtilizationThreshold();
			familiarityThreshold = logisticsHelper.getVanFamiliarityThreshold();
		}
		
		if(Double.compare(utilzation, utilizationThreshold) < 0) results[0] = utilzation - utilizationThreshold;
		if(Double.compare(familiarity, familiarityThreshold) < 0) results[1] = familiarity - familiarityThreshold;
		
		return results;
	}
	
	protected List<DhlShipment> getShipmentsOfEachVehicle(DhlRoute vehicle) {
        logger.trace("getShipmentsOfEachVehicle: " + vehicle.getRoute()); 
		return shipmentList.stream().filter(s -> s.getPudRte().equalsIgnoreCase(vehicle.getRoute())).collect(Collectors.toList());
	}
	
	protected Double[] computeUtilizationFamiliarityEachVehicle(DhlRoute vehicle, List<DhlShipment> shipmentsEachVehicle) {
		
		Double[] results = {0.0, 0.0};
				
		results[0] = calculateUtilizationEachVehicle(vehicle, shipmentsEachVehicle.size());
		
		Double[] familiarity = calculateFamiliarityEachVehicle(vehicle, shipmentsEachVehicle);
		
		if(logisticsHelper.getFamiliarityVersion() == 2) {
			results[1] = calculateFamiliarityVersion2(familiarity[0], familiarity[1], shipmentsEachVehicle.size());
			
		} else { // default version 1
			results[1] = calculateFamiliarityDefault(familiarity[0], familiarity[1], shipmentsEachVehicle.size());			
		}
		
		return results;
		
	}
	
	protected Double calculateFamiliarityDefault(Double areaResponsiblityEachVehicle, Double frequentHistoryEachVehicle, int noOfShipments) {		
		return ((areaResponsiblityEachVehicle + frequentHistoryEachVehicle) / (Double.valueOf(noOfShipments * logisticsHelper.getAreaResponsibilityRate()) + Double.valueOf(noOfShipments * logisticsHelper.getAreaHistoryRate()))) * 100.0;	
	}
	
	protected Double calculateFamiliarityVersion2(Double areaResponsiblityEachVehicle, Double frequentHistoryEachVehicle, int noOfShipments) {
		
		Double areaResponsiblityPortion = logisticsHelper.getAreaResponsibilityPortion();
		Double areaHistoryPortion = 1 - areaResponsiblityPortion;
		
		areaResponsiblityEachVehicle /= Double.valueOf(noOfShipments * logisticsHelper.getAreaResponsibilityRate());
		frequentHistoryEachVehicle /= Double.valueOf(noOfShipments * logisticsHelper.getAreaHistoryRate());
		
		areaResponsiblityEachVehicle *= areaResponsiblityPortion;
		frequentHistoryEachVehicle *= areaHistoryPortion;
		
		return (areaResponsiblityEachVehicle + frequentHistoryEachVehicle) * 100.0;	
	}
	
	protected Double[] calculateFamiliarityEachVehicle(DhlRoute vehicle, List<DhlShipment> shipmentsEachVehicle) {		

		Double[] results = {0.0, 0.0};
		
		shipmentsEachVehicle.stream().forEach(s -> {
			
			List<DhlRoutePostcodeArea> ra = logisticsHelper.getRouteAreaList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vehicle.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
			List<DhlRouteAreaPortion> rap =  logisticsHelper.getRouteAreaPortionList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vehicle.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());

			if(!ra.isEmpty()) results[0] += logisticsHelper.getAreaResponsibilityRate();		
			
			if(!rap.isEmpty())
				results[1] += (rap.get(0).getAreaPortion().doubleValue() * logisticsHelper.getAreaHistoryRate());	
		});
		
		
		return results;		
	}
	
	protected Double calculateUtilizationEachVehicle(DhlRoute vehicle, Integer shipmentSize) {
        logger.trace("calculateUtilizationEachVehicle: " + logisticsHelper.getUtilizationVersion()); 
        
		Double result = 0.0;
		
		Double actualShipments = Double.valueOf(shipmentSize);
		DhlRouteUtilization routeUtil = logisticsHelper.getRouteUtilizationMapping().get(vehicle.getRoute());
		Double avgShipmentMonth = routeUtil.getAllAvg().doubleValue();
		
		Double avgShipmentDay = 0.0;
		
		if(logisticsHelper.getDailyRouteAreaUtilizationMapping().containsKey(shipmentDate + "_" + vehicle.getRoute())) {
			avgShipmentDay = logisticsHelper.getDailyRouteAreaUtilizationMapping().get(shipmentDate + "_" + vehicle.getRoute()).getUtilizedShipments().doubleValue();
		}
		
		Double utilizedShipments = avgShipmentMonth;
		
		if(logisticsHelper.getUtilizationVersion() == 3) {
			if(avgShipmentDay > utilizedShipments) utilizedShipments = avgShipmentDay;			
			result = calculateUtilizationVersion3(vehicle, actualShipments, utilizedShipments);
			
			
		} else if(logisticsHelper.getUtilizationVersion() == 4) {
			if(avgShipmentDay > utilizedShipments) utilizedShipments = avgShipmentDay;	
			result = calculateUtilizationVersion4(vehicle, actualShipments, utilizedShipments);
			
		} else {
			result = calculateUtilizationDefault(actualShipments, utilizedShipments);			
		}
		
		routeUtilizedShipments.put(vehicle.getRoute(), utilizedShipments.intValue());
		
		return result;
	}
	
	
	protected Double calculateUtilizationDefault(Double actualShipments, Double utilizedShipments) {
		return (1-(Math.abs(actualShipments-utilizedShipments)/utilizedShipments))*100.0;
	}
	
	protected Double calculateUtilizationVersion3(DhlRoute vehicle, Double actualShipments, Double utilizedShipments) {
		Double utils = 0.0;
		
		utils = calculateUtilizationDefault(actualShipments, utilizedShipments);
		
		return utils;
	}
	
	protected Double calculateUtilizationVersion4(DhlRoute vehicle, Double actualShipments, Double utilizedShipments) {
		Double utils = 0.0;
		
		if(actualShipments <= utilizedShipments) {
			utils = (actualShipments / utilizedShipments) * 100.0;
		} else {			
			Double multiply = Math.ceil(actualShipments / utilizedShipments);
			utils = (1-(((actualShipments-utilizedShipments)*multiply)/utilizedShipments))*100.0;			
		}
		
		return utils;
	}

}
