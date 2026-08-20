package com.salonbooking.auth.services;

import com.salonbooking.api.auth.UserSummary;
import com.salonbooking.auth.persistence.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Namerno postoji samo entityToSummary (nikad "sve" polje entiteta) -
     * passwordHash ne sme nikada da napusti auth-service kroz API.
     */
    public UserSummary entityToSummary(UserEntity entity) {
        return new UserSummary(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getRole()
        );
    }
}
