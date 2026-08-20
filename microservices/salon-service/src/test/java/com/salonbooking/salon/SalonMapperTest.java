package com.salonbooking.salon;

import com.salonbooking.api.salon.Salon;
import com.salonbooking.salon.persistence.SalonEntity;
import com.salonbooking.salon.services.SalonMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class SalonMapperTest {

    private final SalonMapper mapper = new SalonMapper();

    @Test
    void entityToApi_mapsAllFields() {
        SalonEntity entity = new SalonEntity(
                "Salon Bella", "Bulevar oslobodjenja 10", "Novi Sad", "021123456",
                "Frizerski i kozmeticki salon", LocalTime.of(9, 0), LocalTime.of(20, 0), 42L);
        entity.setId(1L);

        Salon api = mapper.entityToApi(entity, "10.0.0.5:8081");

        assertThat(api.getSalonId()).isEqualTo(1L);
        assertThat(api.getName()).isEqualTo("Salon Bella");
        assertThat(api.getCity()).isEqualTo("Novi Sad");
        assertThat(api.getOwnerId()).isEqualTo(42L);
        assertThat(api.getServiceAddress()).isEqualTo("10.0.0.5:8081");
    }

    @Test
    void apiToEntity_mapsAllFields() {
        Salon api = new Salon(0, "Salon Bella", "Bulevar oslobodjenja 10", "Novi Sad", "021123456",
                "Frizerski i kozmeticki salon", LocalTime.of(9, 0), LocalTime.of(20, 0), 42L, null);

        SalonEntity entity = mapper.apiToEntity(api);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Salon Bella");
        assertThat(entity.getOwnerId()).isEqualTo(42L);
    }
}
