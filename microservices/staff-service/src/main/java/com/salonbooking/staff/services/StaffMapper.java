package com.salonbooking.staff.services;

import com.salonbooking.api.staff.Staff;
import com.salonbooking.api.staff.WorkingHours;
import com.salonbooking.staff.persistence.StaffEntity;
import com.salonbooking.staff.persistence.WorkingHoursEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class StaffMapper {

    public Staff entityToApi(StaffEntity entity, String serviceAddress) {
        List<WorkingHours> hours = entity.getWorkingHours().stream()
                .sorted(Comparator.comparing(WorkingHoursEntity::getDayOfWeek))
                .map(wh -> new WorkingHours(wh.getDayOfWeek(), wh.getStartTime(), wh.getEndTime()))
                .toList();

        return new Staff(
                entity.getId(),
                entity.getSalonId(),
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getSpecialization(),
                entity.isActive(),
                hours,
                serviceAddress
        );
    }

    public StaffEntity apiToEntity(Staff api) {
        StaffEntity entity = new StaffEntity(
                api.getSalonId(),
                api.getUserId(),
                api.getFirstName(),
                api.getLastName(),
                api.getSpecialization(),
                api.isActive()
        );
        applyWorkingHours(entity, api.getWorkingHours());
        return entity;
    }

    
    public void applyWorkingHours(StaffEntity entity, List<WorkingHours> hours) {
        entity.clearWorkingHours();
        if (hours == null) {
            return;
        }
        for (WorkingHours wh : new ArrayList<>(hours)) {
            entity.addWorkingHours(new WorkingHoursEntity(wh.getDayOfWeek(), wh.getStartTime(), wh.getEndTime()));
        }
    }
}
