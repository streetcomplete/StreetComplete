package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.FLUSH
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.KERB_RAMP
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.LOWERED
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.NO_KERB
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.RAISED
import de.westnordost.streetcomplete.view.image_select.Item

class AddKerbHeightForm : AImageListQuestForm<KerbHeight, KerbHeight>() {

    override val items = listOf(
        Item(RAISED, R.drawable.kerb_height_raised, R.string.quest_kerb_height_raised),
        Item(LOWERED, R.drawable.kerb_height_lowered, R.string.quest_kerb_height_lowered),
        Item(FLUSH, R.drawable.kerb_height_flush, R.string.quest_kerb_height_flush),
        Item(KERB_RAMP, R.drawable.kerb_height_lowered_ramp, R.string.quest_kerb_height_lowered_ramp),
        Item(NO_KERB, R.drawable.kerb_height_no, R.string.quest_kerb_height_no)
    )

    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<KerbHeight>) {
        applyAnswer(selectedItems.single())
    }
}
