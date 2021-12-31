package de.westnordost.streetcomplete.quests.railway_electrification

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.railway_electrification.RailwayElectrificationSystem.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddRailwayElectrificationSystemForm :
    AImageListQuestAnswerFragment<RailwayElectrificationSystem, RailwayElectrificationSystem>()
{

    override val items = listOf(
        //TODO: Create and use proper graphics.
        Item(NO, R.drawable.ic_railway_crossing_none),
        Item(CONTACT_LINE, R.drawable.ic_railway_crossing_full),
        Item(THIRD_RAIL, R.drawable.ic_railway_crossing_half),
        Item(FOURTH_RAIL, R.drawable.ic_railway_crossing_double_half),
        Item(GROUND_LEVEL, R.drawable.ic_railway_crossing_gate),
    )

    override val itemsPerRow = 4

    override fun onClickOk(selectedItems: List<RailwayElectrificationSystem>) =
        applyAnswer(selectedItems.single())

}
