package com.kmutt.sit.existing;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kmutt.sit.batch.LogisticsHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobResult;

@Service
public class EvaluationHelper extends LogisticsHelper {

	private static Logger logger = LoggerFactory.getLogger(EvaluationHelper.class);
	
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
