package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.Item

class RemoveWrongSurfaceForm : AImageListQuestForm<WrongSurfaceType, WrongSurfaceAnswer>() {
    override val items = listOf(
        Item(WrongSurfaceType.WRONG_TRACKTYPE, R.drawable.surface_unpaved, R.string.quest_surface_value_unpaved_tracktype_grade1_is_wrong),
        Item(WrongSurfaceType.WRONG_SURFACE_MATERIAL, R.drawable.surface_paved, R.string.quest_surface_value_paved_tracktype_grade1_is_correct),
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<WrongSurfaceType>) {
        if(selectedItems.single() == WrongSurfaceType.WRONG_TRACKTYPE) {
            applyAnswer(TracktypeIsWrong())
        }
        if(selectedItems.single() == WrongSurfaceType.WRONG_SURFACE_MATERIAL) {
            applyAnswer(SpecificSurfaceIsWrong())
        }
    }
}
