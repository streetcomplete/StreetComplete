package de.westnordost.streetcomplete.quests.valves

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.asItem
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddValvesForm : AImageListQuestForm<Valves, List<Valves>>() {

    override val items get() = Valves.entries.map { it.asItem() }
    override val itemsPerRow = 2
    override val maxSelectableItems = -1
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<Valves>) {
        applyAnswer(selectedItems)
    }
}
