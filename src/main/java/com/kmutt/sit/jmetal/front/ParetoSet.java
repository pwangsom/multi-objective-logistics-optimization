package com.kmutt.sit.jmetal.front;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.IntegerSolution;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParetoSet {
	
	private String shipmentDate;
	private String vehicleType;
	private String algorithm;
	private Integer problemId;
	private Integer run;
	
	private List<IntegerSolution> solutions;
	
	public ParetoSet() {
		solutions = new ArrayList<IntegerSolution>();
	}
	
}
