package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddServiceBuildingOperatorForm : ANameWithSuggestionsForm<String>() {

    // TODO: make proper list like ATM operators
    //  but how to extract from data? taginfo does not really help here
    override val suggestions: List<String> get() = OPERATORS

    override fun onClickOk() {
        applyAnswer(name)
    }
}

private val OPERATORS = listOf(
    "Wiener Netze", "Wien Energie", "Wienstrom", "EVN", "Netz Niederösterreich GmbH", "Netz OÖ",
    "Salzburg AG", "KNG-Kärnten Netz GmbH", "Energie Steiermark",
    "ÖBB", "GKB", "Wiener Linien",
    "e.on", "DPMB",
)
