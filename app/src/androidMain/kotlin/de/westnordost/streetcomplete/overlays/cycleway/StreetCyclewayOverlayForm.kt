package de.westnordost.streetcomplete.overlays.cycleway

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_boulevard.applyTo
import de.westnordost.streetcomplete.osm.bicycle_boulevard.parseBicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.applyTo
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.parseBicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.cycleway.withDefaultDirection
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.AnswerItem2
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.quests.cycleway.BicycleBoulevardSign
import de.westnordost.streetcomplete.quests.cycleway.BicycleInPedestrianStreetAllowedSign
import de.westnordost.streetcomplete.quests.cycleway.BicycleInPedestrianStreetDesignatedSign
import de.westnordost.streetcomplete.quests.cycleway.CyclewayForm
import de.westnordost.streetcomplete.quests.cycleway.CyclewayFormSelectionMode
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.toast
import org.koin.android.ext.android.inject

class StreetCyclewayOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    override val contentPadding = false

    override val otherAnswers: List<IAnswerItem> get() =
        createSwitchBicycleInPedestrianZoneAnswers() +
        listOfNotNull(
            createReverseCyclewayDirectionAnswer(),
            createSwitchBicycleBoulevardAnswer(),
        )

    private var originalCycleway: Sides<CyclewayAndDirection> = Sides(null, null)
    private var originalBicycleBoulevard: BicycleBoulevard = BicycleBoulevard.NO
    private var originalBicycleInPedestrianStreet: BicycleInPedestrianStreet? = null

    private var cycleways = mutableStateOf(Sides<CyclewayAndDirection>(null, null))
    private var bicycleBoulevard = mutableStateOf(BicycleBoulevard.NO)
    private var bicycleInPedestrianStreet = mutableStateOf<BicycleInPedestrianStreet?>(null)
    private val selectionMode = mutableStateOf(CyclewayFormSelectionMode.SELECT)

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    /* ---------------------------------------- lifecycle --------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tags = element!!.tags
        originalCycleway = parseCyclewaySides(tags, isLeftHandTraffic)
            ?.selectableOrNullValues(countryInfo)
            ?: Sides(null, null)
        originalBicycleBoulevard = parseBicycleBoulevard(tags)
        originalBicycleInPedestrianStreet = parseBicycleInPedestrianStreet(tags)

        if (savedInstanceState == null) {
            bicycleBoulevard.value = originalBicycleBoulevard
            bicycleInPedestrianStreet.value = originalBicycleInPedestrianStreet
            cycleways.value = originalCycleway
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lastPicked by lazy {
            prefs
                .getLastPicked<Sides<Cycleway>>(this::class.simpleName!!)
                .map { it.withDefaultDirection(countryInfo.isLeftHandTraffic) }
        }

        binding.composeViewBase.content { Surface {
            Box(contentAlignment = Alignment.Center) {
                CyclewayForm(
                    value = cycleways.value,
                    onValueChanged = {
                        cycleways.value = it
                        selectionMode.value = CyclewayFormSelectionMode.SELECT
                        checkIsFormComplete()
                    },
                    selectionMode = selectionMode.value,
                    geometryRotation = geometryRotation.floatValue,
                    mapRotation = mapRotation.floatValue,
                    mapTilt = mapTilt.floatValue,
                    countryInfo = countryInfo,
                    roadDirection = Direction.from(element!!.tags),
                    lastPicked = lastPicked,
                    lastPickedContentPadding = PaddingValues(start = 48.dp, end = 56.dp),
                )
                Box(Modifier.scale(0.5f).alpha(0.75f)) {
                    if (bicycleInPedestrianStreet.value == BicycleInPedestrianStreet.ALLOWED) {
                        BicycleInPedestrianStreetAllowedSign()
                    } else if (bicycleInPedestrianStreet.value == BicycleInPedestrianStreet.DESIGNATED) {
                        BicycleInPedestrianStreetDesignatedSign()
                    } else if (bicycleBoulevard.value == BicycleBoulevard.YES) {
                        BicycleBoulevardSign()
                    }
                }
            }
        } }
    }

    /* ------------------------- pedestrian zone and bicycle boulevards ------------------------- */

    private fun createSwitchBicycleInPedestrianZoneAnswers(): List<IAnswerItem> = buildList {
        // only offer answers in pedestrian zones
        if (bicycleInPedestrianStreet.value == null) return@buildList

        if (bicycleInPedestrianStreet.value != BicycleInPedestrianStreet.DESIGNATED) {
            add(
                AnswerItem(R.string.pedestrian_zone_designated) {
                    bicycleInPedestrianStreet.value = BicycleInPedestrianStreet.DESIGNATED
                }
            )
        }
        if (bicycleInPedestrianStreet.value != BicycleInPedestrianStreet.ALLOWED) {
            add(
                AnswerItem(R.string.pedestrian_zone_allowed_sign) {
                    bicycleInPedestrianStreet.value = BicycleInPedestrianStreet.ALLOWED
                }
            )
        }
        if (bicycleInPedestrianStreet.value != BicycleInPedestrianStreet.NOT_SIGNED) {
            add(
                AnswerItem(R.string.pedestrian_zone_no_sign) {
                    bicycleInPedestrianStreet.value = BicycleInPedestrianStreet.NOT_SIGNED
                }
            )
        }
    }

    private fun createSwitchBicycleBoulevardAnswer(): IAnswerItem? =
        when (bicycleBoulevard.value) {
            BicycleBoulevard.YES ->
                AnswerItem2(getString(R.string.bicycle_boulevard_is_not_a, getString(R.string.bicycle_boulevard))) {
                    bicycleBoulevard.value = BicycleBoulevard.NO
                    checkIsFormComplete()
                }
            BicycleBoulevard.NO ->
                // don't allow pedestrian roads to be tagged as bicycle roads (should rather be
                // highway=pedestrian + bicycle=designated rather than bicycle_road=yes)
                if (element!!.tags["highway"] != "pedestrian") {
                    AnswerItem2(getString(R.string.bicycle_boulevard_is_a, getString(R.string.bicycle_boulevard))) {
                        bicycleBoulevard.value = BicycleBoulevard.YES
                        checkIsFormComplete()
                    }
                } else {
                    null
                }
        }

    /* ------------------------------ reverse cycleway direction -------------------------------- */

    private fun createReverseCyclewayDirectionAnswer(): IAnswerItem =
        AnswerItem(R.string.cycleway_reverse_direction, ::selectReverseCyclewayDirection)

    private fun selectReverseCyclewayDirection() {
        confirmSelectReverseCyclewayDirection {
            selectionMode.value = CyclewayFormSelectionMode.REVERSE
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

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun onClickOk() {
        if (cycleways.value.wasNoOnewayForCyclistsButNowItIs(element!!.tags, isLeftHandTraffic)) {
            confirmNotOnewayForCyclists { saveAndApplyCycleway(cycleways.value) }
        } else {
            saveAndApplyCycleway(cycleways.value)
        }
    }

    private fun confirmNotOnewayForCyclists(callback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_cycleway_confirmation_oneway_for_cyclists_too)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    private fun saveAndApplyCycleway(sides: Sides<CyclewayAndDirection>) {
        val tags = StringMapChangesBuilder(element!!.tags)
        sides.applyTo(tags, countryInfo.isLeftHandTraffic)
        bicycleBoulevard.value.applyTo(tags, countryInfo.countryCode)
        bicycleInPedestrianStreet.value?.applyTo(tags)
        if (sides.left != null && sides.right != null) {
            // only persist the cycleway selection, not the direction. For any road that deviates from
            // the default, the user should select this specifically. Simply carrying over the
            // non-default direction to the next answer might result in mistakes
            val cycleways = Sides(left = sides.left.cycleway, right = sides.right.cycleway)
            prefs.setLastPicked(this::class.simpleName!!, listOf(cycleways))
        }
        applyEdit(UpdateElementTagsAction(element!!, tags.create()))
    }

    override fun isFormComplete() =
        cycleways.value.left != null ||
        cycleways.value.right != null ||
        originalBicycleBoulevard != bicycleBoulevard.value ||
        originalBicycleInPedestrianStreet != bicycleInPedestrianStreet.value

    override fun hasChanges(): Boolean =
        cycleways.value.left != originalCycleway.left ||
        cycleways.value.right != originalCycleway.right ||
        originalBicycleBoulevard != bicycleBoulevard.value ||
        originalBicycleInPedestrianStreet != bicycleInPedestrianStreet.value
}
