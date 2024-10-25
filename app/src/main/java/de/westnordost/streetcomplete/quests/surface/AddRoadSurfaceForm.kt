package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.SELECTABLE_WAY_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddRoadSurfaceForm : AImageListQuestForm<Surface, Surface>() {
    override val items get() = SELECTABLE_WAY_SURFACES.toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        val surface = selectedItems.single()
        applyAnswer(surface)
    }
}
