package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddPowerPolesMaterialForm : AImageListQuestAnswerFragment<PowerPolesMaterial, PowerPolesMaterial>() {

    override val items = listOf(
        Item(WOOD, R.drawable.power_pole_wood, R.string.quest_powerPolesMaterial_wood),
        Item(STEEL, R.drawable.power_pole_steel, R.string.quest_powerPolesMaterial_metal),
        Item(CONCRETE, R.drawable.power_pole_concrete, R.string.quest_powerPolesMaterial_concrete)
    )

    override val itemsPerRow = 3

    override suspend fun addInitialMapMarkers() {
        val mapData = getMapData(100.0)
        mapData.filter("nodes with power = pole or man_made = utility_pole").forEach {
            putMarker(it, R.drawable.ic_pin_power)
        }
        mapData.filter("ways with power ~ line|minor_line or communication = line or telecom = line").forEach {
            putMarker(it)
        }
    }

    override fun onClickOk(selectedItems: List<PowerPolesMaterial>) {
        applyAnswer(selectedItems.single())
    }
}
