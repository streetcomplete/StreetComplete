package de.westnordost.streetcomplete.quests.police_type

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell

class AddPoliceTypeForm : AImageListQuestForm<PoliceType, PoliceType>() {

    override val items = PoliceType.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override val itemContent =
        @Composable { item: ImageListItem<PoliceType>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconCell(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = onClick,
                    role = role
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<PoliceType>) {
        applyAnswer(selectedItems.single())
    }
}
