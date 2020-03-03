package de.westnordost.streetcomplete.quests.tourism_information

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddInformationForm : AImageListQuestAnswerFragment<String,String>() {

    override val itemsPerRow = 2

    override val items get() = listOf(
        Item("office", R.drawable.tourism_information_office, R.string.quest_tourism_information_office),
        Item("board", R.drawable.tourism_information_board, R.string.quest_tourism_information_board),
        Item("terminal", R.drawable.tourism_information_terminal, R.string.quest_tourism_information_terminal),
        Item("map", R.drawable.tourism_information_map, R.string.quest_tourism_information_map),
        Item("guidepost", R.drawable.tourism_information_guidepost, R.string.quest_tourism_information_guidepost)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
