package de.westnordost.streetcomplete.quests.crossing_markings

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddCrossingMarkingsForm : AImageListQuestForm<CrossingMarkings, CrossingMarkings>() {

    override val items = CrossingMarkings.entries
        .filter { it!=CrossingMarkings.YES }
        .map { it.asItem() }

    override val itemsPerRow = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select
    }

    override fun onClickOk(selectedItems: List<CrossingMarkings>) {
        applyAnswer(selectedItems.single())
    }
}
