# Integrazione FoodDb.db (Sola Lettura) & Gestione Lingua Applicazione

Transizione completa alla nuova sorgente dati offline `FoodDb.db` composta da due tabelle gerarchiche (`principal_food_tables` primaria e `food_back_archive` di fallback), ricerca nutrizionale bilingue intelligente (`name` / `english_name`) con de-duplicazione, indicatore cromatico giallo/rosso e schermata dedicata Impostazioni con selettore lingua (Italiano / Inglese) reattivo per l'intera interfaccia.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> Tutte le decisioni critiche sono state chiarite e confermate in Fase 1:
> - **Strategia di ricerca nel DB**: La ricerca interroga prima la colonna corrispondente alla lingua attiva dell'app (`name` per Italiano, `english_name` per Inglese). Se non produce risultati, applica automaticamente il fallback in `OR` su entrambe le colonne per massimizzare la reperibilità.
> - **Accesso Impostazioni**: Icona a forma di ingranaggio (`Icons.Default.Settings`) visibile nella barra superiore dell'applicazione, con apertura di una schermata dedicata alle impostazioni.
> - **Formato nome alimento selezionato**: Nei risultati di ricerca viene mostrato il nome nella lingua attiva con l'altra lingua come sottotitolo (se presente e differente); all'atto della selezione viene salvato il nome nella lingua attiva.
> - **Integrità & Sola Lettura**: `FoodDb.db` viene integrato tramite Room ORM configurato rigorosamente in sola lettura (nessuna operazione di insert/update/delete esposta).

---

### 1. Overview & Core Concept

- **Cosa fa**: Sostituisce il vecchio database `usda_food.db` con il nuovo database locale pre-confezionato `FoodDb.db` (2.2 MB, ~14.000 alimenti). Effettua la ricerca combinata tra alimenti già usati nei pasti precedenti (pallino giallo) e le tabelle del database (pallino rosso), interrogando primariamente `principal_food_tables` e, solo se questa non produce risultati, la tabella `food_back_archive`.
- **Integrazione Lingua**: Introduce una gestione centralizzata e reattiva della lingua dell'applicazione (Italiano e Inglese), che aggiorna istantaneamente sia le etichette dell'interfaccia (giorni, tipi pasto, pulsanti, categorie spesa) sia la priorità linguistica nella ricerca alimenti.
- **Valore per l'utente**: Ricerca ultrarapida completamente offline a latenza zero, copertura sia in lingua italiana che inglese, zero duplicati e piena autonomia nella scelta della lingua di navigazione.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Flusso Ricerca Alimento (AddFoodModalBottomSheet)**:
   - L'utente tocca un pasto per aggiungere un alimento.
   - Digitando nel campo di ricerca ("Cerca Alimento"), il sistema raccoglie i risultati:
     - Scenario A: Alimenti usati in precedenza (pallino giallo) + Alimenti da `principal_food_tables` (pallino rosso).
     - Scenario B: Alimenti usati in precedenza (pallino giallo) + Alimenti da `food_back_archive` (pallino rosso, se la tabella principale non ha corrispondenze).
     - Scenario C: Solo alimenti dalla tabella principale (pallino rosso).
     - Scenario D: Solo alimenti da inserimenti precedenti (pallino giallo).
   - Ogni elemento mostra il titolo nella lingua attiva, eventuale sottotitolo nella seconda lingua (es. *"Farro perlato, crudo"* con sottotitolo *"Spelt, grain"*), categoria e calorie per 100g.
   - Toccando l'elemento, i valori nutrizionali (calorie, proteine, carboidrati, grassi) vengono pre-compilati automaticamente.

2. **Flusso Impostazioni Lingua (SettingsScreen)**:
   - L'utente tocca l'icona ingranaggio nella barra superiore di `DailyMealPlanScreen`.
   - Si apre la schermata dedicata **Impostazioni**.
   - Viene mostrata la sezione **Lingua / Language** con schede di selezione per **Italiano** (IT) ed **English** (EN), con radio button interattivi.
   - Cambiando selezione, l'intera app si aggiorna reattivamente: etichette dei pasti, giorni della settimana, categorie della spesa, pulsanti e testi del bottom sheet. La scelta viene salvata permanentemente in SharedPreferences.
   - Pulsante Indietro per tornare comodamente al piano pasti.

#### Visual Identity & Theme
- **Colori Guida**: Palette organica Sage & Slate (`SagePrimary`, `SagePrimaryContainer`, `DarkSurface`, `DarkBackground`).
- **Indicatori di Provenienza (Dot Color System)**:
  - Giallo caldi (`#FFD54F`): Archivio pasti già utilizzati dall'utente.
  - Rosso chiaro (`#E57373`): Database alimentare `FoodDb.db` (sia `principal_food_tables` che `food_back_archive`).
- **Tipografia**: Material 3 Typography con font scalabili `sp`, titoli chiari e micro-testi informativi per i nutrienti e le lingue secondarie.

---

### 3. Key Product Decisions & Trade-Offs

- **Room ORM per FoodDb.db**:
  - *Scelta*: Utilizzo di `Room.databaseBuilder(...).createFromAsset("databases/FoodDb.db")` con DAO dedicato di sola lettura (`FoodDbDao`) contenente esclusivamente query `@Query(SELECT ...)`.
  - *Motivazione*: Massima aderenza alle linee guida Android e garanzia formale di sola lettura.
  - *Integrità Schema*: Le tabelle `principal_food_tables` (food_code) e `food_back_archive` (id) nel file SQLite asset vengono allineate con le chiavi primarie attese da Room per consentire la validazione istantanea senza discrepanze di schema.

- **Gerarchia di Ricerca & De-duplicazione**:
  - *Scelta*: Se `principal_food_tables` restituisce risultati, non viene interrogata `food_back_archive`. Se invece `principal_food_tables` è vuota, viene interrogata `food_back_archive`. I risultati locali (precedenti) vengono sempre preservati e affiancati.
  - *Motivazione*: Rispetta fedelmente la regola richiesta dall'utente, garantendo alte prestazioni e prevenendo l'eccesso di duplicati da archivi secondari più estesi.

- **Gestione Lingua in Jetpack Compose**:
  - *Scelta*: Modello `AppLanguageManager` reattivo con `StateFlow<AppLanguage>` esposto nel `MealPlanViewModel` e sincronizzato con `SharedPreferences`. Tutte le stringhe dell'app sono organizzate in un catalogo bilingue tipizzato (`AppStrings`).
  - *Motivazione*: Consente un cambio lingua immediato in-memory a runtime senza dover riavviare l'Activity o provocare glitch visivi.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│                                   │                                    │
│   ┌───────────────────────────────┴────────────────────────────────┐   │
│   │ TopAppBar Action: [Icons.Default.Settings]                     │   │
│   ▼                                                                ▼   │
│ ┌───────────────────────────┐                        ┌─────────────┴─┐ │
│ │ DailyMealPlanScreen       │                        │ SettingsScreen│ │
│ │ (Pasti, Giorni, Totali)   │                        │ (Lingua IT/EN)│ │
│ └─────────────┬─────────────┘                        └─────────────┬─┘ │
│               │                                                    │   │
│               ▼                                                    │   │
│ ┌───────────────────────────┐                                      │   │
│ │ AddFoodModalBottomSheet   │                                      │   │
│ │ (Ricerca reattiva 250ms)  │                                      │   │
│ └─────────────┬─────────────┘                                      │   │
└───────────────┼────────────────────────────────────────────────────┼───┘
                │                                                    │
                ▼                                                    ▼
   ┌─────────────────────────┐                         ┌─────────────────┐
   │    MealPlanViewModel    │◄────────────────────────┤AppLanguageState │
   └────────────┬────────────┘                         └─────────────────┘
                │
                ▼
   ┌────────────────────────────────────────────────────────┐
   │                   MealPlanRepository                   │
   │                                                        │
   │  ┌────────────────────────┐  ┌──────────────────────┐  │
   │  │   MealPlanDatabase     │  │    FoodDatabase      │  │
   │  │   (Lettura / Scrittura)│  │   (SOLA LETTURA)     │  │
   │  │   - Alimenti usati     │  │   - principal_tables │  │
   │  │   - Piani & Spesa      │  │   - food_back_archive│  │
   │  └────────────────────────┘  └──────────────────────┘  │
   └────────────────────────────────────────────────────────┘
```

#### Mapping Colonne Nutrizionali
- **`principal_food_tables`**:
  - Nome: `name` (IT), `english_name` (EN)
  - Calorie: `energy_kcal`
  - Proteine: `proteins`
  - Carboidrati: `available_carbohydrates`
  - Grassi: `lipids`
  - Categoria: `category`
- **`food_back_archive`**:
  - Nome: `name` (IT), `english_name` (EN)
  - Calorie: `Kcal`
  - Proteine: `Proteins`
  - Carboidrati: `Carbohydrates`
  - Grassi: `Fat`
  - Categoria: Categorizzazione dinamica automatica

---

### Piano di Esecuzione delle Modifiche

1. **Configurazione DB Asset `FoodDb.db`**:
   - Assicurare la definizione delle chiavi primarie sul database pre-confezionato in assets per la compatibilità con Room ORM.
2. **Definizione Struttura Dati Room (Sola Lettura)**:
   - Creazione entità `PrincipalFoodEntity` e `FoodBackArchiveEntity`.
   - Creazione DAO `FoodDbDao` con metodi di ricerca prioritari (lingua attiva + fallback OR) e ritorno dati nutrizionali.
   - Creazione classe database `FoodDatabase` con `.createFromAsset("databases/FoodDb.db")`.
3. **Gestione Centralizzata Lingue (`AppLanguageManager` & `AppStrings`)**:
   - Definizione enum `AppLanguage` (`IT`, `EN`).
   - Catalogo bilingue `AppStrings` per navigazione, schermate, giorni, pasti, categorie alimentari e messaggi.
   - Persistenza della lingua in SharedPreferences.
4. **Aggiornamento Logica di Ricerca nel Repository**:
   - Riorganizzazione di `searchFood` secondo i 3 scenari (locali + principal, oppure locali + back).
   - Generazione dei modelli con visualizzazione bilingue (titolo principale + sottotitolo lingua secondaria).
5. **Creazione Schermata Impostazioni & Integrazione UI**:
   - `SettingsScreen` con selettore lingua elegante a schede con icone e radio button.
   - Icona ingranaggio nella barra dell'applicazione.
   - Collegamento di tutti i testi di `MainActivity`, `DailyMealPlanScreen`, `ShoppingListScreen`, `PlanEditorScreen` e `AddFoodModalBottomSheet` alla lingua selezionata.
6. **Verifica & Test**:
   - Esecuzione unit test con Robolectric.
   - Compilazione con `compile_applet`.
