package de.westnordost.streetcomplete.quests.lanes

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestLanesSelectTypeBinding
import de.westnordost.streetcomplete.databinding.QuestStreetLanesPuzzleBinding
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.lanes.LanesType.MARKED
import de.westnordost.streetcomplete.quests.lanes.LanesType.MARKED_SIDES
import de.westnordost.streetcomplete.quests.lanes.LanesType.UNMARKED
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import de.westnordost.streetcomplete.view.dialogs.ValuePickerDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI

class AddLanesForm : AbstractOsmQuestForm<LanesAnswer>() {

    private var selectedLanesType: LanesType? = null
    private var leftSide: Int = 0
    private var rightSide: Int = 0
    private var hasCenterLeftTurnLane: Boolean = false

    private var mapRotation: Float = 0f
    private var mapTilt: Float = 0f
    private var wayRotation: Float = 0f

    override val contentPadding get() = selectedLanesType == null

    private var streetLanesPuzzleBinding: QuestStreetLanesPuzzleBinding? = null

    // just some shortcuts

    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    private val isOneway get() = isOneway(element.tags)

    private val isForwardOneway get() = isForwardOneway(element.tags)
    private val isReversedOneway get() = isReversedOneway(element.tags)

    override val otherAnswers: List<AnswerItem> get() {
        val answers = mutableListOf<AnswerItem>()

        if (!isOneway && countryInfo.hasCenterLeftTurnLane) {
            answers.add(AnswerItem(R.string.quest_lanes_answer_lanes_center_left_turn_lane) {
                selectedLanesType = MARKED_SIDES
                hasCenterLeftTurnLane = true
                setStreetSideLayout()
            })
        }
        return answers
    }

    //region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            selectedLanesType = savedInstanceState.getString(LANES_TYPE)?.let { LanesType.valueOf(it) }
            leftSide = savedInstanceState.getInt(LANES_LEFT, 0)
            rightSide = savedInstanceState.getInt(LANES_RIGHT, 0)
            hasCenterLeftTurnLane = savedInstanceState.getBoolean(CENTER_LEFT_TURN_LANE)
        }

        if (selectedLanesType == null || selectedLanesType == UNMARKED) {
            setSelectLanesTypeLayout()
        } else {
            setStreetSideLayout()
        }
    }

    @AnyThread override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation = rotation.toFloat()
        mapTilt = tilt.toFloat()
        updateStreetOrientation()
    }

    private fun updateStreetOrientation() {
        val streetLanesPuzzleBinding = streetLanesPuzzleBinding ?: return

        streetLanesPuzzleBinding.puzzleViewRotateContainer.streetRotation = wayRotation - mapRotation
        streetLanesPuzzleBinding.littleCompass.root.rotation = -mapRotation
        streetLanesPuzzleBinding.littleCompass.root.rotationX = mapTilt
    }

    override fun isFormComplete(): Boolean = when (selectedLanesType) {
        null -> false
        UNMARKED -> true
        MARKED_SIDES -> leftSide > 0 && rightSide > 0
        else -> leftSide > 0 || rightSide > 0
    }

    override fun isRejectingClose() = leftSide > 0 || rightSide > 0

    override fun onClickOk() {
        val totalLanes = leftSide + rightSide
        when (selectedLanesType) {
            MARKED -> applyAnswer(MarkedLanes(totalLanes))
            UNMARKED -> applyAnswer(UnmarkedLanes)
            MARKED_SIDES -> {
                val forwardLanes = if (isLeftHandTraffic) leftSide else rightSide
                val backwardLanes = if (isLeftHandTraffic) rightSide else leftSide
                applyAnswer(MarkedLanesSides(forwardLanes, backwardLanes, hasCenterLeftTurnLane))
            }
            null -> {}
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LANES_TYPE, selectedLanesType?.name)
        outState.putInt(LANES_LEFT, leftSide)
        outState.putInt(LANES_RIGHT, rightSide)
        outState.putBoolean(CENTER_LEFT_TURN_LANE, hasCenterLeftTurnLane)
    }

    //endregion

    //region Select lanes type

    private fun setSelectLanesTypeLayout() {
        val view = setContentView(R.layout.quest_lanes_select_type)
        val laneSelectBinding = QuestLanesSelectTypeBinding.bind(view)

        val unmarkedLanesButton = laneSelectBinding.unmarkedLanesButton

        unmarkedLanesButton.isSelected = selectedLanesType == UNMARKED

        unmarkedLanesButton.setOnClickListener {
            val wasSelected = unmarkedLanesButton.isSelected
            unmarkedLanesButton.isSelected = !wasSelected
            selectedLanesType = if (wasSelected) null else UNMARKED
            checkIsFormComplete()
        }
        laneSelectBinding.markedLanesButton.setOnClickListener {
            selectedLanesType = MARKED
            unmarkedLanesButton.isSelected = false
            checkIsFormComplete()
            askLanesAndSwitchToStreetSideLayout()
        }
        laneSelectBinding.markedLanesOddButton.isGone = isOneway

        laneSelectBinding.markedLanesOddButton.setOnClickListener {
            selectedLanesType = MARKED_SIDES
            unmarkedLanesButton.isSelected = false
            setStreetSideLayout()
        }
    }

    private fun askLanesAndSwitchToStreetSideLayout() {
        viewLifecycleScope.launch {
            val lanes = askForTotalNumberOfLanes()
            setTotalLanesCount(lanes)
            setStreetSideLayout()
        }
    }

    //endregion

    //region Street side layout

    private fun setStreetSideLayout() {
        streetLanesPuzzleBinding?.let {
            it.puzzleView.onPause(this)
            lifecycle.removeObserver(it.puzzleView)
        }

        setHint(requireContext().getString(R.string.quest_street_side_puzzle_tutorial))
        val view = setContentView(R.layout.quest_street_lanes_puzzle)
        val streetLanesPuzzleBinding = QuestStreetLanesPuzzleBinding.bind(view)
        this.streetLanesPuzzleBinding = streetLanesPuzzleBinding
        val puzzleView = streetLanesPuzzleBinding.puzzleView
        lifecycle.addObserver(puzzleView)

        when (selectedLanesType) {
            MARKED -> {
                puzzleView.onClickListener = this::selectTotalNumberOfLanes
                puzzleView.onClickSideListener = null
            }
            MARKED_SIDES -> {
                puzzleView.onClickListener = null
                puzzleView.onClickSideListener = this::selectNumberOfLanesOnOneSide
            }
            else -> {}
        }
        puzzleView.isShowingLaneMarkings = selectedLanesType in listOf(MARKED, MARKED_SIDES)
        puzzleView.isShowingBothSides = !isOneway
        puzzleView.isForwardTraffic = if (isOneway) isForwardOneway else !isLeftHandTraffic

        val edgeLine = countryInfo.edgeLineStyle

        puzzleView.edgeLineColor =
            if (edgeLine.contains("yellow")) Color.YELLOW else Color.WHITE
        puzzleView.edgeLineStyle = when {
            !edgeLine.contains("dashes") -> LineStyle.CONTINUOUS
            edgeLine.contains("short") -> LineStyle.SHORT_DASHES
            else -> LineStyle.DASHES
        }

        puzzleView.centerLineColor = if (countryInfo.centerLineStyle.contains("yellow")) Color.YELLOW else Color.WHITE

        updateStreetOrientation()
        updatePuzzleView()
    }

    private fun updatePuzzleView() {
        streetLanesPuzzleBinding?.puzzleView?.setLaneCounts(leftSide, rightSide, hasCenterLeftTurnLane)
        checkIsFormComplete()
    }

    //endregion

    //region Lane selection dialog

    private fun selectNumberOfLanesOnOneSide(isRight: Boolean) {
        viewLifecycleScope.launch {
            setLanesCount(askForNumberOfLanesOnOneSide(isRight), isRight)
        }
    }

    private suspend fun askForNumberOfLanesOnOneSide(isRight: Boolean): Int {
        val currentLaneCount = if (isRight) rightSide else leftSide
        return showSelectMarkedLanesDialogForOneSide(currentLaneCount)
    }

    private fun setLanesCount(lanes: Int, isRightSide: Boolean) {
        if (isRightSide) {
            rightSide = lanes
        } else {
            leftSide = lanes
        }
        updatePuzzleView()
    }

    private fun selectTotalNumberOfLanes() {
        viewLifecycleScope.launch {
            setTotalLanesCount(askForTotalNumberOfLanes())
        }
    }

    private suspend fun askForTotalNumberOfLanes(): Int {
        val currentLaneCount = rightSide + leftSide
        return if (selectedLanesType == MARKED) {
            if (isOneway) {
                showSelectMarkedLanesDialogForOneSide(currentLaneCount.takeIf { it > 0 })
            } else {
                showSelectMarkedLanesDialogForBothSides(currentLaneCount.takeIf { it > 0 })
            }
        } else {
            throw IllegalStateException()
        }
    }

    private fun setTotalLanesCount(lanes: Int) {
        if (isOneway) {
            leftSide = 0
            rightSide = lanes
        } else {
            leftSide = lanes / 2
            rightSide = lanes - lanes / 2
        }
        updatePuzzleView()
    }

    private suspend fun showSelectMarkedLanesDialogForBothSides(selectedValue: Int?): Int =
        suspendCancellableCoroutine { cont ->
            ValuePickerDialog(requireContext(),
                listOf(2, 4, 6, 8, 10, 12, 14),
                selectedValue, null,
                R.layout.quest_lanes_select_lanes,
                { cont.resume(it) }
            ).show()
        }

    private suspend fun showSelectMarkedLanesDialogForOneSide(selectedValue: Int?): Int =
        suspendCancellableCoroutine { cont ->
            ValuePickerDialog(requireContext(),
                listOf(1, 2, 3, 4, 5, 6, 7, 8),
                selectedValue, null,
                R.layout.quest_lanes_select_lanes_one_side_only,
                { cont.resume(it) }
            ).show()
        }

    // endregion

    companion object {
        private const val LANES_TYPE = "lanes_type"
        private const val LANES_LEFT = "lanes_left"
        private const val LANES_RIGHT = "lanes_right"
        private const val CENTER_LEFT_TURN_LANE = "center_left_turn_lane"
    }
}

private enum class LanesType {
    MARKED,
    MARKED_SIDES,
    UNMARKED
}
