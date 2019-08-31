package com.kmutt.sit.jpa.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "logistics_job_problem_benchmark")
public class LogisticsJobProblemBenchmark {
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name="shipment_date")
	private String shipmentDate;
	
	@Column(name="vehicle_type")
	private String vehicleType;
	
	@Column(name="job_id")
	private String jobId;

	@Column(name="objective_1")
	private BigDecimal objective1;

	@Column(name="objective_2")
	private BigDecimal objective2;

	@Column(name="objective_3")
	private BigDecimal objective3;
}
