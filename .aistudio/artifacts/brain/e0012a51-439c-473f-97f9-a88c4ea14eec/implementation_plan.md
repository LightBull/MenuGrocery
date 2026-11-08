# Ottimizzazione UX Schermate Piani e Modifica Pasti

Questo aggiornamento rimuove la card informativa introduttiva nella schermata "I miei piani", elimina la TopAppBar principale (con titolo e icona impostazioni) durante la creazione e modifica del piano alimentare lasciando spazio alla barra nativa dell'editor, e implementa lo scorrimento automatico fluido sul pasto correntemente attivo una volta inserito un nuovo alimento.

### User Review & Critical Decisions

> [!IMPORTANT]
> Le seguenti preferenze confermate nella fase di chiarimento guidano l'architettura della soluzione:

- **Barra di navigazione inferiore**: Rimane visibile per garantire la coerenza di navigazione tra le sezioni principali dell'applicazione.
- **TopAppBar in Creazione/Modifica Piano**: Viene nascosta la barra superiore dell'applicazione (titolo "I miei piani" + tasto impostazioni), lasciando attiva esclusivamente la TopAppBar contestuale dell'editor (pulsante indietro "←" e pulsante salva).
- **Comportamento di scorrimento sul pasto**: Scorrimento fluido e animato (`animateScrollToItem`) all'inizio della scheda del pasto attivo immediatamente dopo l'inserimento dell'alimento e la chiusura della form di ricerca.

---

### 1. Overview & Core Concept

- **Cosa fa**:
  1. Nella scheda **"I miei piani"**, elimina la card statica "I Miei piani alimentari" in cima alla lista per mostrare immediatamente i piani disponibili in modo compatto e pulito.
  2. Nella schermata di **Creazione / Modifica del piano** (`PlanEditorScreen`), maschera la `TopAppBar` globale dell'applicazione (`MainActivity`) evitando la duplicazione di barre di stato e rimuovendo il pulsante impostazioni non pertinente durante l'editing.
  3. Mantiene uno stato del **pasto attivo** (`activeMealType`): al tocco di "+ Aggiungi" su un pasto (es. Pranzo), quel pasto diventa attivo fino alla selezione esplicita di un pasto diverso. Al completamento dell'aggiunta dell'alimento e chiusura della form espandibile, la vista esegue uno scorrimento animato verso la scheda del pasto attivo, posizionando l'utente direttamente sui comandi del pasto senza disorientamenti.
- **Valore per l'utente**: Eliminazione di attriti visivi e operativi durante l'inserimento continuativo di alimenti nei pasti del piano settimanale.

---

### 2. User Experience & Visual Design

- **Flusso "I miei piani"**:
  - L'utente apre la tab "Piani": visualizza immediatamente la lista a scorrimento con i piani salvati e il badge del piano attivo, senza ingombri introduttivi.
- **Flusso "Creazione / Modifica Piano"**:
  - L'utente tocca un piano o il FAB "+": la TopAppBar di sistema dell'app scompare istantaneamente. Viene mostrata solo la barra dell'editor con la freccia indietro, il titolo "Nuovo piano / Modifica piano" e l'azione rapida "Salva".
  - La barra inferiore con le tab rimane accessibile sul fondo.
- **Flusso Aggiunta Alimento & Focus Automatico**:
  - L'utente tocca "+" su "Pranzo": la form di inserimento rapido si apre nel pasto e il sistema memorizza "Pranzo" come pasto corrente.
  - L'utente digita o seleziona un alimento e preme "Aggiungi al pasto":
    1. L'alimento viene aggiunto alla lista degli alimenti del pasto corrente.
    2. La form di ricerca si chiude.
    3. Il `LazyListState` attiva uno scorrimento animato che ricentra la card "Pranzo" in cima al viewport.
    4. L'utente ritrova immediatamente il pasto senza essere spinto visivamente sulla cena o sui pasti successivi a causa della contrazione della form.

---

### 3. Key Product Decisions & Trade-Offs

- **Decisione 1: Tracciamento dello stato Editor nel ViewModel (`isPlanEditorOpen`)**:
  - *Approccio scelto*: Aggiunta di `isPlanEditorOpen: StateFlow<Boolean>` in `MealPlanViewModel`, attivato/disattivato da `PlanEditorScreen` tramite `DisposableEffect`.
  - *Perché*: `MainActivity` osserva reattivamente questo stato e nasconde la TopAppBar globale (`!showSettings && !isPlanEditorOpen`) in modo pulito e disaccoppiato, senza dover passare lambda nidificate su più livelli di composable.
- **Decisione 2: Stato `activeMealType` persistito a livello di sessione editor**:
  - *Approccio scelto*: Memorizzazione del `MealType` cliccato in una variabile di stato `activeMealType` che non si azzera alla chiusura della form, ma solo quando l'utente tocca esplicitamente un pasto differente o cambia giorno.
  - *Perché*: Risponde fedelmente alla specifica dell'utente: *"ogni prima volta che viene cliccato l'aggiungi in un pasto attiva di fatto il pasto corrente finchè non viene cliccato l'aggiungi in un nuovo pasto lo stato non cambia"*.
- **Decisione 3: Timing dello scorrimento animato**:
  - *Approccio scelto*: Coroutine con leggero ritardo (circa 80-100ms) al trigger di chiusura per consentire a Compose di ricalcolare l'altezza contratta della lista prima di animare lo scroll all'indice del pasto (`2 + mealIndex`).
  - *Perché*: Evita salti di rendering e garantisce che lo scorrimento porti la vista al punto esatto.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                        MainActivity                         │
│  - Observes: isPlanEditorOpen from ViewModel                │
│  - Condition: if (!showSettings && !isPlanEditorOpen)       │
│    TopAppBar(title, settingsButton)                         │
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌───────────────────────────────┐   ┌───────────────────────────┐
│  MealPlansManagementScreen    │   │      PlanEditorScreen     │
│  - Removed intro Surface card │   │  - Own dedicated header   │
│  - Shows only plan cards + FAB│   │  - State: activeMealType  │
└───────────────────────────────┘   │  - Auto-scroll coroutine  │
                                    └───────────────────────────┘
```

#### Modifiche ai Componenti:

1. **`MealPlanViewModel.kt`**:
   - `_isPlanEditorOpen = MutableStateFlow(false)`
   - `val isPlanEditorOpen: StateFlow<Boolean> = _isPlanEditorOpen.asStateFlow()`
   - `fun setPlanEditorOpen(open: Boolean)`

2. **`MainActivity.kt`**:
   - `val isPlanEditorOpen by viewModel.isPlanEditorOpen.collectAsState()`
   - Condizione `topBar`: `if (!showSettings && !isPlanEditorOpen)`

3. **`MealPlansManagementScreen.kt`**:
   - Rimozione completa del blocco `item { Surface(...) { ... myPlansTitle ... } }` all'interno della `LazyColumn`.

4. **`PlanEditorScreen.kt`**:
   - `DisposableEffect` per impostare `viewModel.setPlanEditorOpen(true)` all'avvio e `false` su `onDispose`.
   - Gestione di `activeMealType: MealType?`.
   - Nel pulsante inline `+`: assegnazione `activeMealType = mealWithItems.mealType`.
   - In `onConfirmAdd`: chiusura form, ricalcolo posizionamento del pasto attivo con `currentDayMeals.indexOfFirst { it.mealType == targetMeal }` e chiamata a `listState.animateScrollToItem(index = 2 + targetIdx)`.
