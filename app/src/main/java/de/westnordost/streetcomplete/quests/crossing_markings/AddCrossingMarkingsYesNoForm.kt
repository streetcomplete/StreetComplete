package de.westnordost.streetcomplete.quests.crossing_markings

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddCrossingMarkingsYesNoForm : AbstractOsmQuestForm<CrossingMarkings>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(CrossingMarkings.NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(CrossingMarkings.YES) }
    )
}
