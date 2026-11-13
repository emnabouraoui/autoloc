# Atelier — Améliorer la conception d’AutoLoc avec SOLID et les patrons GoF

Ce document répond aux questions de l’atelier et décrit les choix de conception appliqués au projet AutoLoc.

L’objectif est de conserver les fonctionnalités CRUD existantes tout en séparant mieux les responsabilités et en rendant le calcul tarifaire extensible.

---

## 1. Partie A — Identifier les problèmes de conception

### A.1. Responsabilités prises en charge par `VehiculeService`

Dans le code initial, `VehiculeService` cumule plusieurs responsabilités :

1. **Validation métier** : vérifier que le tarif est positif, que le nombre de jours est positif.
2. **Journalisation** : écrire directement dans la sortie standard avec `System.out.println`.
3. **Gestion des véhicules / persistance** : créer et enregistrer un véhicule via le repository.
4. **Calcul tarifaire** : appliquer la formule `tarifJournalier × nombreJours`.
5. **Coordination** : orchestrer ces différentes opérations dans les mêmes méthodes.

Cette classe a donc plusieurs raisons de changer : une nouvelle règle de validation, un changement de format de log, une évolution de la persistance ou une nouvelle politique tarifaire peuvent toutes modifier `VehiculeService`.

### A.2. Pourquoi ces responsabilités peuvent-elles évoluer indépendamment ?

Parce qu’elles répondent à des besoins et à des acteurs différents :

- La **validation** dépend des règles métier et réglementaires : âge minimum, permis, tarif, disponibilité, etc.
- La **journalisation** dépend de l’infrastructure d’observabilité : console, fichier, ELK, niveau de log, format JSON, audit.
- La **gestion des véhicules** dépend de la persistance et de l’API : JPA, SQL, cache, pagination, sécurité.
- Le **calcul tarifaire** dépend des politiques commerciales : standard, week-end, longue durée, entreprise, promotions.

Ces éléments n’ont pas la même fréquence de changement ni les mêmes responsables. Les regrouper dans une seule classe augmente le couplage et le risque de régression.

### A.3. Principe SOLID concerné

Le principe principal est le **SRP (Single Responsibility Principle)** : une classe ne doit avoir qu’une seule raison de changer.

D’autres principes complètent la solution :

- **DIP** pour dépendre d’abstractions de persistance plutôt que d’une implémentation concrète.
- **OCP** pour ajouter des politiques tarifaires sans modifier le service de calcul.

### A.4. Répartition plus claire des responsabilités

| Composant | Responsabilité |
|---|---|
| `VehiculeService` | Orchestrer les cas d’usage CRUD et les transactions |
| `VehiculeValidator` | Vérifier les règles métier du véhicule |
| `JournalisationService` | Centraliser les logs applicatifs |
| `VehiculeRepository` | Abstraction de la persistance JPA |
| `TarificationService` | Choisir et appliquer une stratégie tarifaire |
| `StrategieTarification` et implémentations | Encapsuler chacune une formule de prix |
| DTO / contrôleur | Exposer l’API et valider le format des requêtes |

---

## 2. Partie B — Appliquer SRP et DIP

### B.1. SRP — refactorisation de `VehiculeService`

Le projet utilise des DTO en entrée, donc le validateur métier travaille sur `VehiculeRequestDTO` avant la conversion en entité.

#### `VehiculeValidator`

```java
@Component
public class VehiculeValidator {

    public void valider(VehiculeRequestDTO dto) {
        if (dto == null) {
            throw new BusinessRuleException("Les données du véhicule sont obligatoires");
        }
        if (dto.immatriculation() == null || dto.immatriculation().isBlank()) {
            throw new BusinessRuleException("L'immatriculation est obligatoire");
        }
        if (dto.tarifJournalier() == null
                || dto.tarifJournalier().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le tarif journalier doit être strictement positif");
        }
        // ... autres règles métier
    }
}
```

#### `JournalisationService`

```java
@Service
public class JournalisationService {

    private static final Logger log = LoggerFactory.getLogger(JournalisationService.class);

    public void journaliserCreationVehicule(Long idVehicule, String immatriculation) {
        log.info("Création du véhicule id={} immatriculation={}", idVehicule, immatriculation);
    }
}
```

#### `VehiculeService` après refactorisation

```java
@Transactional
public VehiculeResponseDTO create(VehiculeRequestDTO dto) {
    vehiculeValidator.valider(dto);

    String immatriculation = dto.immatriculation().trim();
    if (vehiculeRepository.existsByImmatriculationIgnoreCase(immatriculation)) {
        throw new ConflictException("Un véhicule avec cette immatriculation existe déjà");
    }

    Vehicule vehicule = new Vehicule();
    apply(vehicule, dto);
    Vehicule saved = vehiculeRepository.save(vehicule);

    journalisationService.journaliserCreationVehicule(
            saved.getIdVehicule(), saved.getImmatriculation());

    return VehiculeResponseDTO.from(saved);
}
```

`VehiculeService` ne valide plus les règles métier et n’écrit plus directement dans la console. Il orchestre :

1. la validation via `VehiculeValidator` ;
2. la vérification d’unicité ;
3. la conversion DTO → entité ;
4. la persistance via le repository ;
5. la journalisation via `JournalisationService`.

Chaque classe peut maintenant évoluer pour sa propre raison.

### B.2. DIP — inversion des dépendances

#### 1. Rôle de `VehiculeRepository`

`VehiculeRepository` est une interface Spring Data JPA. Elle joue le rôle de **port de persistance** entre le service et la base de données.

Elle expose des opérations comme :

```java
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    boolean existsByImmatriculationIgnoreCase(String immatriculation);
}
```

Spring Data génère une implémentation (un proxy) au démarrage. Le service ne manipule donc ni `EntityManager`, ni SQL, ni `SessionFactory`.

#### 2. Pourquoi le service dépend-il d’un contrat plutôt que de créer lui-même son mécanisme de persistance ?

Parce que le service métier ne doit pas connaître les détails techniques de la persistance. Il dépend d’une abstraction, ce qui permet :

- de **tester** le service avec un mock de repository ;
- de **remplacer** la persistance sans réécrire le service ;
- de **réduire le couplage** entre la logique métier et JPA ;
- de respecter le **DIP** : les modules de haut niveau dépendent d’abstractions.

Si `VehiculeService` faisait `new VehiculeRepositoryImpl()` ou gérait lui-même les transactions JDBC, il serait fortement couplé à un choix technique et difficile à tester.

#### 3. Comment Spring injecte-t-il le repository dans le service ?

Le projet utilise l’**injection par constructeur** :

```java
public VehiculeService(VehiculeRepository vehiculeRepository,
                       AgenceRepository agenceRepository,
                       EquipementRepository equipementRepository,
                       VehiculeValidator vehiculeValidator,
                       JournalisationService journalisationService) {
    this.vehiculeRepository = vehiculeRepository;
    // ...
}
```

Au démarrage :

1. Spring scanne les composants (`@Service`, `@Component`, repositories Spring Data).
2. Spring Data crée un proxy qui implémente `VehiculeRepository`.
3. Le conteneur Spring résout les dépendances par type.
4. Le proxy est passé au constructeur de `VehiculeService`.

Il n’y a donc aucun `new VehiculeRepository()` dans le code métier.

#### 4. Lien entre DIP et injection de dépendances

- **DIP** est un principe de conception : dépendre d’abstractions, pas d’implémentations concrètes.
- **DI** est le mécanisme technique qui permet de fournir ces abstractions à l’exécution.

Dans AutoLoc, `VehiculeService` dépend de l’interface `VehiculeRepository` (DIP), et Spring lui injecte le proxy réel via le constructeur (DI). Le même principe s’applique à `VehiculeValidator` et `JournalisationService`.

---

## 3. Partie C — GoF Strategy et principe OCP

### 3.1. Structure mise en place

```text
service/tarification/
├── StrategieTarification.java       (interface)
├── TarificationStandard.java        (tarif normal)
├── TarificationWeekend.java         (-10 %)
├── TarificationLongueDuree.java     (-20 %)
├── TarificationEntreprise.java      (-15 %)
└── TarificationService.java         (sélection de la stratégie)
```

Interface :

```java
@FunctionalInterface
public interface StrategieTarification {
    double calculer(double tarifJournalier, int jours);
}
```

Chaque stratégie est un bean Spring nommé :

```java
@Component("standard")
public class TarificationStandard implements StrategieTarification {
    @Override
    public double calculer(double tarifJournalier, int jours) {
        return tarifJournalier * jours;
    }
}
```

### 3.2. Formules

| Stratégie | Bean Spring | Formule |
|---|---|---|
| Standard | `standard` | `tarifJournalier × jours` |
| Week-end | `weekend` | `tarifJournalier × jours × 0,90` |
| Longue durée | `longueDuree` | `tarifJournalier × jours × 0,80` |
| Entreprise | `entreprise` | `tarifJournalier × jours × 0,85` |

### 3.3. `TarificationService`

```java
@Service
public class TarificationService {

    private final Map<String, StrategieTarification> strategies;

    public TarificationService(Map<String, StrategieTarification> strategies) {
        this.strategies = Map.copyOf(strategies);
    }

    public double calculer(String type, double tarifJournalier, int jours) {
        if (tarifJournalier <= 0) {
            throw new BusinessRuleException("Le tarif journalier doit être strictement positif");
        }
        if (jours <= 0) {
            throw new BusinessRuleException("Le nombre de jours doit être strictement positif");
        }

        StrategieTarification strategie = strategies.get(type);
        if (strategie == null) {
            strategie = strategies.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(type))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "Type de tarification inconnu : " + type));
        }

        return strategie.calculer(tarifJournalier, jours);
    }
}
```

Spring injecte automatiquement une `Map<String, StrategieTarification>` dont les clés sont les noms des beans (`standard`, `weekend`, `longueDuree`, `entreprise`).

Le service ne contient aucune formule : il sélectionne la stratégie et délègue le calcul.

### 3.4. Ajout de `TarificationEntreprise`

L’ajout se limite à créer une nouvelle classe :

```java
@Component("entreprise")
public class TarificationEntreprise implements StrategieTarification {

    private static final double REDUCTION = 0.85;

    @Override
    public double calculer(double tarifJournalier, int jours) {
        return tarifJournalier * jours * REDUCTION;
    }
}
```

Aucune modification de `TarificationService` n’est nécessaire : Spring l’ajoute automatiquement à la Map injectée.

### 3.5. Réponses aux questions

#### 1. Quel problème le patron Strategy permet-il de résoudre ?

Il évite de concentrer toutes les politiques tarifaires dans une succession de `if/else` ou de `switch` dans le service. Chaque algorithme est isolé dans une classe interchangeable, et le service choisit la stratégie appropriée à l’exécution.

#### 2. Pourquoi est-il préférable d’isoler chaque formule dans une classe différente ?

- Chaque classe a une seule responsabilité : une formule.
- Les formules sont testables indépendamment.
- Le code est plus lisible et plus facile à maintenir.
- Une modification de la réduction week-end ne risque pas d’affecter la formule entreprise.
- Les ajouts futurs ne provoquent pas de conflits dans une méthode géante.

#### 3. Comment cette conception applique-t-elle le principe OCP ?

Le service est **fermé à la modification** : on ne le modifie pas pour ajouter une politique. Il est **ouvert à l’extension** : il suffit d’ajouter une nouvelle implémentation de `StrategieTarification` annotée comme bean Spring. C’est exactement le cas de `TarificationEntreprise`.

#### 4. Que doit faire le service si le type de tarif demandé n’existe pas ?

Il doit signaler une erreur explicite, par exemple avec `BusinessRuleException`, ce qui produit une réponse HTTP `400` dans l’API. Le message peut lister les types disponibles :

```text
Type de tarification inconnu : premium. Types disponibles : entreprise, longueDuree, standard, weekend
```

Il ne faut pas appliquer silencieusement le tarif standard, car cela masquerait une erreur de configuration ou une demande invalide.

### 3.6. Exposition REST

Un endpoint permet de tester le calcul :

```http
POST /api/tarifications/calculer
Content-Type: application/json

{
  "type": "weekend",
  "tarifJournalier": 100,
  "jours": 3
}
```

Réponse :

```json
{
  "type": "weekend",
  "tarifJournalier": 100,
  "jours": 3,
  "montant": 270.0
}
```

Les types disponibles sont exposés par :

```http
GET /api/tarifications/types
```

---

## 4. Partie D — Factory, ISP et LSP

### Situation 1 — Factory

#### Quel problème une `FactureFactory` pourrait-elle résoudre ?

Dans une application qui doit produire des factures PDF ou Excel, le code client risque de contenir :

```java
if (type.equals("PDF")) {
    facture = new FacturePdf();
} else if (type.equals("EXCEL")) {
    facture = new FactureExcel();
}
```

Cette logique de création est répétée, couple le client aux classes concrètes et complique l’ajout d’un nouveau format. Une fabrique centralise la création et cache les détails d’instanciation.

#### Quelle méthode pourrait-elle exposer ?

```java
public interface FactureFactory {
    Facture creer(TypeFacture type);
}
```

Ou, avec une clé textuelle :

```java
Facture creer(String type);
```

Une implémentation peut utiliser une `Map<TypeFacture, Supplier<Facture>>` pour rester facilement extensible.

#### Quel avantage apporte la centralisation de la création des objets ?

- Un seul endroit à modifier pour ajouter un format.
- Le client dépend de l’interface `Facture`, pas des classes concrètes.
- Les paramètres de construction et la configuration sont uniformisés.
- Les tests peuvent injecter une fabrique mockée.
- Le code respecte davantage **OCP** et **DIP**.

Exemple de conception :

```java
public interface Facture {
    byte[] generer(Contrat contrat);
}

public class FacturePdf implements Facture { /* ... */ }
public class FactureExcel implements Facture { /* ... */ }

public class FactureFactory {

    private final Map<String, Supplier<Facture>> fabricants = Map.of(
            "PDF", FacturePdf::new,
            "EXCEL", FactureExcel::new
    );

    public Facture creer(String type) {
        Supplier<Facture> fabricant = fabricants.get(type.toUpperCase());
        if (fabricant == null) {
            throw new IllegalArgumentException("Format de facture inconnu : " + type);
        }
        return fabricant.get();
    }
}
```

> Le projet AutoLoc ne contient pas encore de domaine « facture » ; cette partie est une proposition de conception, pas un ajout au CRUD existant.

### Situation 2 — ISP

#### Quel principe SOLID est concerné ?

Le **principe de ségrégation des interfaces (ISP)** : un client ne doit pas dépendre de méthodes dont il n’a pas besoin. L’interface `GestionVehicule` mélange consultation, modification, export et notification, ce qui force un composant de lecture seule à dépendre de toutes ces opérations.

#### Comment séparer cette interface en contrats plus spécialisés ?

```java
public interface VehiculeConsultation {
    Vehicule consulter(Long id);
}

public interface VehiculeGestion {
    Vehicule ajouter(Vehicule vehicule);
    Vehicule modifier(Long id, Vehicule vehicule);
    void supprimer(Long id);
}

public interface VehiculeExport {
    void exporterRapport();
}

public interface VehiculeNotification {
    void envoyerNotification();
}
```

Une implémentation peut cumuler plusieurs interfaces :

```java
public class VehiculeService implements VehiculeConsultation,
        VehiculeGestion, VehiculeExport, VehiculeNotification {
    // ...
}
```

Un contrôleur de consultation n’injecte que `VehiculeConsultation`. Il ne dépend donc pas des méthodes d’export ou de notification.

**Bénéfices** : moins de couplage, interfaces plus faciles à comprendre, tests plus ciblés, possibilité de fournir des implémentations partielles sans méthodes vides.

### Situation 3 — LSP

#### Pourquoi une sous-classe qui lance `UnsupportedOperationException` peut-elle violer le LSP ?

Le **principe de substitution de Liskov (LSP)** impose qu’un objet d’une sous-classe puisse remplacer un objet de la classe de base sans casser le comportement attendu par les clients.

Si le code client appelle une méthode déclarée dans la classe de base, il s’attend à ce qu’elle fonctionne. Une sous-classe qui lance systématiquement `UnsupportedOperationException` :

- ne respecte pas le contrat de la classe de base ;
- oblige les clients à connaître les sous-types concrets ;
- provoque des erreurs à l’exécution alors que le type de base promettait l’opération.

C’est un signe que l’héritage est utilisé pour réutiliser du code alors que le comportement n’est pas réellement substituable.

#### Quelle solution permettrait de respecter le contrat attendu ?

Plusieurs solutions selon le cas :

1. **Ne pas hériter** si l’opération n’a pas de sens : utiliser la composition.
2. **Séparer les contrats** : garder dans la classe de base uniquement les opérations communes, et créer des interfaces spécifiques (`Exportable`, `Notifiable`, etc.).
3. **Redessiner la hiérarchie** : une classe de base ne doit pas imposer une méthode optionnelle à toutes les sous-classes.
4. **Respecter les préconditions, postconditions et invariants** de la classe de base dans chaque sous-classe.

Exemple :

```java
public interface Vehicule {
    String getImmatriculation();
    double getTarifJournalier();
}

public interface VehiculeExportable {
    byte[] exporter();
}

public class VehiculeStandard implements Vehicule, VehiculeExportable {
    // ...
}
```

Ainsi, un client qui manipule `Vehicule` ne s’attend pas à pouvoir exporter, et un client qui a besoin de l’export dépend explicitement de `VehiculeExportable`.

---

## 5. Synthèse des principes SOLID et patrons GoF utilisés

| Principe / patron | Application dans AutoLoc | Bénéfice |
|---|---|---|
| **SRP** | `VehiculeValidator`, `JournalisationService`, `TarificationService` | Une seule raison de changer par classe |
| **OCP** | Nouvelles stratégies tarifaires sans modifier `TarificationService` | Extension sans régression |
| **LSP** | Contrats respectés ; pas de sous-type qui casse le comportement de base | Substitution fiable |
| **ISP** | Proposition de découpage de `GestionVehicule` | Interfaces ciblées, moins de couplage |
| **DIP** | `VehiculeService` dépend de `VehiculeRepository` (interface) | Persistance remplaçable et testable |
| **Strategy (GoF)** | `StrategieTarification` + 4 implémentations | Algorithmes tarifaires interchangeables |
| **Factory (GoF)** | Proposition `FactureFactory` | Centralisation de la création PDF/Excel |
| **Injection de dépendances** | Constructeurs de `VehiculeService`, `TarificationService` | Découplage et testabilité |

### Ce qui a changé dans le code

- `VehiculeService` délègue la validation à `VehiculeValidator`.
- `VehiculeService` délègue la journalisation à `JournalisationService`.
- Les formules tarifaires sont isolées dans des classes `StrategieTarification`.
- `TarificationService` sélectionne la stratégie via une `Map` injectée par Spring.
- `TarificationEntreprise` a été ajoutée sans toucher au service de calcul.
- Les fonctionnalités CRUD existantes sont conservées.

### Vérification

```bash
./mvnw test
./mvnw clean package
```

Tests unitaires ajoutés :

- validation d’un véhicule avec un tarif positif ;
- refus d’un tarif nul ou négatif ;
- refus d’une immatriculation vide ;
- tarif standard ;
- réduction week-end ;
- réduction longue durée ;
- réduction entreprise ;
- type inconnu ;
- nombre de jours invalide ;
- tarif invalide ;
- ajout d’une nouvelle stratégie sans modifier `TarificationService`.
