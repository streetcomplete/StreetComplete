package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddRoadSurfaceForm : AImageListQuestForm<Surface, Surface>() {
    override val items get() = Surface.selectableValuesForWays.toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        val surface = selectedItems.single()
        applyAnswer(surface)
    }
}
