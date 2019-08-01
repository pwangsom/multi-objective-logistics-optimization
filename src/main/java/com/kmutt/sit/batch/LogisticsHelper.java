package com.kmutt.sit.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kmutt.sit.jpa.entities.DhlRouteAreaPortion;
import com.kmutt.sit.jpa.entities.DhlRoutePostcodeArea;
import com.kmutt.sit.jpa.respositories.DhlAreaRouteScoreRespository;
import com.kmutt.sit.jpa.respositories.DhlDailyShipmentRespository;
import com.kmutt.sit.jpa.respositories.DhlRouteAreaPortionRepository;
import com.kmutt.sit.jpa.respositories.DhlRoutePostcodeAreaRespository;
import com.kmutt.sit.jpa.respositories.DhlRouteRespository;
import com.kmutt.sit.jpa.respositories.DhlShipmentRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobProblemRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobResultRepository;

import lombok.Getter;

@Service
public class LogisticsHelper {	

	private static Logger logger = LoggerFactory.getLogger(LogisticsHelper.class);
	
    @Value("${shipment.month}")
    protected String shipmentMonth;
    
    @Value("${shipment.date}")
    protected String shipmentDate;
    
    @Getter
    @Value("${vehicle.types}")
    protected String vehicleTypes;
    
    @Value("${vehicle.van}")
    protected String vanType;
    
    @Value("${vehicle.bike}")
    protected String bikeType;
    
    @Getter
    protected List<String> vehicleTypeList;
    protected List<String> vanTypes;
    protected List<String> bikeTypes;
    
    @Getter
    protected List<DhlRoutePostcodeArea> routeAreaList;
    
    @Getter
    protected List<DhlRouteAreaPortion> routeAreaPortionList;
    
    @Autowired
    protected DhlShipmentRepository dhlShipmentRepository;    
    @Autowired
    protected DhlAreaRouteScoreRespository dhlAreaRouteScoreRespository;    
    @Autowired
    protected DhlRouteRespository dhlRouteRespository;     
    @Autowired
    protected DhlRoutePostcodeAreaRespository dhlRoutePostcodeAreaRespository;     
    @Autowired
    protected DhlRouteAreaPortionRepository dhlRouteAreaPortionRepository;     
    @Autowired
    protected DhlDailyShipmentRespository dhlDailyShipmentRespository;    
    @Autowired
    protected LogisticsJobRepository logisticsJobRepository;    
    @Autowired
    protected LogisticsJobProblemRepository logisticsJobProblemRepository;    
    @Autowired
    protected LogisticsJobResultRepository logisticsJobResultRepository;    
    
    @PostConstruct
    private void postConstruct() {
    	this.vehicleTypeList = Arrays.asList(vehicleTypes.split(","));
    	this.vanTypes = Arrays.asList(vanType.split(","));
    	this.bikeTypes = Arrays.asList(bikeType.split(","));
    	this.routeAreaList = new ArrayList<DhlRoutePostcodeArea>();
    	this.routeAreaPortionList = new ArrayList<DhlRouteAreaPortion>();
    }
    
    
    public void initial() {
    	routeAreaList.addAll(dhlRoutePostcodeAreaRespository.findAll());
    	routeAreaPortionList.addAll(dhlRouteAreaPortionRepository.findAll());
    	
    	logger.info("Route-Area        : " + routeAreaList.size());
    	logger.info("Route-Area-Portion: " + routeAreaPortionList.size());
    }

}
