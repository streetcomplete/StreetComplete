package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddPathPartSurfaceForm : AImageListQuestComposeForm<Surface, Surface>() {
    override val items get() = Surface.selectableValuesForWays.toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        applyAnswer(selectedItems.single())
    }
}
