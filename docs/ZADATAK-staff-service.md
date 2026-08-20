# Zadatak: napiši `staff-service`

Ovo je tvoj deo. `catalog-service` ti stoji kao gotov uzor — struktura je ista,
razlikuje se samo domen i jedna dodatna metoda (provera dostupnosti).

Prepiši `catalog-service` red po red i menjaj gde treba. Ne pokušavaj napamet.

---

## Šta servis radi

Čuva zaposlene u salonima (frizer, kozmetičar…) i njihovo radno vreme po danima,
i odgovara na pitanje **„da li ovaj zaposleni radi u traženom terminu?"**.

API ugovor je već napisan — nalazi se u `api/src/main/java/com/salonbooking/api/staff/`:

- `Staff.java` — DTO zaposlenog
- `WorkingHours.java` — radno vreme za jedan dan u nedelji
- `AvailabilityResponse.java` — odgovor na proveru dostupnosti
- `StaffService.java` — interfejs koji implementiraš

Tvoj posao je da napišeš implementaciju.

---

## Fajlovi koje praviš

Sve ide u `microservices/staff-service/`:

```
build.gradle                                        <- kopija catalog-service verzije, bez izmena
Dockerfile                                          <- kopija, promeni samo EXPOSE na 8084
src/main/resources/application.yml                  <- kopija, promeni portove i imena
src/main/java/com/salonbooking/staff/
    StaffServiceApplication.java
    persistence/StaffEntity.java
    persistence/WorkingHoursEntity.java
    persistence/StaffRepository.java
    services/StaffMapper.java
    services/StaffServiceImpl.java
src/test/java/com/salonbooking/staff/
    StaffMapperTest.java
    StaffServiceApplicationTests.java
```

Na kraju u `settings.gradle` odkomentariši liniju:
```groovy
include ':microservices:staff-service'
```

---

## Vrednosti koje se razlikuju od catalog-service-a

| Šta | catalog-service | staff-service |
|---|---|---|
| `server.port` | 8083 | **8084** |
| baza (u kontejneru) | catalogdb | **staffdb** |
| host port baze | 5435 | **5436** |
| korisnik / lozinka | catalog | **staff** |
| prefiks env varijabli | `CATALOG_DB_` | **`STAFF_DB_`** |
| paket | `com.salonbooking.catalog` | **`com.salonbooking.staff`** |

---

## Entiteti

**`StaffEntity`** — kao `ServiceOfferingEntity`, sa poljima:
`id`, `salonId` (long), `userId` (Long, može biti null), `firstName`, `lastName`,
`specialization`, `active` (boolean).

Plus veza ka radnom vremenu:

```java
@OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
private List<WorkingHoursEntity> workingHours = new ArrayList<>();
```

**`WorkingHoursEntity`** — `id`, `dayOfWeek`, `startTime`, `endTime`, i veza nazad:

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private DayOfWeek dayOfWeek;

private LocalTime startTime;
private LocalTime endTime;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "staff_id")
private StaffEntity staff;
```

> `@Enumerated(EnumType.STRING)` znači da se u bazu upisuje `MONDAY`, a ne broj `1`.
> Sa brojevima bi dodavanje nove vrednosti u enum pomerilo značenje starih zapisa.

---

## Logika `checkAvailability` — srce servisa

Metoda dobija `staffId`, `start` i `end` (oba `LocalDateTime`) i vraća
`AvailabilityResponse`. Redosled provera:

1. **`end` mora biti posle `start`** → inače `InvalidInputException` (422)
2. **`start` i `end` moraju biti istog dana** → termin ne sme da prelazi ponoć;
   vrati `available = false`, razlog „Termin ne moze da prelazi u naredni dan"
3. **Zaposleni mora postojati** → inače `NotFoundException` (404)
4. **Zaposleni mora biti aktivan** → `available = false`, razlog „Zaposleni nije aktivan"
5. **Nađi radno vreme za taj dan u nedelji** (`start.getDayOfWeek()`):
   - nema ga → `available = false`, razlog „Zaposleni ne radi <dan>"
6. **Traženi interval mora biti unutar radnog vremena:**
   ```java
   LocalTime startTime = start.toLocalTime();
   LocalTime endTime = end.toLocalTime();
   boolean unutar = !startTime.isBefore(wh.getStartTime())
                 && !endTime.isAfter(wh.getEndTime());
   ```
   - nije → `available = false`, razlog sa radnim vremenom u poruci
7. Sve prošlo → `available = true`, `reason = null`

> Obrati pažnju na `!startTime.isBefore(...)` umesto `startTime.isAfter(...)`.
> Ako zaposleni radi od 09:00, termin **u** 09:00 je ispravan. Sa `isAfter` bi bio odbijen.

**Šta ovaj servis NAMERNO ne radi:** ne proverava da li zaposleni već ima
zakazan termin u to vreme. Ti podaci žive u bazi `booking-service`-a i on ih
sam proverava. Svaki mikroservis odlučuje isključivo o podacima koje poseduje —
to je granica koju ćeš verovatno braniti na usmenom.

---

## Testovi

**`StaffMapperTest`** — kao `ServiceOfferingMapperTest`: entitet → API i nazad,
proveri da se `workingHours` prenese.

**`StaffServiceApplicationTests`** — kao `CatalogServiceApplicationTests`
(Testcontainers, Postgres), pokrij:

- kreiranje zaposlenog sa radnim vremenom i čitanje nazad
- `GET /staff?salonId=…` filtrira po salonu
- 404 za nepostojećeg zaposlenog
- **dostupnost: termin unutar radnog vremena** → `available = true`
- **dostupnost: termin van radnog vremena** (npr. u 22h) → `available = false`
- **dostupnost: dan kada ne radi** → `available = false`

Za datume u testovima biraj konkretan dan u nedelji, npr. `2026-09-07` je
ponedeljak — tako test ne zavisi od toga kad se pokreće.

---

## Kad završiš

```bash
./gradlew :microservices:staff-service:test
```

Pa mi javi — pregledaću kod i reći šta bih drugačije. Ako negde zapneš, pitaj,
ne gubi sate na jednu grešku.
