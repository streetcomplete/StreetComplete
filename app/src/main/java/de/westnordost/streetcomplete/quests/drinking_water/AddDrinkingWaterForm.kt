package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.AListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.*

class AddDrinkingWaterForm : AListQuestAnswerFragment<DrinkingWater>() {

    override val items = listOf(
        TextItem(POTABLE_SIGNED, R.string.quest_drinking_water_potable_signed),
        TextItem(POTABLE_UNSIGNED, R.string.quest_drinking_water_potable_unsigned),
        TextItem(NOT_POTABLE_SIGNED, R.string.quest_drinking_water_not_potable_signed),
        TextItem(NOT_POTABLE_UNSIGNED, R.string.quest_drinking_water_not_potable_unsigned),
    )

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("""
        nodes with (
          man_made = water_tap
          or man_made = water_well
          or natural = spring
          or amenity = drinking_water
        )"""
        ).forEach {
            putMarker(it, R.drawable.ic_pin_water)
        }
    }
}
