package com.kmutt.sit.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "dhl_route_postcode_area")
public class DhlRoutePostcodeArea {

	@Id
	@Column(name="id")
	private Integer id;

	@Column(name="route")
	private String route;	

	@Column(name="post_code")
	private String postCode;	

	@Column(name="area_code")
	private Integer areaCode;
	
}
