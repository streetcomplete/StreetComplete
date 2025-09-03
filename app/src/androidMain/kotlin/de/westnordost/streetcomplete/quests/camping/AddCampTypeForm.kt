package de.westnordost.streetcomplete.quests.camping

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.camping.CampType.BACKCOUNTRY
import de.westnordost.streetcomplete.quests.camping.CampType.CARAVANS_ONLY
import de.westnordost.streetcomplete.quests.camping.CampType.TENTS_AND_CARAVANS
import de.westnordost.streetcomplete.quests.camping.CampType.TENTS_ONLY
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_camp_type_caravans_only
import de.westnordost.streetcomplete.resources.quest_camp_type_tents_and_caravans
import de.westnordost.streetcomplete.resources.quest_camp_type_tents_only
import de.westnordost.streetcomplete.ui.common.TextItem

class AddCampTypeForm : AListQuestForm<CampType>() {

    override val items = listOf(
        TextItem(TENTS_AND_CARAVANS, Res.string.quest_camp_type_tents_and_caravans),
        TextItem(TENTS_ONLY, Res.string.quest_camp_type_tents_only),
        TextItem(CARAVANS_ONLY, Res.string.quest_camp_type_caravans_only),
    )

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_camp_type_backcountry) { applyAnswer(BACKCOUNTRY) },
    )
}
