package de.westnordost.streetcomplete.overlays.sidewalk

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.asItem
import de.westnordost.streetcomplete.osm.sidewalk.asStreetSideItem
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk.validOrNullValues
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class SidewalkOverlayForm : AStreetSideSelectOverlayForm<Sidewalk>() {

    private var originalSidewalk: LeftAndRightSidewalk? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalSidewalk = parseSidewalkSides(element!!.tags)?.validOrNullValues()
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        streetSideSelect.setPuzzleSide(originalSidewalk?.left?.asStreetSideItem(), false)
        streetSideSelect.setPuzzleSide(originalSidewalk?.right?.asStreetSideItem(), true)
    }

    override fun onClickSide(isRight: Boolean) {
        val items = listOf(YES, NO, SEPARATE).mapNotNull { it.asItem() }
        ImageListPickerDialog(requireContext(), items, R.layout.cell_icon_select_with_label_below, 2) { item ->
            streetSideSelect.replacePuzzleSide(item.value!!.asStreetSideItem()!!, isRight)
        }.show()
    }

    override fun onClickOk() {
        streetSideSelect.saveLastSelection()
        val sidewalks = LeftAndRightSidewalk(streetSideSelect.left?.value, streetSideSelect.right?.value)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        sidewalks.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != originalSidewalk?.left ||
        streetSideSelect.right?.value != originalSidewalk?.right

    override fun serialize(item: Sidewalk) = item.name
    override fun deserialize(str: String) = Sidewalk.valueOf(str)
    override fun asStreetSideItem(item: Sidewalk, isRight: Boolean) = item.asStreetSideItem()!!
}
