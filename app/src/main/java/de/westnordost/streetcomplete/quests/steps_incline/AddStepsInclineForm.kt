package de.westnordost.streetcomplete.quests.steps_incline

import android.content.Context
import android.os.Bundle
import androidx.annotation.AnyThread
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.steps_incline.StepsIncline.*
import de.westnordost.streetcomplete.util.getOrientationAtCenterLineInDegrees
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.*
import kotlin.math.PI

class AddStepsInclineForm : AbstractQuestFormAnswerFragment<StepsIncline>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    private val binding by contentViewBinding(QuestStreetSidePuzzleBinding::bind)

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

        binding.puzzleView.showOnlyRightSide()
        binding.puzzleView.onClickSideListener = { showDirectionSelectionDialog() }

        val defaultResId = R.drawable.ic_steps_incline_unknown

        binding.puzzleView.setRightSideImage(ResImage(selection?.iconResId ?: defaultResId))
        binding.puzzleView.setRightSideText(selection?.titleResId?.let { resources.getString(it) })
        if (selection == null && !HAS_SHOWN_TAP_HINT) {
            binding.puzzleView.showRightSideTapHint()
            HAS_SHOWN_TAP_HINT = true
        }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )
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
        val items = StepsIncline.values().map { it.toItem(ctx, wayRotation + mapRotation) }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) { selected ->
            val dir = selected.value!!
            binding.puzzleView.replaceRightSideImage(ResImage(dir.iconResId))
            binding.puzzleView.setRightSideText(resources.getString(dir.titleResId))
            selection = dir
            checkIsFormComplete()
        }.show()
    }

    companion object {
        private const val SELECTION = "selection"
        private var HAS_SHOWN_TAP_HINT = false
    }
}

private fun StepsIncline.toItem(context: Context, rotation: Float): DisplayItem<StepsIncline> {
    val drawable = RotatedCircleDrawable(context.getDrawable(iconResId)!!)
    drawable.rotation = rotation
    return Item2(this, DrawableImage(drawable), ResText(titleResId))
}

private val StepsIncline.titleResId: Int get() = R.string.quest_steps_incline_up

private val StepsIncline.iconResId: Int get() = when(this) {
    UP -> R.drawable.ic_steps_incline_up
    UP_REVERSED -> R.drawable.ic_steps_incline_up_reversed
}
