package de.westnordost.streetcomplete.quests.recycling_glass

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.ANY
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.BOTTLES

class DetermineRecyclingGlassForm : AbstractQuestAnswerFragment<RecyclingGlass>() {
    override val contentLayoutResId = R.layout.quest_determine_recycling_glass_explanation

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_recycling_type_any_glass) { applyAnswer(ANY) },
        AnswerItem(R.string.quest_recycling_type_glass_bottles_short) { applyAnswer(BOTTLES) }
    )
}
