package com.salonbooking.catalog.services;

import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.catalog.persistence.ServiceOfferingEntity;
import org.springframework.stereotype.Component;

@Component
public class ServiceOfferingMapper {

    public ServiceOffering entityToApi(ServiceOfferingEntity entity, String serviceAddress) {
        return new ServiceOffering(
                entity.getId(),
                entity.getSalonId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDurationMinutes(),
                entity.getPrice(),
                entity.isActive(),
                serviceAddress
        );
    }

    public ServiceOfferingEntity apiToEntity(ServiceOffering api) {
        return new ServiceOfferingEntity(
                api.getSalonId(),
                api.getName(),
                api.getDescription(),
                api.getDurationMinutes(),
                api.getPrice(),
                api.isActive()
        );
    }
}
