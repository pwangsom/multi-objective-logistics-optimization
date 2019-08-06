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
                
		Double[] accumulateUtilization = {0.0};
		Double[] accumulateFamiliarity = {0.0};
		
		vehicleList.stream().forEach(vid -> {
			
			List<DhlShipment> shipmentsOfEachVehicle = getShipmentsOfEachVehicle(vid);
			
			Double[] results = computeUtilizationFamiliarityEachVehicle(vid, shipmentsOfEachVehicle);
			
			Double[] utilizationEachVehicle = {0.0};
			Double[] areaResponsiblityEachVehicle = {0.0};
			Double[] frequentHistoryEachVehicle = {0.0};
			Double[] familiarityEachVehicle = {0.0};
			
			utilizationEachVehicle[0] = results[0];
			areaResponsiblityEachVehicle[0] = results[1];
			frequentHistoryEachVehicle[0] = results[2];
			
			familiarityEachVehicle[0] = ((areaResponsiblityEachVehicle[0] + frequentHistoryEachVehicle[0]) / Double.valueOf(shipmentsOfEachVehicle.size() * 2.0)) * 100.0;
			
			accumulateUtilization[0] += utilizationEachVehicle[0];
			accumulateFamiliarity[0] += familiarityEachVehicle[0];		
			

			logger.trace(String.format("%d, %s: having %d shipments -> %.4f, %.4f {%.4f, %.4f}", vid.getChromosomeId(), vid.getRoute(),
					shipmentsOfEachVehicle.size(), utilizationEachVehicle[0], familiarityEachVehicle[0], areaResponsiblityEachVehicle[0], frequentHistoryEachVehicle[0]));
			
			logger.trace("");
		});
		
		utilization = accumulateUtilization[0] / Double.valueOf(vehicleList.size());
		familiarity = accumulateFamiliarity[0] / Double.valueOf(vehicleList.size());

        logger.debug("assessUtilizationFamiliarity: finished..");     
	}
	
	protected List<DhlShipment> getShipmentsOfEachVehicle(DhlRoute vehicle) {
        logger.trace("getShipmentsOfEachVehicle: " + vehicle.getRoute()); 
		return shipmentList.stream().filter(s -> s.getPudRte().equalsIgnoreCase(vehicle.getRoute())).collect(Collectors.toList());
	}
	
	protected Double[] computeUtilizationFamiliarityEachVehicle(DhlRoute vehicle, List<DhlShipment> shipmentsEachVehicle) {
		
		// Double[] results = {0.0, 0.0, 0.0, 0.0};
		
		Double[] results = {0.0, 0.0, 0.0};
		
		/*
		 *  results[0] for utilizationEachVehicle;
		 *  results[1] for areaResponsiblityEachVehicle;
		 *  results[2] for frequentHistoryEachVehicle;
		 *  results[3] for areaShipmentPortionEachVehicle;
		 */
		
		
		Double actualShipments = Double.valueOf(shipmentsEachVehicle.size());
		DhlRouteUtilization routeUtil = logisticsHelper.getRouteUtilizationMapping().get(vehicle.getRoute());
		
		results[0] = calculateUtilizationOfEachVehicle(actualShipments, routeUtil.getAllAvg().doubleValue());		
		
		shipmentsEachVehicle.stream().forEach(s -> {
			
			List<DhlRoutePostcodeArea> ra = logisticsHelper.getRouteAreaList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vehicle.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
			List<DhlRouteAreaPortion> rap =  logisticsHelper.getRouteAreaPortionList().stream().filter(row -> row.getRoute().equalsIgnoreCase(vehicle.getRoute()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
			
			if(!ra.isEmpty()) results[1] += 1.0;
			
			if(!rap.isEmpty())
				results[2] += rap.get(0).getAreaPortion().doubleValue();
			
		});
		
		/*
		 * String shipmentKeyList = shipmentsEachVehicle.stream().map(s ->
		 * s.getShipmentKey()).collect(Collectors.toList()).toString(); shipmentKeyList
		 * = shipmentKeyList.replace(" ", "").replace("[", "").replace("]", "");
		 * results[3] += logisticsHelper.getCalculateAreaPortion(shipmentDate,
		 * shipmentKeyList).doubleValue();
		 */			
		
		return results;
		
	}
	
	
	protected Double calculateUtilizationOfEachVehicle(Double actualShipments, Double utilizedShipments) {
		return (1-(Math.abs(actualShipments-utilizedShipments)/utilizedShipments))*100.0;
	}

}
