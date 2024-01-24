package de.westnordost.streetcomplete.quests.tourism_information

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddInformationForm : AImageListQuestForm<TourismInformation, TourismInformation>() {

    override val itemsPerRow = 2

    override val items = TourismInformation.entries.map { it.asItem() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<TourismInformation>) {
        applyAnswer(selectedItems.single())
    }
}
