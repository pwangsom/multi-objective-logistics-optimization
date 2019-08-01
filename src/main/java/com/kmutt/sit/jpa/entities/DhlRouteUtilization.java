package com.kmutt.sit.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "dhl_route_utilization")
public class DhlRouteUtilization {
	
	@Id
	@Column(name="id")
	private Integer id;
	
	@Column(name="route")
	private String route;	

	@Column(name="all_days")
	private Integer allDays;
	
	@Column(name="all_shipments")
	private Integer allShipments;	

	@Column(name="weekday_days")
	private Integer weekdayDays;
	
	@Column(name="weekday_shipments")
	private Integer weekdayShipments;

	@Column(name="sat_days")
	private Integer satDays;
	
	@Column(name="sat_shipments")
	private Integer satShipments;	

}
