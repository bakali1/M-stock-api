# CAHIER DES CHARGES
## M-STOCK — Système de Gestion des Stocks Militaires Hospitaliers

**Version:** 1.0  
**Date:** Avril 2026  
**Statut:** Phase de Développement (MVP)  

---

## 1. CONTEXTE ET ENJEUX

### 1.1 Problématique
L'hôpital militaire doit assurer la **disponibilité opérationnelle** des stocks de matériel médical et de fournitures critiques. Les défaillances actuelles :

- **Traçabilité insuffisante** : Impossibilité de localiser rapidement un lot de vaccin, sanguin, ou un kit chirurgical spécifique en cas de rappel.
- **Gestion des dates d'expiration** : Perte de matériel par expiration sans contrôle anticipé ; impossibilité de garantir que le matériel déployé en opération reste valide pendant toute la durée de la mission (6+ mois).
- **Absence d'historique** : Aucun audit trail des mouvements de stock → risque de conformité et d'accountability.
- **Dysfonctionnement des niveaux PAR** : Ruptures de stock imprévisibles dans les services ; pas de renouvellement automatisé.

### 1.2 Objectif
Déployer une **plateforme web de gestion des stocks et de traçabilité** accessible depuis l'intranet hospitalier. Priorité absolue : **Readiness** (disponibilité opérationnelle) et **Compliance** (respect des normes militaires et hospitalières).

---

## 2. PÉRIMÈTRE FONCTIONNEL (MVP — 2 Mois)

### 2.1 Module 1 : Forecaster d'Expiration (Semaines 1–2)

**Fonction critique** : Anticiper les expirations et éviter la perte de matériel.

#### Spécifications :
- **CRON quotidien** : Job automatisé qui analyse chaque lot en stock et calcule les jours avant expiration.
- **Seuils d'alerte** :
  - 🔴 **Critique** : < 7 jours → Action immédiate requise.
  - 🟡 **Attention** : 7–30 jours → Marquer pour utilisation urgente.
  - 🟢 **Normal** : > 30 jours → Stock sain.
- **Interface** :
  - Dashboard principal affiche un **widget "Expiration Alert"** avec compteurs par seuil.
  - Liste détaillée : Produit, Lot, Date exp., Quantité, Localisation (bin), Actions (marquer comme "À utiliser en priorité").
  - Email quotidien au responsable des stocks si alerte critique.
- **Données nécessaires** : Table `batches` (lot_number, expiration_date, quantity, location).

#### Livrables :
- Requête SQL de détection + Java service Spring avec `@Scheduled`.
- Page Angular affichant la liste d'expiration.
- (Optionnel Phase 2) : Intégration email SMTP.

---

### 2.2 Module 2 : Recherche & Traçabilité par Lot (Semaines 2–3)

**Fonction critique** : En cas de rappel FDA/EMA, localiser instantanément tous les lots affectés.

#### Spécifications :
- **Recherche rapide** : Barre de recherche par :
  - NSN (Nomenclature System Number) du produit.
  - Numéro de lot exact.
  - Nom du produit (substring).
- **Résultat** :
  - Tous les lots correspondants.
  - Pour chaque lot : Quantité, Date exp., Localisation (Zone/Bin), Statut (Actif/Quarantaine/Retiré).
  - Historique des mouvements de ce lot (qui l'a reçu, qui l'a utilisé, quand).
- **Actions rapides** :
  - Marquer le lot comme "Quarantaine" (blocage des prélèvements).
  - Générer un rapport PDF de tous les lots de ce produit à des fins de rappel.
- **Données nécessaires** : Tables `batches`, `products`, `transactions` (audit trail).

#### Livrables :
- Endpoint REST `GET /api/batches/search?nsn=...&lot=...`.
- Page Angular avec formulaire + résultats + historique pour chaque lot.

---

### 2.3 Module 3 : Gestion des Niveaux PAR (Semaines 3–5)

**Fonction critique** : Assurer que chaque zone (service, bloc opératoire, urgences) maintient un minimum de stock réglementaire.

#### Spécifications :
- **Paramètres PAR par localisation** :
  - Chaque produit a un **seuil minimum (MIN)** et un **seuil maximum (MAX)** définis par zone de stockage.
  - Exemple : "Kits IV — Urgences : MIN=10, MAX=30."
- **Transactions de prélèvement** :
  - Interface mobile-friendly : Staff scanne le code barres du produit → Confirm quantity → Submit.
  - Le système déduit la quantité du stock de la zone.
  - Si quantité < MIN après la déduction, l'article apparaît dans la **liste de réquisition** (Replenishment Queue).
- **Dashboard de réquisition** :
  - Vue centralisée pour Central Supply : "10 items below PAR level across all wards."
  - Tri par urgence (criticalité du produit).
  - Bouton "Générer bon de transfert" → Document pour Central Supply.
- **Données nécessaires** : Tables `products` (PAR parameters per location), `transactions` (stock movements).

#### Livrables :
- Endpoint REST pour POST transaction de prélèvement.
- Page Angular "Prélever un Item" (barcode scanner + qty input).
- Dashboard "Items Below PAR" avec export PDF/CSV.

---

### 2.4 Module 4 : Audit Trail & Chain of Custody (Semaines 4–6)

**Fonction critique** : Conformité militaire + HIPAA-adjacent. Chaque mouvement de stock doit être tracé.

#### Spécifications :
- **Capture automatique** :
  - Chaque INSERT/UPDATE/DELETE dans `products`, `batches`, `transactions` déclenche un enregistrement dans `audit_logs`.
  - Champs obligatoires :
    - `timestamp` (quand).
    - `user_id` + `user_name` (qui) — extrait du header HTTP `X-Forwarded-User` (CAC).
    - `action` (CREATE/UPDATE/DELETE).
    - `table_name` (quelle table).
    - `old_value` (ancien état, JSON).
    - `new_value` (nouvel état, JSON).
    - `ip_address` (d'où).
    - `reason` (optionnel : pourquoi — ex. "Expired", "Patient Use", "Recall").
- **Interface de consultation** :
  - Vue "Historique complet" pour officiers/superviseurs.
  - Filtrage : par date, utilisateur, type d'action, produit.
  - Rapport mensuel "Chain of Custody" pour audit militaire.
- **Sécurité** :
  - Audit logs en **append-only** (jamais modifiables/supprimables).
  - Accès restreint aux rôles autorisés (RBAC).

#### Livrables :
- Middleware/Aspect Spring pour capture automatique.
- Table `audit_logs` avec index sur timestamp + user_id.
- Page Angular "Audit Log Viewer" avec filtres + export PDF.

---

## 3. ARCHITECTURE TECHNIQUE

### 3.1 Stack Existant (Validé)
- **Backend** : Spring Boot 4.0.5 (Java 21).
- **Frontend** : Angular 21 + Tailwind CSS 4.
- **Database** : PostgreSQL 15 (hébergé en Docker, migration depuis MySQL).
- **Authentification** : CAC (Common Access Card) via proxy header HTTP.
- **JDBC Driver** : `org.postgresql:postgresql` (Spring auto-detects dialect).
- **ORM** : Hibernate + Spring Data JPA (auto-configured pour PostgreSQL).

### 3.2 Modèle de Données (Relatif à `init.sql`)

```
USERS (id, username, role: ADMIN|USER|CLERK|OFFICER)

PRODUCTS (id, name, nsn_code UNIQUE, description, par_level_default)
  ↓ (n-to-1)
BATCHES (id, product_id, lot_number, quantity, expiration_date, location, status: ACTIVE|QUARANTINE|RETIRED)

TRANSACTIONS (id, batch_id, user_id, transaction_type: RECEIPT|WITHDRAW, quantity, timestamp, reason)
  ↓
AUDIT_LOGS (id, timestamp, user_id, action, table_name, old_value, new_value, ip_address, reason)

PAR_LEVELS (id, product_id, location, min_qty, max_qty, last_updated)
```

**Ajouts nécessaires** :
- Colonne `status` dans `BATCHES` (pour Quarantine).
- Table `PAR_LEVELS` (si pas déjà présente).
- Colonne `reason` dans `TRANSACTIONS` et `AUDIT_LOGS`.

### 3.3 Endpoints API REST (Priorité)

| Endpoint | Méthode | Fonction | Semaine |
|----------|---------|----------|---------|
| `/api/products` | GET | Lister tous les produits | S1 |
| `/api/batches/search` | GET | Rechercher par NSN/Lot | S2 |
| `/api/batches/{id}/history` | GET | Historique d'un lot | S2 |
| `/api/transactions` | POST | Enregistrer un prélèvement | S3 |
| `/api/par-alerts` | GET | Items below PAR | S4 |
| `/api/audit-logs` | GET | Consultation audit trail | S5 |
| `/api/reports/expiration` | GET | Rapport expiration (PDF/CSV) | S6 |
| `/api/reports/chain-of-custody` | GET | Rapport audit mensuel | S7 |

### 3.4 Pages Angular (Priorité)

| Page | Route | Composants | Semaine |
|------|-------|-----------|---------|
| Dashboard | `/dashboard` | Expiration Widget, PAR Widget, Quick Stats | S2 |
| Recherche Lot | `/search` | Search Bar, Results Table, Lot Detail Modal | S2 |
| Prélever Item | `/withdraw` | Barcode Scanner, Qty Input, Confirmation | S3 |
| Réquisition | `/requisition` | Items Below PAR, Generate PO | S4 |
| Audit Log | `/audit` | Table filtrable, Export PDF | S5 |
| Rapports | `/reports` | Expiration Report, CoC Report (PDF Export) | S6–S7 |

---

## 4. EXIGENCES DE SÉCURITÉ & CONFORMITÉ

### 4.1 Authentification & Autorisation
- **Source** : CAC via en-tête HTTP `X-Forwarded-User` (administré par le proxy de l'hôpital).
- **Rôles RBAC** :
  - `VIEWER` : Consultable uniquement (infirmières, personnel médical).
  - `CLERK` : Prélèvement, réception de stock (techniciens d'approvisionnement).
  - `OFFICER` : Approbation haute valeur, accès audit logs.
  - `ADMIN` : Gestion des paramètres, gestion utilisateurs (limité).
- **Implémentation** : Spring Security + custom `UserDetailsService` qui lit le header.

### 4.2 Audit & Non-Répudiation
- Tous les changements d'état sont enregistrés dans `audit_logs` en append-only.
- Aucune suppression/modification de logs historiques.
- Export audit trail disponible pour inspection/certification.

### 4.3 Accès Réseau
- **Déploiement** : Intranet hospitalier uniquement (pas de cloud public).
- **HTTPS obligatoire** : Certificat auto-signé ou PKI militaire.
- **Pas de synchronisation** avec EMR (Electronic Medical Record) ou données patients.

### 4.4 Validation Entrée & Sanitization
- Tous les inputs (lot_number, product_name, etc.) échappés pour prévenir injection SQL/XSS.
- Barcode scanner : Simulation de clavier → Aucune validation supplémentaire (trust intranet).

### 4.5 Confidentialité
- **Pas de données patient** dans M-Stock.
- **HIPAA N/A** pour le système lui-même, mais respect des principes (audit, access control, encryption in transit).

---

## 5. CALENDRIER DE RÉALISATION

### Phase MVP — 8 Semaines (Avec Migration PostgreSQL)

| Semaine | Jalons | Tâches Backend | Tâches Frontend | Livrables |
|---------|--------|----------------|-----------------|-----------|
| **S1** | **Migration DB** + Data Modeling | Update pom.xml, docker-compose, application.properties. Entities + Repos (Lombok). | Project Setup | PostgreSQL running + Schema validated |
| **S2** | Expiration & Search | Expiration service + Search endpoint + JSONB audit prep | Dashboard + Search page | Expiration forecaster opérationnel |
| **S3** | Transactions | POST transaction endpoint + manual user seeding | Withdraw form + Barcode scanner feature (late) | Transaction recording OK |
| **S4** | PAR Level Alerts | PAR check service per-location | PAR Dashboard + Requisition list | PAR-based workflow validé |
| **S5** | Audit Trail | Audit middleware + Logs table (JSONB for old/new values) | Audit Log viewer | Audit trail complète + 1-year rolling policy |
| **S6** | Reporting | PDF/CSV generators | Report pages (Expiration, CoC) | Rapports exportables |
| **S7** | Integration & QA | API test suite | E2E test, UX polish | MVP testé |
| **S8** | User Testing & Doc | Performance tuning (indexes, connection pool) | Final UX feedback | Documentation + Handoff |

### Migration PostgreSQL — Semaine 1 (Temps alloué)

**Tâches spécifiques :**
1. Remplacer `docker-compose.yml` : MySQL → PostgreSQL 15.
2. Mettre à jour `pom.xml` : `mysql-connector-j` → `postgresql` driver.
3. Configurer `application.properties` : URL, user, password PostgreSQL.
4. Mettre à jour `init.sql` : Dialecte PostgreSQL (compatible, syntaxe validée).
5. Tester localement : Build + Docker startup + Schema import.

**Bénéfices en retour (S4–S5)** :
- JSONB native pour audit_logs : Stockage + requêtes plus efficaces.
- Meilleure gestion transactionnelle (MVCC, isolation levels).
- Full-text search ready pour Phase 2.

**Risque minimal** : Syntaxe SQL `init.sql` déjà compatible PostgreSQL.

### Points de Contrôle Obligatoires
- **Fin S1** : Modèle de données validé par le responsable supply.
- **Fin S3** : Prototype fonctionnel (search + withdraw) opérationnel en intranet test.
- **Fin S6** : MVP complet + audit trail validé pour conformité.
- **Fin S8** : Déploiement en production + formation utilisateurs.

---

## 6. MÉTRIQUES DE SUCCÈS

| Métrique | Cible | Mesure |
|----------|-------|--------|
| **Temps de recherche d'un lot** | < 3 secondes | Requête + affichage résultats |
| **Couverture audit** | 100% des transactions | Aucune transaction sans log |
| **Disponibilité système** | 99.5% intranet | SLA militaire standard |
| **Satisfaction utilisateur** | ≥ 8/10 | Sondage post-déploiement |
| **Taux d'erreur prélèvement** | < 1% | Transactions rejetées / total |

---

## 7. RISQUES & MITIGATION

| Risque | Probabilité | Impact | Mitigation |
|--------|------------|--------|-----------|
| Données corrompues lors migration | Moyenne | Critique | Backup complet avant import ; validation données |
| CAC authentication failure | Basse | Haute | Test en pré-prod ; fall-back LDAP planifié |
| Perf DB sous charge (1000 requêtes/jour) | Moyenne | Moyenne | Index sur lot_number, expiration_date, timestamp |
| Refus utilisateurs (UX friction) | Moyenne | Haute | User testing S7 ; itérations rapides |
| Conformité audit militaire non validée | Basse | Critique | Audit trail complet ; documentation fournie |

---

## 8. RESSOURCES & ENGAGEMENT

### Équipe Requise
- **1 développeur backend** (Spring Boot + DB).
- **1 développeur frontend** (Angular + UX).
- **1 PO/Représentant métier** (Supply Sergeant ou Officer).
- **1 DBA/Infra** (Docker, MySQL, HTTPS, CAC proxy — support).

### Infrastructure
- **VM intranet** : 2 CPU, 4 GB RAM, 50 GB disque.
- **MySQL container** : Backup quotidien.
- **Proxy CAC** : Existant (hospital-provided).

### Budget Indicatif (si externalisé)
- Développement MVP : 300–500 heures dev.
- QA/Testing : 100 heures.
- Documentation : 50 heures.

---

## 9. CONTRAINTES & HYPOTHÈSES

### Contraintes
1. **Pas de cloud public** — Intranet seulement.
2. **CAC obligatoire** — Pas de sign-up public.
3. **MySQL seulement** — Stack déjà déployé.
4. **Timeline strict** — 8 semaines MVP.

### Hypothèses
1. **Proxy CAC fonctionne** — Headers HTTP injectés correctement.
2. **Utilisateurs acceptent une UI web** — Pas de client lourd requis.
3. **Barcode scanner USB standard** — Simulation clavier.
4. **Pas de charge pic > 1000 tx/jour** — Scaling pas nécessaire à court terme.

---

## 10. PROCHAINES ÉTAPES

1. **Validation du cahier des charges** par :
   - CIO hôpital (infra, sécurité).
   - Responsable des stocks (besoins métier).
   - Officier de conformité militaire.

2. **Atelier de démarrage** (J0) :
   - Présentations rôles/responsabilités.
   - Demo base de données existante.
   - Accès proxy CAC configuré.

3. **Semaine 1 — Sprint 0** :
   - ✅ Setup environment (Docker, Spring, Angular).
   - ✅ Validation modèle de données.
   - ✅ Première US (Entity creation) en done.

---

**Document approuvé par :** _____________________________  
**Date :** _____________________________  
**Version suivante :** Post-MVP (Phase 2) — Intégration EMR, Cold Chain Management, Barcode generation.
