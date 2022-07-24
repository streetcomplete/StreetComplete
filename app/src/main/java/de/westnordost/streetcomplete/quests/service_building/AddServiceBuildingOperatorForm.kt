package de.westnordost.streetcomplete.quests.service_building

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.mostCommonWithin

class AddServiceBuildingOperatorForm : ANameWithSuggestionsForm<String>() {

    // TODO: make proper list like ATM operators
    //  but how to extract from data? taginfo does not really help here
    override val suggestions: List<String> get() = (lastPickedAnswers + OPERATORS).distinct()

    override fun onClickOk() {
        favs.add(name)
        applyAnswer(name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameInput.showDropDown()
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
            maxEntries = 10
        )
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 10, historyCount = 15, first = 1)
            .toList()
    }
}

private val OPERATORS = listOf(
    "Wiener Netze", "Wien Energie", "Wienstrom", "EVN", "Netz Niederösterreich GmbH", "Netz OÖ",
    "Salzburg AG", "KNG-Kärnten Netz GmbH", "Energie Steiermark",
    "ÖBB", "GKB", "Wiener Linien",
    "e.on", "DPMB",
)
