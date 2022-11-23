package de.westnordost.streetcomplete.overlays.cycleway

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.asDialogItem
import de.westnordost.streetcomplete.osm.cycleway.asStreetSideItem
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.getSelectableCycleways
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class StreetCyclewayOverlayForm : AStreetSideSelectOverlayForm<Cycleway>() {

    // TODO bicycle road!

    private var currentCycleway: LeftAndRightCycleway? = null

    /** returns whether the side that goes into the opposite direction of the driving direction of a
     * one-way is on the right side of the way */
    private val isReverseSideRight get() = isReversedOneway xor isLeftHandTraffic

    private val isOneway get() = isOneway(element!!.tags)
    private val isReversedOneway get() = isReversedOneway(element!!.tags)

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    private fun isContraflowInOneway(isRight: Boolean): Boolean =
        isOneway && (isReverseSideRight == isRight)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        currentCycleway = createCyclewaySides(element!!.tags, isLeftHandTraffic)?.selectableOrNullValues(countryInfo)

        val leftItem = currentCycleway?.left?.asStreetSideItem(countryInfo, isContraflowInOneway(false))
        streetSideSelect.setPuzzleSide(leftItem, false)

        val rightItem = currentCycleway?.right?.asStreetSideItem(countryInfo, isContraflowInOneway(true))
        streetSideSelect.setPuzzleSide(rightItem, true)
    }

    override fun onClickSide(isRight: Boolean) {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        val dialogItems = getSelectableCycleways(countryInfo, element!!.tags, isRight)
            .map { it.asDialogItem(requireContext(), countryInfo, isContraflowInOneway) }

        ImageListPickerDialog(requireContext(), dialogItems, R.layout.labeled_icon_button_cell, 2) { item ->
            val streetSideItem = item.value!!.asStreetSideItem(countryInfo, isContraflowInOneway)
            streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
        }.show()
    }

    override fun onClickOk() {
        val cycleways = LeftAndRightCycleway(streetSideSelect.left?.value, streetSideSelect.right?.value)
        if (cycleways.wasNoOnewayForCyclistsButNowItIs(element!!.tags, isLeftHandTraffic)) {
            confirmNotOnewayForCyclists {
                saveAndApply(cycleways)
            }
        } else {
            saveAndApply(cycleways)
        }
    }

    private fun saveAndApply(cycleways: LeftAndRightCycleway) {
        streetSideSelect.saveLastSelection()
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        cycleways.applyTo(tagChanges, countryInfo.isLeftHandTraffic)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
    }

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != currentCycleway?.left  ||
        streetSideSelect.right?.value != currentCycleway?.right

    override fun serialize(item: Cycleway) =  item.name
    override fun deserialize(str: String) = Cycleway.valueOf(str)
    override fun asStreetSideItem(item: Cycleway, isRight: Boolean): StreetSideDisplayItem<Cycleway> {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        // NONE_NO_ONEWAY is displayed as simply NONE if not in contraflow because the former makes
        // only really sense in contraflow. This can only happen when applying the side(s) via the
        // last answer button
        val item2 = if (item == Cycleway.NONE_NO_ONEWAY && !isContraflowInOneway) Cycleway.NONE else item
        return item2.asStreetSideItem(countryInfo, isContraflowInOneway)
    }

    private fun confirmNotOnewayForCyclists(callback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_cycleway_confirmation_oneway_for_cyclists_too)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
