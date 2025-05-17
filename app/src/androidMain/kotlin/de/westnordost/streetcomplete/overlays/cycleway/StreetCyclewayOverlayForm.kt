package de.westnordost.streetcomplete.overlays.cycleway

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.Direction
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_boulevard.applyTo
import de.westnordost.streetcomplete.osm.bicycle_boulevard.parseBicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.applyTo
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.parseBicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.asDialogItem
import de.westnordost.streetcomplete.osm.cycleway.asStreetSideItem
import de.westnordost.streetcomplete.osm.cycleway.getSelectableCycleways
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StreetCyclewayOverlayForm : AStreetSideSelectOverlayForm<CyclewayAndDirection>() {

    override val contentLayoutResId = R.layout.fragment_overlay_cycleway

    override val otherAnswers: List<IAnswerItem> get() =
        createSwitchBicycleInPedestrianZoneAnswers() +
        listOfNotNull(
            createSwitchBicycleBoulevardAnswer(),
            createReverseCyclewayDirectionAnswer()
        )

    private var originalCycleway: LeftAndRightCycleway? = null
    private var originalBicycleBoulevard: BicycleBoulevard = BicycleBoulevard.NO
    private var originalBicycleInPedestrianStreet: BicycleInPedestrianStreet? = null
    private var bicycleBoulevard: BicycleBoulevard = BicycleBoulevard.NO
    private var bicycleInPedestrianStreet: BicycleInPedestrianStreet? = null
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

        val tags = element!!.tags
        originalCycleway = parseCyclewaySides(tags, isLeftHandTraffic)?.selectableOrNullValues(countryInfo)
        originalBicycleBoulevard = parseBicycleBoulevard(tags)
        originalBicycleInPedestrianStreet = parseBicycleInPedestrianStreet(tags)

        if (savedInstanceState == null) {
            initStateFromTags()
        } else {
            onLoadInstanceState(savedInstanceState)
        }
        updateStreetSign()

        streetSideSelect.transformLastSelection = { item: CyclewayAndDirection, isRight: Boolean ->
            if (item.direction == Direction.BOTH) {
                item
            } else {
                CyclewayAndDirection(item.cycleway, Direction.getDefault(isRight, isLeftHandTraffic))
            }
        }
    }

    private fun onLoadInstanceState(state: Bundle) {
        bicycleBoulevard = state.getString(BICYCLE_BOULEVARD)
            ?.let { BicycleBoulevard.valueOf(it) }
            ?: BicycleBoulevard.NO
        bicycleInPedestrianStreet = state.getString(BICYCLE_IN_PEDESTRIAN_STREET)
            ?.let { BicycleInPedestrianStreet.valueOf(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BICYCLE_BOULEVARD, bicycleBoulevard.name)
        outState.putString(BICYCLE_IN_PEDESTRIAN_STREET, bicycleInPedestrianStreet?.name)
    }

    private fun initStateFromTags() {
        bicycleBoulevard = originalBicycleBoulevard
        bicycleInPedestrianStreet = originalBicycleInPedestrianStreet

        val leftItem = originalCycleway?.left?.asStreetSideItem(false, isContraflowInOneway(false), countryInfo)
        streetSideSelect.setPuzzleSide(leftItem, false)

        val rightItem = originalCycleway?.right?.asStreetSideItem(true, isContraflowInOneway(true), countryInfo)
        streetSideSelect.setPuzzleSide(rightItem, true)
    }

    /* ------------------------- pedestrian zone and bicycle boulevards ------------------------- */

    private fun createSwitchBicycleInPedestrianZoneAnswers(): List<IAnswerItem> {
        if (bicycleInPedestrianStreet == null) return listOf()

        return listOfNotNull(
            AnswerItem(R.string.pedestrian_zone_designated) {
                bicycleInPedestrianStreet = BicycleInPedestrianStreet.DESIGNATED
                updateStreetSign()
            }.takeIf { bicycleInPedestrianStreet != BicycleInPedestrianStreet.DESIGNATED },

            AnswerItem(R.string.pedestrian_zone_allowed_sign) {
                bicycleInPedestrianStreet = BicycleInPedestrianStreet.ALLOWED
                updateStreetSign()
            }.takeIf { bicycleInPedestrianStreet != BicycleInPedestrianStreet.ALLOWED },

            AnswerItem(R.string.pedestrian_zone_no_sign) {
                bicycleInPedestrianStreet = BicycleInPedestrianStreet.NOT_SIGNED
                updateStreetSign()
            }.takeIf { bicycleInPedestrianStreet != BicycleInPedestrianStreet.NOT_SIGNED }
        )
    }

    private fun createSwitchBicycleBoulevardAnswer(): IAnswerItem? =
        when (bicycleBoulevard) {
            BicycleBoulevard.YES ->
                AnswerItem2(getString(R.string.bicycle_boulevard_is_not_a, getString(R.string.bicycle_boulevard))) {
                    bicycleBoulevard = BicycleBoulevard.NO
                    updateStreetSign()
                }
            BicycleBoulevard.NO ->
                // don't allow pedestrian roads to be tagged as bicycle roads (should rather be
                // highway=pedestrian + bicycle=designated rather than bicycle_road=yes)
                if (element!!.tags["highway"] != "pedestrian") {
                    AnswerItem2(getString(R.string.bicycle_boulevard_is_a, getString(R.string.bicycle_boulevard))) {
                        bicycleBoulevard = BicycleBoulevard.YES
                        updateStreetSign()
                    }
                } else {
                    null
                }
        }

    private fun updateStreetSign() {
        val signContainer = requireView().findViewById<ViewGroup>(R.id.signContainer)
        signContainer.removeAllViews()

        if (bicycleInPedestrianStreet == BicycleInPedestrianStreet.ALLOWED) {
            layoutInflater.inflate(R.layout.sign_bicycles_ok, signContainer, true)
        } else if (bicycleInPedestrianStreet == BicycleInPedestrianStreet.DESIGNATED) {
            layoutInflater.inflate(R.layout.sign_bicycle_and_pedestrians, signContainer, true)
        } else if (bicycleBoulevard == BicycleBoulevard.YES) {
            layoutInflater.inflate(R.layout.sign_bicycle_boulevard, signContainer, true)
        }
        checkIsFormComplete()
    }

    /* ------------------------------ reverse cycleway direction -------------------------------- */

    private fun createReverseCyclewayDirectionAnswer(): IAnswerItem =
        AnswerItem(R.string.cycleway_reverse_direction, ::selectReverseCyclewayDirection)

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
        val dialogItems = getSelectableCycleways(countryInfo, element!!.tags, isRight, isLeftHandTraffic, direction)
            .map { it.asDialogItem(isRight, isContraflowInOneway, requireContext(), countryInfo) }

        ImageListPickerDialog(requireContext(), dialogItems, R.layout.labeled_icon_button_cell, 2) { item ->
            val streetSideItem = item.value!!.asStreetSideItem(isRight, isContraflowInOneway, countryInfo)
            streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
        }.show()
    }

    override fun onClickOk() {
        val cycleways = LeftAndRightCycleway(streetSideSelect.left?.value, streetSideSelect.right?.value)
        if (cycleways.wasNoOnewayForCyclistsButNowItIs(element!!.tags, isLeftHandTraffic)) {
            confirmNotOnewayForCyclists { saveAndApplyCycleway(cycleways) }
        } else {
            saveAndApplyCycleway(cycleways)
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
        bicycleInPedestrianStreet?.applyTo(tags)
        applyEdit(UpdateElementTagsAction(element!!, tags.create()))
    }

    /* ----------------------------- AStreetSideSelectOverlayForm ------------------------------- */

    override fun isFormComplete() =
        streetSideSelect.left != null ||
        streetSideSelect.right != null ||
        originalBicycleBoulevard != bicycleBoulevard ||
        originalBicycleInPedestrianStreet != bicycleInPedestrianStreet

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != originalCycleway?.left ||
        streetSideSelect.right?.value != originalCycleway?.right ||
        originalBicycleBoulevard != bicycleBoulevard ||
        originalBicycleInPedestrianStreet != bicycleInPedestrianStreet

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
        private const val BICYCLE_IN_PEDESTRIAN_STREET = "bicycle_in_pedestrian_street"
    }
}
