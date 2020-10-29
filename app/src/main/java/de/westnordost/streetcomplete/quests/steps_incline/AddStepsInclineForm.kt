package de.westnordost.streetcomplete.quests.steps_incline

import android.content.res.Resources
import android.os.Bundle
import androidx.annotation.AnyThread
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.steps_incline.StepsIncline.*
import de.westnordost.streetcomplete.util.getOrientationAtCenterLineInDegrees
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.*
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*
import kotlinx.android.synthetic.main.view_little_compass.*
import kotlin.math.PI

class AddStepsInclineForm : AbstractQuestFormAnswerFragment<StepsIncline>() {

    override val contentLayoutResId = R.layout.quest_oneway
    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var selection: StepsIncline? = null

    private var mapRotation: Float = 0f
    private var wayRotation: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.getString(SELECTION)?.let { selection = valueOf(it) }

        wayRotation = (elementGeometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        puzzleView.showOnlyRightSide()
        puzzleView.listener = { showDirectionSelectionDialog() }

        val defaultResId = R.drawable.ic_steps_incline_unknown
        val defaultTitleId = R.string.quest_street_side_puzzle_select

        puzzleView.setRightSideImageResource(selection?.iconResId ?: defaultResId)
        puzzleView.setRightSideText(resources.getString( selection?.titleResId ?: defaultTitleId ))

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedleView, elementGeometry as ElementPolylinesGeometry)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selection?.let { outState.putString(SELECTION, it.name) }
    }

    override fun isFormComplete() = selection != null

    override fun onClickOk() {
        applyAnswer(selection!!)
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
        mapRotation = (rotation * 180 / PI).toFloat()
    }

    private fun showDirectionSelectionDialog() {
        val ctx = context ?: return
        val items = StepsIncline.values().map { it.toItem(resources, wayRotation + mapRotation) }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) { selected ->
            val dir = selected.value!!
            puzzleView.replaceRightSideImageResource(dir.iconResId)
            puzzleView.setRightSideText(resources.getString(dir.titleResId))
            selection = dir
            checkIsFormComplete()
        }.show()
    }

    companion object {
        private const val SELECTION = "selection"
    }
}

private fun StepsIncline.toItem(resources: Resources, rotation: Float): DisplayItem<StepsIncline> {
    val drawable = RotatedCircleDrawable(resources.getDrawable(iconResId))
    drawable.rotation = rotation
    return Item2(this, DrawableImage(drawable), ResText(titleResId))
}

private val StepsIncline.titleResId: Int get() = R.string.quest_steps_incline_up

private val StepsIncline.iconResId: Int get() = when(this) {
    UP -> R.drawable.ic_steps_incline_up
    UP_REVERSED -> R.drawable.ic_steps_incline_up_reversed
}
