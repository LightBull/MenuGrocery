# Internazionalizzazione Globale & Nuova TopAppBar con Ingranaggio

Revisione completa del sistema linguistico per garantire che la selezione della lingua (Italiano / Inglese) aggiorni istantaneamente **ogni singolo testo dell'intera applicazione** (titoli di pagina, menù, nomi dei pasti, giorni della settimana, categorie spesa, etichette dei form, dialoghi di conferma, riepiloghi nutrizionali e titoli/note dei piani predefiniti) e introduzione di una **TopAppBar globale fissa in alto** con titolo della pagina attiva e icona impostazioni (ingranaggio) posizionata a destra.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> Decisioni chiarite e confermate in Fase 1:
> - **TopAppBar Globale**: Invece di barre sparse o pulsanti isolati nei banner, viene creata una TopAppBar standard in cima all'applicazione visibile su tutte le sezioni principali, con titolo della sezione attiva allineato a sinistra e icona ingranaggio (`Icons.Default.Settings`) posizionata a destra con apposito testTag.
> - **Copertura Linguistica Integrale**: Ogni stringa visibile all'utente viene collegata dinamicamente allo stato `currentLanguage` (`AppLanguage.IT` / `AppLanguage.EN`), inclusi i titoli e le descrizioni dei piani alimentari predefiniti e dei cibi dimostrativi.
> - **Persistenza Immediata**: Il cambio lingua nelle impostazioni si applica reattivamente all'intera interfaccia senza riavvio dell'Activity e viene salvato permanentemente nelle SharedPreferences.

---

### 1. Overview & Core Concept

- **Cosa fa**: Estende il dizionario bilingue tipizzato `AppStrings` per coprire il 100% dei testi dell'applicazione, eliminando qualsiasi stringa hardcoded rimasta in italiano nelle schermate (`DailyMealPlanScreen`, `ShoppingListScreen`, `MealPlansManagementScreen`, `PlanEditorScreen`, `AddFoodModalBottomSheet`, `CommonComponents`). Aggiunge la traduzione dinamica per i nomi dei giorni, i pasti, le unità di misura, le categorie merceologiche e i contenuti dimostrativi predefiniti.
- **Nuova TopAppBar**: Struttura l'intestazione superiore dell'app con una barra M3 elegante, sfondo coerente con la palette Sage/Dark, titolo della sezione corrente e pulsante ingranaggio ben visibile e accessibile sulla destra.

---

### 2. User Experience & Visual Design

#### TopAppBar Globale
- **Posizione**: Sempre presente in cima allo schermo in tutte le 3 sezioni principali (`Piano Pasti`, `Lista Spesa`, `I Miei Piani`).
- **Contenuto**:
  - Titolo a sinistra in grassetto (es. *"Piano Pasti"* / *"Meal Plan"*).
  - Tasto azione a destra: `IconButton` con `Icons.Default.Settings` (touch target conforme >= 48dp, testTag `"topbar_btn_settings"`).
- **Apertura Impostazioni**: Al tocco dell'ingranaggio si apre a schermo intero la `SettingsScreen` con pulsante Indietro per tornare comodamente alla schermata precedente.

#### Traduzione a 360 Gradi
1. **Navigazione & Schede**:
   - `Piano Pasti` ⟷ `Meal Plan`
   - `Lista Spesa` ⟷ `Shopping List`
   - `I Miei Piani` ⟷ `My Plans`
2. **Pasti & Orari**:
   - `Colazione`, `Spuntino Mattutino`, `Pranzo`, `Spuntino Pomeridiano`, `Cena` ⟷ `Breakfast`, `Morning Snack`, `Lunch`, `Afternoon Snack`, `Dinner`.
   - `Orario consigliato:` ⟷ `Recommended time:`.
3. **Giorni della Settimana**:
   - `Lunedì` .. `Domenica` (Lun..Dom) ⟷ `Monday` .. `Sunday` (Mon..Sun).
4. **Categorie della Spesa / Corsie**:
   - `Frutta e Verdura` ⟷ `Fruits & Vegetables`
   - `Carne e Pesce` ⟷ `Meat & Seafood`
   - `Latticini e Uova` ⟷ `Dairy & Eggs`
   - `Cereali, Pane e Pasta` ⟷ `Grains, Bread & Pasta`
   - `Dispensa e Condimenti` ⟷ `Pantry & Condiments`
   - `Extra e Cura della Casa` ⟷ `Extra & Household`
5. **Creazione & Modifica Piano (PlanEditorScreen & MealPlansManagementScreen)**:
   - Campi di input: *"Nome del Piano *" ⟷ *"Plan Name *"*, *"Note o Obiettivi"* ⟷ *"Notes or Goals"*, *"Imposta come attivo"* ⟷ *"Set as active"*.
   - Pulsanti e dialoghi: *"Nuovo Piano"* ⟷ *"New Plan"*, *"Salva"* ⟷ *"Save"*, *"Esci senza salvare"* ⟷ *"Exit without saving"*, *"Modifiche non salvate"* ⟷ *"Unsaved changes"*, *"Copia alimento"* ⟷ *"Copy food"*.
6. **Lista della Spesa & Dispensa (ShoppingListScreen)**:
   - *"Cerca nella spesa..."* ⟷ *"Search grocery..."*
   - *"Genera Lista Aggregata"* ⟷ *"Generate Combined List"*, *"Ripristina"* ⟷ *"Reset All"*, *"Extra"* ⟷ *"Add Extra"*.
   - Dialoghi di regolazione dispensa: *"Fabbisogno dai pasti"*, *"Già in frigo / dispensa"*, *"Quantità reale da comprare"*.
7. **Piani Predefiniti & Alimenti Dimostrativi**:
   - *"Piano Benessere Mediterraneo"* ⟷ *"Mediterranean Wellness Plan"*
   - Cibi demo (Yogurt greco, Fiocchi d'avena, Pollo ai ferri, Riso integrale, Mela, Salmone, Broccoli) tradotti dinamicamente a vista.

---

### 3. Technical Architecture & Component Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ TopAppBar: [Titolo Dinamico]              [Icons.Default.Settings] │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                   │                                    │
│   ┌───────────────────────────────┴────────────────────────────────┐   │
│   ▼                               ▼                                ▼   │
│ ┌───────────────────┐ ┌───────────────────────┐ ┌───────────────────┐  │
│ │DailyMealPlanScreen│ │  ShoppingListScreen   │ │MealPlansManagement│  │
│ │(Pasti, Totali,    │ │  (Corsie, Dispensa,   │ │(Elenco piani,     │  │
│ │ Giorni tradotti)  │ │   Extra, Dialoghi)    │ │ Crea/Modifica/Del)│  │
│ └─────────┬─────────┘ └───────────┬───────────┘ └─────────┬─────────┘  │
│           │                       │                       │            │
│           ▼                       ▼                       ▼            │
│ ┌───────────────────┐ ┌───────────────────────┐ ┌───────────────────┐  │
│ │AddFoodBottomSheet │ │  PlanEditorScreen     │ │   SettingsScreen  │  │
│ │(Form & Ricerca)   │ │  (Editor 7 giorni)    │ │   (Selettore IT/EN│  │
│ └───────────────────┘ └───────────────────────┘ └───────────────────┘  │
│                                                                        │
│   ▲                                                                    │
│   └──────────────────── ViewModel.currentLanguage ────────────────────┘│
└────────────────────────────────────────────────────────────────────────┘
```

#### Moduli & File Coinvolti:
1. **`AppStrings.kt`**: Ampliamento con cataloghi bilingue completi per tutte le sezioni (editor, shopping, management, dialoghi, dispense, piani predefiniti).
2. **`FoodCategory.kt`**: Aggiunta di `getLocalizedName(lang: AppLanguage)` per ciascuna categoria alimentare.
3. **`Models.kt`**: Localizzazione dinamica di `QuantityUnit`, `DayOfWeekInfo`, `MealType`.
4. **`MainActivity.kt`**: Inserimento della `TopAppBar` globale con titolo dinamico e pulsante ingranaggio a destra; passaggio di `currentLanguage` a tutte le sotto-schermate.
5. **`DailyMealPlanScreen.kt`**: Rimozione dell'icona ingranaggio dal banner interno (spostata nella TopAppBar globale) e traduzione di tutti i testi di intestazione, giorni, orari e alimenti.
6. **`ShoppingListScreen.kt`**: Collegamento di tutti i chip, pulsanti, dialoghi di dispensa, extra e intestazioni di reparto ad `AppStrings`.
7. **`MealPlansManagementScreen.kt`**: Collegamento dei dialoghi di creazione/eliminazione piano, badge di stato e schede piano ad `AppStrings`.
8. **`PlanEditorScreen.kt`**: Traduzione completa del form di modifica piano, dell'editor dei pasti, dei preset di quantità, del dialogo di copia e del dialogo di modifiche non salvate.

---

### Piano di Esecuzione delle Modifiche

1. **Estensione `AppStrings.kt` & Modelli Dati**:
   - Aggiunta di tutte le chiavi bilingue mancanti per editor, gestione piani, lista spesa e traduzione piani/alimenti demo.
   - Aggiunta di metodi di traduzione dinamica per `FoodCategory`, `QuantityUnit`, `MealType` e `DayOfWeekInfo`.
2. **TopAppBar Globale in `MainActivity.kt`**:
   - Creazione della `TopAppBar` nello Scaffold di `MainActivity`, con titolo basato sul tab attivo e icona ingranaggio a destra.
   - Rimozione dei controlli ingranaggio duplicati dalle sotto-schermate.
3. **Aggiornamento Schermate Utente**:
   - Aggiornamento di `DailyMealPlanScreen.kt`.
   - Aggiornamento di `ShoppingListScreen.kt`.
   - Aggiornamento di `MealPlansManagementScreen.kt`.
   - Aggiornamento di `PlanEditorScreen.kt`.
   - Aggiornamento di `CommonComponents.kt`.
4. **Verifica & Test**:
   - Esecuzione dei test Robolectric (`gradle :app:testDebugUnitTest`).
   - Verifica di compilazione (`compile_applet`).
   - Aggiornamento APK.
