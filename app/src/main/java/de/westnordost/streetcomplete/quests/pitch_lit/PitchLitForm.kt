package de.westnordost.streetcomplete.quests.pitch_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.pitch_lit.PitchLit.*
import de.westnordost.streetcomplete.quests.OtherAnswer

class PitchLitForm : AYesNoQuestAnswerFragment<PitchLit>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_way_lit_24_7) { applyAnswer(NIGHT_AND_DAY) },
        OtherAnswer(R.string.quest_way_lit_automatic) { applyAnswer(AUTOMATIC) }
    )

    override fun onClick(answer: Boolean) {
        applyAnswer(answer.toPitchLit())
    }
}
