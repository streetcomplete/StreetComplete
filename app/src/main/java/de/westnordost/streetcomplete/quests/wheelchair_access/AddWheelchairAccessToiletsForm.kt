package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.LIMITED
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.NO
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.YES

class AddWheelchairAccessToiletsForm : AbstractOsmQuestForm<WheelchairAccessToiletsAnswer>() {
    override val contentLayoutResId = R.layout.quest_wheelchair_toilets_explanation

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(WheelchairAccessToilets(NO)) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(WheelchairAccessToilets(YES)) },
        AnswerItem(R.string.quest_wheelchairAccess_limited) { applyAnswer(WheelchairAccessToilets(LIMITED)) },
    )

    override val otherAnswers get() = listOf(
        AnswerItem(R.string.quest_wheelchairAccessPat_noToilet) { applyAnswer(NoToilet) }
    )
}
