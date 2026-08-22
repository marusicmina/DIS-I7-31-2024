# Objašnjenja odluka u projektu

Ovaj dokument sadrži kratka objašnjenja najvažnijih odluka koje sam donela tokom izrade projekta. Fokus je na tome kako su pojedini delovi sistema organizovani i zbog čega su odabrana baš takva rešenja.

---

## 1. Struktura projekta

### `api` i `util` moduli

`api` modul sadrži DTO klase i interfejse koje koriste različiti servisi pri međusobnoj komunikaciji. Na taj način isti objekti predstavljaju zajednički ugovor između servisa. Na primer, kada `booking-service` komunicira sa `catalog-service`, oba koriste istu `ServiceOffering` klasu, čime se smanjuje mogućnost neslaganja u strukturi podataka.

`util` modul sam izdvojila za zajedničke izuzetke i obradu grešaka. Time se postiže da različiti mikroservisi vraćaju greške u istom formatu.

U `api` modulu nisam uključivala Swagger/OpenAPI zavisnosti, već samo standardne Spring MVC anotacije. Na taj način svaki mikroservis može samostalno da odluči da li mu je Swagger potreban i da ga uključi kroz svoj `build.gradle`.

---

## 2. Baze podataka

### Odvojena baza za svaki servis

Za svaki mikroservis sam koristila posebnu bazu podataka. Ideja je da svaki servis bude vlasnik svojih podataka i da drugi servisi tim podacima pristupaju preko njegovog API-ja, a ne direktnim pristupom bazi.

Na taj način servisi ostaju nezavisniji i promena šeme jedne baze ne utiče direktno na ostale delove sistema.

Iz istog razloga između podataka koji pripadaju različitim servisima nema stranih ključeva. Na primer, `salonId` u `catalog-service` predstavlja običan identifikator:

```java
@Column(nullable = false)
private long salonId;
```

Podaci o salonu se nalaze u `salondb`, dok se podaci o uslugama nalaze u `catalogdb`. Zbog toga postojanje salona ne proverava baza `catalog-service`-a, već se ta provera obavlja pozivom ka `salon-service`.

### Tipovi podataka

Za cenu sam koristila `BigDecimal` umesto `double`, jer je pogodniji za rad sa novčanim vrednostima i izbegava probleme sa preciznošću decimalnih brojeva.

Za enum vrednosti koristim:

```java
@Enumerated(EnumType.STRING)
private DayOfWeek dayOfWeek;
```

Na taj način se u bazu upisuje naziv vrednosti, na primer `MONDAY`, umesto njenog rednog broja. To je sigurnije ako se enum kasnije proširuje ili menja.

### Radno vreme zaposlenih

Radno vreme zaposlenog se učitava zajedno sa zaposlenim, jer se radi o malom broju zapisa i ti podaci su potrebni pri proveri dostupnosti.

Kod dodavanja radnog vremena koristim pomoćnu metodu:

```java
public void addWorkingHours(WorkingHoursEntity wh) {
    wh.setStaff(this);
    this.workingHours.add(wh);
}
```

Na ovaj način se pravilno postavljaju obe strane JPA veze.

---

## 3. Rukovanje greškama

Za centralizovano rukovanje greškama koristim `GlobalControllerExceptionHandler` iz `util` modula. Time svi servisi vraćaju greške u istom formatu.

Koriste se sledeći HTTP statusi:

| Status                       | Situacija                                  | Primer                           |
| ---------------------------- | ------------------------------------------ | -------------------------------- |
| **404 Not Found**            | traženi resurs ne postoji                  | `/salons/999`                    |
| **422 Unprocessable Entity** | prosleđeni zahtev nema smisla              | negativan ID, termin u prošlosti |
| **409 Conflict**             | zahtev je u konfliktu sa trenutnim stanjem | zaposleni već ima termin         |
| **503 Service Unavailable**  | zavisni servis trenutno nije dostupan      | `staff-service` nije dostupan    |

Na ovaj način se razlikuju greške u podacima od situacija kada resurs ne postoji ili kada neki servis trenutno nije dostupan.

---

## 4. `staff-service` — provera dostupnosti

Jedna od važnijih funkcionalnosti `staff-service`-a je provera da li zaposleni radi u traženom terminu.

Metoda `checkAvailability` proverava:

1. da li su vreme početka i završetka prosleđeni i validni,
2. da li zaposleni postoji,
3. da li je zaposleni aktivan,
4. da li termin ostaje u okviru istog dana,
5. da li zaposleni ima definisano radno vreme za taj dan,
6. da li se traženi termin nalazi unutar njegovog radnog vremena.

Granice radnog vremena su uključive:

```java
boolean insideWorkingHours =
        !startTime.isBefore(hours.getStartTime())
        && !endTime.isAfter(hours.getEndTime());
```

To znači da je, na primer, termin u 09:00 dozvoljen ako zaposlenom radno vreme počinje u 09:00.

`staff-service` ne proverava postojeće rezervacije zaposlenog. Njegova odgovornost je samo da zna da li zaposleni radi u određenom periodu. Informacije o već zakazanim terminima pripadaju `booking-service`-u i proveravaju se u njegovoj bazi.

---

## 5. `booking-service` — kreiranje i upravljanje terminima

`booking-service` predstavlja centralni servis za poslovni proces zakazivanja termina. On sinhrono komunicira sa drugim servisima zato što je pre kreiranja rezervacije potrebno dobiti njihove odgovore.

Prilikom kreiranja termina izvršavaju se sledeće provere:

1. proverava se postojanje salona preko `salon-service`,
2. proverava se usluga preko `catalog-service`,
3. proverava se da li je usluga aktivna i da li pripada izabranom salonu,
4. na osnovu trajanja usluge računa se vreme završetka termina,
5. preko `staff-service` se proverava da li zaposleni tada radi,
6. u bazi `booking-service`-a se proverava da li postoji preklapanje sa postojećim terminima.

Klijent ne šalje vreme završetka termina. Ono se računa na osnovu trajanja izabrane usluge, čime se sprečava da korisnik prosledi neispravno trajanje rezervacije.

### Čuvanje podataka o usluzi

U `BookingEntity` se čuvaju `serviceName` i `price`, iako ti podaci originalno pripadaju `catalog-service`-u.

To je urađeno zato što već kreirana rezervacija treba da zadrži naziv i cenu koji su važili u trenutku zakazivanja. Ako se cena usluge kasnije promeni, prethodno zakazan termin ostaje nepromenjen.

### Provera preklapanja termina

Za proveru preklapanja koristi se uslov:

```sql
where b.staffId = :staffId
  and b.status <> :cancelled
  and b.startTime < :end
  and b.endTime > :start
```

Dva termina se smatraju preklopljenim ako jedan počinje pre završetka drugog i završava se nakon početka drugog.

Termini koji se samo dodiruju nisu u konfliktu. Na primer, ako jedan termin završava u 10:00, drugi može da počne tačno u 10:00.

Otkazani termini se ne uzimaju u obzir jer je njihov vremenski slot ponovo slobodan.

---

## 6. Otpornost na greške — Resilience4j

Pozivi `booking-service`-a ka drugim servisima zaštićeni su pomoću `Retry` i `CircuitBreaker` mehanizama.

`Retry` ponavlja poziv kada postoji mogućnost da je greška privremena, na primer tokom kratkog mrežnog prekida ili dok se servis pokreće.

`CircuitBreaker` se koristi da spreči stalno pozivanje servisa koji je trenutno nedostupan. Nakon određenog broja neuspešnih poziva, circuit breaker privremeno prekida nove pokušaje.

Za mrežne pozive su podešeni i kratki timeout-i, kako zahtev ne bi predugo čekao servis koji ne odgovara.

Greške tipa `404 Not Found` se ne tretiraju kao kvar sistema. Ako tražena usluga ili drugi resurs ne postoji, to predstavlja validan odgovor udaljenog servisa.

Ako nije moguće proveriti raspoloživost zaposlenog, zakazivanje se odbija:

```java
throw new ServiceUnavailableException(
    "Trenutno nije moguce proveriti raspolozivost zaposlenog...");
```

U toj situaciji je sigurnije ne kreirati rezervaciju nego rizikovati duplo zakazivanje.

---

## 7. Eureka — service discovery

Eureka se koristi kao servisni registar.

Svaki mikroservis se pri pokretanju registruje pod svojim imenom, pa ostali servisi ne moraju da znaju njegovu konkretnu adresu i port.

Na primer:

```java
String url =
    "http://staff-service/staff/" + staffId + "/availability";
```

Ovde `staff-service` predstavlja ime servisa registrovano u Eureki.

`RestTemplate` sa `@LoadBalanced` koristi to ime da pronađe odgovarajuću instancu servisa.

Ovakav pristup je koristan jer instance servisa mogu da menjaju adrese ili da postoji više instanci istog servisa, dok ostatak sistema i dalje koristi samo njegovo logičko ime.

---

## 8. Config Server — centralizovana konfiguracija

Za zajedničku konfiguraciju servisa koristim Spring Cloud Config Server.

Na taj način podešavanja koja se ponavljaju kroz više mikroservisa ne moraju da se nalaze u svakom projektu posebno.

Servisi učitavaju konfiguraciju preko:

```yaml
spring.config.import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

Prefiks `optional:` omogućava da servis može da se pokrene i ako Config Server trenutno nije dostupan, koristeći lokalna podrazumevana podešavanja.

Za ovu funkcionalnost mikroservisi koriste `spring-cloud-starter-config`.

U projektu je Config Server podešen u `native` režimu, odnosno konfiguracija se učitava iz lokalnog config foldera. U produkcionom sistemu ista konfiguracija bi mogla da se nalazi u posebnom Git repozitorijumu.

---

## 9. API Gateway

API Gateway predstavlja jedinstvenu ulaznu tačku u sistem.

Bez Gateway-a klijent bi morao da zna adrese svih mikroservisa pojedinačno. Ovako se svi zahtevi šalju na jednu adresu, a Gateway ih prosleđuje odgovarajućem servisu.

Primer rute:

```yaml
- id: catalog-service
  uri: lb://catalog-service
  predicates:
    - Path=/catalog/**
```

`lb://catalog-service` označava da Gateway koristi service discovery i load balancing, odnosno traži servis preko Eureke umesto preko konkretne IP adrese i porta.

Gateway je implementiran kao reaktivna aplikacija zasnovana na WebFlux-u.

Na Gateway nivou bi kasnije mogla da se dodaju i pravila koja važe za ceo sistem, kao što su JWT validacija, rate limiting ili centralizovano logovanje zahteva.

---

## 10. Kafka — asinhrona komunikacija

U projektu se koriste i sinhrona i asinhrona komunikacija.

Sinhroni pozivi koriste se kada je odgovor drugog servisa neophodan da bi se poslovni proces nastavio. Primer je provera raspoloživosti zaposlenog pre kreiranja termina.

Asinhrona komunikacija koristi se za procese kod kojih odgovor nije potreban odmah, kao što su notifikacije nakon kreiranja ili promene statusa termina.

`booking-service` šalje događaje, dok ih `notification-service` obrađuje.

Događaji predstavljaju nešto što se već dogodilo, na primer:

```text
BOOKING_CREATED
BOOKING_CANCELLED
```

Na taj način `booking-service` ne mora da zna ko će obrađivati događaj.

Događaj sadrži podatke potrebne `notification-service`-u, kako taj servis ne bi morao naknadno da poziva `booking-service`.

### Consumer group

`notification-service` koristi:

```yaml
group: notification-group
```

Ako postoji više instanci `notification-service`-a, Kafka raspoređuje poruke između njih, tako da se isti događaj ne obrađuje više puta u okviru iste consumer grupe.

### Redosled događaja

Kao ključ poruke koristi se `bookingId`. Time događaji koji pripadaju istoj rezervaciji završavaju u istoj particiji i njihov redosled ostaje očuvan.

### Obrada neuspešnih poruka

Poruke koje ni nakon više pokušaja ne mogu da se obrade šalju se u dead-letter topic, kako jedna problematična poruka ne bi blokirala obradu ostalih.

U trenutnoj implementaciji upis rezervacije u bazu i slanje Kafka događaja predstavljaju dve odvojene operacije. U produkcionom sistemu bi za dodatnu pouzdanost mogao da se koristi transactional outbox obrazac.

Za komunikaciju koristim Spring Cloud Stream (`StreamBridge` i `Consumer<BookingEvent>`), tako da poslovna logika nije direktno vezana za Kafka API.

Kafka je pokrenuta u KRaft režimu, pa u `docker-compose.yml` nije potreban poseban Zookeeper kontejner.

---

## 11. Testovi

Za integracione testove koristim Testcontainers, koji tokom izvršavanja testa podiže PostgreSQL bazu u Docker kontejneru.

Prednost ovog pristupa u odnosu na bazu kao što je H2 je to što se testovi izvršavaju nad istim tipom baze koji se koristi i u aplikaciji.

U testovima `booking-service`-a spoljne zavisnosti se mock-uju. Na taj način nije potrebno da tokom testa budu pokrenuti `salon-service`, `catalog-service`, `staff-service` i Kafka, već se testira logika samog `booking-service`-a.

Testovi su napravljeni tako da ne zavise od redosleda izvršavanja. Zbog toga pojedinačni testovi koriste različite identifikatore zaposlenih i sopstvene test podatke.

Kod testiranja rada sa datumima koriste se unapred poznati datumi, kako rezultat testa ne bi zavisio od dana kada se test pokreće.

---

## 12. Konfiguracija za lokalno i Docker okruženje

Isti build aplikacije treba da može da radi lokalno iz IDE-a i unutar Docker kontejnera.

Zbog toga adrese baza i drugih servisa nisu direktno upisane u kod, već se koriste promenljive okruženja sa podrazumevanim vrednostima.

Na primer:

```yaml
url: jdbc:postgresql://${SALON_DB_HOST:localhost}:${SALON_DB_PORT:5433}/${SALON_DB_NAME:salondb}
```

Kada servis pokrećem lokalno, koristi se `localhost` i podrazumevani port. Kada se sistem pokrene kroz Docker Compose, vrednosti se prosleđuju kroz environment promenljive.

Baze su na host računaru mapirane na različite portove kako ne bi došlo do konflikta sa lokalno instaliranim PostgreSQL-om.

Na ovaj način isti artefakt može da se koristi u više okruženja, dok se menja samo spoljašnja konfiguracija.
