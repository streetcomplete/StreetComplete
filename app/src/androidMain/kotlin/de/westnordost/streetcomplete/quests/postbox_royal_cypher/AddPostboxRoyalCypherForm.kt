package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPostboxRoyalCypherForm : AItemSelectQuestForm<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = PostboxRoyalCypher.entries
    override val itemsPerRow = 3
    override val serializer = serializer<PostboxRoyalCypher>()

    @Composable override fun ItemContent(item: PostboxRoyalCypher) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: PostboxRoyalCypher) {
        applyAnswer(selectedItem)
    }
}
