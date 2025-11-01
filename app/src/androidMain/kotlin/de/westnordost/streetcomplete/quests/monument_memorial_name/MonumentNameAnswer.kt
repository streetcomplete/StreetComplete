package de.westnordost.streetcomplete.quests.monument_memorial_name

import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

sealed interface MonumentNameAnswer {
    data object NoName : MonumentNameAnswer
}

data class MonumentName(val localizedNames: List<LocalizedName>) : MonumentNameAnswer
