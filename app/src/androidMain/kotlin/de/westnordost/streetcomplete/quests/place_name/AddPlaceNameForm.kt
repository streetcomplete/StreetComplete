package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.localized_name.confirmNoName
import de.westnordost.streetcomplete.view.localized_name.showKeyboardInfo

class AddPlaceNameForm : AAddLocalizedNameForm<PlaceNameAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) {
            confirmNoName(requireContext()) { applyAnswer(PlaceNameAnswer.NoNameSign) }
        },
        AnswerItem(R.string.quest_streetName_answer_cantType) {
            showKeyboardInfo(requireContext())
        }
    )

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(PlaceName(names))
    }
}
