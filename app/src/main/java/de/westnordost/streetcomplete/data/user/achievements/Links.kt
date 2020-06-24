package de.westnordost.streetcomplete.data.user.achievements

data class Link(val id: String,
                val url: String,
                val title: String,
                val category: LinkCategory,
                val icon: Int? = null,
                val description: Int? = null)

enum class LinkCategory {
    INTRO,
    EDITORS,
    MAPS,
    SHOWCASE,
    GOODIES
}

