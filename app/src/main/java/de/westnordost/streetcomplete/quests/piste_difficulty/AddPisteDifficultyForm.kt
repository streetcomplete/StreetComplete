package de.westnordost.streetcomplete.quests.piste_difficulty

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddPisteDifficultyForm : AImageListQuestForm<PisteDifficulty, PisteDifficulty>() {

    override val items get() = PisteDifficulty.values().mapNotNull { it.asItem(countryInfo.countryCode) }
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(selectedItems: List<PisteDifficulty>) {
        applyAnswer(selectedItems.single())
    }
}
