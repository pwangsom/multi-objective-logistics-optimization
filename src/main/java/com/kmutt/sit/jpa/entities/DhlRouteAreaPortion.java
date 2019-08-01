package com.kmutt.sit.jpa.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "dhl_route_area_portion")
public class DhlRouteAreaPortion {
	
	@Id
	@Column(name="id")
	private Integer id;
	
	@Column(name="route")
	private String route;	

	@Column(name="area_code")
	private Integer areaCode;
	
	@Column(name="no_of_shipments")
	private Integer noOfShipments;	

	@Column(name="max_shipments")
	private Integer maxShipments;

	@Column(name="area_portion")
	private BigDecimal areaPortion;

	@Column(name="is_area_reponsibility")
	private Integer isAreaReponsibility;
	
}
