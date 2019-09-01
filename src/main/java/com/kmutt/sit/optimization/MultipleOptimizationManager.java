package com.kmutt.sit.optimization;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.kmutt.sit.jpa.entities.DhlRoute;

@Controller
public class MultipleOptimizationManager extends OptimizationManager {
	
	private static Logger logger = LoggerFactory.getLogger(MultipleOptimizationManager.class);
	
	public MultipleOptimizationManager() {
		vanList = new ArrayList<DhlRoute>();
		bikeList = new ArrayList<DhlRoute>();
	}
	
	@Override
	public void opitmize() {		
        logger.info("MultipleOptimizationManager: Job ID: " + jobId + "\t start.....");         

        prepareInformation();
		

        logger.info("MultipleOptimizationManager: Job ID: " + jobId + "\t finished.."); 
	}

}
