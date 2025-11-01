package de.westnordost.streetcomplete.quests.monument_memorial_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.localized_name.confirmNoName
import de.westnordost.streetcomplete.view.localized_name.showKeyboardInfo

class AddMonumentNameForm : AAddLocalizedNameForm<MonumentNameAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) {
            confirmNoName(requireContext()) { applyAnswer(MonumentNameAnswer.NoName) }
        },
        AnswerItem(R.string.quest_streetName_answer_cantType) {
            showKeyboardInfo(requireContext())
        }
    )

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(MonumentName(names))
    }
}
