package com.salonbooking.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface AuthService {

    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/auth/register", consumes = "application/json", produces = "application/json")
    UserSummary register(@RequestBody RegisterRequest body);

 
    @PostMapping(value = "/auth/login", consumes = "application/json", produces = "application/json")
    AuthResponse login(@RequestBody LoginRequest body);

    @GetMapping(value = "/auth/users", produces = "application/json")
    List<UserSummary> getUsers();

    @GetMapping(value = "/auth/users/{userId}", produces = "application/json")
    UserSummary getUser(@PathVariable("userId") long userId);

    @PutMapping(value = "/auth/users/{userId}", consumes = "application/json", produces = "application/json")
    UserSummary updateUser(@PathVariable("userId") long userId, @RequestBody UpdateUserRequest body);


    @DeleteMapping(value = "/auth/users/{userId}")
    void deleteUser(@PathVariable("userId") long userId);
}
