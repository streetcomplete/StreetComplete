package de.westnordost.streetcomplete.quests.atm_operator

import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddAtmOperatorForm : ANameWithSuggestionsForm<String>() {

    override val suggestions: List<String>? get() = countryInfo.atmOperators

    override fun onClickOk() {
        applyAnswer(name!!)
    }
}
