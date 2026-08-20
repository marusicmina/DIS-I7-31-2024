package com.salonbooking.catalog;

import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.catalog.persistence.ServiceOfferingEntity;
import com.salonbooking.catalog.services.ServiceOfferingMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOfferingMapperTest {

    private final ServiceOfferingMapper mapper = new ServiceOfferingMapper();

    @Test
    void entityToApi_mapsAllFields() {
        ServiceOfferingEntity entity = new ServiceOfferingEntity(
                1L, "Zensko sisanje", "Pranje, sisanje i fen", 45, new BigDecimal("1500.00"), true);
        entity.setId(7L);

        ServiceOffering api = mapper.entityToApi(entity, "10.0.0.5:8083");

        assertThat(api.getServiceId()).isEqualTo(7L);
        assertThat(api.getSalonId()).isEqualTo(1L);
        assertThat(api.getName()).isEqualTo("Zensko sisanje");
        assertThat(api.getDurationMinutes()).isEqualTo(45);
        assertThat(api.getPrice()).isEqualByComparingTo("1500.00");
        assertThat(api.isActive()).isTrue();
        assertThat(api.getServiceAddress()).isEqualTo("10.0.0.5:8083");
    }

    @Test
    void apiToEntity_mapsAllFields() {
        ServiceOffering api = new ServiceOffering(0, 1L, "Manikir", "Klasican manikir",
                30, new BigDecimal("800.00"), true, null);

        ServiceOfferingEntity entity = mapper.apiToEntity(api);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Manikir");
        assertThat(entity.getDurationMinutes()).isEqualTo(30);
        assertThat(entity.getPrice()).isEqualByComparingTo("800.00");
    }
}
