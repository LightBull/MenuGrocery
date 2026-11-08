# Piano di Implementazione & Note di Rilascio (Patch Notes)

## 1. Risoluzione Problema Sovrascrittura Dati & Travaso Vettoriale
È stato risolto il problema della sovrascrittura accidentale dei dati dell'utente durante gli aggiornamenti dell'app:
1. **Travaso automatico con auto-embedding**: Quando lo schema del database viene aggiornato (introduzione di nuove colonne o nuove tabelle come `food_embeddings`), i dati presenti nel backup locale vengono riversati nel nuovo database SQLite valorizzando i campi mancanti con stringhe vuote o progressivi d'ordine, e generando contestualmente il vettore di embedding normalizzato a 64 dimensioni per ciascun alimento travasato.
2. **Blocco inserimento demo**: In `MealPlanRepository.initializeDefaultDataIfNeeded()`, se sono presenti backup utente nella cartella dell'app, l'inserimento del piano demo predefinito ("Piano Benessere Mediterraneo") viene bloccato e viene eseguito automaticamente il ripristino dall'ultimo backup disponibile.
3. **Pannello Backup & Ripristino in Impostazioni**: Aggiunta la sezione "Backup & Ripristino Dati" in `SettingsScreen` per visualizzare tutti i backup salvati su disco, creare nuovi backup manuali e forzare il ripristino di qualsiasi backup storico con finestra di conferma.
4. **Eliminazione alimento consentita per l'unico cibo del pasto o piano**: In `PlanEditorScreen`, è ora possibile eliminare un alimento anche se è l'unico nel pasto o nell'intero piano, mostrando uno stato vuoto chiaro ("Nessun alimento in questo pasto. Tocca '+' per aggiungerne uno.").
5. **Pulizia Header Lista Pasti**: Rimossa l'icona della diagnostica scongelamento dall'header di `DailyMealPlanScreen` (ora situata unicamente in Impostazioni).

---

## 2. Note della Patch (Changelog)

### Patch v10.0 (Versione Corrente)
- **Salvaguardia e Ripristino Dati Utente**: Implementato il backup preventivo automatico e il travaso nel nuovo schema Room.
- **Predisposizione Modello Vettoriale ONNX**: Creata la tabella `food_embeddings` e il servizio `OnnxVectorService` (onnxruntime-android) per calcolare vettori di alimenti, categorie, quantità e cibi correlati; generazione degli embedding durante il travaso.
- **Pannello Backup & Ripristino**: Sezione in Impostazioni per visualizzare la lista dei backup JSON locali, creare nuovi backup e ripristinare i propri dati storici.
- **Percorsi Database in Chiaro**: Aggiunti in Impostazioni i percorsi assoluti sia del database principale dei piani (`mealplan_database`), sia della cartella di backup (`/files/backups/`), sia del database alimenti (`FoodDb.db`).
- **Nuove Gesture in "I Miei Piani"**:
  - Click sulla riga per accedere direttamente alla modifica (rimossa l'icona della matita).
  - Swipe a destra (50%) per duplicare il piano con richiesta di conferma.
  - Swipe a sinistra (50%) per eliminare il piano con richiesta di conferma contenente lo stato del piano (attivo/disattivo/unico piano).
  - Rimozione completa del pulsante "Ricrea Database".
- **Spostamento Diagnostica Scongelamento**: Spostata la diagnostica esclusivamente all'interno della schermata Impostazioni.
- **Auto-scroll Lista Pasti**: All'apertura della schermata "Lista Pasti", la vista scorre fluidamente al pasto più vicino da consumare se la distanza temporale è $\le 2$ ore.
- **Eliminazione cibo singolo**: Consentita l'eliminazione dell'alimento anche se è l'unico alimento nel pasto o nell'intero piano.
- **Disabilitazione Creazione Automatica APK**: Rimosse le routine di copia automatica di file `.apk` nelle directory del workspace.

---

### Patch v9.0 (Versione Precedente)
- **Gestione Piani Multipli**: Supporto per più piani alimentari e attivazione dinamica del piano corrente (BR-01).
- **Lista della Spesa Intelligente**: Raggruppamento per categoria e unità di misura, unione per ingredienti compatibili (BR-02).
- **Notifiche & Promemoria di Scongelamento**: Schedulazione allarmi nel sistema operativo per cibi con flag di congelamento attivo (BR-03).
- **Integrazione Alimenti FoodDb**: Consultazione integrata delle tabelle alimentari di riferimento in sola lettura.
