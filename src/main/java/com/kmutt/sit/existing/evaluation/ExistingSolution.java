package com.kmutt.sit.existing.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kmutt.sit.existing.EvaluationHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
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
	private List<DhlRoute> routeList = new ArrayList<DhlRoute>();
	
	public ExistingSolution(EvaluationHelper helper, String shipmentDate, List<DhlShipment> shipmentList) {
		this.helper = helper;
		this.shipmentDate = shipmentDate;
		this.shipmentList = shipmentList;
	}
	
	public void evaluate() {
        logger.info("evaluate: start....."); 

        List<String> routes = shipmentList.stream().map(s -> s.getPudRte()).distinct().collect(Collectors.toList());
        routeList = helper.retrieveRoutesByRouteList(routes);
        noOfCar = routes.size();
        
        logger.info("evaluate: finished..");          
	}

}
