package com.salonbooking.salon.services;

import com.salonbooking.api.salon.Salon;
import com.salonbooking.salon.persistence.SalonEntity;
import org.springframework.stereotype.Component;

@Component
public class SalonMapper {

    public Salon entityToApi(SalonEntity entity, String serviceAddress) {
        return new Salon(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getCity(),
                entity.getPhoneNumber(),
                entity.getDescription(),
                entity.getOpeningTime(),
                entity.getClosingTime(),
                entity.getOwnerId(),
                serviceAddress
        );
    }

    public SalonEntity apiToEntity(Salon api) {
        return new SalonEntity(
                api.getName(),
                api.getAddress(),
                api.getCity(),
                api.getPhoneNumber(),
                api.getDescription(),
                api.getOpeningTime(),
                api.getClosingTime(),
                api.getOwnerId()
        );
    }
}
