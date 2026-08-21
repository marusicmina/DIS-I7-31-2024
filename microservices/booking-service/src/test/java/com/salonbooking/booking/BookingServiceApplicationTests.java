package com.salonbooking.booking;

import com.salonbooking.api.booking.Booking;
import com.salonbooking.api.booking.BookingStatus;
import com.salonbooking.api.booking.CreateBookingRequest;
import com.salonbooking.api.catalog.ServiceOffering;
import com.salonbooking.api.salon.Salon;
import com.salonbooking.api.staff.AvailabilityResponse;
import com.salonbooking.booking.integration.BookingIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Integracioni testovi orkestracije.
 *
 * Baza je prava (Testcontainers Postgres), ali su ostali mikroservisi zamenjeni
 * laznom implementacijom BookingIntegration-a. Zato ovi testovi rade i kad
 * salon, catalog i staff servis nisu pokrenuti - testiramo logiku zakazivanja,
 * a ne mrezu.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingServiceApplicationTests {

    private static final long SALON_ID = 1L;
    private static final long STAFF_ID = 10L;
    private static final long SERVICE_ID = 100L;
    private static final long CLIENT_ID = 55L;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookingdb")
            .withUsername("booking")
            .withPassword("booking");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private BookingIntegration integration;

    @BeforeEach
    void setUpHappyPath() {
        when(integration.getSalon(anyLong())).thenReturn(salon());
        when(integration.getService(anyLong())).thenReturn(serviceOffering(SALON_ID, 45, true));
        when(integration.checkStaffAvailability(anyLong(), any(), any()))
                .thenReturn(new AvailabilityResponse(STAFF_ID, true, null));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createBooking_happyPath_computesEndTimeFromServiceDuration() {
        LocalDateTime start = futureStart(10, 0);

        Booking booking = createBooking(start);

        assertThat(booking.getBookingId()).isPositive();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        // usluga traje 45 minuta -> kraj se racuna, klijent ga ne salje
        assertThat(booking.getEndTime()).isEqualTo(start.plusMinutes(45));
        // cena je snimljena u trenutku zakazivanja
        assertThat(booking.getPrice()).isEqualByComparingTo("1500.00");
        assertThat(booking.getServiceName()).isEqualTo("Zensko sisanje");
    }

    @Test
    void createBooking_overlappingWithExisting_returns409() {
        LocalDateTime start = futureStart(11, 0);
        createBooking(start);

        // Drugi termin pocinje 20 minuta kasnije - upada u prvih 45 minuta.
        ResponseEntity<String> response = postBooking(start.plusMinutes(20), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("preklapa");
    }

    @Test
    void createBooking_backToBack_isAllowed() {
        LocalDateTime start = futureStart(12, 0);
        createBooking(start);

        // Termin koji pocinje tacno kad se prethodni zavrsava mora da prodje.
        ResponseEntity<Booking> response = postBooking(start.plusMinutes(45), Booking.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createBooking_afterCancellation_slotIsFreeAgain() {
        LocalDateTime start = futureStart(13, 0);
        Booking first = createBooking(start);

        restTemplate.postForEntity(url("/bookings/" + first.getBookingId() + "/cancel"), null, Booking.class);

        // Otkazan termin ne zauzima vise slot.
        ResponseEntity<Booking> response = postBooking(start, Booking.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createBooking_staffNotAvailable_returns422() {
        when(integration.checkStaffAvailability(anyLong(), any(), any()))
                .thenReturn(new AvailabilityResponse(STAFF_ID, false, "Zaposleni ne radi u danu: SUNDAY"));

        ResponseEntity<String> response = postBooking(futureStart(14, 0), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("nije raspoloziv");
    }

    @Test
    void createBooking_serviceFromDifferentSalon_returns422() {
        // Usluga pripada salonu 999, a zakazuje se u salonu 1.
        when(integration.getService(anyLong())).thenReturn(serviceOffering(999L, 45, true));

        ResponseEntity<String> response = postBooking(futureStart(15, 0), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("ne pripada salonu");
    }

    @Test
    void createBooking_inactiveService_returns422() {
        when(integration.getService(anyLong())).thenReturn(serviceOffering(SALON_ID, 45, false));

        ResponseEntity<String> response = postBooking(futureStart(16, 0), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("nije u ponudi");
    }

    @Test
    void createBooking_inThePast_returns422() {
        CreateBookingRequest request = new CreateBookingRequest(
                CLIENT_ID, SALON_ID, STAFF_ID, SERVICE_ID,
                LocalDateTime.now().minusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0),
                null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url("/bookings"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("proslosti");
    }

    @Test
    void cancelThenCancelAgain_returns409() {
        Booking booking = createBooking(futureStart(17, 0));

        restTemplate.postForEntity(url("/bookings/" + booking.getBookingId() + "/cancel"), null, Booking.class);
        ResponseEntity<String> second =
                restTemplate.postForEntity(url("/bookings/" + booking.getBookingId() + "/cancel"), null, String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void completeBooking_thenCancel_returns409() {
        Booking booking = createBooking(futureStart(18, 0));

        ResponseEntity<Booking> completed = restTemplate.postForEntity(
                url("/bookings/" + booking.getBookingId() + "/complete"), null, Booking.class);
        assertThat(completed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completed.getBody().getStatus()).isEqualTo(BookingStatus.COMPLETED);

        ResponseEntity<String> cancel = restTemplate.postForEntity(
                url("/bookings/" + booking.getBookingId() + "/cancel"), null, String.class);
        assertThat(cancel.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getBooking_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/bookings/999999"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBookings_filteredByClient() {
        createBooking(futureStart(19, 0));

        ResponseEntity<Booking[]> response =
                restTemplate.getForEntity(url("/bookings?clientId=" + CLIENT_ID), Booking[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).allMatch(b -> b.getClientId() == CLIENT_ID);
    }

    // --- pomocne metode ---

    private Booking createBooking(LocalDateTime start) {
        ResponseEntity<Booking> response = postBooking(start, Booking.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private <T> ResponseEntity<T> postBooking(LocalDateTime start, Class<T> type) {
        CreateBookingRequest request = new CreateBookingRequest(
                CLIENT_ID, SALON_ID, STAFF_ID, SERVICE_ID, start, "test termin");
        return restTemplate.postForEntity(url("/bookings"), request, type);
    }

    /** Uvek u buducnosti, da ne padne na proveri "termin u proslosti". */
    private LocalDateTime futureStart(int hour, int minute) {
        return LocalDateTime.now().plusDays(7).with(LocalTime.of(hour, minute)).withSecond(0).withNano(0);
    }

    private Salon salon() {
        return new Salon(SALON_ID, "Salon Bella", "Bulevar oslobodjenja 10", "Novi Sad",
                "021123456", "Frizerski salon", LocalTime.of(9, 0), LocalTime.of(20, 0), 1L, null);
    }

    private ServiceOffering serviceOffering(long salonId, int durationMinutes, boolean active) {
        return new ServiceOffering(SERVICE_ID, salonId, "Zensko sisanje", "Pranje, sisanje i fen",
                durationMinutes, new BigDecimal("1500.00"), active, null);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
