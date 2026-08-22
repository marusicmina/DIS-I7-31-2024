package com.salonbooking.staff.persistence;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "staff_members", indexes = {
        @Index(name = "staff_salon_idx", columnList = "salonId")
})
public class StaffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long salonId;

    private Long userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String specialization;

    @Column(nullable = false)
    private boolean active = true;

    
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkingHoursEntity> workingHours = new ArrayList<>();

    public StaffEntity() {
    }

    public StaffEntity(long salonId, Long userId, String firstName, String lastName,
                        String specialization, boolean active) {
        this.salonId = salonId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.active = active;
    }

    
    public void addWorkingHours(WorkingHoursEntity wh) {
        wh.setStaff(this);
        this.workingHours.add(wh);
    }

    public void clearWorkingHours() {
        this.workingHours.clear();
    }

    public Optional<WorkingHoursEntity> workingHoursFor(DayOfWeek day) {
        return workingHours.stream()
                .filter(wh -> wh.getDayOfWeek() == day)
                .findFirst();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getSalonId() {
        return salonId;
    }

    public void setSalonId(long salonId) {
        this.salonId = salonId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<WorkingHoursEntity> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(List<WorkingHoursEntity> workingHours) {
        this.workingHours = workingHours;
    }
}
