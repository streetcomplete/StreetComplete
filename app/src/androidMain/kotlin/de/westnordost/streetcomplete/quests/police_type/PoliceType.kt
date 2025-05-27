package de.westnordost.streetcomplete.quests.police_type

enum class PoliceType(val operatorName: String, val wikidata: String) {
    CARABINIERI("Arma dei Carabinieri", "Q54852"),
    POLIZIA_DI_STATO("Polizia di Stato", "Q897817"),
    GUARDIA_DI_FINANZA("Guardia di Finanza", "Q1552861"),
    POLIZIA_MUNICIPALE("Polizia Municipale", "Q1431981"),
    POLIZIA_LOCALE("Polizia Locale", "Q61634147"),
    GUARDIA_COSTIERA("Guardia Costiera", "Q1552839")
}
