package com.salonbooking.auth.services;

import com.salonbooking.api.auth.UserSummary;
import com.salonbooking.auth.persistence.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

   
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
