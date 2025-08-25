package de.westnordost.streetcomplete.quests.roof_shape

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.MANY
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell

class AddRoofShapeForm : AImageListQuestForm<RoofShape, RoofShape>() {

    override val itemContent =
        @Composable { item: ImageListItem<RoofShape>, index: Int, onClick: () -> Unit, role: Role ->
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
        AnswerItem(R.string.quest_roofShape_answer_many) { applyAnswer(MANY) }
    )

    override val items = RoofShape.entries.mapNotNull { it.asItem() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<RoofShape>) {
        applyAnswer(selectedItems.single())
    }
}
