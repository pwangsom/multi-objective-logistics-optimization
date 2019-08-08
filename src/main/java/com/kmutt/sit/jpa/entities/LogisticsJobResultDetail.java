package com.kmutt.sit.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "logistics_job_result_detail")
public class LogisticsJobResultDetail {

	@Id
	@Column(name="detail_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer detailId;

	@Column(name="problem_id")
	private Integer problemId;

	@Column(name="solution_index")
	private Integer solutionIndex;

	@Column(name="shipment_date")
	private String shipmentDate;
	
	@Column(name="vehicle_type")
	private String vehicleType;
	
	@Column(name="chromosome_id")
	private Integer chromosomeId;
	
	@Column(name="route")
	private String route;	

	@Column(name="shipment_list")
	private String shipmentList;
	
	@Column(name="solution_type")
	private String solutionType;
}
