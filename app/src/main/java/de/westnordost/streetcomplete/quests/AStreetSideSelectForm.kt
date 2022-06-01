package de.westnordost.streetcomplete.quests

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleWithLastAnswerButtonBinding
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController
import org.koin.android.ext.android.inject

/** Abstract base class for any quest answer form in which the user selects items for the left and
 *  the right side of the street */
abstract class AStreetSideSelectForm<I, T> : AbstractOsmQuestAnswerForm<T>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    private val prefs: SharedPreferences by inject()

    protected val puzzleView: StreetSideSelectPuzzle get() = binding.puzzleView

    override val contentPadding = false

    protected lateinit var streetSideSelect: StreetSideSelectWithLastAnswerButtonViewController<I>

    var isDisplayingPrevious: Boolean = false
    set(value) {
        field = value
        streetSideSelect.isEnabled = !value
        updateButtonPanel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        streetSideSelect = StreetSideSelectWithLastAnswerButtonViewController(
            binding.puzzleView,
            binding.littleCompass.root,
            binding.lastAnswerButton
        )
        streetSideSelect.onInputChanged = { checkIsFormComplete() }
        streetSideSelect.isEnabled = !isDisplayingPrevious
        streetSideSelect.onClickSide = ::onClickSide
        streetSideSelect.lastSelection = prefs.getString(Prefs.LAST_PICKED_PREFIX + javaClass.simpleName, null)?.let {
            deserializeLastSelection(it)
        }
        streetSideSelect.offsetPuzzleRotation = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
        val defaultPuzzleImage = ResImage(if (countryInfo.isLeftHandTraffic) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)
        streetSideSelect.defaultPuzzleImageLeft = defaultPuzzleImage
        streetSideSelect.defaultPuzzleImageRight = defaultPuzzleImage

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        if (!HAS_SHOWN_TAP_HINT) {
            streetSideSelect.showTapHint()
            HAS_SHOWN_TAP_HINT = true
        }
        checkIsFormComplete()
    }

    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideSelect.onMapOrientation(rotation, tilt)
    }

    /* --------------------------------- last answer button ------------------------------------- */

    protected fun saveLastSelection() {
        val isUpsideDown = streetSideSelect.isStreetDisplayedUpsideDown()
        val l = if (isUpsideDown) streetSideSelect.right else streetSideSelect.left
        val r = if (isUpsideDown) streetSideSelect.left else streetSideSelect.right
        /** by default only save selection if both sides were filled because cases where only one side
         *  may be filled are usually rare and pre-filling just one side is less of a time-saving */
        if (l?.value != null && r?.value != null) {
            val str = serializeLastSelection(StreetSideSelectWithLastAnswerButtonViewController.LastSelection(l, r))
            prefs.edit().putString(Prefs.LAST_PICKED_PREFIX + javaClass.simpleName, str).apply()
        }
    }

    private fun serializeLastSelection(selection: StreetSideSelectWithLastAnswerButtonViewController.LastSelection<I>): String =
        "${serialize(selection.left, false)}#${serialize(selection.right, true)}"

    private fun deserializeLastSelection(str: String): StreetSideSelectWithLastAnswerButtonViewController.LastSelection<I> {
        val split = str.split('#')
        val left = deserialize(split[0], false)
        val right = deserialize(split[1], true)
        return StreetSideSelectWithLastAnswerButtonViewController.LastSelection(left, right)
    }

    protected abstract fun serialize(item: StreetSideDisplayItem<I>, isRight: Boolean): String

    protected abstract fun deserialize(str: String, isRight: Boolean): StreetSideDisplayItem<I>

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        streetSideSelect.showSides = StreetSideSelectWithLastAnswerButtonViewController.Sides.valueOf(inState.getString(SHOW_SIDES, null)!!)
        streetSideSelect.setPuzzleSide(inState.getString(LEFT, null)?.let { deserialize(it, false) }, false)
        streetSideSelect.setPuzzleSide(inState.getString(RIGHT, null)?.let { deserialize(it, true) }, true)
        isDisplayingPrevious = inState.getBoolean(IS_DISPLAYING_PREVIOUS, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SHOW_SIDES, streetSideSelect.showSides.name)
        outState.putString(LEFT, streetSideSelect.left?.toString())
        outState.putString(RIGHT, streetSideSelect.right?.toString())
        outState.putBoolean(IS_DISPLAYING_PREVIOUS, isDisplayingPrevious)
    }

    /* --------------------------------------- apply answer ------------------------------------- */

    protected abstract fun onClickSide(isRight: Boolean)

    override fun isFormComplete() =
        !isDisplayingPrevious && streetSideSelect.isComplete

    override fun isRejectingClose() =
        !isDisplayingPrevious || streetSideSelect.left != null || streetSideSelect.right != null

    companion object {
        private const val SHOW_SIDES = "show_sides"
        private const val LEFT = "left"
        private const val RIGHT = "right"
        private const val IS_DISPLAYING_PREVIOUS = "is_displaying_previous"

        private var HAS_SHOWN_TAP_HINT = false
    }
}
