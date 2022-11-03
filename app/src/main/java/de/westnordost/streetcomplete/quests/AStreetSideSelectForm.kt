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
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController
import org.koin.android.ext.android.inject

/** Abstract base class for any quest answer form in which the user selects items for the left and
 *  the right side of the street */
abstract class AStreetSideSelectForm<I, T> : AbstractOsmQuestForm<T>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    private val prefs: SharedPreferences by inject()

    override val contentPadding = false

    protected lateinit var streetSideSelect: StreetSideSelectWithLastAnswerButtonViewController<I>

    protected var isDisplayingPrevious: Boolean = false
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
            binding.lastAnswerButton,
            prefs,
            Prefs.LAST_PICKED_PREFIX + javaClass.simpleName,
            ::serialize,
            ::deserialize,
            ::asStreetSideItem
        )
        streetSideSelect.onInputChanged = { checkIsFormComplete() }
        streetSideSelect.isEnabled = !isDisplayingPrevious
        streetSideSelect.onClickSide = ::onClickSide
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

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        streetSideSelect.showSides = StreetSideSelectWithLastAnswerButtonViewController.Sides.valueOf(inState.getString(SHOW_SIDES, null)!!)
        val left = inState.getString(LEFT)?.let { asStreetSideItem(deserialize(it), false) }
        val right = inState.getString(RIGHT)?.let { asStreetSideItem(deserialize(it), true) }
        streetSideSelect.setPuzzleSide(left, false)
        streetSideSelect.setPuzzleSide(right, true)
        isDisplayingPrevious = inState.getBoolean(IS_DISPLAYING_PREVIOUS, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SHOW_SIDES, streetSideSelect.showSides.name)
        outState.putString(LEFT, streetSideSelect.left?.let { serialize(it.value) })
        outState.putString(RIGHT, streetSideSelect.right?.let { serialize(it.value) })
        outState.putBoolean(IS_DISPLAYING_PREVIOUS, isDisplayingPrevious)
    }

    protected abstract fun serialize(item: I): String
    protected abstract fun deserialize(str: String): I
    protected abstract fun asStreetSideItem(item: I, isRight: Boolean): StreetSideDisplayItem<I>

    /* --------------------------------------- apply answer ------------------------------------- */

    protected abstract fun onClickSide(isRight: Boolean)

    override fun isFormComplete() =
        !isDisplayingPrevious && streetSideSelect.isComplete

    override fun isRejectingClose() =
        !isDisplayingPrevious && (streetSideSelect.left != null || streetSideSelect.right != null)

    companion object {
        private const val SHOW_SIDES = "show_sides"
        private const val LEFT = "left"
        private const val RIGHT = "right"
        private const val IS_DISPLAYING_PREVIOUS = "is_displaying_previous"

        private var HAS_SHOWN_TAP_HINT = false
    }
}
