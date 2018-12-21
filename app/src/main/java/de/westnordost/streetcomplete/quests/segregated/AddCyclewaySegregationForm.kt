package de.westnordost.streetcomplete.quests.segregated

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddCyclewaySegregationForm : ImageListQuestAnswerFragment() {

    override fun getItems() = arrayOf(
        Item("yes",
            if (countryInfo.isLeftHandTraffic) R.drawable.ic_path_segregated_l else R.drawable.ic_path_segregated,
            R.string.quest_segregated_separated),
        Item("no", R.drawable.ic_path_segregated_no, R.string.quest_segregated_mixed)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageSelector.setCellLayout(R.layout.cell_labeled_icon_select_right)
    }

    override fun getItemsPerRow() = 2
}
