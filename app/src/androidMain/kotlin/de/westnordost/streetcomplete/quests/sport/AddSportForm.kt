package de.westnordost.streetcomplete.quests.sport

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.sport.Sport.MULTI
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell

class AddSportForm : AImageListQuestForm<Sport, List<Sport>>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sport_answer_multi) { applyMultiAnswer() }
    )

    override val items get() = Sport.entries
        .mapNotNull { it.asItem() }
        .sortedBy { sportPosition(it.value!!.osmValue) }

    override val itemContent =
        @Composable { item: ImageListItem<Sport>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconCell(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = onClick,
                    role = role
                )
            }
        }

    override val maxSelectableItems = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<Sport>) {
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
        applyAnswer(listOf(MULTI))
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
