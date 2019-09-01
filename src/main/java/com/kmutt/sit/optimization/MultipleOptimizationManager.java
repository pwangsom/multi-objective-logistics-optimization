package com.kmutt.sit.optimization;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class MultipleOptimizationManager extends OptimizationManager {
	
	private static Logger logger = LoggerFactory.getLogger(MultipleOptimizationManager.class);
	
	private List<String> algorithmList;
	
	public MultipleOptimizationManager() {
		super();
		
		algorithmList = new ArrayList<String>();
	}

}
