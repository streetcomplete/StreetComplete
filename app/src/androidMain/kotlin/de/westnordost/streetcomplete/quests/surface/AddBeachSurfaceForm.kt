package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBeachSurfaceForm : AItemSelectQuestForm<Surface, Surface>() {

    override val items get() = listOf(
        Surface.SAND,
        Surface.PEBBLES,
        Surface.GRAVEL,
        Surface.GRASS,
        Surface.ROCK,
        Surface.FINE_GRAVEL,
        Surface.GROUND
    )

    override val itemsPerRow = 3
    override val serializer = serializer<Surface>()

    @Composable
    override fun ItemContent(item: Surface) {
        ImageWithLabel(
            item.icon?.let { painterResource(it) },
            stringResource(item.title)
        )
    }

    override fun onClickOk(selectedItem: Surface) {
        applyAnswer(selectedItem)
    }
}
