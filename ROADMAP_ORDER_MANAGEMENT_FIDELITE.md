# 🗺️ ROADMAP - Order Management avec Système de Fidélité

## 📅 Date: 27 Novembre 2025 | Deadline: 28 Novembre 2025

---

## 🎯 OBJECTIF PRINCIPAL

Implémenter le système de gestion des commandes avec calcul automatique du niveau de fidélité client, incluant :
- ✅ Création de commandes multi-produits
- ✅ Application des remises selon le niveau de fidélité
- ✅ Application des codes promo
- ✅ Calcul automatique de la TVA
- ✅ Validation du stock
- ✅ Confirmation des commandes (avec mise à jour stock + statistiques client)
- ✅ **Recalcul automatique du niveau de fidélité après confirmation**
- ✅ Annulation et rejet des commandes

---

## 📊 ARCHITECTURE COMPLÈTE

```
┌─────────────────────────────────────────────────────────────────┐
│                         FLUX COMPLET                             │
└─────────────────────────────────────────────────────────────────┘

1. CLIENT                2. CRÉATION           3. PAIEMENTS
   └─ Niveau: BASIC         └─ Statut: PENDING    └─ Espèces: 6000 DH
   └─ 0 commandes          └─ Total: 10,000 DH   └─ Chèque: 3000 DH (EN_ATTENTE)
   └─ 0 DH dépensé         └─ Remise: 0%         └─ Virement: 1000 DH
                           └─ Montant restant    └─ Total payé: 10,000 DH
                              = 10,000 DH

4. CONFIRMATION (ADMIN)                    5. MISE À JOUR AUTOMATIQUE
   └─ Vérifier: montant_restant = 0           └─ Décrémenter stock produits
   └─ Changer statut: CONFIRMED               └─ Client: totalOrders = 1
   └─ Déclencher mises à jour →               └─ Client: totalSpent = 10,000 DH
                                              └─ Client: firstOrderDate = NOW
                                              └─ Client: lastOrderDate = NOW
                                              └─ RECALCUL TIER → reste BASIC

6. APRÈS 3 COMMANDES CONFIRMÉES            7. NOUVELLE COMMANDE
   └─ Client: totalOrders = 3                 └─ Niveau actuel: SILVER
   └─ Client: totalSpent = 12,000 DH          └─ Sous-total: 600 DH
   └─ RECALCUL TIER → SILVER!                 └─ Remise SILVER: -30 DH (5%)
                                              └─ Total avec TVA: 684 DH
```

---

## 📋 ÉTAPES D'IMPLÉMENTATION

### ═══════════════════════════════════════════════════════════════
### 🔵 PHASE 1 : PRÉPARATION DES DTOs (30 minutes)
### ═══════════════════════════════════════════════════════════════

#### **Étape 1.1 : Créer OrderItemRequestDTO** (5 min)
**Fichier**: `src/main/java/com/smartshop/api/dto/request/OrderItemRequestDTO.java`

**Ce que contient ce DTO:**
- `productId` : ID du produit à commander
- `quantite` : Quantité demandée

**Validations:**
- `@NotNull` sur productId
- `@NotNull` et `@Min(1)` sur quantite

**Pourquoi ce DTO?**
Car une commande contient plusieurs produits avec différentes quantités.

---

#### **Étape 1.2 : Créer OrderRequestDTO** (5 min)
**Fichier**: `src/main/java/com/smartshop/api/dto/request/OrderRequestDTO.java`

**Ce que contient ce DTO:**
- `clientId` : ID du client qui passe la commande
- `items` : Liste de OrderItemRequestDTO (les produits + quantités)
- `codePromo` : Code promo optionnel (format: PROMO-XXXX)

**Validations:**
- `@NotNull` sur clientId
- `@NotEmpty` sur items (au moins 1 produit)
- `@Valid` sur items (valider chaque item)
- `@Pattern(regexp = "^PROMO-[A-Z0-9]{4}$")` sur codePromo

**Exemple JSON:**
```json
{
  "clientId": 1,
  "items": [
    { "productId": 1, "quantite": 2 },
    { "productId": 3, "quantite": 1 }
  ],
  "codePromo": "PROMO-2024"
}
```

---

#### **Étape 1.3 : Créer OrderItemResponseDTO** (5 min)
**Fichier**: `src/main/java/com/smartshop/api/dto/response/OrderItemResponseDTO.java`

**Ce que contient ce DTO:**
- `id` : ID de l'OrderItem
- `productId` : ID du produit
- `productNom` : Nom du produit (pour affichage)
- `quantite` : Quantité commandée
- `prixUnitaire` : Prix unitaire au moment de la commande
- `totalLigne` : Total de cette ligne (prix × quantité)

**Pourquoi productNom?**
Pour éviter de devoir faire une requête supplémentaire pour afficher le nom.

---

#### **Étape 1.4 : Créer OrderResponseDTO** (10 min)
**Fichier**: `src/main/java/com/smartshop/api/dto/response/OrderResponseDTO.java`

**Ce que contient ce DTO (TOUS les détails):**
- `id` : ID de la commande
- `clientId` : ID du client
- `clientNom` : Nom du client
- `items` : Liste de OrderItemResponseDTO
- `sousTotal` : Sous-total HT (somme des lignes)
- `montantRemise` : Total des remises appliquées
- `montantHT` : Montant HT après remise
- `tauxTVA` : Taux de TVA appliqué (20%)
- `montantTVA` : Montant de la TVA
- `totalTTC` : Total TTC final
- `montantRestant` : Montant restant à payer
- `codePromo` : Code promo utilisé (si applicable)
- `promoApplied` : Boolean indiquant si promo appliquée
- `status` : Statut de la commande (OrderStatus)
- `createdAt` : Date de création
- `updatedAt` : Date de dernière modification

**Exemple de réponse:**
```json
{
  "id": 1,
  "clientId": 5,
  "clientNom": "Mohamed Client 1",
  "items": [
    {
      "id": 1,
      "productId": 10,
      "productNom": "Laptop Dell",
      "quantite": 2,
      "prixUnitaire": 5000.00,
      "totalLigne": 10000.00
    }
  ],
  "sousTotal": 10000.00,
  "montantRemise": 0.00,
  "montantHT": 10000.00,
  "tauxTVA": 20.0,
  "montantTVA": 2000.00,
  "totalTTC": 12000.00,
  "montantRestant": 12000.00,
  "codePromo": null,
  "promoApplied": false,
  "status": "PENDING",
  "createdAt": "2025-11-27T10:30:00",
  "updatedAt": "2025-11-27T10:30:00"
}
```

---

#### **Étape 1.5 : Créer OrderHistoryDTO** (5 min)
**Fichier**: `src/main/java/com/smartshop/api/dto/response/OrderHistoryDTO.java`

**Ce que contient ce DTO (version simplifiée):**
- `id` : ID de la commande
- `createdAt` : Date de création
- `totalTTC` : Montant total TTC
- `status` : Statut de la commande

**Pourquoi un DTO séparé?**
Pour l'historique client, on n'a pas besoin de tous les détails. Version allégée.

**Exemple de réponse (liste):**
```json
[
  {
    "id": 1,
    "createdAt": "2025-11-27T10:30:00",
    "totalTTC": 12000.00,
    "status": "CONFIRMED"
  },
  {
    "id": 2,
    "createdAt": "2025-11-26T15:20:00",
    "totalTTC": 5500.00,
    "status": "PENDING"
  }
]
```

---

### ═══════════════════════════════════════════════════════════════
### 🟢 PHASE 2 : CRÉER L'INTERFACE OrderService (10 minutes)
### ═══════════════════════════════════════════════════════════════

#### **Étape 2.1 : Définir l'interface OrderService** (10 min)
**Fichier**: `src/main/java/com/smartshop/api/service/OrderService.java`

**Méthodes à définir:**
```java
public interface OrderService {
    // Créer une nouvelle commande
    OrderResponseDTO createOrder(OrderRequestDTO orderRequest);
    
    // Consulter une commande par ID
    OrderResponseDTO getOrderById(Long id);
    
    // Liste toutes les commandes (pour ADMIN)
    Page<OrderResponseDTO> getAllOrders(Pageable pageable);
    
    // Historique des commandes d'un client
    List<OrderHistoryDTO> getClientOrderHistory(Long clientId);
    
    // Confirmer une commande (ADMIN uniquement)
    OrderResponseDTO confirmOrder(Long id);
    
    // Annuler une commande (ADMIN uniquement)
    OrderResponseDTO cancelOrder(Long id);
    
    // Rejeter une commande (automatique si stock insuffisant)
    OrderResponseDTO rejectOrder(Long id);
}
```

---

### ═══════════════════════════════════════════════════════════════
### 🔴 PHASE 3 : IMPLÉMENTER OrderServiceImpl (3-4 heures)
### ═══════════════════════════════════════════════════════════════

C'est LA PARTIE LA PLUS IMPORTANTE! Prenez votre temps ici.

---

#### **Étape 3.1 : Structure de base de OrderServiceImpl** (10 min)

**Fichier**: `src/main/java/com/smartshop/api/service/Impl/OrderServiceImpl.java`

**Injecter les dépendances nécessaires:**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    
    // Repositories nécessaires
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    
    // Configuration du taux de TVA
    @Value("${smartshop.tax.tva-rate:20.0}")
    private Double tvaRate;
    
    // Méthodes à implémenter...
}
```

**Ne pas oublier:**
- Ajouter `smartshop.tax.tva-rate=20.0` dans `application.properties`

---

#### **Étape 3.2 : Implémenter createOrder() - PARTIE 1/5** (30 min)
**Validation du client et des produits**

**LOGIQUE:**
```
1. Valider que le client existe
2. Pour chaque produit dans la commande:
   a. Valider que le produit existe
   b. VÉRIFIER que le stock est suffisant
   c. Si stock insuffisant → lancer InsufficientStockException
3. Si TOUS les produits OK → continuer
4. Si UN SEUL produit manque de stock → STOP et exception
```

**CODE:**
```java
@Override
public OrderResponseDTO createOrder(OrderRequestDTO request) {
    // 1. Valider que le client existe
    Client client = clientRepository.findById(request.getClientId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Client non trouvé avec l'ID: " + request.getClientId()));
    
    // 2. Valider les produits et vérifier le stock
    List<OrderItem> orderItems = new ArrayList<>();
    double sousTotal = 0.0;
    
    for (OrderItemRequestDTO itemDTO : request.getItems()) {
        // a. Trouver le produit
        Product product = productRepository.findById(itemDTO.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Produit non trouvé avec l'ID: " + itemDTO.getProductId()));
        
        // b. VÉRIFIER le stock (CRITIQUE!)
        if (product.getStock() < itemDTO.getQuantite()) {
            throw new InsufficientStockException(
                "Stock insuffisant pour le produit: " + product.getNom() 
                + ". Disponible: " + product.getStock() 
                + ", Demandé: " + itemDTO.getQuantite());
        }
        
        // c. Calculer le total de cette ligne
        double prixUnitaire = product.getPrixUnitaire();
        double totalLigne = prixUnitaire * itemDTO.getQuantite();
        sousTotal += totalLigne;
        
        // d. Créer l'OrderItem (sera sauvegardé avec Order)
        OrderItem orderItem = OrderItem.builder()
            .product(product)
            .quantite(itemDTO.getQuantite())
            .prixUnitaire(prixUnitaire)
            .totalLigne(round(totalLigne))
            .build();
        
        orderItems.add(orderItem);
    }
    
    // Arrondir le sous-total
    sousTotal = round(sousTotal);
    
    // Suite de la méthode...
}
```

**IMPORTANT:**
- Cette validation AVANT tout calcul évite de créer une commande qui sera rejetée
- Si exception levée ici → la transaction est annulée (rollback)

---

#### **Étape 3.3 : Implémenter createOrder() - PARTIE 2/5** (30 min)
**Calcul de la remise fidélité**

**LOGIQUE DU SYSTÈME DE FIDÉLITÉ:**
```
Le client a un niveau actuel (BASIC, SILVER, GOLD, PLATINUM)
Ce niveau a été calculé selon ses commandes PASSÉES

Maintenant on crée une NOUVELLE commande:
1. On regarde son niveau ACTUEL
2. On vérifie si la commande atteint le minimum requis
3. Si OUI → on applique la remise

RÈGLES:
- BASIC: pas de remise (0%)
- SILVER: 5% si sous-total ≥ 500 DH
- GOLD: 10% si sous-total ≥ 800 DH
- PLATINUM: 15% si sous-total ≥ 1200 DH
```

**CODE:**
```java
// Suite de createOrder()...

// 3. Calculer la remise fidélité
double montantRemise = 0.0;
CustomerTier tier = client.getTier(); // Niveau actuel du client

// Vérifier si le client est éligible à la remise
if (tier.isEligibleForDiscount(sousTotal)) {
    montantRemise = sousTotal * tier.getDiscountRate();
}

// Exemple de calcul:
// Client SILVER, sous-total = 600 DH
// → isEligibleForDiscount(600) → 600 ≥ 500 → true
// → discountRate = 0.05 (5%)
// → montantRemise = 600 * 0.05 = 30 DH
```

**MÉTHODES DANS L'ENUM CustomerTier:**
```java
// Ces méthodes existent déjà dans votre enum!
public boolean isEligibleForDiscount(double orderSubtotal) {
    return orderSubtotal >= minimumOrderAmount;
}

public double getDiscountRate() {
    return discountRate;
}
```

**EXEMPLES CONCRETS:**

| Client | Niveau | Sous-total | Éligible? | Remise | Montant remise |
|--------|--------|-----------|-----------|--------|----------------|
| Amine | BASIC | 1000 DH | NON | 0% | 0 DH |
| Sara | SILVER | 400 DH | NON (< 500) | 0% | 0 DH |
| Sara | SILVER | 600 DH | OUI (≥ 500) | 5% | 30 DH |
| Karim | GOLD | 900 DH | OUI (≥ 800) | 10% | 90 DH |
| Fatima | PLATINUM | 1500 DH | OUI (≥ 1200) | 15% | 225 DH |

---

#### **Étape 3.4 : Implémenter createOrder() - PARTIE 3/5** (20 min)
**Application du code promo**

**LOGIQUE:**
```
Si un code promo est fourni:
1. Valider le format (PROMO-XXXX)
2. Si valide → ajouter 5% de remise SUPPLÉMENTAIRE
3. Marquer promoApplied = true
```

**CODE:**
```java
// Suite de createOrder()...

// 4. Appliquer le code promo (si fourni)
boolean promoApplied = false;
if (request.getCodePromo() != null && isValidPromoCode(request.getCodePromo())) {
    // Ajouter 5% supplémentaire
    montantRemise += sousTotal * 0.05;
    promoApplied = true;
}

// Méthode helper pour valider le format
private boolean isValidPromoCode(String code) {
    if (code == null || code.trim().isEmpty()) {
        return false;
    }
    return code.matches("^PROMO-[A-Z0-9]{4}$");
}
```

**EXEMPLES:**

| Scenario | Sous-total | Remise fidélité | Code promo | Remise promo | Remise totale |
|----------|-----------|----------------|-----------|--------------|---------------|
| SILVER sans promo | 600 DH | 30 DH (5%) | - | 0 DH | 30 DH |
| SILVER avec promo | 600 DH | 30 DH (5%) | PROMO-2024 | 30 DH (5%) | 60 DH |
| GOLD avec promo | 1000 DH | 100 DH (10%) | PROMO-SAVE | 50 DH (5%) | 150 DH |

**IMPORTANT:**
- Les remises sont CUMULATIVES!
- Le code promo s'applique sur le sous-total initial (pas après remise fidélité)

---

#### **Étape 3.5 : Implémenter createOrder() - PARTIE 4/5** (20 min)
**Calculs financiers (HT, TVA, TTC)**

**FORMULES OFFICIELLES AU MAROC:**
```
1. Montant HT = Sous-total - Remise totale
2. Montant TVA = Montant HT × (Taux TVA / 100)
3. Total TTC = Montant HT + Montant TVA
```

**⚠️ ATTENTION:**
La TVA se calcule sur le montant APRÈS remise (pas sur le sous-total initial)!

**CODE:**
```java
// Suite de createOrder()...

// 5. Calculer les totaux
montantRemise = round(montantRemise);
double montantHT = round(sousTotal - montantRemise);
double montantTVA = round(montantHT * (tvaRate / 100.0));
double totalTTC = round(montantHT + montantTVA);

// Méthode helper pour arrondir à 2 décimales
private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
}
```

**EXEMPLE COMPLET DE CALCUL:**
```
Client: SILVER (niveau actuel)
Produits:
  - Laptop: 5000 DH × 1 = 5000 DH
  - Souris: 50 DH × 2 = 100 DH
  - Clavier: 150 DH × 1 = 150 DH

1. Sous-total HT = 5250 DH

2. Remise fidélité:
   - Client SILVER → 5% si ≥ 500 DH
   - 5250 ≥ 500 → OUI
   - Remise = 5250 × 0.05 = 262.50 DH

3. Code promo: PROMO-2024
   - Remise supplémentaire = 5250 × 0.05 = 262.50 DH
   - Remise totale = 262.50 + 262.50 = 525 DH

4. Montant HT après remise = 5250 - 525 = 4725 DH

5. TVA (20%) = 4725 × 0.20 = 945 DH

6. Total TTC = 4725 + 945 = 5670 DH

7. Montant restant = 5670 DH (aucun paiement encore)
```

---

#### **Étape 3.6 : Implémenter createOrder() - PARTIE 5/5** (20 min)
**Création et sauvegarde de la commande**

**CODE:**
```java
// Suite de createOrder()...

// 6. Créer l'entité Order
Order order = Order.builder()
    .client(client)
    .sousTotal(sousTotal)
    .montantRemise(montantRemise)
    .montantHT(montantHT)
    .tauxTVA(tvaRate)
    .montantTVA(montantTVA)
    .totalTTC(totalTTC)
    .montantRestant(totalTTC) // Au début, tout reste à payer
    .codePromo(request.getCodePromo())
    .promoApplied(promoApplied)
    .status(OrderStatus.PENDING) // Statut initial
    .build();

// 7. Lier les OrderItems à la commande
for (OrderItem item : orderItems) {
    item.setOrder(order);
}
order.setOrderItems(orderItems);

// 8. Sauvegarder (cascade sauvegarde les OrderItems)
Order savedOrder = orderRepository.save(order);

// 9. Convertir en DTO et retourner
return convertToResponseDTO(savedOrder);
```

**IMPORTANT:**
- Le statut initial est TOUJOURS `PENDING`
- Le `montantRestant` est initialement égal au `totalTTC`
- Les `OrderItems` sont sauvegardés automatiquement (cascade)

---

#### **Étape 3.7 : Implémenter confirmOrder() - CŒUR DU SYSTÈME** (45 min)

**C'EST LA MÉTHODE LA PLUS IMPORTANTE!**

**LOGIQUE COMPLÈTE:**
```
1. Trouver la commande
2. VALIDER: statut = PENDING (seules les PENDING peuvent être confirmées)
3. VALIDER: montantRestant = 0 (doit être totalement payée)
4. Changer le statut → CONFIRMED
5. DÉCRÉMENTER le stock de chaque produit
6. METTRE À JOUR les statistiques du client:
   a. Incrémenter totalOrders
   b. Ajouter totalTTC au totalSpent
   c. Mettre à jour firstOrderDate (si première commande)
   d. Mettre à jour lastOrderDate
7. ⭐ RECALCULER LE NIVEAU DE FIDÉLITÉ (CRITIQUE!)
8. Sauvegarder tout
9. Retourner la commande mise à jour
```

**CODE COMPLET:**
```java
@Override
public OrderResponseDTO confirmOrder(Long id) {
    // 1. Trouver la commande
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Commande non trouvée avec l'ID: " + id));
    
    // 2. VALIDER: peut être confirmée?
    if (!order.getStatus().canBeConfirmed()) {
        throw new BusinessRuleViolationException(
            "Seules les commandes PENDING peuvent être confirmées. " +
            "Statut actuel: " + order.getStatus());
    }
    
    // 3. VALIDER: totalement payée?
    if (order.getMontantRestant() > 0.01) { // Tolérance pour virgule flottante
        throw new BusinessRuleViolationException(
            "La commande doit être entièrement payée avant confirmation. " +
            "Montant restant: " + order.getMontantRestant() + " DH");
    }
    
    // 4. Changer le statut
    order.setStatus(OrderStatus.CONFIRMED);
    
    // 5. DÉCRÉMENTER le stock (CRITIQUE!)
    for (OrderItem item : order.getOrderItems()) {
        Product product = item.getProduct();
        int newStock = product.getStock() - item.getQuantite();
        product.setStock(newStock);
        productRepository.save(product);
    }
    
    // 6. METTRE À JOUR les statistiques client
    Client client = order.getClient();
    
    // a. Incrémenter totalOrders
    client.setTotalOrders(client.getTotalOrders() + 1);
    
    // b. Ajouter au totalSpent
    client.setTotalSpent(round(client.getTotalSpent() + order.getTotalTTC()));
    
    // c. Mettre à jour firstOrderDate
    if (client.getFirstOrderDate() == null) {
        client.setFirstOrderDate(LocalDateTime.now());
    }
    
    // d. Mettre à jour lastOrderDate
    client.setLastOrderDate(LocalDateTime.now());
    
    // 7. ⭐ RECALCULER LE NIVEAU (LA PARTIE LA PLUS IMPORTANTE!)
    updateClientTier(client);
    
    // 8. Sauvegarder
    clientRepository.save(client);
    Order confirmedOrder = orderRepository.save(order);
    
    // 9. Retourner
    return convertToResponseDTO(confirmedOrder);
}
```

---

#### **Étape 3.8 : Implémenter updateClientTier() - LE CŒUR DU SYSTÈME DE FIDÉLITÉ** (30 min)

**⭐ C'EST LA LOGIQUE LA PLUS CRITIQUE DE TOUT LE PROJET!**

**RÈGLES PRÉCISES DU CONTEXTE:**
```
Niveau calculé selon:
- Nombre total de commandes CONFIRMÉES
- Montant total dépensé (somme des commandes CONFIRMÉES)

Conditions (OR = OU):
- PLATINUM: 20 commandes OU 15,000 DH cumulés
- GOLD:     10 commandes OU 5,000 DH cumulés
- SILVER:   3 commandes  OU 1,000 DH cumulés
- BASIC:    par défaut
```

**ALGORITHME:**
```
Vérifier dans l'ordre (du plus élevé au plus bas):
1. Si totalOrders >= 20 OU totalSpent >= 15000 → PLATINUM
2. Sinon si totalOrders >= 10 OU totalSpent >= 5000 → GOLD
3. Sinon si totalOrders >= 3 OU totalSpent >= 1000 → SILVER
4. Sinon → BASIC
```

**CODE:**
```java
/**
 * ⭐ MÉTHODE CRITIQUE - Recalcule le niveau de fidélité du client
 * Appelée après chaque confirmation de commande
 */
private void updateClientTier(Client client) {
    int totalOrders = client.getTotalOrders();
    double totalSpent = client.getTotalSpent();
    
    CustomerTier newTier;
    
    // Vérifier PLATINUM (le plus élevé en premier)
    if (totalOrders >= 20 || totalSpent >= 15000) {
        newTier = CustomerTier.PLATINUM;
    }
    // Sinon vérifier GOLD
    else if (totalOrders >= 10 || totalSpent >= 5000) {
        newTier = CustomerTier.GOLD;
    }
    // Sinon vérifier SILVER
    else if (totalOrders >= 3 || totalSpent >= 1000) {
        newTier = CustomerTier.SILVER;
    }
    // Sinon rester BASIC
    else {
        newTier = CustomerTier.BASIC;
    }
    
    // Mettre à jour le niveau
    client.setTier(newTier);
}
```

**EXEMPLES CONCRETS:**

| Commandes | Total dépensé | Calcul | Nouveau niveau |
|-----------|---------------|--------|----------------|
| 1 | 500 DH | 1 < 3 ET 500 < 1000 | BASIC |
| 2 | 700 DH | 2 < 3 ET 700 < 1000 | BASIC |
| 3 | 900 DH | 3 = 3 ✅ | **SILVER** |
| 2 | 1200 DH | 2 < 3 MAIS 1200 ≥ 1000 ✅ | **SILVER** |
| 5 | 2000 DH | 5 < 10 ET 2000 < 5000 | SILVER |
| 10 | 4500 DH | 10 = 10 ✅ | **GOLD** |
| 8 | 6000 DH | 8 < 10 MAIS 6000 ≥ 5000 ✅ | **GOLD** |
| 20 | 12000 DH | 20 = 20 ✅ | **PLATINUM** |
| 15 | 18000 DH | 15 < 20 MAIS 18000 ≥ 15000 ✅ | **PLATINUM** |

**SCÉNARIO COMPLET - CLIENT AMINE:**
```
Inscription → BASIC (0 commandes, 0 DH)

Commande 1: 250 DH confirmée
→ totalOrders = 1, totalSpent = 250
→ 1 < 3 ET 250 < 1000
→ Reste BASIC

Commande 2: 350 DH confirmée
→ totalOrders = 2, totalSpent = 600
→ 2 < 3 ET 600 < 1000
→ Reste BASIC

Commande 3: 450 DH confirmée ⭐
→ totalOrders = 3, totalSpent = 1050
→ 3 = 3 ✅
→ DEVIENT SILVER!

Commande 4 (nouvelle): 600 DH
→ Niveau actuel: SILVER
→ Remise: 5% = -30 DH
→ Total: 684 DH TTC
→ Après confirmation: totalOrders = 4, totalSpent = 1734
→ Reste SILVER

... (continues jusqu'à 10 commandes ou 5000 DH) ...

Commande 10: 500 DH confirmée ⭐
→ totalOrders = 10, totalSpent = 5200
→ 10 = 10 ✅
→ DEVIENT GOLD!
```

---

#### **Étape 3.9 : Implémenter cancelOrder()** (15 min)

**LOGIQUE:**
```
1. Trouver la commande
2. VALIDER: peut être annulée? (seules les PENDING)
3. Changer statut → CANCELED
4. NE PAS toucher au stock (pas décrementé à la création)
5. NE PAS toucher aux statistiques client
```

**CODE:**
```java
@Override
public OrderResponseDTO cancelOrder(Long id) {
    // 1. Trouver la commande
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Commande non trouvée avec l'ID: " + id));
    
    // 2. VALIDER: peut être annulée?
    if (!order.getStatus().canBeCanceled()) {
        throw new BusinessRuleViolationException(
            "Seules les commandes PENDING peuvent être annulées. " +
            "Statut actuel: " + order.getStatus());
    }
    
    // 3. Changer le statut
    order.setStatus(OrderStatus.CANCELED);
    
    // 4. Sauvegarder
    Order canceledOrder = orderRepository.save(order);
    
    // 5. Retourner
    return convertToResponseDTO(canceledOrder);
}
```

**IMPORTANT:**
- Seules les commandes `PENDING` peuvent être annulées
- On ne touche PAS au stock (car jamais décrémenté)
- On ne touche PAS aux stats client (jamais ajoutées)

---

#### **Étape 3.10 : Implémenter les méthodes de consultation** (30 min)

**a. getOrderById()**
```java
@Override
@Transactional(readOnly = true)
public OrderResponseDTO getOrderById(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Commande non trouvée avec l'ID: " + id));
    
    return convertToResponseDTO(order);
}
```

**b. getAllOrders() - avec pagination**
```java
@Override
@Transactional(readOnly = true)
public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
    Page<Order> orders = orderRepository.findAll(pageable);
    return orders.map(this::convertToResponseDTO);
}
```

**c. getClientOrderHistory()**
```java
@Override
@Transactional(readOnly = true)
public List<OrderHistoryDTO> getClientOrderHistory(Long clientId) {
    // Valider que le client existe
    clientRepository.findById(clientId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Client non trouvé avec l'ID: " + clientId));
    
    // Récupérer les commandes
    List<Order> orders = orderRepository.findByClientId(clientId);
    
    // Convertir en DTO simplifié
    return orders.stream()
        .map(order -> OrderHistoryDTO.builder()
            .id(order.getId())
            .createdAt(order.getCreatedAt())
            .totalTTC(order.getTotalTTC())
            .status(order.getStatus())
            .build())
        .collect(Collectors.toList());
}
```

---

#### **Étape 3.11 : Implémenter convertToResponseDTO()** (15 min)

**CODE:**
```java
/**
 * Convertit une entité Order en OrderResponseDTO
 */
private OrderResponseDTO convertToResponseDTO(Order order) {
    // Convertir les OrderItems
    List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
        .map(item -> OrderItemResponseDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productNom(item.getProduct().getNom())
            .quantite(item.getQuantite())
            .prixUnitaire(item.getPrixUnitaire())
            .totalLigne(item.getTotalLigne())
            .build())
        .collect(Collectors.toList());
    
    // Créer le OrderResponseDTO
    return OrderResponseDTO.builder()
        .id(order.getId())
        .clientId(order.getClient().getId())
        .clientNom(order.getClient().getNom())
        .items(itemDTOs)
        .sousTotal(order.getSousTotal())
        .montantRemise(order.getMontantRemise())
        .montantHT(order.getMontantHT())
        .tauxTVA(order.getTauxTVA())
        .montantTVA(order.getMontantTVA())
        .totalTTC(order.getTotalTTC())
        .montantRestant(order.getMontantRestant())
        .codePromo(order.getCodePromo())
        .promoApplied(order.getPromoApplied())
        .status(order.getStatus())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .build();
}
```

---

### ═══════════════════════════════════════════════════════════════
### 🟣 PHASE 4 : CRÉER LE CONTROLLER (1 heure)
### ═══════════════════════════════════════════════════════════════

#### **Étape 4.1 : Créer OrderController avec authorization** (1 heure)

**Fichier**: `src/main/java/com/smartshop/api/controller/OrderController.java`

**ENDPOINTS À CRÉER:**
```
POST   /api/orders                    → createOrder()
GET    /api/orders/{id}               → getOrderById()
GET    /api/orders                     → getAllOrders()
GET    /api/orders/client/{clientId}  → getClientOrderHistory()
PUT    /api/orders/{id}/confirm       → confirmOrder()
PUT    /api/orders/{id}/cancel        → cancelOrder()
```

**STRUCTURE AVEC AUTHORIZATION:**
```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final AuthService authService;
    private final ClientRepository clientRepository;
    
    // Endpoints...
}
```

**RÈGLES D'AUTHORIZATION:**
- CREATE ordre: **ADMIN uniquement**
- GET order by ID: **ADMIN** ou **CLIENT** (si c'est sa propre commande)
- GET all orders: **ADMIN uniquement**
- GET client history: **ADMIN** ou **CLIENT** (si c'est son propre historique)
- CONFIRM/CANCEL: **ADMIN uniquement**

**EXEMPLE - createOrder():**
```java
@PostMapping
public ResponseEntity<OrderResponseDTO> createOrder(
        @Valid @RequestBody OrderRequestDTO request,
        HttpSession session) {
    
    // 1. Vérifier authentification
    if (!authService.isAuthenticated(session)) {
        throw new UnauthorizedException("Authentification requise");
    }
    
    // 2. Vérifier rôle ADMIN
    UserRole role = authService.getAuthenticatedUserRole(session);
    if (role != UserRole.ADMIN) {
        throw new ForbiddenException(
            "Seuls les ADMIN peuvent créer des commandes");
    }
    
    // 3. Créer la commande
    OrderResponseDTO order = orderService.createOrder(request);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(order);
}
```

**EXEMPLE - getOrderById() avec vérification CLIENT:**
```java
@GetMapping("/{id}")
public ResponseEntity<OrderResponseDTO> getOrderById(
        @PathVariable Long id,
        HttpSession session) {
    
    // 1. Vérifier authentification
    if (!authService.isAuthenticated(session)) {
        throw new UnauthorizedException("Authentification requise");
    }
    
    // 2. Récupérer la commande
    OrderResponseDTO order = orderService.getOrderById(id);
    
    // 3. Si CLIENT, vérifier que c'est SA commande
    UserRole role = authService.getAuthenticatedUserRole(session);
    if (role == UserRole.CLIENT) {
        Long userId = authService.getAuthenticatedUserId(session);
        Client client = clientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Client non trouvé"));
        
        // Vérifier que la commande appartient à ce client
        if (!order.getClientId().equals(client.getId())) {
            throw new ForbiddenException(
                "Vous ne pouvez consulter que vos propres commandes");
        }
    }
    
    return ResponseEntity.ok(order);
}
```

---

### ═══════════════════════════════════════════════════════════════
### 🧪 PHASE 5 : TESTS COMPLETS (1-2 heures)
### ═══════════════════════════════════════════════════════════════

#### **Scénario de test complet - CLIENT AMINE**

**TEST 1 : Créer 1ère commande (client BASIC)**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "clientId": 1,
  "items": [
    { "productId": 1, "quantite": 1 }
  ]
}

RÉSULTAT ATTENDU:
- sousTotal: 5000 (prix du produit)
- montantRemise: 0 (client BASIC)
- montantHT: 5000
- montantTVA: 1000 (20%)
- totalTTC: 6000
- status: PENDING
```

**TEST 2 : Payer la commande (via PaymentService)**
```http
POST http://localhost:8080/api/payments
Content-Type: application/json

{
  "orderId": 1,
  "montant": 6000,
  "methodePaiement": "ESPECES"
}

RÉSULTAT ATTENDU:
- statut: ENCAISSE
- Order.montantRestant: 0
```

**TEST 3 : Confirmer la commande**
```http
PUT http://localhost:8080/api/orders/1/confirm

RÉSULTAT ATTENDU:
- Order.status: CONFIRMED
- Product.stock: décrémenté
- Client.totalOrders: 1
- Client.totalSpent: 6000
- Client.tier: BASIC (encore, car 1 < 3)
```

**TEST 4 : Créer et confirmer 2ème commande**
```
Répéter TEST 1 + 2 + 3

RÉSULTAT ATTENDU:
- Client.totalOrders: 2
- Client.totalSpent: 12000
- Client.tier: BASIC (encore, car 2 < 3)
```

**TEST 5 : Créer et confirmer 3ème commande ⭐**
```
Répéter TEST 1 + 2 + 3

RÉSULTAT ATTENDU:
- Client.totalOrders: 3 ✅
- Client.totalSpent: 18000
- Client.tier: SILVER ⭐ (PASSAGE DE NIVEAU!)
```

**TEST 6 : Créer nouvelle commande (client maintenant SILVER)**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "clientId": 1,
  "items": [
    { "productId": 2, "quantite": 1 }
  ]
}

Supposons produit prix = 600 DH

RÉSULTAT ATTENDU:
- sousTotal: 600
- montantRemise: 30 ⭐ (SILVER: 5% car 600 ≥ 500)
- montantHT: 570
- montantTVA: 114
- totalTTC: 684
```

**TEST 7 : Tester code promo**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "clientId": 1,
  "items": [
    { "productId": 3, "quantite": 1 }
  ],
  "codePromo": "PROMO-2024"
}

Supposons produit prix = 1000 DH

RÉSULTAT ATTENDU:
- sousTotal: 1000
- montantRemise: 100 (SILVER 5% + PROMO 5% = 10%)
- montantHT: 900
- montantTVA: 180
- totalTTC: 1080
- promoApplied: true
```

**TEST 8 : Stock insuffisant**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "clientId": 1,
  "items": [
    { "productId": 4, "quantite": 999 }
  ]
}

Si stock < 999:

RÉSULTAT ATTENDU:
- HTTP 422
- Exception: InsufficientStockException
- Message: "Stock insuffisant..."
```

**TEST 9 : Confirmer commande non payée**
```
Créer commande (PENDING, montantRestant = 1000)
Essayer de confirmer SANS payer:

PUT http://localhost:8080/api/orders/X/confirm

RÉSULTAT ATTENDU:
- HTTP 422
- Exception: BusinessRuleViolationException
- Message: "La commande doit être entièrement payée..."
```

**TEST 10 : Annuler commande PENDING**
```http
PUT http://localhost:8080/api/orders/X/cancel

RÉSULTAT ATTENDU:
- status: CANCELED
- Stock: inchangé
- Client stats: inchangées
```

**TEST 11 : Annuler commande CONFIRMED (doit échouer)**
```
Commande déjà confirmée

PUT http://localhost:8080/api/orders/X/cancel

RÉSULTAT ATTENDU:
- HTTP 422
- Exception: BusinessRuleViolationException
- Message: "Seules les commandes PENDING..."
```

**TEST 12 : CLIENT essayant de voir commande d'un autre**
```
Login as CLIENT 1
Essayer de voir commande de CLIENT 2:

GET http://localhost:8080/api/orders/X

RÉSULTAT ATTENDU:
- HTTP 403
- Exception: ForbiddenException
```

---

### ═══════════════════════════════════════════════════════════════
### 📊 PHASE 6 : VALIDATION FINALE (30 min)
### ═══════════════════════════════════════════════════════════════

**CHECKLIST DE VALIDATION:**

```
✅ DTOs créés:
   □ OrderItemRequestDTO
   □ OrderRequestDTO
   □ OrderItemResponseDTO
   □ OrderResponseDTO
   □ OrderHistoryDTO

✅ Service:
   □ OrderService interface
   □ OrderServiceImpl avec toutes les méthodes
   □ createOrder() - calculs corrects
   □ confirmOrder() - mise à jour stock + stats + tier
   □ updateClientTier() - logique correcte
   □ cancelOrder()
   □ getOrderById()
   □ getAllOrders()
   □ getClientOrderHistory()

✅ Controller:
   □ OrderController créé
   □ Tous les endpoints
   □ Authorization ADMIN vs CLIENT
   □ Validation des permissions

✅ Tests:
   □ Créer commande BASIC (pas de remise)
   □ Créer commande SILVER (5% remise)
   □ Créer commande avec promo (remise cumulée)
   □ Confirmer commande → stock décrémente
   □ Confirmer commande → stats mises à jour
   □ Confirmer 3ème commande → tier SILVER ⭐
   □ Stock insuffisant → exception
   □ Confirmer sans paiement → exception
   □ Annuler PENDING → OK
   □ Annuler CONFIRMED → exception
   □ CLIENT voir autre commande → 403

✅ Calculs:
   □ Sous-total = somme des lignes
   □ Remise fidélité selon tier
   □ Remise promo si code valide
   □ Remises cumulatives
   □ Montant HT = sous-total - remises
   □ TVA = montant HT × 20%
   □ Total TTC = montant HT + TVA
   □ Arrondis à 2 décimales

✅ Règles métier:
   □ Tier calculé après confirmation
   □ SILVER: 3 commandes OU 1000 DH
   □ GOLD: 10 commandes OU 5000 DH
   □ PLATINUM: 20 commandes OU 15000 DH
   □ Stock validé à la création
   □ Paiement complet requis pour confirmation
   □ Seules PENDING peuvent être annulées
```

---

## 🎯 RÉCAPITULATIF - ORDRE D'EXÉCUTION

### **JOUR 1 - MATIN (4 heures)**
```
08:00 - 08:30  □ Créer les 5 DTOs
08:30 - 08:40  □ Créer l'interface OrderService
08:40 - 09:10  □ createOrder() - Partie 1 (validation)
09:10 - 09:40  □ createOrder() - Partie 2 (remise fidélité)
09:40 - 10:00  □ createOrder() - Partie 3 (code promo)
10:00 - 10:20  □ createOrder() - Partie 4 (calculs)
10:20 - 10:40  □ createOrder() - Partie 5 (sauvegarde)
10:40 - 11:25  □ confirmOrder() avec stock + stats
11:25 - 12:00  □ updateClientTier() ⭐ CRITIQUE
```

### **JOUR 1 - APRÈS-MIDI (4 heures)**
```
14:00 - 14:15  □ cancelOrder()
14:15 - 14:45  □ Méthodes de consultation (3)
14:45 - 15:00  □ convertToResponseDTO()
15:00 - 16:00  □ OrderController avec authorization
16:00 - 18:00  □ Tests complets (12 scénarios)
```

---

## 🔥 POINTS CRITIQUES À NE PAS MANQUER

### **1. Le Tier se calcule sur CONFIRMÉES uniquement**
```java
// ❌ FAUX
client.setTotalOrders(client.getTotalOrders() + 1); // dès la création

// ✅ CORRECT
// Seulement dans confirmOrder(), pas dans createOrder()
```

### **2. La remise fidélité vérifie le minimum**
```java
// ❌ FAUX
montantRemise = sousTotal * tier.getDiscountRate(); // toujours appliquer

// ✅ CORRECT
if (tier.isEligibleForDiscount(sousTotal)) {
    montantRemise = sousTotal * tier.getDiscountRate();
}
```

### **3. Les remises sont cumulatives**
```java
// ❌ FAUX - prendre la plus grande
montantRemise = Math.max(remiseFidelite, remisePromo);

// ✅ CORRECT - additionner
montantRemise = remiseFidelite + remisePromo;
```

### **4. TVA sur montant APRÈS remise**
```java
// ❌ FAUX
montantTVA = sousTotal * 0.20;

// ✅ CORRECT
montantHT = sousTotal - montantRemise;
montantTVA = montantHT * 0.20;
```

### **5. Le stock se décrémente à la confirmation**
```java
// ❌ FAUX - dans createOrder()
product.setStock(product.getStock() - quantite);

// ✅ CORRECT - dans confirmOrder()
// Car si annulée, on n'a pas touché au stock
```

---

## 📈 PROGRESSION ATTENDUE

```
Après 2 heures:  DTOs + interface + début createOrder
Après 4 heures:  createOrder() complète
Après 6 heures:  confirmOrder() + updateClientTier()
Après 8 heures:  Tout le service terminé
Après 10 heures: Controller terminé
Après 12 heures: Tests validés ✅
```

---

## 🎓 POUR LA DÉMONSTRATION

**Préparer ce scénario:**
```
1. Montrer un client BASIC
2. Créer commande 1 → pas de remise
3. Payer + confirmer → client reste BASIC
4. Créer commande 2 → pas de remise
5. Payer + confirmer → client reste BASIC
6. Créer commande 3 → pas de remise
7. Payer + confirmer → ⭐ CLIENT DEVIENT SILVER!
8. Créer commande 4 → REMISE 5% appliquée!
9. Montrer l'historique du client
10. Montrer les statistiques (totalOrders, totalSpent, tier)
```

**Cela prouve:**
- ✅ Le calcul des commandes fonctionne
- ✅ Le système de paiement fonctionne
- ✅ La confirmation met à jour les stats
- ✅ **Le tier se recalcule automatiquement** ⭐
- ✅ **La nouvelle remise s'applique immédiatement** ⭐

---

## 🚀 VOUS ÊTES PRÊT!

**Ce roadmap couvre:**
- ✅ Chaque étape détaillée
- ✅ Chaque ligne de code expliquée
- ✅ Chaque règle métier clarifiée
- ✅ Tous les pièges identifiés
- ✅ Tous les tests nécessaires

**Suivez ce guide étape par étape et vous aurez un système de commandes avec fidélité PARFAIT!**

**BON COURAGE! 💪🔥**

