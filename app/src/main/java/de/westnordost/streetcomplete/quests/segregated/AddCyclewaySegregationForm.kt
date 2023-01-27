package de.westnordost.streetcomplete.quests.segregated

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddCyclewaySegregationForm : AImageListQuestForm<CyclewaySegregation, CyclewaySegregation>() {

    override val items get() =
        listOf(CyclewaySegregation(true), CyclewaySegregation(false))
            .map { it.asItem(countryInfo.isLeftHandTraffic) }

    override val itemsPerRow = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_right
    }

    override fun onClickOk(selectedItems: List<CyclewaySegregation>) {
        applyAnswer(selectedItems.single())
    }
}
