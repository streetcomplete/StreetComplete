package de.westnordost.streetcomplete.screens.about

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

abstract class CreditsViewModel : ViewModel() {
    abstract val credits: StateFlow<Credits?>
}

data class Credits(
    val mainContributors: List<Contributor>,
    val codeContributors: List<Contributor>,
    val projectsContributors: List<String>,
    val artContributors: List<String>,
    /** language -> sorted list of translators*/
    val translators: Map<String, List<String>>,
)

@Serializable
data class Contributor(
    val name: String,
    val githubUsername: String? = null,
    val linesOfCodeChanged: Int = 0,
    val linesOfInterfaceMarkupChanged: Int = 0,
    val assetFilesChanged: Int = 0
) {
    val githubLink: String get() = "https://github.com/$githubUsername"
}

class CreditsViewModelImpl(private val resources: Resources) : CreditsViewModel() {
    override val credits = MutableStateFlow<Credits?>(null)

    init {
        launch(Dispatchers.IO) {
            val mainContributors = readMainContributors()
            credits.value = Credits(
                mainContributors = mainContributors,
                codeContributors = readCodeContributors(mainContributors.map { it.githubUsername }),
                projectsContributors = readProjectsContributors(),
                artContributors = readArtContributors(),
                translators = readTranslators()
            )
        }
    }

    private fun readMainContributors(): List<Contributor> =
        resources.getYamlObject<List<Contributor>>(R.raw.credits_main)

    private fun readArtContributors(): List<String> =
        resources.getYamlObject<List<String>>(R.raw.credits_art)

    private fun readProjectsContributors(): List<String> =
        resources.getYamlObject<List<String>>(R.raw.credits_projects)

    private fun readCodeContributors(skipUsers: List<String?>): List<Contributor> =
        resources
            .getYamlObject<List<Contributor>>(R.raw.credits_contributors)
            .filter { it.githubUsername !in skipUsers && it.score >= 50 }
            .sortedByDescending { it.score }

    private fun readTranslators(): Map<String, List<String>> {
        val translatorsByLanguage =
            resources.getYamlObject<MutableMap<String, MutableMap<String, Int>>>(R.raw.credits_translators)

        // skip plain English. That's not a translation
        translatorsByLanguage.remove("en")

        // skip those translators who contributed less than 2% of the translation
        for (contributors in translatorsByLanguage.values) {
            val totalTranslated = contributors.values.sum()
            val removedAnyone = contributors.values.removeAll { 100 * it / totalTranslated < 2 }
            if (removedAnyone) {
                contributors["…"] = 1
            }
        }

        return translatorsByLanguage.mapValues { (_, translators) ->
            translators.entries.sortedByDescending { it.value }.map { it.key }
        }
    }
}

private val Contributor.score: Int get() =
    linesOfCodeChanged + linesOfInterfaceMarkupChanged / 5 + assetFilesChanged * 15
