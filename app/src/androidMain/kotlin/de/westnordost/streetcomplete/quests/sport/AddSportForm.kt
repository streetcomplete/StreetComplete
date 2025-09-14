package de.westnordost.streetcomplete.quests.sport

import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.sport.Sport.MULTI
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddSportForm : AItemsSelectQuestForm<Sport, Set<Sport>>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sport_answer_multi) { applyMultiAnswer() }
    )

    override val items get() = (Sport.entries - MULTI).sortedBy { sportPosition(it.osmValue) }

    @Composable override fun ItemContent(item: Sport) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: Set<Sport>) {
        if (selectedItems.size > 3) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_sport_manySports_confirmation_title)
                .setMessage(R.string.quest_sport_manySports_confirmation_description)
                .setPositiveButton(R.string.quest_manySports_confirmation_specific) { _, _ -> applyAnswer(selectedItems) }
                .setNegativeButton(R.string.quest_manySports_confirmation_generic) { _, _ -> applyMultiAnswer() }
                .show()
        } else {
            applyAnswer(selectedItems)
        }
    }

    private fun applyMultiAnswer() {
        applyAnswer(setOf(MULTI))
    }

    private fun sportPosition(osmValue: String): Int {
        val position = countryInfo.popularSports.indexOf(osmValue)
        if (position < 0) {
            // not present at all in config, so should be put at the end
            return Integer.MAX_VALUE
        }
        return position
    }
}
