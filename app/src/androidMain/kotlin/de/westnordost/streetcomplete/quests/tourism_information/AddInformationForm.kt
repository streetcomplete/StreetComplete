package de.westnordost.streetcomplete.quests.tourism_information

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell

class AddInformationForm : AImageListQuestForm<TourismInformation, TourismInformation>() {

    override val itemsPerRow = 2

    override val items = TourismInformation.entries.map { it.asItem() }

    override val itemContent =
        @Composable { item: ImageListItem<TourismInformation>, index: Int, onClick: () -> Unit, role: Role ->
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

    override fun onClickOk(selectedItems: List<TourismInformation>) {
        applyAnswer(selectedItems.single())
    }
}
