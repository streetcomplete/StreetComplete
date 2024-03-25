package de.westnordost.streetcomplete.screens.about

import androidx.lifecycle.ViewModel
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
