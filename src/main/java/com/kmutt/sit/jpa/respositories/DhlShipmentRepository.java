package com.kmutt.sit.jpa.respositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kmutt.sit.jpa.entities.DhlShipment;

public interface DhlShipmentRepository extends JpaRepository<DhlShipment, Integer>{
	
    List<DhlShipment> findByActDt(String actDt);
    List<DhlShipment> findByActDtAndCycleOperateAndVehicleTypeIn(String actDt, String cycleOperate, List<String> vehicleTypes);
    List<DhlShipment> findByActDtAndIsValidForMopAndVehicleTypeIn(String actDt, Integer isValidForMop, List<String> vehicleTypes);
	
	@Query("SELECT DISTINCT actDt FROM DhlShipment ORDER BY actDt")
	List<String> findDistinctActDt();

}
