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
@Table(schema = "public", name = "dhl_daily_shipment")
public class DhlDailyShipment {

	@Id
	@Column(name="act_dt")
	private String actDt;
	
	@Temporal(TemporalType.DATE)
	@Column(name="act_date")
	private Date actDate;

	@Column(name="no_of_shipment")
	private Integer noOfShipment;
}
