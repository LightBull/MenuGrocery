# Creazione Privacy Policy HTML per Google Play Console (GitHub Pages)

Creazione di una pagina web HTML/CSS moderna, professionale e bilingue (italiano e inglese), posizionata in `docs/index.html` per essere ospitata gratuitamente tramite GitHub Pages (`https://<username>.github.io/<repo>/`) e soddisfare pienamente tutti i requisiti di conformità richiesti da Google Play Console per la pubblicazione dell'app **MealPlan**.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Scelte confermate dall'utente:**
> - **Percorso di pubblicazione:** `docs/index.html`, configurazione standard per GitHub Pages (impostando Branch `main` e cartella `/docs` nelle impostazioni del repository GitHub).
> - **Lingua:** Bilingue (Italiano completo con versione Inglese speculare accessibile tramite selettore rapido).
> - **Contatto Privacy Ufficiale:** `devsmapo@gmail.com` specificato chiaramente come titolare e contatto per qualsiasi richiesta privacy o chiarimento degli utenti.

---

### 1. Overview & Core Concept

- **Obiettivo:** Fornire un URL pubblico, accessibile e conforme alle policy di Google Play Developer (Data Safety, Privacy Policy URL) e al GDPR.
- **Specifiche dell'app MealPlan coperte nell'informativa:**
  1. **Nessuna raccolta dati personali:** L'app non richiede registrazione, account, nome, numero di telefono o email.
  2. **Archiviazione esclusivamente locale (On-Device):** I piani alimentari, i pasti, le quantità, le preferenze e la lista della spesa risiedono unicamente nel database SQLite/Room del dispositivo dell'utente. Nessun dato viene caricato su server cloud proprietari o condiviso con terzi.
  3. **Permessi di sistema spiegati in dettaglio:**
     - `POST_NOTIFICATIONS`: Utilizzato esclusivamente per notifiche e promemoria locali di scongelamento alimenti programmati dall'utente (gestiti dal componente `AlarmManager` di Android).
     - `INTERNET`: Utilizzato esclusivamente per inviare le query di ricerca testo inserite dall'utente alle banche dati pubbliche nutrizionali (Open Food Facts e USDA FoodData Central) per recuperare valori nutrizionali degli alimenti. Nessun identificativo univoco o dato personale viene trasmesso.
  4. **Nessuna pubblicità o tracciamento:** L'app non contiene SDK pubblicitari (Google AdMob, Unity, ecc.), né tracciatori analitici o di profilazione (Firebase Analytics, Facebook Pixel, ecc.).
  5. **Privacy dei minori (COPPA/GDPR Kids):** L'app non raccoglie dati da minori né profila gli utenti.
  6. **Diritti dell'utente e cancellazione dati:** Essendo i dati salvati solo localmente, la disinstallazione dell'app o la cancellazione dei dati dell'app dalle impostazioni di Android elimina istantaneamente e definitivamente ogni dato.

---

### 2. User Experience & Visual Design

- **Layout & Stile:**
  - Design pulito, moderno ed elegante ispirato all'identità visiva dell'app MealPlan (colori Sage Green `#6B8E23`, accenti Terracotta e sfondi neutri riposanti).
  - Pienamente responsive su smartphone, tablet e desktop.
  - Barra superiore fissa con selettore di lingua istantaneo (`IT` / `EN`) che mostra/nasconde la sezione desiderata con transizione fluida senza ricaricare la pagina.
  - Indice dei contenuti (Table of Contents) navigabile con ancore a salto rapido.
  - Box di riepilogo "In Breve / Summary at a glance" con badge visivi per una lettura immediata sia per i revisori di Google Play che per gli utenti finali.
  - Sezione chiara per i contatti con link mailto attivo (`devsmapo@gmail.com`).

---

### 3. Key Product Decisions & Trade-Offs

- **Decisione 1: Struttura a file unico `docs/index.html` con CSS e logica linguistica integrata**
  - *Scelta:* Includere stile e script di commutazione lingua direttamente in `docs/index.html` senza dipendenze da framework pesanti o CDN esterne che potrebbero non caricarsi o fallire i controlli automatizzati di Google.
  - *Perché:* Garantisce tempo di caricamento immediato (<100ms), zero dipendenze rotte, e massima affidabilità per il crawler di verifica di Google Play Console.

- **Decisione 2: Conformità al questionario "Data Safety" di Google Play Console**
  - *Scelta:* La privacy policy include una tabella di correlazione esplicita per la sezione "Sicurezza dei dati" (Data Safety) di Google Play Console, specificando che l'app dichiara *"No data collected"* e *"No data shared"*.
  - *Perché:* Velocizza e facilita l'approvazione dell'app durante la revisione di Google Play.

---

### 4. Technical Architecture & File Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       GitHub Repository                     │
├─────────────────────────────────────────────────────────────┤
│  docs/                                                      │
│   └── index.html  <-- GitHub Pages Source (Branch: main)    │
│                       URL: https://<user>.github.io/<repo>/ │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Google Play Console Setup                   │
│  - Scheda App -> Informativa sulla privacy -> Inserisci URL │
│  - Sezione Sicurezza dei Dati (Data Safety) -> Conforme     │
└─────────────────────────────────────────────────────────────┘
```

#### Modifiche previste:
1. Creazione della cartella `docs/` e del file `docs/index.html`.
2. Redazione del contenuto completo sia in Italiano che in Inglese:
   - Titolare del trattamento e contatti (`devsmapo@gmail.com`)
   - Natura dei dati trattati e principio di minimizzazione
   - Spiegazione dei permessi Android (`POST_NOTIFICATIONS`, `INTERNET`)
   - Servizi terzi interpellati (Open Food Facts, USDA)
   - Archiviazione e sicurezza (database locale Room SQLite)
   - Procedure di cancellazione dati
   - Note e istruzioni rapide per l'attivazione di GitHub Pages nel repository.
