package de.westnordost.streetcomplete.overlays.cycleway

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_boulevard.applyTo
import de.westnordost.streetcomplete.osm.bicycle_boulevard.createBicycleBoulevard
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.Direction
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.asDialogItem
import de.westnordost.streetcomplete.osm.cycleway.asStreetSideItem
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.getDefault
import de.westnordost.streetcomplete.osm.cycleway.getSelectableCycleways
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.isInContraflowOfOneway
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.AnswerItem2
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StreetCyclewayOverlayForm : AStreetSideSelectOverlayForm<CyclewayAndDirection>() {

    override val otherAnswers: List<IAnswerItem> get() =
        listOfNotNull(
            createSwitchBicycleBoulevardAnswer(),
            createReverseCyclewayDirectionAnswer()
        )

    private var originalCycleway: LeftAndRightCycleway? = null
    private var originalBicycleBoulevard: BicycleBoulevard = BicycleBoulevard.NO
    private var bicycleBoulevard: BicycleBoulevard = BicycleBoulevard.NO
    private var reverseDirection: Boolean = false

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    private fun isContraflowInOneway(isRight: Boolean): Boolean {
        val direction = streetSideSelect.getPuzzleSide(isRight)?.value?.direction
            ?: Direction.getDefault(isRight, isLeftHandTraffic)
        return isInContraflowOfOneway(element!!.tags, direction)
    }

    /* ---------------------------------------- lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalCycleway = createCyclewaySides(element!!.tags, isLeftHandTraffic)?.selectableOrNullValues(countryInfo)
        originalBicycleBoulevard = createBicycleBoulevard(element!!.tags)

        if (savedInstanceState == null) {
            initStateFromTags()
        } else {
            savedInstanceState.getString(BICYCLE_BOULEVARD)?.let {
                bicycleBoulevard = BicycleBoulevard.valueOf(it)
            }
        }
        updateBicycleBoulevard()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BICYCLE_BOULEVARD, bicycleBoulevard.name)
    }

    private fun initStateFromTags() {
        bicycleBoulevard = originalBicycleBoulevard

        val leftItem = originalCycleway?.left?.asStreetSideItem(false, isContraflowInOneway(false), countryInfo)
        streetSideSelect.setPuzzleSide(leftItem, false)

        val rightItem = originalCycleway?.right?.asStreetSideItem(true, isContraflowInOneway(true), countryInfo)
        streetSideSelect.setPuzzleSide(rightItem, true)
    }

    /* ----------------------------------- bicycle boulevards ----------------------------------- */

    private fun createSwitchBicycleBoulevardAnswer(): IAnswerItem? =
        if (bicycleBoulevard == BicycleBoulevard.YES) {
            AnswerItem2(
                getString(R.string.bicycle_boulevard_is_not_a, getString(R.string.bicycle_boulevard)),
                ::removeBicycleBoulevard
            )
        } else if (countryInfo.hasBicycleBoulevard) {
            AnswerItem2(
                getString(R.string.bicycle_boulevard_is_a, getString(R.string.bicycle_boulevard)),
                ::addBicycleBoulevard
            )
        } else {
            null
        }

    private fun removeBicycleBoulevard() {
        bicycleBoulevard = BicycleBoulevard.NO
        updateBicycleBoulevard()
    }

    private fun addBicycleBoulevard() {
        bicycleBoulevard = BicycleBoulevard.YES
        updateBicycleBoulevard()
    }

    private fun updateBicycleBoulevard() {
        val bicycleBoulevardSignView = requireView().findViewById<View>(R.id.signBicycleBoulevard)
        if (bicycleBoulevard == BicycleBoulevard.YES) {
            if (bicycleBoulevardSignView == null) {
                layoutInflater.inflate(
                    R.layout.sign_bicycle_boulevard,
                    requireView().findViewById(R.id.content), true
                )
            }
        } else {
            (bicycleBoulevardSignView?.parent as? ViewGroup)?.removeView(bicycleBoulevardSignView)
        }
        checkIsFormComplete()
    }

    /* ------------------------------ reverse cycleway direction -------------------------------- */

    private fun createReverseCyclewayDirectionAnswer(): IAnswerItem? =
        if (bicycleBoulevard == BicycleBoulevard.YES) null
        else AnswerItem(R.string.cycleway_reverse_direction, ::selectReverseCyclewayDirection)

    private fun selectReverseCyclewayDirection() {
        confirmSelectReverseCyclewayDirection {
            reverseDirection = true
            context?.toast(R.string.cycleway_reverse_direction_toast)
        }
    }

    private fun confirmSelectReverseCyclewayDirection(callback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.cycleway_reverse_direction_warning)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    private fun reverseCyclewayDirection(isRight: Boolean) {
        reverseDirection = false
        val value = streetSideSelect.getPuzzleSide(isRight)?.value ?: return
        val newValue = value.copy(direction = value.direction.reverse())
        val newItem = newValue.asStreetSideItem(isRight, isContraflowInOneway(isRight), countryInfo)
        streetSideSelect.replacePuzzleSide(newItem, isRight)
    }

    /* --------------------------------- select & apply answer ---------------------------------- */

    override fun onClickSide(isRight: Boolean) {
        if (reverseDirection) {
            reverseCyclewayDirection(isRight)
        } else {
            selectCycleway(isRight)
        }
    }

    private fun selectCycleway(isRight: Boolean) {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        val direction = streetSideSelect.getPuzzleSide(isRight)?.value?.direction
            ?: Direction.getDefault(isRight, isLeftHandTraffic)
        val dialogItems = getSelectableCycleways(countryInfo, element!!.tags, direction)
            .map { it.asDialogItem(isRight, isContraflowInOneway, requireContext(), countryInfo) }

        ImageListPickerDialog(requireContext(), dialogItems, R.layout.labeled_icon_button_cell, 2) { item ->
            val streetSideItem = item.value!!.asStreetSideItem(isRight, isContraflowInOneway, countryInfo)
            streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
        }.show()
    }

    override fun onClickOk() {
        if (bicycleBoulevard == BicycleBoulevard.YES) {
            val tags = StringMapChangesBuilder(element!!.tags)
            bicycleBoulevard.applyTo(tags, countryInfo.countryCode)
            applyEdit(UpdateElementTagsAction(tags.create()))
        } else {
            // only tag the cycleway if that is what is currently displayed
            val cycleways = LeftAndRightCycleway(streetSideSelect.left?.value, streetSideSelect.right?.value)
            if (cycleways.wasNoOnewayForCyclistsButNowItIs(element!!.tags, isLeftHandTraffic)) {
                confirmNotOnewayForCyclists { saveAndApplyCycleway(cycleways) }
            } else {
                saveAndApplyCycleway(cycleways)
            }
        }
    }

    private fun confirmNotOnewayForCyclists(callback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_cycleway_confirmation_oneway_for_cyclists_too)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    private fun saveAndApplyCycleway(cycleways: LeftAndRightCycleway) {
        streetSideSelect.saveLastSelection()
        val tags = StringMapChangesBuilder(element!!.tags)
        cycleways.applyTo(tags, countryInfo.isLeftHandTraffic)
        bicycleBoulevard.applyTo(tags, countryInfo.countryCode)
        applyEdit(UpdateElementTagsAction(tags.create()))
    }

    /* ----------------------------- AStreetSideSelectOverlayForm ------------------------------- */

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != originalCycleway?.left  ||
        streetSideSelect.right?.value != originalCycleway?.right ||
        originalBicycleBoulevard != bicycleBoulevard

    override fun serialize(item: CyclewayAndDirection) = Json.encodeToString(item)
    override fun deserialize(str: String) = Json.decodeFromString<CyclewayAndDirection>(str)
    override fun asStreetSideItem(item: CyclewayAndDirection, isRight: Boolean): StreetSideDisplayItem<CyclewayAndDirection> {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        // NONE_NO_ONEWAY is displayed as simply NONE if not in contraflow because the former makes
        // only really sense in contraflow. This can only happen when applying the side(s) via the
        // last answer button
        val item2 = if (item.cycleway == Cycleway.NONE_NO_ONEWAY && !isContraflowInOneway) item.copy(cycleway = Cycleway.NONE) else item
        return item2.asStreetSideItem(isRight, isContraflowInOneway, countryInfo)
    }

    companion object {
        private const val BICYCLE_BOULEVARD = "bicycle_boulevard"
    }
}
