package de.westnordost.streetcomplete.quests.bridge_structure

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource

class AddBridgeStructureForm : AItemSelectQuestForm<BridgeStructure, BridgeStructure>() {

    override val items = BridgeStructure.entries
    override val itemsPerRow = 1
    override val serializer = serializer<BridgeStructure>()

    @Composable override fun ItemContent(item: BridgeStructure) {
        Image(painterResource(item.icon), null)
    }

    override fun onClickOk(selectedItem: BridgeStructure) {
        applyAnswer(selectedItem)
    }
}
