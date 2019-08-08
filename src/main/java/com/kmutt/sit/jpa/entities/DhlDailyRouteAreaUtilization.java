package com.kmutt.sit.jpa.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "dhl_daily_route_area_utilization")
public class DhlDailyRouteAreaUtilization {
	
	@Id
	@Column(name="id")
	private Integer id;
	
	@Column(name="shipment_date")
	private String shipmentDate;	
	
	@Column(name="route")
	private String route;	
	
	@Column(name="chromosome_id")
	private Integer chromosomeId;
	
	@Column(name="vehicle_type")
	private String vehicleType;
	
	@Column(name="utilized_shipments")
	private BigDecimal utilizedShipments;
	
}
