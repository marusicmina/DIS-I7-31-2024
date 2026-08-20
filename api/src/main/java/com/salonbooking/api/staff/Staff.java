package com.salonbooking.api.staff;

import java.util.List;

/**
 * Zaposleni u salonu (frizer, kozmeticar, maser...).
 *
 * userId je opciona veza ka nalogu u auth-service-u - zaposleni moze imati nalog
 * kojim se prijavljuje i vidi svoj raspored, ali ne mora (npr. salon ga vodi
 * samo evidenciono). Kao i salonId, to je obican broj a ne strani kljuc,
 * jer je rec o drugom mikroservisu sa svojom bazom.
 */
public class Staff {

    private long staffId;
    private long salonId;
    private Long userId;
    private String firstName;
    private String lastName;

    /** Npr. "frizer", "kozmeticar", "maniklir" - po cemu klijent bira zaposlenog. */
    private String specialization;

    private boolean active;

    /** Radno vreme po danima u nedelji. */
    private List<WorkingHours> workingHours;

    private String serviceAddress;

    public Staff() {
    }

    public Staff(long staffId, long salonId, Long userId, String firstName, String lastName,
                  String specialization, boolean active, List<WorkingHours> workingHours,
                  String serviceAddress) {
        this.staffId = staffId;
        this.salonId = salonId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.active = active;
        this.workingHours = workingHours;
        this.serviceAddress = serviceAddress;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
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

    public List<WorkingHours> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(List<WorkingHours> workingHours) {
        this.workingHours = workingHours;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
