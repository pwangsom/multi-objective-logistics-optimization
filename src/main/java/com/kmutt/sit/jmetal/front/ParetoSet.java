package com.kmutt.sit.jmetal.front;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.point.util.PointSolution;

import com.kmutt.sit.jpa.entities.LogisticsJobProblem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParetoSet {
	
	private LogisticsJobProblem problem;
	
	private String shipmentDate;
	private String vehicleType;
	private String algorithm;
	private Integer run;
	
	private List<IntegerSolution> solutions;
	private List<PointSolution> normalizedSolutions;
	
	public ParetoSet() {
		solutions = new ArrayList<IntegerSolution>();
		normalizedSolutions = new ArrayList<PointSolution>();
	}
	
}
