package de.westnordost.streetcomplete.quests.segregated

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconRightItem

class AddCyclewaySegregationForm : AImageListQuestComposeForm<CyclewaySegregation, CyclewaySegregation>() {

    override val items get() =
        CyclewaySegregation.entries.map { it.asItem(countryInfo.isLeftHandTraffic) }

    override val itemContent =
        @Composable { item: ImageListItem<CyclewaySegregation>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconRightItem(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = onClick,
                    role = role
                )
            }
        }

    override val itemsPerRow = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<CyclewaySegregation>) {
        applyAnswer(selectedItems.single())
    }
}
