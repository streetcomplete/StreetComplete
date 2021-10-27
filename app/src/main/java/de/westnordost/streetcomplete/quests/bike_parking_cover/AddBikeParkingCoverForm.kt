package de.westnordost.streetcomplete.quests.bike_parking_cover

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class AddBikeParkingCoverForm : AbstractQuestAnswerFragment<Boolean>() {

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes, ways with amenity = bicycle_parking").forEach {
            putMarker(it, R.drawable.ic_pin_bicycle_parking)
        }
    }

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )
}
