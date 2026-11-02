# Migrazione a Database Locale USDA Food (Room ORM - Sola Lettura)

Sostituzione completa delle API esterne remote (OpenFoodFacts e USDA Web API) con il database SQLite locale pre-popolato `usda_food.db`, utilizzato esclusivamente in **sola lettura** tramite Room ORM. La ricerca diventa istantanea, offline e reattiva, combinando gli alimenti già utilizzati con il catalogo nutrizionale USDA e mantenendo la distinzione visiva tramite indicatori cromatici (pallino giallo/rosso).

## User Review & Critical Decisions

> [!IMPORTANT]
> Decisioni confermate e precisazioni recepite:

- **Database di Sola Lettura (Read-Only)**: Il database `usda_food.db` è rigorosamente di **sola lettura**. Non verrà eseguita alcuna operazione di scrittura, inserimento o modifica sul catalogo; tutte le modifiche e i pasti dell'utente rimangono confinati nel database separato `MealPlanDatabase`.
- **Dicitura UI Standardizzata**: I testi e i placeholder dell'interfaccia utente (come `"Ricerca USDA & OpenFoodFacts"`, `"Cerca su OpenFoodFacts"`, ecc.) verranno tutti uniformati con la dicitura pulita ed essenziale: **`"Cerca Alimento"`**.
- **Lingua di ricerca nel catalogo USDA**: La ricerca si adatta automaticamente alla lingua dell'applicazione/sistema (italiano `it`, con fallback `en` per termini o dispositivi in inglese).
- **Esperienza di digitazione e latenza**: Ricerca reattiva con micro-attesa fluida di 250ms durante la digitazione, eliminando completamente i ritardi di rete (i precedenti 2 secondi) e i tentativi ripetuti.
- **Integrazione ORM Room**: Room (già in uso per `MealPlanDatabase`) gestirà l'accesso a `usda_food.db` tramite un'istanza dedicata `UsdaFoodDatabase` con prepackaged database da `databases/usda_food.db` (`createFromAsset`).
- **Coesistenza dei risultati senza mutua esclusione**: La ricerca interroga in parallelo gli alimenti già registrati nei piani locali (pallino giallo) e il catalogo USDA (pallino rosso), fondendo i risultati senza duplicati e senza che una fonte escluda l'altra.
- **Rimozione totale dipendenze e chiavi esterne**: Eliminazione definitiva di `OpenFoodFactsApi.kt`, `UsdaFoodDataApi.kt` (inclusa la `DEMO_KEY`), dei relativi interceptor di rete e di qualsiasi riferimento nei testi dell'interfaccia.

---

## 1. Overview & Core Concept

- **Cosa fa**: Consente all'utente di cercare qualsiasi alimento all'interno del database nutrizionale locale USDA di oltre 5 MB (in sola lettura, con migliaia di voci tradotte e valori per calorie, proteine, carboidrati e grassi) e tra i cibi precedentemente inseriti nei propri pasti.
- **Target Audience / Persona**: Utenti dell'app MealPlan che necessitano di aggiungere rapidamente cibi al piano settimanale o alla lista spesa senza dipendere da connessione Internet o subire rallentamenti dovuti ad API esterne.
- **Valore Chiave**: Velocità di risposta immediata (inferiore a 10ms su query SQLite indicizzate), funzionamento 100% offline, privacy totale (nessuna richiesta verso server esterni), integrità garantita del catalogo (sola lettura) e zero rischi di rate-limiting o chiavi API scadute.

---

## 2. User Experience & Visual Design

- **Flussi Utente**:
  1. L'utente apre il modulo di aggiunta alimento (in linea in `PlanEditorScreen` o nel modal bottom sheet).
  2. Il campo di testo presenta il placeholder chiaro e uniforme: **`"Cerca Alimento"`**.
  3. Digitando una parola (es. "pollo", "pasta", "mela"), dopo appena 250ms compaiono istantaneamente i risultati ordinati per rilevanza e popolarità.
  4. L'utente riconosce a colpo d'occhio l'origine di ciascun alimento:
     - **Pallino Giallo** (`#FFCA28`): Alimento già registrato nei piani passati con le grammature e i valori personalizzati dell'utente.
     - **Pallino Rosso** (`#E57373`): Alimento proveniente dal database nutrizionale ufficiale USDA.
  5. Toccando un risultato, i campi (nome, categoria, calorie, proteine, carboidrati, grassi per 100g) vengono compilati istantaneamente per consentire l'inserimento o la modifica della grammatura.

- **Aggiornamento Testi e Suggerimenti**:
  - Sostituzione mirata di ogni etichetta e placeholder obsoleto con la stringa **`"Cerca Alimento"`**.
  - Rimozione di spinner di caricamento prolungati o messaggi di errore di rete non più pertinenti.

---

## 3. Key Product Decisions & Trade-Offs

- **Decisione 1: Architettura Room a doppio database con isolamento di Sola Lettura**:
  - *Scelta*: Mantenere `MealPlanDatabase` per i dati utente mutabili (piani, pasti, lista spesa, impostazioni) e introdurre `UsdaFoodDatabase` dedicato al catalogo USDA come istanza di sola lettura da asset pre-compilato.
  - *Motivazione*: Nessun rischio di corruzione o mutazione accidentale del database nutrizionale di riferimento. Inoltre, eventuali aggiornamenti del database utente non impatteranno il catalogo USDA e viceversa.

- **Decisione 2: Query di sola lettura ottimizzata per i macronutrienti**:
  - *Scelta*: Un unico DAO di query con SELECT indicizzate per i nutrienti chiave (id 1008 per calorie, 1003 per proteine, 1005 per carboidrati, 1004 per grassi).
  - *Motivazione*: Estrema velocità di lettura senza overhead di parsing o scrittura.

- **Decisione 3: Debounce reattivo a 250ms**:
  - *Scelta*: `debounce(250)` nei Flow del ViewModel.
  - *Motivazione*: Esperienza utente fluida, reattiva e priva di sfarfallii durante la digitazione.

---

## 4. Technical Architecture & Data Strategy

### Diagramma dei Componenti e Flusso Dati

```
┌───────────────────────────────────────────────────────────────┐
│                     Jetpack Compose UI                        │
│   PlanEditorScreen (Inline Food Form) / AddFoodBottomSheet    │
│   - Etichetta / Placeholder uniforme: "Cerca Alimento"        │
└───────────────────────────────┬───────────────────────────────┘
                                │ Query testo (debounce 250ms)
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                      MealPlanViewModel                        │
│   - foodSearchResults: StateFlow<List<FoodItemModel>>         │
│   - isSearchingFood: StateFlow<Boolean>                       │
└───────────────────────────────┬───────────────────────────────┘
                                │ searchFood(query)
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                     MealPlanRepository                        │
│   Fonde i risultati senza duplicati e senza mutua esclusione  │
└───────────────┬───────────────────────────────┬───────────────┘
                │ (async)                       │ (async)
                ▼                               ▼
┌──────────────────────────────┐ ┌──────────────────────────────┐
│       MealFoodItemDao        │ │         UsdaFoodDao          │
│    (in MealPlanDatabase)     │ │    (in UsdaFoodDatabase)     │
│   - Dati mutabili utente     │ │   - STRICT READ-ONLY         │
│   - Pallino Giallo ("Local") │ │   - Pallino Rosso ("USDA")   │
└──────────────────────────────┘ └──────────────────────────────┘
```

### Struttura Entità e DAO Room per `usda_food.db`

1. **`UsdaFoodEntities.kt`**:
   - `CategoryEntity` (`@Entity(tableName = "categories")`)
   - `FoodEntity` (`@Entity(tableName = "foods")`)
   - `FoodTranslationEntity` (`@Entity(tableName = "food_translations", primaryKeys = ["food_id", "lang_code"])`)
   - `NutrientEntity` (`@Entity(tableName = "nutrients")`)
   - `FoodNutrientEntity` (`@Entity(tableName = "food_nutrients", primaryKeys = ["food_id", "nutrient_id"])`)
2. **`UsdaFoodSearchResult.kt`**:
   - Data class di sola lettura: `id`, `description`, `categoryIt`, `categoryEn`, `calories`, `protein`, `carbs`, `fat`.
3. **`UsdaFoodDao.kt`**:
   - Solo metodi `@Query` (nessun `@Insert`, `@Update`, `@Delete`). Filtro su `lang_code` (`it` / `en`), ricerca su `description` e `search_keywords`, ordinamento per `is_common DESC`.
4. **`UsdaFoodDatabase.kt`**:
   - Database Room di sola lettura creato con `.createFromAsset("databases/usda_food.db")`.
5. **Pulizia e refactoring**:
   - Rimozione file `app/src/main/java/com/example/data/remote/OpenFoodFactsApi.kt` e `app/src/main/java/com/example/data/remote/UsdaFoodDataApi.kt`.
   - Rimozione di `DEMO_KEY` e chiamate HTTP esterne.
   - Aggiornamento dei componenti UI con la dicitura standard **`"Cerca Alimento"`**.
