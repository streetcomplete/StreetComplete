package de.westnordost.streetcomplete.quests.trail_visibility

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddTrailVisibilityForm : AImageListQuestForm<TrailVisibility, TrailVisibility>() {

    override val items: List<DisplayItem<TrailVisibility>> get() = listOf(
        TrailVisibility.EXCELLENT,
        TrailVisibility.GOOD,
        TrailVisibility.INTERMEDIATE,
        TrailVisibility.BAD,
        TrailVisibility.HORRIBLE,
        TrailVisibility.NO
    ).toItems()

    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_trail_visibility
    }

    override fun onClickOk(selectedItems: List<TrailVisibility>) {
        applyAnswer(selectedItems.first())
    }
}
