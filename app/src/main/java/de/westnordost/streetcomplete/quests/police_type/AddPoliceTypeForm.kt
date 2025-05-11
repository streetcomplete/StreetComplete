package de.westnordost.streetcomplete.quests.police_type

import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconItem

class AddPoliceTypeForm : AImageListQuestComposeForm<PoliceType, PoliceType>() {

    override val items = PoliceType.entries.map { it.asItem() }
    override val itemContent =
        @androidx.compose.runtime.Composable { item: ImageListItem<PoliceType>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconItem(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = onClick,
                    role = role
                )
            }
        }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PoliceType>) {
        applyAnswer(selectedItems.single())
    }
}
