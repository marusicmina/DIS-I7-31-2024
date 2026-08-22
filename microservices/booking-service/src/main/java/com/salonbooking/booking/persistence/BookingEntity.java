package com.salonbooking.booking.persistence;

import com.salonbooking.api.booking.BookingStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "booking_staff_time_idx", columnList = "staffId,startTime"),
        @Index(name = "booking_client_idx", columnList = "clientId"),
        @Index(name = "booking_salon_idx", columnList = "salonId")
})
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long clientId;

    @Column(nullable = false)
    private long salonId;

    @Column(nullable = false)
    private long staffId;

    @Column(nullable = false)
    private long serviceId;

    
    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public BookingEntity() {
    }

    public BookingEntity(long clientId, long salonId, long staffId, long serviceId,
                          String serviceName, BigDecimal price,
                          LocalDateTime startTime, LocalDateTime endTime,
                          BookingStatus status, String note, LocalDateTime createdAt) {
        this.clientId = clientId;
        this.salonId = salonId;
        this.staffId = staffId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getSalonId() {
        return salonId;
    }

    public void setSalonId(long salonId) {
        this.salonId = salonId;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
