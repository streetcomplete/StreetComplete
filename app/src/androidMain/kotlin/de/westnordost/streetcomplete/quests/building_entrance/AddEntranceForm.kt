package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.EMERGENCY_EXIT
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.EXIT
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.GENERIC
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.MAIN
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.SERVICE
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.SHOP
import de.westnordost.streetcomplete.quests.building_entrance.EntranceExistsAnswer.STAIRCASE
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_building_entrance_dead_end
import de.westnordost.streetcomplete.resources.quest_building_entrance_emergency_exit
import de.westnordost.streetcomplete.resources.quest_building_entrance_exit
import de.westnordost.streetcomplete.resources.quest_building_entrance_main
import de.westnordost.streetcomplete.resources.quest_building_entrance_service
import de.westnordost.streetcomplete.resources.quest_building_entrance_shop
import de.westnordost.streetcomplete.resources.quest_building_entrance_staircase
import de.westnordost.streetcomplete.resources.quest_building_entrance_yes
import de.westnordost.streetcomplete.ui.common.TextItem

class AddEntranceForm : AListQuestForm<EntranceAnswer>() {
    override val items: List<TextItem<EntranceAnswer>> = listOf(
        TextItem(MAIN, Res.string.quest_building_entrance_main),
        TextItem(STAIRCASE, Res.string.quest_building_entrance_staircase),
        TextItem(SERVICE, Res.string.quest_building_entrance_service),
        TextItem(EXIT, Res.string.quest_building_entrance_exit),
        TextItem(EMERGENCY_EXIT, Res.string.quest_building_entrance_emergency_exit),
        TextItem(SHOP, Res.string.quest_building_entrance_shop),
        TextItem(GENERIC, Res.string.quest_building_entrance_yes),
        TextItem(DeadEnd, Res.string.quest_building_entrance_dead_end),
    )
}
