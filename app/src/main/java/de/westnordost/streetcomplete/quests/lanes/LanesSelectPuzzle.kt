package de.westnordost.streetcomplete.quests.lanes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.StreetRotateable
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle

class LanesSelectPuzzle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), StreetRotateable {

    private val puzzle = StreetSideSelectPuzzle(context, attrs, defStyleAttr)

    var onClickSideListener: ((isRight: Boolean) -> Unit)?
        set(value) { puzzle.onClickSideListener = value }
        get() = puzzle.onClickSideListener

    var onClickListener: (() -> Unit)?
        set(value) { puzzle.onClickListener = value }
        get() = puzzle.onClickListener

    var isShowingLaneMarkings: Boolean = true
    set(value) {
        field = value
        updateShowingSides()
        update()
    }
    var laneCountLeft: Int? = null
    set(value) {
        field = value
        update()
    }
    var laneCountRight: Int? = null
    set(value) {
        field = value
        update()
    }

    var isShowingBothSides: Boolean = true
    set(value) {
        field = value
        updateShowingSides()
    }

    init {
        addView(puzzle, MATCH_PARENT, MATCH_PARENT)
    }

    override fun setStreetRotation(rotation: Float) {
        puzzle.setStreetRotation(rotation)
    }

    private fun updateShowingSides() {
        if (!isShowingBothSides || !isShowingLaneMarkings)
            puzzle.showOnlyRightSide()
        else
            puzzle.showBothSides()
    }

    private fun update() {
        if (height == 0) doOnLayout { updateView() }
        else updateView()
    }

    private fun updateView() {
        val leftBitmap: Bitmap?
        val rightBitmap: Bitmap?
        if (isShowingLaneMarkings) {
            leftBitmap = laneCountLeft?.let { createLanesBitmap(height, it, laneCountRight, true) }
            rightBitmap = laneCountRight?.let { createLanesBitmap(height, it, laneCountLeft, true) }
        } else {
            val totalLaneCount = ((laneCountLeft ?: 0) + (laneCountRight ?: 0)).takeIf { it > 0 }
            leftBitmap = null
            rightBitmap = totalLaneCount?.let { createLanesBitmap(height, it, null, false) }
        }

        puzzle.setLeftSideImage(leftBitmap?.asImage())
        puzzle.setRightSideImage(rightBitmap?.asImage())

        val defaultTitle = resources.getString(R.string.quest_street_side_puzzle_select)
        puzzle.setLeftSideText(if (laneCountLeft != null) null else defaultTitle)
        puzzle.setRightSideText(if (laneCountRight != null) null else defaultTitle)
    }

    private fun Bitmap.asImage() = DrawableImage(BitmapDrawable(resources, this))
}
