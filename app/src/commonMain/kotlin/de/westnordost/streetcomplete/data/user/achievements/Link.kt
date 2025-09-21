package de.westnordost.streetcomplete.data.user.achievements

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class Link(
    val id: String,
    val url: String,
    val title: String,
    val category: LinkCategory,
    val icon: DrawableResource? = null,
    val description: StringResource? = null
)

enum class LinkCategory {
    INTRO,
    EDITORS,
    MAPS,
    SHOWCASE,
    GOODIES
}
