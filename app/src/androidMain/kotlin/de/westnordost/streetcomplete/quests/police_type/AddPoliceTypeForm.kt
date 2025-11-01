package de.westnordost.streetcomplete.quests.police_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPoliceTypeForm : AItemSelectQuestForm<PoliceType, PoliceType>() {

    override val items = PoliceType.entries
    override val itemsPerRow = 3
    override val serializer = serializer<PoliceType>()

    @Composable override fun ItemContent(item: PoliceType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: PoliceType) {
        applyAnswer(selectedItem)
    }
}
