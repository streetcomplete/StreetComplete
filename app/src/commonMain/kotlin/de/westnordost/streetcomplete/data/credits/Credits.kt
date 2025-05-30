package de.westnordost.streetcomplete.data.credits

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readYaml
import kotlinx.serialization.Serializable

data class Credits(
    val mainContributors: List<Contributor>,
    val codeContributors: List<Contributor>,
    val projectsContributors: List<String>,
    val artContributors: List<String>,
    /** language code -> (translator name -> translated count) */
    val translatorsByLanguage: Map<String, Map<String, Int>>,
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

/** Read app credits from various files. */
suspend fun Res.readCredits(): Credits {
    val mainContributors = readYaml<List<Contributor>>("files/credits_main.yml")
    val mainContributorNames = mainContributors.mapNotNull { it.githubUsername }.toSet()

    return Credits(
        mainContributors = mainContributors,
        codeContributors = readYaml<List<Contributor>>("files/credits_contributors.yml")
            .filter { it.githubUsername !in mainContributorNames },
        projectsContributors = readYaml("files/credits_projects.yml"),
        artContributors = readYaml("files/credits_art.yml"),
        translatorsByLanguage = readYaml("files/credits_translators.yml"),
    )
}
