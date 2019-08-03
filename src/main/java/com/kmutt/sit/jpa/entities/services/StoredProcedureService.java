package com.kmutt.sit.jpa.entities.services;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

import org.springframework.stereotype.Service;

@Service
public class StoredProcedureService {
	
	@PersistenceContext
	private EntityManager em;
	
	public BigDecimal calculateAreaPortion(String shipmentDate, String shipmentKeyList) {	
		
		StoredProcedureQuery query1 = em.createStoredProcedureQuery("calculate_area_portion");
		query1.registerStoredProcedureParameter("shipment_date", String.class, ParameterMode.IN);
		query1.registerStoredProcedureParameter("shipment_key_list", String.class, ParameterMode.IN);
		query1.setParameter("shipment_date", shipmentDate);
		query1.setParameter("shipment_key_list", shipmentKeyList);
	    
	    return (BigDecimal) query1.getSingleResult();
	    
	}
	
	public BigDecimal calculateAreaShipment(Integer s1, Integer s2, Integer s3) {	
		
		StoredProcedureQuery query1 = em.createStoredProcedureQuery("calculate_area_shipment");
		query1.registerStoredProcedureParameter("s1", Integer.class, ParameterMode.IN);
		query1.registerStoredProcedureParameter("s2", Integer.class, ParameterMode.IN);
		query1.registerStoredProcedureParameter("s3", Integer.class, ParameterMode.IN);
		query1.setParameter("s1", s1);
		query1.setParameter("s2", s2);
		query1.setParameter("s3", s3);
	    
	    return (BigDecimal) query1.getSingleResult();
	    
	}
	
	public BigDecimal calculateAreaShipment() {	
		
		StoredProcedureQuery query1 = em.createStoredProcedureQuery("calculate_area_shipment");
		query1.registerStoredProcedureParameter("shipment", String.class, ParameterMode.IN);
		query1.setParameter("shipment", "test");
	    
	    return (BigDecimal) query1.getSingleResult();
	    
	}
	
}
