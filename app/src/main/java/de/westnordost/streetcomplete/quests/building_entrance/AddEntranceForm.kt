package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.EMERGENCY_EXIT
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.EXIT
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.GARAGE
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.MAIN
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.SECONDARY
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.SERVICE
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.SHOP
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.GENERIC
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddEntranceForm : AImageListQuestAnswerFragment<EntranceAnswer, EntranceAnswer>() {

    override val items: List<DisplayItem<EntranceAnswer>> = listOf(MAIN, SECONDARY, SERVICE,
        GARAGE, EMERGENCY_EXIT, EXIT, SHOP, GENERIC, DeadEnd).toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<EntranceAnswer>) {
        applyAnswer(selectedItems.single())
    }
}
