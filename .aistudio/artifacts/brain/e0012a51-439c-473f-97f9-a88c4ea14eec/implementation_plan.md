# Note di Rilascio v5.0 per Google Play Console

Predisposizione del testo ufficiale delle note di rilascio ("Novità di questa versione" / "What's new in this release") per la versione **5.0 (versionCode 5)** di **MealPlan** su Google Play Console, formulate in lingua **italiana** e **inglese**, rispettando rigidamente il vincolo massimo di **500 caratteri per lingua** imposto da Google Play.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Specifiche confermate:**
> - **Formato:** Sintetico per Google Play Console (entro 500 caratteri per lingua).
> - **Lingue:** Bilingue (Italiano e Inglese).
> - **Novità coperte:**
>   1. Persistenza immediata a database dello stato degli alimenti presi nella Lista Spesa (nessuna perdita di stato durante la navigazione).
>   2. Nuovo layout card compatto con badge dei giorni (`L, Ma, Me...`) affiancati alle quantità.
>   3. Risoluzione notifiche di scongelamento orfane e nuovo pannello di diagnostica allarmi nel menu.
>   4. Miglioramenti di stabilità e conformità alle normative sulla privacy.

---

### 1. Overview & Core Concept

Fornire allo sviluppatore i testi pronti per il copia-incolla nella scheda di rilascio della traccia di produzione o test di Google Play Console nei tag standard `<it-IT>` ed `<en-US>`.

---

### 2. Contenuto delle Note di Rilascio

#### Versione Italiana (`it-IT`)
- **Focus:**
  - 🛒 **Lista Spesa:** Risolto il salvataggio degli articoli spuntati: ora lo stato viene memorizzato istantaneamente a database e non si perde navigando tra le schede.
  - 📐 **Grafica più compatta:** I giorni della settimana sono ora posizionati accanto alle grammature, eliminando spazio superfluo.
  - ❄️ **Promemoria Scongelamento:** Notifiche ottimizzate (solo per cibi da scongelare nel piano attivo) e nuovo strumento di diagnostica allarmi.
  - ⚡ Miglioramenti generali di stabilità.
- **Conteggio caratteri previsto:** ~380 caratteri (ben al di sotto del limite di 500).

#### Versione Inglese (`en-US`)
- **Focus:**
  - 🛒 **Shopping List:** Checked items are now immediately saved to the local database—no progress lost when navigating the app.
  - 📐 **Compact View:** Weekday badges moved next to quantities for a cleaner, sleeker look.
  - ❄️ **Defrost Reminders:** Fixed notifications to only alert for active plan items + added new defrost diagnostics tool.
  - ⚡ General stability and performance improvements.
- **Conteggio caratteri previsto:** ~390 caratteri (ben al di sotto del limite di 500).

---

### 3. Step di Esecuzione

1. Generazione e presentazione formattata dei blocchi di testo pronti per Google Play Console (inclusi i tag XML di lingua facoltativi usati da Fastlane o inseribili nei campi dedicati della Console).
2. Verifica del conteggio esatto dei caratteri per garantire la conformità al 100% con Google Play.
