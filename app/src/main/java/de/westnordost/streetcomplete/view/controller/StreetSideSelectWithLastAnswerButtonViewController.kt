package de.westnordost.streetcomplete.view.controller

import android.content.SharedPreferences
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewStreetSideLastAnswerButtonBinding
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.setImage
import kotlin.math.PI
import kotlin.math.absoluteValue

/** View controller that manages a street side select puzzle and accompanying last answer button
 *  plus compass view. */
class StreetSideSelectWithLastAnswerButtonViewController<I>(
    private val puzzleView: StreetSideSelectPuzzle,
    private val compassView: View,
    private val lastAnswerButtonBinding: ViewStreetSideLastAnswerButtonBinding,
    private val prefs: SharedPreferences,
    private val lastSelectionPreferencePrefix: String,
    private val serializeLastSelection: (item: StreetSideDisplayItem<I>, isRight: Boolean) -> String,
    private val deserializeLastSelection: (str: String, isRight: Boolean) -> StreetSideDisplayItem<I>
) {
    /** Callback when the user makes a selection */
    var onInputChanged: (() -> Unit)? = null

    /** Callback on tapping a side */
    var onClickSide: ((isRight: Boolean) -> Unit)? = null

    /** Selection displayed in the last answer button and that is applied when pressed */
    private val lastSelectionLeft: StreetSideDisplayItem<I>?
    private val lastSelectionRight: StreetSideDisplayItem<I>?

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
        lastSelectionLeft = prefs.getString("$lastSelectionPreferencePrefix.left", null)?.let { deserializeLastSelection(it, false) }
        lastSelectionRight = prefs.getString("$lastSelectionPreferencePrefix.right", null)?.let { deserializeLastSelection(it, true) }

        puzzleView.onClickSideListener = { isRight -> onClickSide?.invoke(isRight) }
        lastAnswerButtonBinding.root.setOnClickListener { applyLastSelection() }
        puzzleView.setLeftSideImage(defaultPuzzleImageLeft)
        puzzleView.setRightSideImage(defaultPuzzleImageRight)
        updateLastSelectionButton()
    }

    /* ------------------------------------ rotate view ----------------------------------------- */

    fun onMapOrientation(rotation: Float, tilt: Float) {
        puzzleView.streetRotation = offsetPuzzleRotation + (180 * rotation / PI).toFloat()
        compassView.rotation = (180 * rotation / PI).toFloat()
        compassView.rotationX = (180 * tilt / PI).toFloat()
    }

    /* ------------------------------------------------------------------------------------------ */

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

        prefs.edit {
            putString("$lastSelectionPreferencePrefix.left", l?.let { serializeLastSelection(it, false) })
            putString("$lastSelectionPreferencePrefix.right", r?.let { serializeLastSelection(it, true) })
        }
    }

    private fun updateLastSelectionButton() {
        updateLastSelectionButtonVisibility()
        if (showSides != Sides.RIGHT) {
            lastSelectionLeft?.let { lastAnswerButtonBinding.leftSideImageView.setImage(it.icon) }
        }
        if (showSides != Sides.LEFT) {
            lastSelectionRight?.let { lastAnswerButtonBinding.rightSideImageView.setImage(it.icon) }
        }
    }

    private fun updateLastSelectionButtonVisibility() {
        lastAnswerButtonBinding.root.isGone =
            when (showSides) {
                Sides.BOTH -> lastSelectionLeft == null && lastSelectionRight == null
                Sides.LEFT -> lastSelectionLeft == null
                Sides.RIGHT -> lastSelectionRight == null
            } ||
            left != null ||
            right != null ||
            showSides != Sides.BOTH ||
            !puzzleView.isEnabled
    }

    private fun applyLastSelection() {
        val isUpsideDown = isStreetDisplayedUpsideDown()
        val l = if (isUpsideDown) lastSelectionRight else lastSelectionLeft
        val r = if (isUpsideDown) lastSelectionLeft else lastSelectionRight
        if (l != null && showSides != Sides.RIGHT) replacePuzzleSide(l, false)
        if (r != null && showSides != Sides.LEFT) replacePuzzleSide(r, true)
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
