package com.salonbooking.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOfferingEntity, Long> {

    List<ServiceOfferingEntity> findBySalonId(long salonId);
}
