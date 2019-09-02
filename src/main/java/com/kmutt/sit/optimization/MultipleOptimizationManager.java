package com.kmutt.sit.optimization;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.kmutt.sit.jmetal.front.ParetoSet;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlShipment;

@Controller
public class MultipleOptimizationManager extends OptimizationManager {
	
	private static Logger logger = LoggerFactory.getLogger(MultipleOptimizationManager.class);
	
	private List<String> algorithmList;
	
	public MultipleOptimizationManager() {
		super();
	}
	
	public void retrieveExtraInformation() {		
		algorithmList = this.nsgaIIIHelper.getLogisticsHelper().getAlgorithmList();		
	}
	
	protected void runNsgaIII(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList) {
		
		List<ParetoSet> allParetoSets = new ArrayList<ParetoSet>(); 
		
		algorithmList.stream().forEach(algorithm ->{
			
			prepareNsgaIIIHelperBeforeRunningNsgaIII(vehicleType, shipmentList, routeList, algorithm);
			
			int maxRun = nsgaIIIHelper.getMaxRun();
			
	        // Allocate each run
	        for(int i = 1; i <= maxRun; i++) {
	        	
	        	String runInfo = String.format("[Job ID: %s, Shipment Date: %s, Vehicle: %s, Algorithm: %s, Run: %d, Max Run: %d]", 
	        					jobId, nsgaIIIHelper.getShipmentDate(), nsgaIIIHelper.getVehicleType(), nsgaIIIHelper.getNsgaVersion(), i, maxRun);

	            logger.info(runInfo + ": Starting....");
	        	
	            nsgaIIIHelper.setCurrentRun(i);
	        	
				/*
				 * LogisticsNsgaIIIIntegerRunner runner = new
				 * LogisticsNsgaIIIIntegerRunner(nsgaIIIHelper); runner.setRunnerParameter();
				 * runner.execute();
				 */
	    		logger.debug(runInfo + ": Finished....");
	        }
		});
	}
	
	
	protected void prepareNsgaIIIHelperBeforeRunningNsgaIII(String vehicleType, List<DhlShipment> shipmentList, List<DhlRoute> routeList, String algorithm) {
		nsgaIIIHelper.setNsgaVersion(algorithm);
		nsgaIIIHelper.setVehicleType(vehicleType);
		nsgaIIIHelper.setShipmentList(shipmentList);
		nsgaIIIHelper.setRouteList(routeList);
		nsgaIIIHelper.setFunFile(OptimizationHelper.getFileOutputName(nsgaIIIHelper, vehicleType.toLowerCase(), "fun"));
		nsgaIIIHelper.setVarFile(OptimizationHelper.getFileOutputName(nsgaIIIHelper, vehicleType.toLowerCase(), "var"));		
	}

}
