package com.kmutt.sit.existing.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kmutt.sit.existing.EvaluationHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlRouteAreaPortion;
import com.kmutt.sit.jpa.entities.DhlRoutePostcodeArea;
import com.kmutt.sit.jpa.entities.DhlShipment;

import lombok.Getter;

public class ExistingSolution {	

	private Logger logger = LoggerFactory.getLogger(ExistingSolution.class);
	
	@Getter
	private Integer noOfCar = 0;
	@Getter
	private Double utilization = 0.0;
	@Getter
	private Double familiarity = 0.0;
	
	private EvaluationHelper helper;
	private String shipmentDate;
	private List<DhlShipment> shipmentList = new ArrayList<DhlShipment>();
	@Getter
	private List<DhlRoute> vehicleList = new ArrayList<DhlRoute>();
	
	public ExistingSolution(EvaluationHelper helper, String shipmentDate, List<DhlShipment> shipmentList) {
		this.helper = helper;
		this.shipmentDate = shipmentDate;
		this.shipmentList = shipmentList;
	}
	
	public void evaluate() {
        logger.info("evaluate: start....."); 

        List<String> routes = shipmentList.stream().map(s -> s.getPudRte()).distinct().collect(Collectors.toList());
        vehicleList = helper.retrieveRoutesByRouteList(routes);
        noOfCar = routes.size();
        
        assessUtilization();
        assessFamiliarity();
        
        logger.info("evaluate: finished..");          
	}
	
	private void assessFamiliarity() {
        logger.debug("assessFamiliarity: start.....");
        
		Double[] areaResponsiblity = {0.0};
		Double[] frequentHistory = {0.0};
		
		shipmentList.stream().forEach(s -> {
						
			// List<DhlRoutePostcodeArea> ra = new ArrayList<DhlRoutePostcodeArea>();
			// List<DhlRouteAreaPortion> rap = new ArrayList<DhlRouteAreaPortion>();
			
			/*
			 * for(DhlRoutePostcodeArea rat : helper.getRouteAreaList()) {
			 * logger.debug(rat.getRoute() + " <==> " + s.getPudRte());
			 * logger.debug(rat.getAreaCode() + " <==> " + s.getAreaCode());
			 * 
			 * if(rat.getRoute().equalsIgnoreCase(s.getPudRte())) {
			 * logger.debug(rat.getRoute() + " <==> " + s.getPudRte() + ": EQUAL"); }
			 * if(rat.getAreaCode() == s.getAreaCode()) { logger.debug(rat.getAreaCode() +
			 * " <==> " + s.getAreaCode() + ": EQUAL"); } }
			 */
			
			logger.debug("1st size" + helper.getRouteAreaList().stream().filter(row -> row.getRoute().equalsIgnoreCase(s.getPudRte()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList()));
			logger.debug("2st size" + helper.getRouteAreaPortionList().stream().filter(row -> row.getRoute().equalsIgnoreCase(s.getPudRte()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList()));
			
			List<DhlRoutePostcodeArea> ra = helper.getRouteAreaList().stream().filter(row -> row.getRoute().equalsIgnoreCase(s.getPudRte()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
			List<DhlRouteAreaPortion> rap =  helper.getRouteAreaPortionList().stream().filter(row -> row.getRoute().equalsIgnoreCase(s.getPudRte()) && row.getAreaCode() == s.getAreaCode()).collect(Collectors.toList());
			
			if(!ra.isEmpty()) areaResponsiblity[0] += 1.0;
			
			if(!rap.isEmpty())
				frequentHistory[0] += rap.get(0).getAreaPortion().doubleValue();
			
			logger.debug(String.format("Shipment Key: %d -> [%d, %s] => {area:%.4f, history:%.4f}", s.getShipmentKey(), s.getAreaCode(), s.getPudRte(), areaResponsiblity[0], frequentHistory[0]));
			
		});
		
		familiarity = areaResponsiblity[0] + frequentHistory[0];

        logger.debug("assessFamiliarity: finished..");     
	}
	
	private void assessUtilization() {
        logger.debug("assessUtilization: start....."); 
        
		Double[] accumulate = {0.0};
		
		vehicleList.stream().forEach(vid -> {
			
			List<DhlShipment> shipmentsOfEachVehicleId = shipmentList.stream().filter(s -> s.getPudRte().equalsIgnoreCase(vid.getRoute())).collect(Collectors.toList());
			Double actualShipments = Double.valueOf(shipmentsOfEachVehicleId.size());
			
			accumulate[0] += calculateUtilizationOfEachVehicle(actualShipments, Double.valueOf(vid.getAvgAllday()));
			
		});
		
		utilization = accumulate[0] / vehicleList.size();

        logger.debug("assessUtilization: finished..");     
	}
	
	private Double calculateUtilizationOfEachVehicle(Double actualShipments, Double utilizedShipments) {
		return (1-(Math.abs(actualShipments-utilizedShipments)/utilizedShipments))*100;
	}

}
