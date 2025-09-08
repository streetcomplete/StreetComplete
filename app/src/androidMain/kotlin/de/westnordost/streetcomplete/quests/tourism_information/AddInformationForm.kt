package de.westnordost.streetcomplete.quests.tourism_information

import android.os.Bundle
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm
import de.westnordost.streetcomplete.quests.smoothness.Smoothness
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconRightItem

class AddInformationForm : AImageListQuestComposeForm<TourismInformation, TourismInformation>() {

    override val itemsPerRow = 2

    override val items = TourismInformation.entries.map { it.asItem() }
    override val itemContent =
        @androidx.compose.runtime.Composable { item: ImageListItem<TourismInformation>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconItem(
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
