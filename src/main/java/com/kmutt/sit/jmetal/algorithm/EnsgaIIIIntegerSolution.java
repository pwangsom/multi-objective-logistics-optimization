package com.kmutt.sit.jmetal.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;
import com.kmutt.sit.jpa.entities.DhlRoute;
import com.kmutt.sit.jpa.entities.DhlRouteUtilization;
import com.kmutt.sit.utilities.JavaUtils;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
public class EnsgaIIIIntegerSolution extends GenericNsgaIIIIntegerSolution {
	
	public EnsgaIIIIntegerSolution(NSGAIIIBuilder<IntegerSolution> builder, NsgaIIIHelper helper) {
		super(builder, helper);
	}
	
	@Override
	protected List<IntegerSolution> createInitialPopulation() {
		
		List<IntegerSolution> population = new ArrayList<>(getMaxPopulationSize());		
		
		population.add(getFirstExtremeSolution()); // index 0
		population.add(getSecondExtremeSolution()); // index 1
		population.add(getThirdExtremeSolution()); // index 2
		population.add(getFourthExtremeSolution()); // index 3

		int startIdx = 4;
		
		for (int i = startIdx; i < getMaxPopulationSize(); i++) {
			IntegerSolution newIndividual = getProblem().createSolution();			
			population.add(newIndividual);
		}
		
		return population;
	}
	
	protected IntegerSolution getFirstExtremeSolution() {
		IntegerSolution extreme = getProblem().createSolution();
		JMetalRandom randomGenerator = JMetalRandom.getInstance();
		Integer value = randomGenerator.nextInt(extreme.getLowerBound(0), extreme.getUpperBound(0));
		
		for(int i = 0; i < getProblem().getNumberOfVariables(); i++) {
			extreme.setVariableValue(i, value);
		}
		
		return extreme;
	}
	
	protected IntegerSolution getSecondExtremeSolution() {
		IntegerSolution extreme = getProblem().createSolution();
		JMetalRandom randomGenerator = JMetalRandom.getInstance();
		
		List<Integer> assignedVehicle = new ArrayList<Integer>();
		
		List<DhlRoute> routes = this.helper.getRouteList();
		
		int c = 0;
		
		while(c < extreme.getNumberOfVariables()) {
			
			Integer[] vehicleChromosomeId = {randomGenerator.nextInt(extreme.getLowerBound(0), extreme.getUpperBound(0))};
			
			while(assignedVehicle.contains(vehicleChromosomeId[0])) {
				vehicleChromosomeId[0] = randomGenerator.nextInt(extreme.getLowerBound(0), extreme.getUpperBound(0));
			}
			
			assignedVehicle.add(vehicleChromosomeId[0]);
			
			DhlRoute route = routes.stream().filter(r -> r.getChromosomeId() == vehicleChromosomeId[0]).collect(Collectors.toList()).get(0);
			int utilizedShipment = determineUtilizedShipments(route);
			
			int last = c + utilizedShipment;
			if(last > extreme.getNumberOfVariables()) last = extreme.getNumberOfVariables();
			
			while(c < last) {				
				extreme.setVariableValue(c, vehicleChromosomeId[0]);				
				c++;
			}			
		}
		
		return extreme;
	}
	
	protected IntegerSolution getThirdExtremeSolution() {
		// TODO Auto-generated method stub
		IntegerSolution extreme = getProblem().createSolution();
		JMetalRandom randomGenerator = JMetalRandom.getInstance();
				
		for(int i = 0; i < extreme.getNumberOfVariables(); i++) {			
			Integer areaCode = this.helper.getShipmentList().get(i).getAreaCode();			
			Integer value = randomGenerator.nextInt(extreme.getLowerBound(0), extreme.getUpperBound(0));
			
			List<Integer> chromosomeList = this.helper.getLogisticsHelper().getAreaChromosomeMapping().get(this.helper.getVehicleType() + "_" + areaCode);
			
			if(!JavaUtils.isNull(chromosomeList) && !chromosomeList.isEmpty()) {
				value = randomGenerator.nextInt(0, chromosomeList.size()-1);				
				extreme.setVariableValue(i, chromosomeList.get(value));
			} else {				
				extreme.setVariableValue(i, value);
			}
		}
		
		return extreme;
	}
	
	protected IntegerSolution getFourthExtremeSolution() {
		// TODO Auto-generated method stub
		IntegerSolution extreme = getProblem().createSolution();
		JMetalRandom randomGenerator = JMetalRandom.getInstance();
		
		List<DhlRoute> routes = this.helper.getRouteList();		
		Map<Integer, List<RouteCapacity>> assignedAreaRouteMapping = new HashMap<Integer, List<RouteCapacity>>();
		
		int[] i = {0};
		
		this.helper.getShipmentList().stream().forEach(s -> {
			
			Integer areaCode = s.getAreaCode();	
			Integer[] value = {randomGenerator.nextInt(extreme.getLowerBound(0), extreme.getUpperBound(0))};
			Integer[] chromosomeId = {value[0]};
			
			List<RouteCapacity> routeInfos;
			RouteCapacity routeC;
			
			List<Integer> chromosomeList = this.helper.getLogisticsHelper().getAreaChromosomeMapping().get(this.helper.getVehicleType() + "_" + areaCode);
			
			if(!JavaUtils.isNull(chromosomeList) && !chromosomeList.isEmpty()) {
				value[0] = randomGenerator.nextInt(0, chromosomeList.size() - 1);
				chromosomeId[0] = chromosomeList.get(value[0]);				
				
				if(assignedAreaRouteMapping.containsKey(areaCode)) {
					routeInfos = assignedAreaRouteMapping.get(areaCode);
					routeC = routeInfos.get(routeInfos.size() - 1);
					
					if(routeC.getCurrentShipments() < routeC.getUtilizedShipments()) {
						routeC.setCurrentShipments(routeC.getCurrentShipments() + 1);
					} else {
						
						if(routeInfos.size() < chromosomeList.size()) {						
							while(routeInfos.stream().map(m -> m.getChromosomeId()).collect(Collectors.toList()).contains(chromosomeId[0])) {
								value[0] = randomGenerator.nextInt(0, chromosomeList.size() - 1);
								chromosomeId[0] = chromosomeList.get(value[0]);
							}
						} else {
							routeInfos.stream().sorted(Comparator.comparingInt(RouteCapacity::getCurrentShipments));
							chromosomeId[0] = routeInfos.get(0).getChromosomeId();
						}
								
						routeC =createNewRoute(routes, chromosomeId[0]);	
						
						routeInfos.add(routeC);					
					}
				} else {
					routeInfos= new ArrayList<RouteCapacity>();				
					routeC = createNewRoute(routes, chromosomeId[0]);				
					routeInfos.add(routeC);
					
					assignedAreaRouteMapping.put(areaCode, routeInfos);
				}				
				
			} else {
				routeC = new RouteCapacity();
				routeC.setChromosomeId(chromosomeId[0]);
			}
			
			extreme.setVariableValue(i[0], routeC.getChromosomeId());
			
			i[0]++;
		});
		
		return extreme;
	}
	
	private RouteCapacity createNewRoute(List<DhlRoute> routes, Integer chromosomeId) {		
		
		RouteCapacity routeC = new RouteCapacity();
		routeC.setChromosomeId(chromosomeId);
		
		DhlRoute route = routes.stream().filter(r -> r.getChromosomeId() == chromosomeId).collect(Collectors.toList()).get(0);
		int utilizedShipment = determineUtilizedShipments(route);
		
		routeC.setRoute(route.getRoute());
		routeC.setUtilizedShipments(utilizedShipment);
		routeC.setCurrentShipments(1);
		
		return routeC;
	}

	private int determineUtilizedShipments(DhlRoute route) {
		
		DhlRouteUtilization routeUtil = this.helper.getLogisticsHelper().getRouteUtilizationMapping().get(route.getRoute());
		int utilizedShipments = routeUtil.getAllAvg().intValue();
		
		int avgShipmentDay = 0;
		
		if(this.helper.getLogisticsHelper().getDailyRouteAreaUtilizationMapping()
				.containsKey(this.helper.getShipmentDate() + "_" + route.getRoute())) {
			avgShipmentDay = this.helper.getLogisticsHelper().getDailyRouteAreaUtilizationMapping()
					.get(this.helper.getShipmentDate() + "_" + route.getRoute()).getUtilizedShipments().intValue();
		}
		
		if(avgShipmentDay > utilizedShipments) utilizedShipments = avgShipmentDay;
		
		return utilizedShipments;
	}
	
	@Override
	public String getName() {
		return "E-NSGA-III for Integer Solution";
	}

	@Override
	public String getDescription() {
		return "Extreme Nondominated Sorting Genetic Algorithm version III for Integer Solution";
	}
	
	@Getter
	@Setter
	public class RouteCapacity{
		Integer chromosomeId;
		String route;
		Integer utilizedShipments;
		Integer currentShipments;
	}
}
