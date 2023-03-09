package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.SELECTABLE_PITCH_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddPitchSurfaceForm : AImageListQuestForm<Surface, SurfaceAndNote>() {
    override val items get() = SELECTABLE_PITCH_SURFACES.toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        val value = selectedItems.single()
        collectSurfaceDescriptionIfNecessary(requireContext(), value) {
            applyAnswer(SurfaceAndNote(value, it))
        }
    }
}
