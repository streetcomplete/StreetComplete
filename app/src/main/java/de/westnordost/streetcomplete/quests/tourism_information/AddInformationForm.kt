package de.westnordost.streetcomplete.quests.tourism_information

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.BOARD
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.GUIDEPOST
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.MAP
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.OFFICE
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.TERMINAL
import de.westnordost.streetcomplete.view.image_select.Item

class AddInformationForm : AImageListQuestForm<TourismInformation, TourismInformation>() {

    override val itemsPerRow = 2

    override val items get() = listOf(
        Item(OFFICE, R.drawable.tourism_information_office, R.string.quest_tourism_information_office),
        Item(BOARD, R.drawable.tourism_information_board, R.string.quest_tourism_information_board),
        Item(TERMINAL, R.drawable.tourism_information_terminal, R.string.quest_tourism_information_terminal),
        Item(MAP, R.drawable.tourism_information_map, R.string.quest_tourism_information_map),
        Item(GUIDEPOST, R.drawable.tourism_information_guidepost, R.string.quest_tourism_information_guidepost)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<TourismInformation>) {
        applyAnswer(selectedItems.single())
    }
}
