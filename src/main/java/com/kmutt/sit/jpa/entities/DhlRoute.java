package com.kmutt.sit.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "dhl_route")
public class DhlRoute {
	
	@Id
	@Column(name="route_id")
	private Integer routeId;
	
	@Column(name="avg_allday")
	private Integer avgAllday;
	
	@Column(name="avg_saturday")
	private Integer avgSaturday;

	@Column(name="avg_weekday")
	private Integer avgWeekday;

	@Column(name="chromosome_id")
	private Integer chromosomeId;

	@Column(name="cycle_operate")
	private String cycleOperate;

	@Column(name="max_utilization")
	private Integer maxUtilization;

	@Column(name="mobile_phone")
	private String mobilePhone;

	@Column(name="post_code")
	private String postCode;

	private String route;

	@Column(name="route_area")
	private String routeArea;

	@Column(name="staff_id")
	private String staffId;

	@Column(name="staff_name")
	private String staffName;

	@Column(name="vehicle_id")
	private String vehicleId;

	@Column(name="vehicle_type")
	private String vehicleType;
	
	@Override
	public String toString() {
		return String.format("routeId=%d, chromosomeId=%d, route=%s, vehicleType=%s, maxUtilization=%d", routeId, chromosomeId, route, vehicleType, maxUtilization);
	}

}
