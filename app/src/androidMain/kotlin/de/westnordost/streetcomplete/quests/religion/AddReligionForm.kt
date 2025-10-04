package de.westnordost.streetcomplete.quests.religion

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.religion.Religion.MULTIFAITH
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddReligionForm : AItemSelectQuestForm<Religion, Religion>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_religion_for_place_of_worship_answer_multi) { applyAnswer(MULTIFAITH) }
    )

    override val items get() = (Religion.entries - MULTIFAITH)
        .sortedBy { religionPosition(it.osmValue) }

    fun religionPosition(osmValue: String): Int {
        val position = countryInfo.popularReligions.indexOf(osmValue)
        if (position < 0) {
            // not present at all in config, so should be put at the end
            return Integer.MAX_VALUE
        }
        return position
    }

    @Composable override fun ItemContent(item: Religion) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: Religion) {
        applyAnswer(selectedItem)
    }
}
