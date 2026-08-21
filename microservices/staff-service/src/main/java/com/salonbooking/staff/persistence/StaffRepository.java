package com.salonbooking.staff.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<StaffEntity, Long> {

    List<StaffEntity> findBySalonId(long salonId);
}
