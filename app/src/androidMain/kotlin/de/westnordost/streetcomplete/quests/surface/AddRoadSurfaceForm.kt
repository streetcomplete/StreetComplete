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

class AddRoadSurfaceForm : AItemSelectQuestForm<Surface, Surface>() {
    override val items get() = Surface.selectableValuesForWays
    override val itemsPerRow = 3
    override val serializer = serializer<Surface>()

    @Composable override fun ItemContent(item: Surface) {
        ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
    }

    override fun onClickOk(selectedItem: Surface) {
        applyAnswer(selectedItem)
    }
}
