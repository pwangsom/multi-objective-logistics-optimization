package com.kmutt.sit.existing;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobResult;
import com.kmutt.sit.jpa.respositories.DhlRouteRespository;
import com.kmutt.sit.jpa.respositories.DhlShipmentRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobProblemRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobResultRepository;

import lombok.Getter;

@Service
public class EvaluationHelper {

	private static Logger logger = LoggerFactory.getLogger(EvaluationHelper.class);
	
    @Getter
    @Value("${vehicle.types}")
    private String vehicleTypes;
    
    @Value("${vehicle.van}")
    private String vanType;
    
    @Value("${vehicle.bike}")
    private String bikeType;
    
    @Getter
    private List<String> vehicleTypeList;
    private List<String> vanTypes;
    private List<String> bikeTypes;

    @Autowired
    private DhlShipmentRepository dhlShipmentRepository;

    @Autowired
    private DhlRouteRespository dhlRouteRespository;
    
    @Autowired
    private LogisticsJobProblemRepository logisticsJobProblemRepository;
    
    @Autowired
    private LogisticsJobResultRepository logisticsJobResultRepository;
    
    @PostConstruct
    private void postConstruct() {
    	this.vehicleTypeList = Arrays.asList(vehicleTypes.split(","));
    	this.vanTypes = Arrays.asList(vanType.split(","));
    	this.bikeTypes = Arrays.asList(bikeType.split(","));
    }
	
    public List<String> retrieveShipmentDateList(){    	
    	return dhlShipmentRepository.findDistinctActDt();
    }
    
    public List<DhlShipment> retrieveDailyShipmentForVan(String shipmentDate){
    	return dhlShipmentRepository.findByActDtAndIsValidForMopAndVehicleTypeIn(shipmentDate, 1, vanTypes);
    }
	
    public List<DhlShipment> retrieveDailyShipmentForBike(String shipmentDate){
    	return dhlShipmentRepository.findByActDtAndIsValidForMopAndVehicleTypeIn(shipmentDate, 1, bikeTypes);
    }
    
    public List<DhlRoute> retrieveRoutesOfVan(){
    	return dhlRouteRespository.findByVehicleTypeInOrderByChromosomeIdAsc(vanTypes);
    }
    
    public List<DhlRoute> retrieveRoutesOfBike(){
    	return dhlRouteRespository.findByVehicleTypeInOrderByChromosomeIdAsc(bikeTypes);
    }
    
    public List<DhlRoute> retrieveRoutesByRouteList(List<String> routes){
    	return dhlRouteRespository.findByRouteInOrderByChromosomeIdAsc(routes);
    }
    
    public LogisticsJobProblem saveLogisticsJobProblem(LogisticsJobProblem problem) {
    	LogisticsJobProblem persistProblem = logisticsJobProblemRepository.save(problem);
    	logisticsJobProblemRepository.flush();
    	return persistProblem;
    }
    
    public void saveLogisticsJobResult(LogisticsJobResult result) {    
    	logisticsJobResultRepository.save(result);
    }
}
