package com.kmutt.sit.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;

import lombok.Setter;

@Controller
public class MultipleOptimizationManager {
	
	private static Logger logger = LoggerFactory.getLogger(MultipleOptimizationManager.class);
	
	@Setter
	private String jobId;
	
	@Autowired
	private NsgaIIIHelper nsgaIIIHelper;
	
	private List<DhlRoute> vanList;
	private List<DhlRoute> bikeList;
	private Map<String, Integer> scoreMapping;
	
	public MultipleOptimizationManager() {
		vanList = new ArrayList<DhlRoute>();
		bikeList = new ArrayList<DhlRoute>();
		scoreMapping = new HashMap<String, Integer>();
	}
	
	public void opitmize() {
		
	}

}
