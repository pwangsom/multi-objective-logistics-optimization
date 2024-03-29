package com.kmutt.sit.utilities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kmutt.sit.jpa.entities.DhlAreaRouteScore;
import com.kmutt.sit.jpa.entities.DhlDailyRouteAreaUtilization;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlRouteAreaPortion;
import com.kmutt.sit.jpa.entities.DhlRouteAreas;
import com.kmutt.sit.jpa.entities.DhlRoutePostcodeArea;
import com.kmutt.sit.jpa.entities.DhlRouteUtilization;
import com.kmutt.sit.jpa.entities.DhlShipment;
import com.kmutt.sit.jpa.entities.LogisticsJob;
import com.kmutt.sit.jpa.entities.LogisticsJobProblem;
import com.kmutt.sit.jpa.entities.LogisticsJobProblemBenchmark;
import com.kmutt.sit.jpa.entities.LogisticsJobResult;
import com.kmutt.sit.jpa.entities.LogisticsJobResultDetail;
import com.kmutt.sit.jpa.entities.services.StoredProcedureService;
import com.kmutt.sit.jpa.respositories.DhlAreaRouteScoreRespository;
import com.kmutt.sit.jpa.respositories.DhlDailyRouteAreaUtilizationRepository;
import com.kmutt.sit.jpa.respositories.DhlRouteAreaPortionRepository;
import com.kmutt.sit.jpa.respositories.DhlRouteAreasRepository;
import com.kmutt.sit.jpa.respositories.DhlRoutePostcodeAreaRespository;
import com.kmutt.sit.jpa.respositories.DhlRouteRespository;
import com.kmutt.sit.jpa.respositories.DhlRouteUtilizationRepository;
import com.kmutt.sit.jpa.respositories.DhlShipmentRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobProblemBenchmarkRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobProblemRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobResultDetailRepository;
import com.kmutt.sit.jpa.respositories.LogisticsJobResultRepository;

import lombok.Getter;

@Service
public class LogisticsOptimizationHelper {
    
    @Autowired
    private DhlShipmentRepository dhlShipmentRepository;    
    @Autowired
    private DhlAreaRouteScoreRespository dhlAreaRouteScoreRespository;    
    @Autowired
    private DhlRouteRespository dhlRouteRespository;     
    @Autowired
    private DhlRoutePostcodeAreaRespository dhlRoutePostcodeAreaRespository;     
    @Autowired
    private DhlRouteAreaPortionRepository dhlRouteAreaPortionRepository;      
    @Autowired
    private DhlRouteUtilizationRepository dhlRouteUtilizationRepository;    
    @Autowired
    private DhlDailyRouteAreaUtilizationRepository dhlDailyRouteAreaUtilizationRepository;
    @Autowired
    private DhlRouteAreasRepository dhlRouteAreasRepository;
    @Autowired
    private LogisticsJobRepository logisticsJobRepository;    
    @Autowired
    private LogisticsJobProblemRepository logisticsJobProblemRepository;    
    @Autowired
    private LogisticsJobResultRepository logisticsJobResultRepository; 
    @Autowired
    private LogisticsJobResultDetailRepository logisticsJobResultDetailRepository;
    @Autowired
    private LogisticsJobProblemBenchmarkRepository logisticsJobProblemBenchmarkRepository;
    @Autowired
    private StoredProcedureService storedProcedureService;

    @Getter
    @Value("${app.output.path}")
    private String outputPath;    

    @Getter
    @Value("${utilization.version}")
    private Integer utilizationVersion;    

    @Getter
    @Value("${familiarity.version}")
    private Integer familiarityVersion;
    
    @Getter
    @Value("${nsga.problem.constraint.enabled}")
    private boolean isProblemConstraintEnabled;    

    @Getter
    @Value("${nsga.problem.constraint.type}")
    private Integer problemConstraintType;

    @Getter
    @Value("${utilization.constraint.allowed}")
    private Integer utilizationConstraintRate;
    
    @Getter
    @Value("${area.responsibility.rate}")
    private Double areaResponsibilityRate;    

    @Getter
    @Value("${area.responsibility.portion}")
    private Double areaResponsibilityPortion;  
    
    @Getter
    @Value("${van.utilization.threshold}")
    private Double vanUtilizationThreshold;
    
    @Getter
    @Value("${bike.utilization.threshold}")
    private Double bikeUtilizationThreshold;
    
    @Getter
    @Value("${van.familiarity.threshold}")
    private Double vanFamiliarityThreshold;

    @Getter
    @Value("${bike.familiarity.threshold}")
    private Double bikeFamiliarityThreshold;
    
    @Getter
    @Value("${area.history.rate}")
    private Double areaHistoryRate;
    
    @Value("${shipment.month}")
    private String shipmentMonth;
    
    @Value("${shipment.date}")
    private String shipmentDate;
    
    @Getter
    @Value("${vehicle.types}")
    private String vehicleTypes;
    
    @Value("${vehicle.van}")
    private String vanType;
    
    @Value("${vehicle.bike}")
    private String bikeType;
    
    @Getter
    @Value("${output.database.enabled}")
    private boolean isOutputDatabaseEnabled; 
    
    @Getter
    @Value("${output.file.enabled}")
    private boolean isOutputFileEnabled;
    
    @Getter
    @Value("${multiple.algorithm.enabled}")
    private boolean isMultipleAlgorithmEnabled;
    
    @Getter
    @Value("${multiple.algorithm.list}")
    private String algorithms;
    
    @Getter
    private List<String> vehicleTypeList;
    private List<String> vanTypes;
    private List<String> bikeTypes;
    
    @Getter
    private List<String> algorithmList;
    
    @Getter
    private List<DhlRoutePostcodeArea> routeAreaList;
    
    @Getter
    private List<DhlRouteAreaPortion> routeAreaPortionList;
    
    @Getter
    private List<LogisticsJobProblemBenchmark> problemBenchmarkList;
    
    @Getter
    private Map<String, DhlRouteUtilization> routeUtilizationMapping;    

    @Getter
    private Map<String, DhlDailyRouteAreaUtilization> dailyRouteAreaUtilizationMapping;    

    @Getter
    private Map<String, Integer> routeAreasMapping;
    
    @Getter
    private Map<String, List<Integer>> areaChromosomeMapping;
    
    private List<String> shipmentDateList;
    
    public LogisticsOptimizationHelper() {
    	this.shipmentDateList = new ArrayList<String>();
    }
    
    @PostConstruct
    private void postConstruct() {
    	this.vehicleTypeList = Arrays.asList(vehicleTypes.split(","));
    	this.vanTypes = Arrays.asList(vanType.split(","));
    	this.bikeTypes = Arrays.asList(bikeType.split(","));
    	this.routeAreaList = dhlRoutePostcodeAreaRespository.findAll();
    	this.routeAreaPortionList = dhlRouteAreaPortionRepository.findAll();
    	this.problemBenchmarkList = logisticsJobProblemBenchmarkRepository.findAll();
    	
    	if(isMultipleAlgorithmEnabled) {
    		this.algorithmList = Arrays.asList(algorithms.split(","));
    	}
    	
    	initialRouteUtilizationMapping();
    	initialDailyRouteAreaUtilizationMapping();
    	initialRouteAreasMapping();
    	initialAreaChromosomeMapping();
    }
    
    public List<String> retrieveShipmentDateList(){
    	
    	// For all days in month
    	if(this.shipmentDate.contentEquals("00")) {
    		this.shipmentDateList.addAll(dhlShipmentRepository.findDistinctActDt());
    		
    	} // For multiple days
    	  else if(this.shipmentDate.length() > 2 && this.shipmentDate.contains(",")){
    		
    		String[] dates = this.shipmentDate.split(",");
    		
    		Arrays.asList(dates).stream().forEach(d ->{
    			this.shipmentDateList.add(this.shipmentMonth + d);
    		});
    		
    	} // For single day
    	  else if(this.shipmentDate.length() == 2) {
    		this.shipmentDateList.add(this.shipmentMonth + this.shipmentDate);
    	}
    	
    	Collections.sort(this.shipmentDateList);
    	
    	return this.shipmentDateList;
    }
    
    public void saveLogisticsJob(LogisticsJob job) {
    	logisticsJobRepository.save(job);
    }
    
    public LogisticsJobProblem saveLogisticsJobProblem(LogisticsJobProblem problem) {
    	LogisticsJobProblem persistProblem = logisticsJobProblemRepository.save(problem);
    	logisticsJobProblemRepository.flush();
    	return persistProblem;
    }
    
    public void updateLogisticsJobProblem(LogisticsJobProblem problem) {
    	logisticsJobProblemRepository.save(problem);
    }
    
    @Transactional
    public void saveLogisticsJobResult(List<LogisticsJobResult> results) {    
    	logisticsJobResultRepository.saveAll(results);
    }
    
    @Transactional
    public void saveLogisticsJobResultDetail(List<LogisticsJobResultDetail> resultDetail) {    
    	logisticsJobResultDetailRepository.saveAll(resultDetail);
    }
    
    public List<DhlShipment> retrieveDailyShipment(String shipmentDate){
    	return dhlShipmentRepository.findByActDt(shipmentDate);
    }
    
    public List<DhlShipment> retrieveDailyShipmentForVan(String shipmentDate){
    	return dhlShipmentRepository.findByActDtAndCycleOperateAndVehicleTypeIn(shipmentDate, "A", vanTypes);
    }
    
    public List<DhlShipment> retrieveDailyShipmentForBike(String shipmentDate){
    	return dhlShipmentRepository.findByActDtAndCycleOperateAndVehicleTypeIn(shipmentDate, "A", bikeTypes);
    }
    
    public Map<String, Integer> retrieveAreaRouteScoreMap(){    	
    	Map<String, Integer> map = new HashMap<String, Integer>();
    	
    	List<DhlAreaRouteScore> scoreList = dhlAreaRouteScoreRespository.findAll();
    	
    	scoreList.stream().forEach(score ->{
    		map.put(score.getAreaCode() + "-" + score.getRoute(), score.getScore());
    	});
    	
    	return map;
    }    
	
    public List<String> retrieveShipmentDateAllList(){    	
    	return dhlShipmentRepository.findDistinctActDt();
    }
    
    public List<DhlShipment> retrieveValidDailyShipmentForVan(String shipmentDate){
    	return dhlShipmentRepository.findByActDtAndIsValidForMopAndVehicleTypeIn(shipmentDate, 1, vanTypes);
    }
	
    public List<DhlShipment> retrieveDailyValidShipmentForBike(String shipmentDate){
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
    
    public void saveLogisticsJobResult(LogisticsJobResult result) {    
    	logisticsJobResultRepository.save(result);
    }
    
    private void initialRouteUtilizationMapping() {
    	List<DhlRouteUtilization> routes = dhlRouteUtilizationRepository.findAll();
    	
    	routeUtilizationMapping = new HashMap<String, DhlRouteUtilization>();
    	
    	routes.stream().forEach(r -> {
    		routeUtilizationMapping.put(r.getRoute(), r);
    	});
    }
    
    private void initialDailyRouteAreaUtilizationMapping() {
    	List<DhlDailyRouteAreaUtilization> routes = dhlDailyRouteAreaUtilizationRepository.findAll();
    	
    	dailyRouteAreaUtilizationMapping = new HashMap<String, DhlDailyRouteAreaUtilization>();
    	
    	routes.stream().forEach(r -> {
    		dailyRouteAreaUtilizationMapping.put(r.getShipmentDate() + "_" +r.getRoute(), r);
    	});
    }
    
    private void initialRouteAreasMapping() {
    	List<DhlRouteAreas> routes = dhlRouteAreasRepository.findAll();
    	
    	routeAreasMapping = new HashMap<String, Integer>();
    	
    	routes.stream().forEach(r -> {
    		routeAreasMapping.put(r.getRoute(), r.getAreas());
    	});
    }
        
    private void initialAreaChromosomeMapping() {
    	areaChromosomeMapping = new HashMap<String, List<Integer>>();
    	
    	this.routeAreaList.stream().forEach(r -> {
    		String key = r.getVehicleType() + "_" + r.getAreaCode();
    		
    		if(!areaChromosomeMapping.containsKey(key)) {
    			List<Integer> list = new ArrayList<Integer>();
    			list.add(r.getChromosomeId());
    			areaChromosomeMapping.put(key, list);
    		} else {
    			areaChromosomeMapping.get(key).add(r.getChromosomeId());
    		}
    	});
    	
    	areaChromosomeMapping.forEach((k, v) ->{
    		Collections.sort(v);
    	});
    	
    } 
    
    public Integer getCountByActDtAndPudRte(String actDt, String pudRte) {
    	return dhlShipmentRepository.countByActDtAndPudRte(actDt, pudRte);
    }
    
    public BigDecimal getCalculateAreaPortion(String shipmentDate, String shipmentKeyList) {
    	return storedProcedureService.calculateAreaPortion(shipmentDate, shipmentKeyList);
    }    

	public BigDecimal getCalculateAreaShipment(Integer s1, Integer s2, Integer s3) {
		return storedProcedureService.calculateAreaShipment(s1, s2, s3);
	}
	
	public BigDecimal getCalculateAreaShipment() {
		return storedProcedureService.calculateAreaShipment();
	}
}
