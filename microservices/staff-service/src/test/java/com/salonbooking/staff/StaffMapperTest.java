package com.salonbooking.staff;

import com.salonbooking.api.staff.Staff;
import com.salonbooking.api.staff.WorkingHours;
import com.salonbooking.staff.persistence.StaffEntity;
import com.salonbooking.staff.persistence.WorkingHoursEntity;
import com.salonbooking.staff.services.StaffMapper;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaffMapperTest {

    private final StaffMapper mapper = new StaffMapper();

    @Test
    void entityToApi_mapsAllFieldsIncludingWorkingHours() {
        StaffEntity entity = new StaffEntity(1L, 42L, "Ana", "Jovanovic", "frizer", true);
        entity.setId(5L);
        entity.addWorkingHours(new WorkingHoursEntity(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        entity.addWorkingHours(new WorkingHoursEntity(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(14, 0)));

        Staff api = mapper.entityToApi(entity, "10.0.0.5:8084");

        assertThat(api.getStaffId()).isEqualTo(5L);
        assertThat(api.getSalonId()).isEqualTo(1L);
        assertThat(api.getUserId()).isEqualTo(42L);
        assertThat(api.getFirstName()).isEqualTo("Ana");
        assertThat(api.getSpecialization()).isEqualTo("frizer");
        assertThat(api.getServiceAddress()).isEqualTo("10.0.0.5:8084");
        assertThat(api.getWorkingHours()).hasSize(2);
        // sortirano po danu: ponedeljak pre subote
        assertThat(api.getWorkingHours().get(0).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(api.getWorkingHours().get(1).getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
    }

    @Test
    void apiToEntity_linksWorkingHoursBackToStaff() {
        Staff api = new Staff(0, 1L, null, "Marko", "Peric", "maser", true,
                List.of(new WorkingHours(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(18, 0))),
                null);

        StaffEntity entity = mapper.apiToEntity(api);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getFirstName()).isEqualTo("Marko");
        assertThat(entity.getWorkingHours()).hasSize(1);
        // obe strane veze moraju biti postavljene, inace staff_id ostaje prazan u bazi
        assertThat(entity.getWorkingHours().get(0).getStaff()).isSameAs(entity);
    }

    @Test
    void workingHoursFor_findsCorrectDay() {
        StaffEntity entity = new StaffEntity(1L, null, "Ana", "Jovanovic", "frizer", true);
        entity.addWorkingHours(new WorkingHoursEntity(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));

        assertThat(entity.workingHoursFor(DayOfWeek.MONDAY)).isPresent();
        assertThat(entity.workingHoursFor(DayOfWeek.SUNDAY)).isEmpty();
    }
}
