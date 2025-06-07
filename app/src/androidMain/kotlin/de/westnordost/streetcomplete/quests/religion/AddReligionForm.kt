package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.religion.Religion.MULTIFAITH
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell

class AddReligionForm : AImageListQuestForm<Religion, Religion>() {

    override val itemContent =
        @Composable { item: ImageListItem<Religion>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconCell(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = onClick,
                    role = role
                )
            }
        }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_religion_for_place_of_worship_answer_multi) { applyAnswer(MULTIFAITH) }
    )

    override val items get() = Religion.entries
        .mapNotNull { it.asItem() }
        .sortedBy { religionPosition(it.value!!.osmValue) }

    fun religionPosition(osmValue: String): Int {
        val position = countryInfo.popularReligions.indexOf(osmValue)
        if (position < 0) {
            // not present at all in config, so should be put at the end
            return Integer.MAX_VALUE
        }
        return position
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<Religion>) {
        applyAnswer(selectedItems.single())
    }
}
