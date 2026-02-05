package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

sealed interface RoadNameAnswer {
    data object NoName : RoadNameAnswer
}

data class RoadName(val localizedNames: List<LocalizedName>) : RoadNameAnswer


