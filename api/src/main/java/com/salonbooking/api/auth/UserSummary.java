package com.salonbooking.api.auth;

/**
 * Javni prikaz korisnika - NAMERNO ne sadrzi lozinku/hash.
 * Ovo je ono sto se vraca kroz REST API, za razliku od interne UserEntity.
 */
public class UserSummary {

    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;

    public UserSummary() {
    }

    public UserSummary(long userId, String firstName, String lastName, String email, Role role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
