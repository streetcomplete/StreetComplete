package de.westnordost.streetcomplete.sanitary_dump_station

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddSanitaryDumpStationForm : AbstractOsmQuestForm<Boolean>() {

    override val contentLayoutResId = R.layout.quest_sanitary_dump_station

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )
}
