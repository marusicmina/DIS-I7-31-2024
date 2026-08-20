package com.salonbooking.salon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalonRepository extends JpaRepository<SalonEntity, Long> {

    List<SalonEntity> findByCityIgnoreCase(String city);
}
