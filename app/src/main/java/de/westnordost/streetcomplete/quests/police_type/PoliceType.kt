package de.westnordost.streetcomplete.quests.police_type

enum class PoliceType(val policeName: String, val operator: PoliceOperator) {
    CARABINIERI("Arma dei Carabinieri", PoliceOperator.ARMA_DEI_CARABINIERI),
    GUARDIA_COSTIERA("Guardia Costiera", PoliceOperator.GUARDIA_COSTIERA),
    GUARDIA_DI_FINANZA("Guardia di Finanza", PoliceOperator.GUARDIA_DI_FINANZA),
    POLIZIA_DI_STATO("Polizia di Stato", PoliceOperator.POLIZIA_DI_STATO),
    POLIZIA_MUNICIPALE("Polizia Municipale", PoliceOperator.POLIZIA_MUNICIPALE),
    POLIZIA_LOCALE("Polizia Locale", PoliceOperator.POLIZIA_LOCALE),
    POLIZIA_STRADALE("Polizia Stradale", PoliceOperator.POLIZIA_DI_STATO),
    POLIZIA_FERROVIARIA("Polizia Ferroviaria", PoliceOperator.POLIZIA_DI_STATO)
}

enum class PoliceOperator(val operatorName: String, val wikidata: String, val wikipedia: String) {
    ARMA_DEI_CARABINIERI("Arma dei Carabinieri", "Q54852", "en:Carabinieri"),
    POLIZIA_DI_STATO("Polizia di Stato", "Q897817", "it:Polizia di Stato"),
    GUARDIA_DI_FINANZA("Guardia di Finanza", "Q1552861", "it:Guardia di Finanza"),
    POLIZIA_MUNICIPALE("Polizia Municipale", "Q1431981", "it:Polizia municipale"),
    POLIZIA_LOCALE("Polizia Locale", "Q61634147", "it:Polizia locale (Italia)"),
    GUARDIA_COSTIERA("Guardia Costiera","Q1552839", "it:Corpo delle capitanerie di porto - Guardia costiera")
}
