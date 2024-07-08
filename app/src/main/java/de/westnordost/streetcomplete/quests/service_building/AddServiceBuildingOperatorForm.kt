package de.westnordost.streetcomplete.quests.service_building

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm
import de.westnordost.streetcomplete.util.takeFavourites

class AddServiceBuildingOperatorForm : ANameWithSuggestionsForm<String>() {

    // make proper list like ATM operators?
    override val suggestions: List<String> get() = (lastPickedAnswers + OPERATORS).distinct()

    override fun onClickOk() {
        prefs.addLastPicked(javaClass.simpleName, name!!)
        applyAnswer(name!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameInput.showDropDown()
    }

    private val lastPickedAnswers by lazy {
        prefs.getLastPicked(javaClass.simpleName).takeFavourites(50, 50, 1)
    }
}

private val OPERATORS = listOf(
    "Wiener Netze", "Wien Energie", "Wienstrom", "EVN", "Netz Niederösterreich GmbH", "Netz OÖ",
    "Salzburg AG", "KNG-Kärnten Netz GmbH", "Energie Steiermark",
    "ÖBB", "GKB", "Wiener Linien",
    "e.on", "DPMB",
)
