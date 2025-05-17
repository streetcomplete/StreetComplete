package de.westnordost.streetcomplete.view.controller

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ViewStreetSideLastAnswerButtonBinding
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.setImage
import kotlin.math.absoluteValue

/** View controller that manages a street side select puzzle and accompanying last answer button
 *  plus compass view. */
class StreetSideSelectWithLastAnswerButtonViewController<I>(
    private val puzzleView: StreetSideSelectPuzzle,
    private val compassView: View,
    private val lastAnswerButtonBinding: ViewStreetSideLastAnswerButtonBinding,
    private val prefs: Preferences,
    private val prefKey: String,
    private val serializeSelection: (item: I) -> String,
    private val deserializeSelection: (str: String) -> I,
    private val asStreetSideItem: (item: I, isRight: Boolean) -> StreetSideDisplayItem<I>
) {
    /** Callback when the user makes a selection */
    var onInputChanged: (() -> Unit)? = null

    /** Callback on tapping a side */
    var onClickSide: ((isRight: Boolean) -> Unit)? = null

    /** Selection displayed in the last answer button and that is applied when pressed */
    private val lastSelectionLeft: I?
    private val lastSelectionRight: I?
    private val lastSelectionOneSide: I?

    var transformLastSelection: (item: I, isRight: Boolean) -> I = { item, _ -> item }
        set(value) {
            field = value
            updateLastSelectionButton()
        }

    /** Angle in degrees by which the street side select puzzle should be rotated from North */
    var offsetPuzzleRotation: Float = 0f
        set(value) {
            field = value
            puzzleView.streetRotation = value + compassView.rotation
        }

    /** image to display when no selection has been made */
    var defaultPuzzleImageLeft: Image = ResImage(R.drawable.ic_street_side_unknown)
        set(value) {
            field = value
            if (left == null) puzzleView.setLeftSideImage(value)
        }

    var defaultPuzzleImageRight: Image = ResImage(R.drawable.ic_street_side_unknown)
        set(value) {
            field = value
            if (right == null) puzzleView.setRightSideImage(value)
        }

    /** selected item on the left side */
    var left: StreetSideDisplayItem<I>? = null
        private set(value) {
            field = value
            updateLastSelectionButtonVisibility()
        }

    /** selected item on the right side */
    var right: StreetSideDisplayItem<I>? = null
        private set(value) {
            field = value
            updateLastSelectionButtonVisibility()
        }

    enum class Sides { BOTH, LEFT, RIGHT }
    var showSides: Sides = Sides.BOTH
        set(value) {
            field = value
            when (value) {
                Sides.BOTH -> puzzleView.showBothSides()
                Sides.LEFT -> puzzleView.showOnlyLeftSide()
                Sides.RIGHT -> puzzleView.showOnlyRightSide()
            }
            updateLastSelectionButton()
        }

    var isEnabled: Boolean = true
        set(value) {
            field = value
            puzzleView.isEnabled = value
            updateLastSelectionButtonVisibility()
        }

    val isComplete: Boolean get() = when (showSides) {
        Sides.BOTH -> left != null && right != null
        Sides.LEFT -> left != null
        Sides.RIGHT -> right != null
    }

    init {
        lastSelectionLeft = prefs.getLastPickedLeft(prefKey)?.let { tryDeserializeSelection(it) }
        lastSelectionRight = prefs.getLastPickedRight(prefKey)?.let { tryDeserializeSelection(it) }
        lastSelectionOneSide = prefs.getLastPickedOneSide(prefKey)?.let { tryDeserializeSelection(it) }

        puzzleView.onClickSideListener = { isRight -> onClickSide?.invoke(isRight) }
        lastAnswerButtonBinding.root.setOnClickListener { applyLastSelection() }
        puzzleView.setLeftSideImage(defaultPuzzleImageLeft)
        puzzleView.setRightSideImage(defaultPuzzleImageRight)
    }

    private fun tryDeserializeSelection(str: String): I? =
        try {
            deserializeSelection(str)
        } catch (e: Exception) {
            null
        }

    /* ------------------------------------ rotate view ----------------------------------------- */

    fun onMapOrientation(rotation: Double, tilt: Double) {
        puzzleView.streetRotation = offsetPuzzleRotation - rotation.toFloat()
        compassView.rotation = -rotation.toFloat()
        compassView.rotationX = tilt.toFloat()
    }

    /* ------------------------------------------------------------------------------------------ */

    fun getPuzzleSide(isRight: Boolean) =
        if (isRight) right else left

    fun setPuzzleSide(item: StreetSideDisplayItem<I>?, isRight: Boolean) {
        if (isRight) {
            if (item != null) {
                puzzleView.setRightSideImage(item.image)
                puzzleView.setRightSideFloatingIcon(item.floatingIcon)
                puzzleView.setRightSideText(item.title)
            } else {
                puzzleView.setRightSideImage(defaultPuzzleImageRight)
            }
            right = item
        } else {
            if (item != null) {
                puzzleView.setLeftSideImage(item.image)
                puzzleView.setLeftSideFloatingIcon(item.floatingIcon)
                puzzleView.setLeftSideText(item.title)
            } else {
                puzzleView.setLeftSideImage(defaultPuzzleImageLeft)
            }
            left = item
        }
    }

    fun replacePuzzleSide(item: StreetSideDisplayItem<I>, isRight: Boolean) {
        if (isRight) {
            puzzleView.replaceRightSideImage(item.image)
            puzzleView.replaceRightSideFloatingIcon(item.floatingIcon)
            puzzleView.setRightSideText(item.title)
            right = item
        } else {
            puzzleView.replaceLeftSideImage(item.image)
            puzzleView.replaceLeftSideFloatingIcon(item.floatingIcon)
            puzzleView.setLeftSideText(item.title)
            left = item
        }
        onInputChanged?.invoke()
    }

    /* -------------------------------------- tap hint ------------------------------------------ */

    fun showTapHint() {
        if (left == null) puzzleView.showLeftSideTapHint()
        if (right == null) puzzleView.showRightSideTapHint()
    }

    /* --------------------------------- last answer button ------------------------------------- */

    fun saveLastSelection() {
        val isUpsideDown = isStreetDisplayedUpsideDown()
        val l = if (isUpsideDown) right else left
        val r = if (isUpsideDown) left else right

        if (showSides == Sides.BOTH) {
            prefs.setLastPickedLeft(prefKey, l?.value?.let { serializeSelection(it) })
            prefs.setLastPickedRight(prefKey, r?.value?.let { serializeSelection(it) })
        } else {
            prefs.setLastPickedOneSide(prefKey, (l ?: r)?.value?.let { serializeSelection(it) })
        }
    }

    fun updateLastSelectionButton() {
        updateLastSelectionButtonVisibility()
        val left: I?
        val right: I?
        when (showSides) {
            Sides.BOTH -> {
                left = lastSelectionLeft
                right = lastSelectionRight
            }
            Sides.LEFT -> {
                left = lastSelectionOneSide
                right = null
            }
            Sides.RIGHT -> {
                left = null
                right = lastSelectionOneSide
            }
        }
        lastAnswerButtonBinding.leftSideImageView.setImage(left?.let {
            asStreetSideItem(transformLastSelection(it, false), false).icon }
        )
        lastAnswerButtonBinding.rightSideImageView.setImage(right?.let {
            asStreetSideItem(transformLastSelection(it, true), true).icon }
        )
    }

    private fun updateLastSelectionButtonVisibility() {
        lastAnswerButtonBinding.root.isGone =
            when (showSides) {
                Sides.BOTH -> lastSelectionLeft == null && lastSelectionRight == null
                Sides.LEFT, Sides.RIGHT -> lastSelectionOneSide == null
            } ||
            left != null ||
            right != null ||
            !puzzleView.isEnabled
    }

    private fun applyLastSelection() {
        val isUpsideDown = isStreetDisplayedUpsideDown()

        val l: I?
        val r: I?
        when (showSides) {
            Sides.BOTH -> {
                l = if (isUpsideDown) lastSelectionRight else lastSelectionLeft
                r = if (isUpsideDown) lastSelectionLeft else lastSelectionRight
            }
            Sides.LEFT -> {
                l = lastSelectionOneSide
                r = null
            }
            Sides.RIGHT -> {
                r = lastSelectionOneSide
                l = null
            }
        }

        if (l != null && showSides != Sides.RIGHT) {
            replacePuzzleSide(asStreetSideItem(transformLastSelection(l, false), false), false)
        }
        if (r != null && showSides != Sides.LEFT) {
            replacePuzzleSide(asStreetSideItem(transformLastSelection(r, true), true), true)
        }
    }

    private fun isStreetDisplayedUpsideDown(): Boolean =
        normalizeDegrees(puzzleView.streetRotation, -180f).absoluteValue > 90f
}

interface StreetSideDisplayItem<T> {
    val value: T
    /** shown on road view */
    val image: Image
    val title: Text?
    /** shown in "last used" popup and during object selection */
    val icon: Image
    val floatingIcon: Image?
}

data class StreetSideItem<T>(
    override val value: T,
    @DrawableRes val imageId: Int,
    @StringRes val titleId: Int? = null,
    @DrawableRes val iconId: Int = imageId,
    @DrawableRes val floatingIconId: Int? = null
) : StreetSideDisplayItem<T> {
    override val image: Image get() = ResImage(imageId)
    override val title: Text? get() = titleId?.let { ResText(it) }
    override val icon: Image get() = ResImage(iconId)
    override val floatingIcon: Image? get() = floatingIconId?.let { ResImage(it) }
}

data class StreetSideItem2<T>(
    override val value: T,
    override val image: Image,
    override val title: Text? = null,
    override val icon: Image = image,
    override val floatingIcon: Image? = null
) : StreetSideDisplayItem<T>
