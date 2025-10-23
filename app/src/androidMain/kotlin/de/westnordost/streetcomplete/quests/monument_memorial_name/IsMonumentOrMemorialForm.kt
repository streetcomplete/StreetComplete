package de.westnordost.streetcomplete.quests.monument_memorial_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.localized_name.confirmNoName
import de.westnordost.streetcomplete.view.localized_name.showKeyboardInfo

class IsMonumentOrMemorialForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = listOf(
        // True if memorial, this will initiate a change in IsMonumentOrMemorial
        AnswerItem(R.string.quest_is_monument_or_memorial_option_memorial) { applyAnswer(true) },
        AnswerItem(R.string.quest_is_monument_or_memorial_option_monument) { applyAnswer(false) }
    )
}
