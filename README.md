# Salon Booking System

Projekat za predmet **Distribuirani informacioni sistemi**.

Aplikacija predstavlja mikroservisni sistem za zakazivanje termina u salonima lepote. Razvijena je korišćenjem Spring Boot i Spring Cloud tehnologija, po uzoru na primere iz knjige _Hands-On Microservices with Spring Boot and Spring Cloud_.

Sistem trenutno sadrži **6 poslovnih mikroservisa**, koristi REST komunikaciju između servisa i Kafka-u za asinhronu obradu događaja. Sve komponente su kontejnerizovane, a projekat sadrži unit i integracione testove, kao i GitHub Actions pipeline.

Detaljniji opis poslovne logike, dijagram sistema i uputstvo za build, test i deploy nalaze se u [`DOCUMENTATION.md`](./DOCUMENTATION.md).

Dodatna objašnjenja pojedinih odluka u projektu nalaze se u [`docs/OBJASNJENJA.md`](./docs/OBJASNJENJA.md).

## Brzi start

Preduslovi:

- JDK 17
- Docker Desktop

Build projekta:

```bash
./gradlew build -x test
```

Pokretanje testova:

```bash
./gradlew test
```

Pokretanje celog sistema:

```bash
docker compose up --build
```

Nakon pokretanja, zahtevi ka sistemu mogu da se šalju preko Gateway-a:

```text
http://localhost:8080
```

Na primer:

```bash
curl http://localhost:8080/salons
```

Korisne adrese:

| Adresa                  | Namena        |
| ----------------------- | ------------- |
| `http://localhost:8080` | API Gateway   |
| `http://localhost:8761` | Eureka Server |
| `http://localhost:8888` | Config Server |

Za gašenje sistema:

```bash
docker compose down
```

Ako je potrebno obrisati i Docker volumene:

```bash
docker compose down -v
```

## Produkcijsko okruženje

Najpre napraviti `.env` fajl na osnovu primera:

```bash
cp .env.prod.example .env
```

U `.env` je potrebno uneti lozinke i `JWT_SECRET`.

Pokretanje produkcijske konfiguracije:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

## Komponente

### Poslovni mikroservisi

| Mikroservis            | Odgovornost                          | Port |
| ---------------------- | ------------------------------------ | ---: |
| `salon-service`        | podaci o salonima                    | 8081 |
| `auth-service`         | registracija, login i JWT            | 8082 |
| `catalog-service`      | usluge salona, trajanje i cena       | 8083 |
| `staff-service`        | zaposleni i radno vreme              | 8084 |
| `booking-service`      | zakazivanje termina i Kafka producer | 8085 |
| `notification-service` | obrada Kafka događaja                | 8086 |

### Infrastruktura

Sistem koristi i sledeće infrastrukturne komponente:

- `eureka-server` — service discovery
- `config-server` — centralizovana konfiguracija
- `gateway` — jedinstvena ulazna tačka
- Kafka u KRaft režimu
- 5 PostgreSQL baza

## Struktura repozitorijuma

```text
api/                      zajednički API ugovori
util/                     zajednički izuzeci i obrada grešaka

microservices/            poslovni mikroservisi

spring-cloud/
    eureka-server/
    config-server/
    gateway/

config-repo/              centralizovana konfiguracija
docs/                     dodatna dokumentacija

.github/workflows/ci.yml  GitHub Actions pipeline

docker-compose.yml        razvojno okruženje
docker-compose.prod.yml   produkcijsko okruženje
.env.prod.example         primer produkcijskih promenljivih
```
