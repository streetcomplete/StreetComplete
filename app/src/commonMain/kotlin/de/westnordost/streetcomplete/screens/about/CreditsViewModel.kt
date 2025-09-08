package de.westnordost.streetcomplete.screens.about

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.credits.Contributor
import de.westnordost.streetcomplete.data.credits.Credits
import de.westnordost.streetcomplete.data.credits.readCredits
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class CreditsViewModel : ViewModel() {
    abstract val credits: StateFlow<Credits?>
}

@Stable
class CreditsViewModelImpl(private val res: Res) : CreditsViewModel() {
    override val credits = MutableStateFlow<Credits?>(null)

    init {
        launch {
            val c = res.readCredits()
            credits.value = c.copy(
                // only show code contributors that have contributed more than a few lines of code
                codeContributors = c.codeContributors
                    .filter { it.score >= 50 }
                    .sortedByDescending { it.score },

                // only show translators that have contributed more than a few words
                translatorsByLanguage = c.translatorsByLanguage.mapValues { (_, translators) ->
                    val totalTranslated = translators.values.sum()
                    val mainTranslators = translators.toMutableMap()
                    val removedAnyone = mainTranslators.entries
                        .removeAll { 100 * it.value / totalTranslated < 2 }
                    if (removedAnyone) mainTranslators["…"] = 0
                    mainTranslators
                }
            )
        }
    }
}

/** a very rough measure how time-intensive is to contribute what */
private val Contributor.score: Int get() =
    linesOfCodeChanged + linesOfInterfaceMarkupChanged / 5 + assetFilesChanged * 15
