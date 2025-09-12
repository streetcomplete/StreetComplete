package de.westnordost.streetcomplete.quests.police_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPoliceTypeForm : AImageListQuestForm<PoliceType, PoliceType>() {

    override val items = PoliceType.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: PoliceType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<PoliceType>) {
        applyAnswer(selectedItems.single())
    }
}
