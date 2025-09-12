package de.westnordost.streetcomplete.quests.bridge_structure

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import org.jetbrains.compose.resources.painterResource

class AddBridgeStructureForm : AImageListQuestForm<BridgeStructure, BridgeStructure>() {

    override val items = BridgeStructure.entries
    override val itemsPerRow = 2

    @Composable override fun BoxScope.ItemContent(item: BridgeStructure) {
        Image(painterResource(item.icon), null)
    }

    override fun onClickOk(selectedItems: List<BridgeStructure>) {
        applyAnswer(selectedItems.first())
    }
}
