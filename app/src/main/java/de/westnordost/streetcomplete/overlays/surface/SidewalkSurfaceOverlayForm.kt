/*
package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.SidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.asItem
import de.westnordost.streetcomplete.osm.sidewalk.asStreetSideItem
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.quests.surface.asStreetSideItem
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class SidewalkSurfaceOverlayForm : AStreetSideSelectOverlayForm<Surface>() {
    private var currentSidewalk: LeftAndRightSidewalk? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        val sidewalk = createSidewalkSides(element.tags)
        currentSidewalk = sidewalk
        streetSideSelect.setPuzzleSide(sidewalk?.left?.asStreetSideItem(), false)
        streetSideSelect.setPuzzleSide(sidewalk?.right?.asStreetSideItem(), true)
    }

    override fun onClickSide(isRight: Boolean) {
        val items = listOf(Sidewalk.YES, Sidewalk.NO, Sidewalk.SEPARATE).mapNotNull { it.asItem() }
        ImageListPickerDialog(requireContext(), items, R.layout.cell_icon_select_with_label_below, 2) { item ->
            streetSideSelect.replacePuzzleSide(item.value!!.asStreetSideItem(resources)!!, isRight)
        }.show()
    }

    override fun onClickOk() {
        streetSideSelect.saveLastSelection()
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
            SidewalkSides(streetSideSelect.left!!.value, streetSideSelect.right!!.value).applyTo(it)
        }.create()))
    }

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != currentSidewalk?.left ||
            streetSideSelect.right?.value != currentSidewalk?.right

    override fun serialize(item: StreetSideDisplayItem<Surface>, isRight: Boolean) =
        item.value.name

    override fun deserialize(str: String, isRight: Boolean) =
        Surface.valueOf(str).asStreetSideItem(resources)
}
*/
