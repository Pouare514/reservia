# 🎟️ Réservia — Système de réservation (cinéma, hôtel, coiffeur)

Backend **Spring Boot 3.5 + Java 17** · Frontend **Thymeleaf + Bootstrap 5** · Base **H2** · Tests **JUnit 5 + AssertJ**.
Gestion complète des **disponibilités, réservations et annulations**, avec règles métier **transactionnelles**.
API REST JSON incluse (`/api`) pour brancher un SPA plus tard.

## 1. Fonctionnalités

| Domaine | Services | Ressources | Capacité |
|---|---|---|---|
| 🎬 Cinéma | Film « Dune 2 », concert symphonique | Salle 1 (120 pl.), Salle 2 (60 pl.) | 60–120 places / séance |
| 🏨 Hôtel | Nuitée double (89 €), suite familiale (149 €) | Chambre 101, 102, Suite | 2–4 pers. / nuitée 15h → 11h |
| 💈 Coiffure | Coupe homme (19 €), couleur + balayage (65 €) | Karim, Sofia | 1 client / créneau |

- **Disponibilités** (`/creneaux`) : liste filtrable par catégorie + date, badge couleur par univers, jauge de remplissage, places restantes et prix en temps réel.
- **Réservation** (`/reserver/{slotId}`) : formulaire validé (nom, email, tél, nb places ≤ restantes) → code à 8 caractères, client créé/mis à jour automatiquement par email.
- **Annulations** : réservation (`/reservations` → bouton Annuler, libère les places et rouvre le créneau `COMPLET`) + créneau côté admin (bouton ✕).
- **Admin** (`/admin/creneaux/nouveau`) : création de créneau avec **détection de chevauchement** par ressource.
- **Recherche** : mes réservations filtrables par email ; page services filtrable par catégorie.

## 2. Prérequis & lancement

- **Java 17+** (testé avec JDK 25, bytecode `release 21`) et **aucun Maven à installer** : le wrapper `mvnw` télécharge Maven tout seul au premier lancement.
- Aucune base à installer : **H2 en mémoire**, schéma généré (`ddl-auto=update`), données démo chargées au démarrage.

```powershell
git clone https://github.com/Pouare514/reservia.git
cd reservia

# Lancer l'app (le wrapper télécharge Maven 3.9 tout seul si besoin) :
.\mvnw.cmd spring-boot:run

# Lancer les tests :
.\mvnw.cmd verify
# Résultat attendu : Tests run: 37, Failures: 0, Errors: 0, Skipped: 0 (+ gate JaCoCo 85 % lignes)
```

Pages et outils une fois démarré :

| URL | Contenu |
|---|---|
| 🏠 http://localhost:8080 | Accueil : titre + tableau des départs panaché |
| 🎫 http://localhost:8080/creneaux | Billets à souche + filtres univers/date |
| 💈 http://localhost:8080/services | Programme des 6 services |
| 📋 http://localhost:8080/reservations | Registre + filtre email + action Rendre |
| ➕ http://localhost:8080/admin/creneaux/nouveau | Ouverture de créneau (admin) |
| 🗄️ http://localhost:8080/h2-console | Console H2 — JDBC `jdbc:h2:mem:reservations`, user `sa`, mot de passe vide |
| 📖 http://localhost:8080/swagger-ui.html | Doc OpenAPI interactive (`/v3/api-docs` en JSON) |
| 💚 http://localhost:8080/actuator/health | Sonde Actuator |
| 🔌 http://localhost:8080/api/slots/available | API JSON des créneaux (records) |

## 3. Arborescence du projet

```
reservation/
├── pom.xml                                   # Spring Boot 3.5.6, JPA, Thymeleaf, Validation, H2, Tests
├── README.md                                 # ce fichier : toute la doc
├── .gitignore
└── src/
    ├── main/java/com/reservation/
    │   ├── ReservationApplication.java       # point d'entrée + Locale FR par défaut (dates en français)
    │   ├── config/
    │   │   └── ReservationProperties.java    # réglages métier externalisés (préfixe reservation.*)
    │   ├── api/                              # records de sortie REST (pas d'entités dans le JSON)
    │   │   ├── ConfirmationReservation.java  # code, service, lieu, début, places, prix total, statut
    │   │   └── CreneauDisponible.java        # projection allégée d'un créneau
    │   ├── model/                            # entités JPA + enums
    │   │   ├── Customer.java                 # client (email unique)
    │   │   ├── ServiceOffering.java          # prestation (film, nuitée, coupe…)
    │   │   ├── Resource.java                 # salle / chambre / coiffeur
    │   │   ├── TimeSlot.java                 # créneau (placesTotales / placesReservees / statut + @Version)
    │   │   ├── Booking.java                  # réservation (code unique, statut, creeLe/annuleLe)
    │   │   ├── Category.java                 # CINEMA / HOTEL / COIFFURE (+ labels)
    │   │   ├── BookingStatus.java            # CONFIRMEE / ANNULEE
    │   │   └── SlotStatus.java               # OUVERT / COMPLET / ANNULE
    │   ├── repository/                       # Spring Data JPA
    │   │   ├── CustomerRepository.java       # findByEmailIgnoreCase
    │   │   ├── ServiceOfferingRepository.java
    │   │   ├── ResourceRepository.java
    │   │   ├── TimeSlotRepository.java       # findAvailable (JOIN FETCH anti-N+1), findOverlapping, findByIdForUpdate (PESSIMISTIC_WRITE), countAvailable
    │   │   └── BookingRepository.java        # par email / par statut
    │   ├── service/                          # logique métier transactionnelle
    │   │   ├── BookingService.java           # reserver() + annuler() (erreurs typées, délai externalisé)
    │   │   ├── TimeSlotService.java          # createSlot() + cancelSlot() / reopenSlot()
    │   │   └── DataInitializer.java          # jeu de démo (6 services, 7 ressources, ~30 créneaux)
    │   ├── controller/
    │   │   ├── HomeController.java           # 5 pages Thymeleaf + actions POST (accueil panaché 2/univers, garde créneau supprimé)
    │   │   └── ApiController.java            # REST /api documenté OpenAPI (retourne des records)
    │   ├── dto/
    │   │   └── BookingRequest.java           # slotId + nbPlaces + nom + email + tel (validation Jakarta)
    │   └── exception/                        # hiérarchie scellée → HTTP via ApiExceptionHandler
    │       ├── ReservationException.java     # racine sealed (Introuvable, Conflit, Invalides)
    │       ├── ReservationConflitException.java  # sealed : Complet, Ferme, Chevauchement, Delai, DejaAnnulee → 409
    │       ├── ReservationIntrouvableException.java  # → 404
    │       ├── DonneesInvalidesException.java        # → 400
    │       └── ApiExceptionHandler.java      # @RestControllerAdvice + switch à motifs exhaustif
    ├── main/resources/
    │   ├── application.properties            # port, H2, Flyway, reservation.*, actuator, perfs
    │   ├── db/migration/V1__schema.sql       # schéma versionné (Hibernate valide au boot)
    │   ├── templates/                        # index, services, slots, booking-form, bookings, slot-form (+ layout)
    │   └── static/css/style.css              # design v2 : Inter + Sora, hero mesh, cartes accentuées, pills, animations
    └── test/java/com/reservation/
        ├── service/                          # intégration H2 (rollback)
        │   ├── BookingServiceTest.java       # 6 tests réservation/annulation
        │   ├── TimeSlotServiceTest.java      # 9 tests disponibilités
        │   └── DataInitializerTest.java      # jeu démo présent au boot
        └── controller/                       # slices @WebMvcTest (Thymeleaf réel + mocks)
            ├── ApiControllerTest.java        # 7 tests JSON + codes 201/400/404/409
            └── HomeControllerTest.java       # 14 tests pages + redirects + formulaires
```

Architecture : **MVC en couches** — `controller → service (@Transactional) → repository → model (JPA)`.
Relations : `Booking N-1 Customer`, `Booking N-1 TimeSlot`, `TimeSlot N-1 ServiceOffering`, `TimeSlot N-1 Resource`.
Les contrôleurs ne contiennent aucune règle métier : tout est dans les services, partagés entre le web et l'API.

## 4. Modèle de données

- **Customer** : `id`, `nom` (obligatoire), `email` (obligatoire, unique, validé), `telephone`.
- **ServiceOffering** : `id`, `nom`, `categorie` (CINEMA/HOTEL/COIFFURE), `description`, `dureeMinutes` (≥1), `prix` (≥0), `capaciteDefaut` (≥1, reprise à la création des créneaux), `icone` (emoji).
- **Resource** : `id`, `nom`, `type` (SALLE/CHAMBRE/SUITE/COIFFEUR…), `categorie`, `capacite`, `localisation`.
- **TimeSlot** : `id`, `service` (N-1), `resource` (N-1), `debut`, `fin`, `placesTotales`, `placesReservees` (confirmées uniquement), `statut` (OUVERT/COMPLET/ANNULE), `version` (`@Version` optimiste). Méthodes transient : `getPlacesDisponibles()`, `isComplet()`, `isPasse()`.
- **Booking** : `id`, `code` (unique, 8 caractères, généré au `@PrePersist`), `customer` (N-1), `slot` (N-1), `nbPlaces`, `statut` (CONFIRMEE/ANNULEE), `creeLe`, `annuleLe`.

## 5. Règles métier (couche service)

1. **Anti-surréservation** : `BookingService.reserver()` est `@Transactional` + verrou **pessimiste** `PESSIMISTIC_WRITE` (`findByIdForUpdate`) sur le créneau → même en concurrence, `placesReservees` ne dépasse jamais `placesTotales`. Passage à `COMPLET` automatique quand `placesDisponibles == 0`.
2. **Anti-chevauchement** : `TimeSlotService.createSlot()` refuse tout overlap `[debut, fin[` sur la même ressource (`findOverlapping`, statuts `ANNULE` exclus). Créneaux adjacents (`fin == debut`) autorisés. Cas d'usage : 2 films ne passent pas dans la même salle, 2 clients ne prennent pas la même chambre la même nuit, 2 coupes ne se chevauchent pas chez Karim.
3. **Délai d'annulation externalisé** : `reservation.delai-annulation-minutes=60` (voir `ReservationProperties`) ; `BookingService.annuler()` refuse en deçà (`DelaiAnnulationException`).
4. **Cohérence** : réservation impossible sur créneau `ANNULE` ou passé, ou sans assez de places ; double annulation refusée ; l'annulation décrémente `placesReservees` et rouvre un créneau `COMPLET` vers `OUVERT`.
5. **Validation** : dates obligatoires, `fin > debut`, pas de créneau dans le passé, `nbPlaces ≥ 1`, email valide (Jakarta Validation côté DTO/entités + formulaires).

## 6. API REST

L'API renvoie des **records** (`ConfirmationReservation`, `CreneauDisponible`), jamais d'entités.
Doc interactive : http://localhost:8080/swagger-ui.html (`/v3/api-docs` en JSON).

| Méthode | Endpoint | Réponse | Erreurs typées |
|---|---|---|---|
| GET | `/api/services` | 200 catalogue | — |
| GET | `/api/slots/available?categorie=CINEMA` | 200 `[CreneauDisponible]` | — |
| POST | `/api/bookings` | 201 `ConfirmationReservation` | 400 entrée invalide, 404 créneau inconnu, 409 complet/fermé |
| DELETE | `/api/bookings/{id}` | 200 `ConfirmationReservation` (statut ANNULEE) | 404 inconnue, 409 déjà annulée / délai dépassé |
| GET | `/api/bookings?email=…` | 200 `[ConfirmationReservation]` | — |

Exemple de confirmation : `{"code":"F8211718","service":"Dune : Deuxième partie","lieu":"Salle 1","debut":"…","nbPlaces":2,"prixTotal":19.00,"email":"marie@exemple.com","statut":"CONFIRMEE"}`.

```powershell
# Réserver (PowerShell) :
$body = '{"slotId":1,"nbPlaces":2,"nom":"Marie Dupont","email":"marie@exemple.com","telephone":"0612345678"}'
Invoke-WebRequest http://localhost:8080/api/bookings -Method POST -ContentType "application/json" -Body $body

# Annuler la réservation n°1 :
Invoke-WebRequest http://localhost:8080/api/bookings/1 -Method DELETE

# Disponibilités coiffure :
Invoke-WebRequest "http://localhost:8080/api/slots/available?categorie=COIFFURE"
```

```bash
# Réserver (curl) :
curl -X POST localhost:8080/api/bookings -H "Content-Type: application/json" \
  -d '{"slotId":1,"nbPlaces":2,"nom":"Marie Dupont","email":"marie@exemple.com"}'
```

## 7. Données de démo (DataInitializer)

Chargées au premier démarrage si la base est vide : 6 services, 7 ressources, ~30 créneaux futurs (J+1 à J+3), 1 cliente (`alice@example.com`).
Cinéma : 2 séances/jour/salle (14h30, 20h30) + concert 19h. Hôtel : nuitées 15h → 11h (+20h = 1200 min). Coiffure : 6 coupes de 30 min/jour (Karim, pas de 40 min) + 2 couleurs de 90 min (Sofia).

## 8. Tests (37, tous verts) + JaCoCo 87 % lignes

`./mvnw verify` : compilation Java 21,
tests, rapport JaCoCo (`target/site/jacoco/index.html`), **gate 85 % lignes** (échec du build en deçà).

`@SpringBootTest` sur H2 (rollback par test) + slices `@WebMvcTest` (Thymeleaf réel, services mockés —
un template cassé fait échouer le test).

**BookingServiceTest (6)** — `src/test/java/com/reservation/service/BookingServiceTest.java` :
1. Réservation OK → code 8 caractères, statut CONFIRMEE, `placesReservees` décrémenté.
2. Surréservation refusée → `CreneauCompletException` (« places »).
3. Créneau plein → statut `COMPLET`.
4. Annulation → places libérées, créneau rouvert `OUVERT`, réservation `ANNULEE`.
5. Double annulation → `ReservationDejaAnnuleeException` (« déjà annulée »).
6. Annulation < 1h avant → `DelaiAnnulationException` (« délai », seuil externalisé).

**TimeSlotServiceTest (9)** — `src/test/java/com/reservation/service/TimeSlotServiceTest.java` :
1. Création OK sans chevauchement.
2. Chevauchement même ressource → `ChevauchementException` (« Conflit »).
3. Créneaux adjacents (`fin == debut`) → autorisés.
4. `fin < debut` → `DonneesInvalidesException`.
5. Créneau dans le passé → refusé.
6. Annuler puis rouvrir (`ANNULE` → `OUVERT`), plein → reste `COMPLET`, inexistant → 404 logique.
7. Listage des disponibilités.

**DataInitializerTest (1)** — 6 services, > 20 créneaux, cliente démo au boot.

**ApiControllerTest (7)** — catalogue, records de dispo, 201 + confirmation (prix total calculé),
409 complet, 404 inconnue, annulation, recherche par email.

**HomeControllerTest (14)** — les 6 pages rendent 200, redirects (inconnu, succès, conflits),
formulaires valides et en erreur.

```
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
JaCoCo : lignes 87.0 %, instructions 87.4 %, branches 68.8 % (gate lignes ≥ 85 % OK)
```

Scénario manuel à rejouer dans le navigateur : filtrer `/creneaux?categorie=COIFFURE` → Réserver 1 place → vérifier le code sur `/reservations?email=…` → Annuler → vérifier que la place est de nouveau disponible.


## 9. Choix techniques

| Choix | Pourquoi |
|---|---|
| Spring Boot 3.5 + Java 21 (`release 21`) | Records, classes scellées, switch à motifs ; compatible JDK récents (testé JDK 25) |
| Thymeleaf SSR + Bootstrap 5 (structure seule) | Zéro build JS, rendu serveur ; tout le visuel vient du CSS maison |
| Identité « guichet » (CSS pur, sans lib) | Papier/encre/rouge tampon, Bricolage Grotesque + Archivo, billet perforé + tableau des départs : parti pris anti-template, ancré dans le métier du billet |
| Locale FR par défaut (`Locale.setDefault(FRANCE)`) | Dates Thymeleaf en français au lieu d'anglais (locale système en_GB) |
| Perfs : lazy-init, pas de bannière/JMX, `open-in-view=false`, compression HTTP, cache Thymeleaf, logs WARN | Boot en ~3 s, pages servies plus vite |
| H2 en mémoire + Flyway (`V1__schema.sql`, `ddl-auto=validate`) | Schéma versionné et vérifié au boot, démo sans Docker ; console `/h2-console` intégrée |
| Builders Java explicites (**sans Lombok**) | Lombok incompatible avec le JDK 25 installé → code sans annotation processing, plus robuste |
| JUnit 5 + AssertJ + MockMvc (`spring-boot-starter-test`) | 37 tests (intégration H2 + slices web), gate JaCoCo 85 % lignes |
| Maven Wrapper (`mvnw`) | Build reproductible sans rien installer : Maven 3.9 téléchargé tout seul |
| Monolithe + API REST | Évite la complexité React/Vue pour ce TP ; l'API `/api` permet un SPA ultérieur sans toucher au métier |
| `PESSIMISTIC_WRITE` + `@Version` | Double garde-fou concurrence : verrou à la réservation, version optimiste en général |
| Exceptions scellées + `ApiExceptionHandler` | `ReservationException` (permits Introuvable/Conflit/Invalides) → 404/409/400 via switch exhaustif ; le web garde ses bandeaux |
| Records API (`ConfirmationReservation`, `CreneauDisponible`) | Les entités JPA ne fuient plus dans le JSON ; prix total calculé côté serveur |
| `ReservationProperties` (`reservation.*`) | Délai d'annulation externalisé, plus de constante en dur |
| `JOIN FETCH` sur `findAvailable` | Service + ressource chargés en une requête (anti-N+1 sur la page billets) |
| springdoc-openapi | Doc interactive `/swagger-ui.html` + `/v3/api-docs` |
| Actuator (`health`, `info`) | Observabilité minimale sans sécurité sur ce projet démo |

Écarté en connaissance de cause : **Spring Security / OAuth2-JWT** (pas d'utilisateurs, casserait les formulaires et la console H2 pour zéro gain démo), **WebFlux/R2DBC** (charge classique, JPA bloquant assumé et isolé dans les services), **microservices** (un seul bounded context), **Testcontainers** (H2 suffit, Flyway valide le SQL réel au boot).

## 10. Améliorations possibles

- Spring Security (rôles admin/client), protection CSRF activée sur les POST admin.
- PostgreSQL (les migrations Flyway suivront sans changement), pagination/recherche plein-texte des créneaux.
- Emails de confirmation (Spring Mail), paiement Stripe, QR code du code de réservation.
- Tests de charge concurrence (Awaitility + threads), Testcontainers pour la CI.
- Frontend React/Vue consommant `/api`, notifications temps réel (WebSocket) quand un créneau se remplit.
