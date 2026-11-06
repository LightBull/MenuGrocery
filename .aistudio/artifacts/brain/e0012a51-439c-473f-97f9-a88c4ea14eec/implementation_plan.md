# Piano di Risoluzione Errore di Pubblicazione Play Store

Risoluzione dell'errore di build artifact 404 durante il processo di pubblicazione Android tramite generazione del bundle AAB di release e avanzamento di versione conforme ai requisiti Play Console.

## Decisioni Confermate e Preferenze Utente

> [!IMPORTANT]
> - **Codice Versione (`versionCode`)**: Impostato a `9` (strettamente maggiore della versione 7 attualmente presente su Play Console e superiore a eventuali tentativi precedenti).
> - **Nome Versione (`versionName`)**: Impostato a `"9.0"`.
> - **Identificativo Applicazione (`applicationId`)**: Mantenuto rigorosamente a `"com.aistudio.mealplan.rkfpw"` (già registrato su Google Play Console).
> - **Namespace**: Mantenuto rigorosamente a `"com.example"`.
> - **Generazione Bundle**: Esecuzione del comando Gradle `:app:bundleRelease` per produrre il file richiesto `app/build/outputs/bundle/release/app-release.aab`.

---

## 1. Panoramica & Causa del Problema

- **Causa dell'errore**: Il sistema di pubblicazione automatico di AI Studio ha cercato l'archivio AAB del bundle in `/app/applet/app/build/outputs/bundle/release/app-release.aab`, riscontrando un codice HTTP 404 poiché finora era stato compilato soltanto l'APK di debug e non il bundle di rilascio (`bundleRelease`).
- **Soluzione**:
  1. Aggiornare `versionCode` a `9` e `versionName` a `"9.0"` nel file `app/build.gradle.kts`.
  2. Eseguire il comando di compilazione Gradle dedicato per i bundle di produzione: `gradle :app:bundleRelease`.
  3. Verificare l'effettiva presenza del file `app-release.aab` nel percorso di output atteso dal control plane.

---

## 2. Azioni di Configurazione e Build

```
┌────────────────────────────────────────────────────────┐
│                   app/build.gradle.kts                 │
│  - applicationId: "com.aistudio.mealplan.rkfpw" (Fisso)│
│  - versionCode: 9                                      │
│  - versionName: "9.0"                                  │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│               Task Gradle: :app:bundleRelease          │
│  - Firma con signingConfig release                     │
│  - Generazione archivio ottimizzato Android App Bundle │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│  app/build/outputs/bundle/release/app-release.aab      │
│  Pronto per l'acquisizione dal Play Publishing pipeline│
└────────────────────────────────────────────────────────┘
```

### File Interessati
- `app/build.gradle.kts`: aggiornamento di `versionCode` e `versionName`.
