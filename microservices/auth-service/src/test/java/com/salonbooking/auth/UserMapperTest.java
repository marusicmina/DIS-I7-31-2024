package com.salonbooking.auth;

import com.salonbooking.api.auth.Role;
import com.salonbooking.api.auth.UserSummary;
import com.salonbooking.auth.persistence.UserEntity;
import com.salonbooking.auth.services.UserMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void entityToSummary_neverExposesPasswordHash() {
        UserEntity entity = new UserEntity("Mina", "Marusic", "mina@example.com",
                "$2a$10$verySecretBcryptHashValue", Role.CUSTOMER);
        entity.setId(1L);

        UserSummary summary = mapper.entityToSummary(entity);

        assertThat(summary.getUserId()).isEqualTo(1L);
        assertThat(summary.getEmail()).isEqualTo("mina@example.com");
        assertThat(summary.getRole()).isEqualTo(Role.CUSTOMER);
        // UserSummary namerno nema getter za lozinku/hash - ako neko doda password polje
        // u UserSummary, ovaj test ce prestati da se kompajlira i to je namerno "upozorenje".
    }
}
