package de.westnordost.streetcomplete.quests.self_service

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.*

class AddSelfServiceLaundryFragment : AbstractQuestAnswerFragment<SelfServiceLaundry>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_optional) { applyAnswer(OPTIONAL) },
        AnswerItem(R.string.quest_hasFeature_only) { applyAnswer(ONLY) }
    )
}
