# Controllo Aggiornamenti all'Avvio e Presentazione Novità

Implementazione del controllo automatico della disponibilità di aggiornamenti all'avvio dell'applicazione tramite la libreria ufficiale Google Play In-App Update, con presentazione di un dialog Material 3 contenente l'elenco delle novità introdotte, la possibilità di posticipare l'aggiornamento e il reindirizzamento diretto a Google Play Store.

### User Review & Critical Decisions

> [!IMPORTANT]
> Le seguenti scelte confermate nella fase di chiarimento guidano l'architettura:

- **Sorgente di rilevamento aggiornamenti**: Utilizzo dell'API ufficiale `com.google.android.play:app-update-ktx` (`AppUpdateManager`) che interroga direttamente Google Play Store.
- **Azione di aggiornamento**: Il pulsante "Aggiorna ora" apre direttamente la scheda dell'app sul Google Play Store (`market://details?id=com.aistudio.mealplan.rkfpw` con fallback su URL web).
- **Esperienza utente (UX)**: Dialog Material 3 non bloccante con elenco dettagliato delle novità introdotte ("Cosa c'è di nuovo") e pulsante "Più tardi" per consentire all'utente di posticipare senza forzature.

---

### 1. Overview & Core Concept

- **Cosa fa**:
  1. All'avvio dell'applicazione (`MainActivity`), un gestore dedicato interroga l'`AppUpdateManager` per verificare se Google Play segnala un aggiornamento disponibile (`UpdateAvailability.UPDATE_AVAILABLE`).
  2. In caso positivo, mostra un dialog elegante e informativo che evidenzia la versione disponibile e l'elenco puntato delle novità introdotte.
  3. L'utente può premere **"Aggiorna ora"** per accedere immediatamente a Google Play Store e completare l'update, oppure **"Più tardi"** per continuare ad utilizzare l'app.
  4. Nella schermata delle impostazioni (`SettingsScreen`), aggiunge la voce interattiva **"Verifica aggiornamenti"** con visualizzazione della cronologia delle versioni e delle novità della versione attuale.
- **Valore per l'utente**: Gli utenti rimangono sempre informati sui miglioramenti, le correzioni e le nuove funzionalità dell'app, potendo aggiornare con un solo tocco senza attriti.

---

### 2. User Experience & Visual Design

- **Flusso all'Avvio**:
  - `LaunchedEffect(Unit)` in `MainActivity` avvia la verifica asincrona in background.
  - Se è presente un aggiornamento, compare il dialog con animazione fluida.
- **Design del Dialog di Aggiornamento (`AppUpdateDialog`)**:
  - **Icona di testa**: Icona circolare con `Icons.Default.SystemUpdate` in contenitore tonale primario (SagePrimary).
  - **Titolo**: "Aggiornamento Disponibile" con indicazione del numero di versione (es. "Versione 11.0").
  - **Sezione Novità**: Card racchiusa con elenco puntato chiaro e icone di spunta/badge per ogni novità (es. ottimizzazione piano pasti, gestione scorrimento, nuova interfaccia editor).
  - **Pulsanti di azione**:
    - `Button` in stile riempito primario: "Aggiorna ora" (icona Play Store / download).
    - `TextButton`: "Più tardi" (chiude il dialog e memorizza la scelta della sessione).
- **Integrazione in Impostazioni (`SettingsScreen`)**:
  - Sezione informativa arricchita con stato "L'app è aggiornata (v11.0)" e pulsante per rieseguire la verifica manuale o consultare il changelog delle ultime versioni.

---

### 3. Key Product Decisions & Trade-Offs

- **Decisione 1: Libreria ufficiale `app-update-ktx`**:
  - *Approccio scelto*: Aggiunta di `com.google.android.play:app-update-ktx:2.1.0` in `libs.versions.toml` e `app/build.gradle.kts`.
  - *Perché*: È lo standard raccomandato da Google per le app distribuite su Play Store; gestisce la cache, la compatibilità dei dispositivi e le quote in modo nativo.
- **Decisione 2: Gestione sicura per esecuzione locale/emulatore**:
  - *Approccio scelto*: Gestione con `addOnFailureListener` e try/catch silenzioso: se l'app è in esecuzione in ambiente di sviluppo (dove Play Store non è collegato), l'app non va in crash ma prosegue normalmente.
- **Decisione 3: Provider locale delle note di rilascio (`AppReleaseNotesProvider`)**:
  - *Approccio scelto*: Componente modulare che associa i numeri di versione (v11.0, v10.0, ecc.) alle relative note di rilascio localizzate in italiano e inglese.
  - *Perché*: L'API Play Store non restituisce il testo formattato del changelog all'interno del client; questo provider garantisce una presentazione visiva ricca e coerente con il design dell'app.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                        MainActivity                         │
│  - LaunchedEffect(Unit): AppUpdateHelper.checkForUpdate()   │
│  - State: showUpdateDialog (version, releaseNotes)          │
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌───────────────────────────────┐   ┌───────────────────────────┐
│       AppUpdateManager        │   │  AppReleaseNotesProvider  │
│  - Play Core API check        │   │  - Localized changelog    │
│  - UPDATE_AVAILABLE listener  │   │  - Highlights per version │
└───────────────┬───────────────┘   └─────────────┬─────────────┘
                │                                 │
                └────────────────┬────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│                      AppUpdateDialog                        │
│  - M3 Dialog displaying "Cosa c'è di nuovo"                │
│  - Actions: "Aggiorna ora" (Play Store Intent) & "Più tardi"│
└─────────────────────────────────────────────────────────────┘
```

#### Modifiche ai File:

1. **`gradle/libs.versions.toml`**:
   - Versione `playAppUpdate = "2.1.0"`
   - Dipendenza `play-app-update = { group = "com.google.android.play", name = "app-update-ktx", version.ref = "playAppUpdate" }`

2. **`app/build.gradle.kts`**:
   - Aggiunta di `implementation(libs.play.app.update)` nel blocco `dependencies`.

3. **`com.example.util.AppUpdateManager.kt`**:
   - Classe helper per interagire con `AppUpdateManager` di Google Play.
   - Fornisce funzioni per avviare il controllo, ottenere lo stato e lanciare l'Intent per Google Play Store (`market://details?id=...`).

4. **`com.example.data.model.AppReleaseNotes.kt`**:
   - Modello dati per le versioni e le relative novità introdotte (con supporto bilingue IT/EN).

5. **`com.example.ui.components.AppUpdateDialog.kt`**:
   - Composable M3 per il dialog informativo delle novità con layout curato.

6. **`com.example.MainActivity.kt`**:
   - Invocazione del controllo all'avvio e gestione dello stato di visualizzazione del dialog.

7. **`com.example.ui.screens.SettingsScreen.kt`**:
   - Aggiunta della voce per il controllo manuale degli aggiornamenti e visualizzazione delle note di rilascio.
