package de.westnordost.streetcomplete.quests.cycleway

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.all
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.isSelectable
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.cycleway.withDefaultDirection
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.IAnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class AddCyclewayForm : AbstractOsmQuestForm<Sides<CyclewayAndDirection>>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    override val contentPadding = false

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious.value) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { isDisplayingPrevious.value = false },
                AnswerItem(R.string.quest_generic_hasFeature_yes) { onClickOk() }
            )
        } else {
            emptyList()
        }

    override val otherAnswers: List<IAnswerItem> get() = listOfNotNull(
        createShowBothSidesAnswer(),
        AnswerItem(R.string.quest_cycleway_answer_no_bicycle_infrastructure, ::noCyclewayHereHint),
        AnswerItem(R.string.cycleway_reverse_direction, ::selectReverseCyclewayDirection)
    )

    private var cycleways = mutableStateOf(Sides<CyclewayAndDirection>(null, null))
    private val isLeftSideVisible = mutableStateOf(true)
    private val isRightSideVisible = mutableStateOf(true)
    private val isDisplayingPrevious = mutableStateOf(false)
    private val selectionMode = mutableStateOf(CyclewayFormSelectionMode.SELECT)

    private fun noCyclewayHereHint() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_cycleway_answer_no_bicycle_infrastructure_title)
            .setMessage(R.string.quest_cycleway_answer_no_bicycle_infrastructure_explanation)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    /* ---------------------------------------- lifecycle --------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val originalCycleway = parseCyclewaySides(element.tags, countryInfo.isLeftHandTraffic)
            ?.selectableOrNullValues(countryInfo)
            ?: Sides<CyclewayAndDirection>(null, null)

        val contraflowSide =
            if (countryInfo.isLeftHandTraffic) originalCycleway.right
            else originalCycleway.left
        val contraflowSideWasDefinedBefore = contraflowSide != null
        val bicycleTrafficOnBothSidesIsLikely = !likelyNoBicycleContraflow.matches(element)
        val showBothSides = contraflowSideWasDefinedBefore || bicycleTrafficOnBothSidesIsLikely

        isLeftSideVisible.value = showBothSides || countryInfo.isLeftHandTraffic
        isRightSideVisible.value = showBothSides || !countryInfo.isLeftHandTraffic

        cycleways.value = originalCycleway

        // only show as re-survey (yes/no button) if the previous tagging was complete
        isDisplayingPrevious.value = cycleways.value.all { it != null }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapshotFlow { isDisplayingPrevious.value }
            .onEach {
                updateButtonPanel()
                checkIsFormComplete()
            }
            .launchIn(lifecycleScope)

        val lastPicked by lazy {
            if (isLeftSideVisible.value && isRightSideVisible.value) {
                prefs
                    .getLastPicked<Sides<Cycleway>>(this::class.simpleName!!)
                    .map { it.withDefaultDirection(countryInfo.isLeftHandTraffic) }
                    .filter { sides -> sides.all { it?.isSelectable(countryInfo) != false } }
            } else {
                emptyList()
            }
        }

        binding.composeViewBase.content { Surface {
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
                roadDirection = Direction.from(element.tags),
                lastPicked = lastPicked,
                enabled = !isDisplayingPrevious.value,
                isLeftSideVisible = isLeftSideVisible.value,
                isRightSideVisible = isRightSideVisible.value,
            )
        } }

        checkIsFormComplete()
    }

    /* --------------------------------- showing only one side ---------------------------------- */


    private fun createShowBothSidesAnswer(): IAnswerItem? {
        val isRoundabout = element.tags["junction"] == "roundabout" || element.tags["junction"] == "circular"
        if (isLeftSideVisible.value || isRightSideVisible.value || isRoundabout) return null

        return AnswerItem(R.string.quest_cycleway_answer_contraflow_cycleway) {
            isLeftSideVisible.value = true
            isRightSideVisible.value = true
        }
    }

    private val likelyNoBicycleContraflow by lazy { """
        ways with oneway:bicycle != no and (
            oneway ~ yes|-1 and highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
            or dual_carriageway = yes
            or junction ~ roundabout|circular
        )
    """.toElementFilterExpression()
    }

    /* ------------------------------ reverse cycleway direction -------------------------------- */

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

    override fun isFormComplete() =
        !isDisplayingPrevious.value &&
        (cycleways.value.left != null || !isLeftSideVisible.value) &&
        (cycleways.value.right != null || !isRightSideVisible.value)

    override fun isRejectingClose() =
        !isDisplayingPrevious.value &&
        (cycleways.value.left != null || cycleways.value.right != null)

    override fun onClickOk() {
        if (cycleways.value.wasNoOnewayForCyclistsButNowItIs(element.tags, countryInfo.isLeftHandTraffic)) {
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
        applyAnswer(sides)
        if (sides.left != null && sides.right != null) {
            // only persist the cycleway selection, not the direction. For any road that deviates from
            // the default, the user should select this specifically. Simply carrying over the
            // non-default direction to the next answer might result in mistakes
            val cycleways = Sides(left = sides.left.cycleway, right = sides.right.cycleway)
            prefs.setLastPicked(this::class.simpleName!!, listOf(cycleways))
        }
    }
}
