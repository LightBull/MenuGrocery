# Piano di Risoluzione Notifiche Scongelamento & Pannello di Diagnostica

## Stato Implementazione: COMPLETATO ✅

### 1. Causa Radice Identificata e Risolta
- **Origine delle 4 notifiche:** Il piano predefinito iniziale ("Piano Benessere Mediterraneo") conteneva 14 alimenti contrassegnati con `toDefrost = true`. Martedì e mercoledì sera sono scattate le 4 notifiche di tali alimenti campione (Salmone, Petto di Pollo e Filetto di Merluzzo) perché gli allarmi `PendingIntent` erano rimasti registrati in `AlarmManager` di Android.
- **Risoluzione `AlarmManager`:** Aggiunto il tracciamento persistente di tutti gli ID allarme in `SharedPreferences` con metodo `cancelAllReminders(...)` per eliminare qualsiasi allarme orfano.
- **Risoluzione `DefrostAlarmReceiver`:**
  - Aggiunta la verifica rigorosa che il pasto appartenga al piano attivo E che il giorno del pasto (`meal.dayOfWeek`) coincida esattamente con il giorno di domani. Se non coincidono, l'allarme viene scartato e ripianificato per la data corretta senza disturbare l'utente.
  - Rimosso l'invio della notifica come fallback nel blocco `catch (e: Exception)`.
  - Aggiunta la ripianificazione settimanale automatica (`scheduleDefrostReminder` per la settimana successiva) al termine della notifica andata a buon fine.

### 2. Sincronizzazione Automatica
- All'avvio dell'app (`MealPlanViewModel` init e `initializeDefaultDataIfNeeded`), al cambio del piano attivo (`activatePlan`), e al salvataggio del piano (`saveFullPlan`), viene eseguita automaticamente `DefrostScheduler.resyncAllActiveDefrostReminders`.
- Vengono eliminati tutti i vecchi allarmi e risincronizzati esclusivamente gli alimenti con `toDefrost == true` del piano attualmente attivo.

### 3. Pannello di Diagnostica UI ("Diagnostica Scongelamento")
- Aggiunta la schermata modale `DefrostDiagnosticsBottomSheet.kt` raggiungibile sia da:
  1. **"I Miei Piani"**: Pulsante "Diagnostica Scongelamento & Notifiche" posizionato in cima alla schermata.
  2. **"Piano Pasti"**: Icona a campana/notifica accanto al pulsante "Cambia" piano nella card dell'intestazione.
- Funzionalità del pannello:
  - Riepilogo del piano attivo e numero di alimenti da scongelare.
  - Lista dettagliata degli alimenti con giorno, pasto associato e momento esatto in cui scatta la notifica (sera prima alle ore 20:00).
  - Pulsante **"Ripulisci e Risincronizza Allarmi"**: cancella tutti gli allarmi dal sistema operativo e ripianifica solo quelli del piano attivo, con messaggio di feedback.
  - Pulsante **"Invia Notifica di Test Istantanea"**: emette subito una notifica di prova per verificare audio e vibrazione.
