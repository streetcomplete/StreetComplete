package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

sealed interface RoadNameAnswer {
    data object NoName : RoadNameAnswer
    data object IsServiceRoad : RoadNameAnswer
    data object IsTrack : RoadNameAnswer
    data object IsLinkRoad : RoadNameAnswer
}

data class RoadName(val localizedNames: List<LocalizedName>) : RoadNameAnswer


