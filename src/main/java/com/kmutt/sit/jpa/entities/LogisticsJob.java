package com.kmutt.sit.jpa.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "logistics_job")
public class LogisticsJob {
	@Id
	@Column(name="job_id")
	private String jobId;

	@Column(name="max_iteration")
	private Integer maxIteration;

	@Column(name="max_run")
	private Integer maxRun;

	@Column(name="vehicle_config")
	private String vehicleConfig;
	
	@Temporal(TemporalType.DATE)
	@Column(name="created_time")
	private Date createdTime;
}
