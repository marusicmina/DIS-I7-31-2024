package com.salonbooking.staff;

import com.salonbooking.api.staff.AvailabilityResponse;
import com.salonbooking.api.staff.Staff;
import com.salonbooking.api.staff.WorkingHours;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaffServiceApplicationTests {

    private static final String PONEDELJAK = "2026-09-07";
    private static final String NEDELJA = "2026-09-06";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("staffdb")
            .withUsername("staff")
            .withPassword("staff");

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

    @Test
    void contextLoads() {
    }

    @Test
    void createAndFetchStaffMember() {
        Staff created = createStaff(1L, "Ana", "Jovanovic", true, mondayToFriday());

        assertThat(created.getStaffId()).isPositive();
        assertThat(created.getWorkingHours()).hasSize(5);

        ResponseEntity<Staff> response =
                restTemplate.getForEntity(url("/staff/" + created.getStaffId()), Staff.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFirstName()).isEqualTo("Ana");
        assertThat(response.getBody().getWorkingHours()).hasSize(5);
    }

    @Test
    void getStaffMembers_filteredBySalon() {
        createStaff(77L, "Marko", "Peric", true, mondayToFriday());

        ResponseEntity<Staff[]> response =
                restTemplate.getForEntity(url("/staff?salonId=77"), Staff[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).allMatch(s -> s.getSalonId() == 77L);
    }

    @Test
    void getStaffMember_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/staff/999999"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createStaffMember_withoutFirstName_returns422() {
        Staff invalid = new Staff(0, 1L, null, "", "Peric", "frizer", true, List.of(), null);

        ResponseEntity<String> response = restTemplate.postForEntity(url("/staff"), invalid, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void availability_insideWorkingHours_isAvailable() {
        Staff staff = createStaff(1L, "Ana", "Jovanovic", true, mondayToFriday());

        AvailabilityResponse result = checkAvailability(
                staff.getStaffId(), PONEDELJAK + "T10:00:00", PONEDELJAK + "T10:45:00");

        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getReason()).isNull();
    }

    @Test
    void availability_exactlyAtOpeningTime_isAvailable() {
        Staff staff = createStaff(1L, "Ana", "Jovanovic", true, mondayToFriday());

        // Radi od 09:00 - termin koji pocinje tacno u 09:00 mora da prodje.
        AvailabilityResponse result = checkAvailability(
                staff.getStaffId(), PONEDELJAK + "T09:00:00", PONEDELJAK + "T09:30:00");

        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void availability_outsideWorkingHours_isNotAvailable() {
        Staff staff = createStaff(1L, "Ana", "Jovanovic", true, mondayToFriday());

        AvailabilityResponse result = checkAvailability(
                staff.getStaffId(), PONEDELJAK + "T22:00:00", PONEDELJAK + "T22:45:00");

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getReason()).contains("izvan radnog vremena");
    }

    @Test
    void availability_dayOffWork_isNotAvailable() {
        Staff staff = createStaff(1L, "Ana", "Jovanovic", true, mondayToFriday());

        // Nedeljom ne radi.
        AvailabilityResponse result = checkAvailability(
                staff.getStaffId(), NEDELJA + "T10:00:00", NEDELJA + "T10:45:00");

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getReason()).contains("ne radi");
    }

    @Test
    void availability_inactiveStaff_isNotAvailable() {
        Staff staff = createStaff(1L, "Neaktivna", "Osoba", false, mondayToFriday());

        AvailabilityResponse result = checkAvailability(
                staff.getStaffId(), PONEDELJAK + "T10:00:00", PONEDELJAK + "T10:45:00");

        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getReason()).contains("nije aktivan");
    }

    @Test
    void availability_endBeforeStart_returns422() {
        Staff staff = createStaff(1L, "Ana", "Jovanovic", true, mondayToFriday());

        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/staff/" + staff.getStaffId() + "/availability"
                        + "?start=" + PONEDELJAK + "T11:00:00"
                        + "&end=" + PONEDELJAK + "T10:00:00"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // --- pomocne metode ---

    private Staff createStaff(long salonId, String firstName, String lastName,
                               boolean active, List<WorkingHours> hours) {
        Staff body = new Staff(0, salonId, null, firstName, lastName, "frizer", active, hours, null);
        ResponseEntity<Staff> response = restTemplate.postForEntity(url("/staff"), body, Staff.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private AvailabilityResponse checkAvailability(long staffId, String start, String end) {
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(
                url("/staff/" + staffId + "/availability?start=" + start + "&end=" + end),
                AvailabilityResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private List<WorkingHours> mondayToFriday() {
        return List.of(
                new WorkingHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new WorkingHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new WorkingHours(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new WorkingHours(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)),
                new WorkingHours(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        );
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
