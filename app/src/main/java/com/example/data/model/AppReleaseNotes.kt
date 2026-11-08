package com.example.data.model

data class ReleaseInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseDate: String,
    val highlightsIt: List<String>,
    val highlightsEn: List<String>
) {
    fun getLocalizedHighlights(lang: AppLanguage): List<String> {
        return if (lang == AppLanguage.IT) highlightsIt else highlightsEn
    }
}

object AppReleaseNotesProvider {

    val RELEASES: List<ReleaseInfo> = listOf(
        ReleaseInfo(
            versionCode = 12,
            versionName = "12.0",
            releaseDate = "Novembre 2026",
            highlightsIt = listOf(
                "Controllo automatico degli aggiornamenti all'avvio dell'app con panoramica delle novità",
                "Scorrimento automatico e mantenimento del focus sul pasto corrente dopo l'inserimento di un alimento",
                "Ottimizzazione grafica dell'editor dei piani con rimozione della barra superiore duplicata",
                "Schermata 'I miei piani' più pulita e diretta, senza card informative ingombranti",
                "Ottimizzazione delle dimensioni dell'app e velocizzazione dei caricamenti"
            ),
            highlightsEn = listOf(
                "Automatic update checks on startup with release highlights dialog",
                "Automatic focus and smooth scroll to active meal after adding food",
                "Streamlined plan editor removing duplicate top app bar",
                "Cleaned up plans screen without banner cards",
                "Optimized app bundle size and faster launch responsiveness"
            )
        ),
        ReleaseInfo(
            versionCode = 11,
            versionName = "11.0",
            releaseDate = "Novembre 2026",
            highlightsIt = listOf(
                "Controllo automatico degli aggiornamenti all'avvio dell'app con panoramica delle novità",
                "Scorrimento automatico e mantenimento del focus sul pasto corrente dopo l'inserimento di un alimento",
                "Ottimizzazione visiva dell'editor dei piani: rimossa la barra superiore duplicata per offrire maggiore spazio di lavoro",
                "Schermata 'I miei piani' più pulita e diretta, senza card informative ingombranti",
                "Miglioramenti alle prestazioni generali e alla reattività del database"
            ),
            highlightsEn = listOf(
                "Automatic check for updates on app launch with what's new highlights",
                "Automatic smooth scroll and focus retention on active meal after adding a food item",
                "Visual optimization of plan editor: removed duplicate top bar for a cleaner workspace",
                "Streamlined 'My Plans' screen without introductory banner cards",
                "General performance and database responsiveness improvements"
            )
        ),
        ReleaseInfo(
            versionCode = 10,
            versionName = "10.0",
            releaseDate = "Novembre 2026",
            highlightsIt = listOf(
                "Miglioramenti alla navigazione e rimozione della barra superiore in fase di modifica piano",
                "Ottimizzazione del riposizionamento nei pasti settimanali",
                "Raffinamento della visualizzazione delle categorie di alimenti"
            ),
            highlightsEn = listOf(
                "Navigation improvements and removed top bar during plan editing",
                "Optimized repositioning across weekly meals",
                "Refined food category visualization"
            )
        ),
        ReleaseInfo(
            versionCode = 9,
            versionName = "9.0",
            releaseDate = "Ottobre 2026",
            highlightsIt = listOf(
                "Duplicazione rapida ed eliminazione sicura dei piani tramite gesture di swipe",
                "Miglioramenti alla lista della spesa aggregata per corsie di supermercato",
                "Promemoria di scongelamento intelligente con notifiche di allarme"
            ),
            highlightsEn = listOf(
                "Fast duplication and safe deletion of meal plans via swipe gestures",
                "Enhanced shopping list aggregated by supermarket aisles",
                "Smart defrost reminders with background alarm notifications"
            )
        )
    )

    fun getLatestRelease(): ReleaseInfo = RELEASES.first()

    fun getReleaseInfo(versionCode: Int): ReleaseInfo {
        return RELEASES.firstOrNull { it.versionCode == versionCode }
            ?: if (versionCode > (RELEASES.firstOrNull()?.versionCode ?: 0)) {
                // Nuova versione futura non ancora inserita nel codice: mostra l'ultima nota disponibile
                getLatestRelease().copy(
                    versionCode = versionCode,
                    versionName = "$versionCode.0"
                )
            } else {
                getLatestRelease()
            }
    }
}
