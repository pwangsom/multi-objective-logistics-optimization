package com.kmutt.sit.jpa.respositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kmutt.sit.jpa.entities.DhlDailyShipment;

public interface DhlDailyShipmentRespository extends JpaRepository<DhlDailyShipment, String> {

}
