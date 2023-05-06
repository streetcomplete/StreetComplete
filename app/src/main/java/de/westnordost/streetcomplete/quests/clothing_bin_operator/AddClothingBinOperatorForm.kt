package de.westnordost.streetcomplete.quests.clothing_bin_operator

import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddClothingBinOperatorForm : ANameWithSuggestionsForm<String>() {

    override val suggestions: List<String>? get() = countryInfo.clothesContainerOperators

    override fun onClickOk() {
        applyAnswer(name!!)
    }
}
