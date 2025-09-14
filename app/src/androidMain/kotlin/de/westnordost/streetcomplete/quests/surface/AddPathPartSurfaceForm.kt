package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPathPartSurfaceForm : AItemSelectQuestForm<Surface, Surface>() {
    override val items get() = Surface.selectableValuesForWays

    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: Surface) {
        val icon = item.icon
        if (icon != null) {
            ImageWithLabel(painterResource(icon), stringResource(item.title))
        }
    }

    override fun onClickOk(selectedItem: Surface) {
        applyAnswer(selectedItem)
    }
}
