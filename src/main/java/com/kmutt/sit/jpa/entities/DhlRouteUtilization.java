package com.kmutt.sit.jpa.entities;

import java.math.BigDecimal;

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
	
	@Column(name="all_avg")
	private BigDecimal allAvg;

	@Column(name="weekday_days")
	private Integer weekdayDays;
	
	@Column(name="weekday_shipments")
	private Integer weekdayShipments;

	@Column(name="weekday_avg")
	private BigDecimal weekdayAvg;

	@Column(name="sat_days")
	private Integer satDays;
	
	@Column(name="sat_shipments")
	private Integer satShipments;	

	@Column(name="sat_avg")
	private BigDecimal satAvg;
}
