package de.westnordost.streetcomplete.quests.sidewalk

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.Item
import de.westnordost.streetcomplete.view.dialogs.ImageListPickerDialog
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*

class AddSidewalkForm : AbstractQuestFormAnswerFragment<SidewalkAnswer>() {
    override val otherAnswers = listOf(
            OtherAnswer(R.string.quest_sidewalk_separately_mapped) { confirmSeparatelyMappedSidewalk() }
    )

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null
    private var leftSide: Sidewalk? = null
    private var rightSide: Sidewalk? = null

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.getString(SIDEWALK_RIGHT)?.let { rightSide = Sidewalk.valueOf(it) }
        savedInstanceState?.getString(SIDEWALK_LEFT)?.let { leftSide = Sidewalk.valueOf(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        puzzleView.listener = { isRight -> showSidewalkSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedleView, elementGeometry as ElementPolylinesGeometry)

        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_sidewalk_unknown_l
            else                   R.drawable.ic_sidewalk_unknown

        puzzleView.setLeftSideImageResource(leftSide?.puzzleResId ?: defaultResId)
        puzzleView.setRightSideImageResource(rightSide?.puzzleResId ?: defaultResId)

        checkIsFormComplete()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rightSide?.let { outState.putString(SIDEWALK_RIGHT, it.name) }
        leftSide?.let { outState.putString(SIDEWALK_LEFT, it.name) }
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    override fun onClickOk() {
        applyAnswer(SidewalkSides(
            left = leftSide == Sidewalk.YES,
            right = rightSide == Sidewalk.YES
        ))
    }

    private fun confirmSeparatelyMappedSidewalk() {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(SeparatelyMapped) }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
    }

    override fun isFormComplete() = leftSide != null && rightSide != null

    override fun isRejectingClose() = leftSide != null || rightSide != null

    private fun showSidewalkSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return

        val items = Sidewalk.values().map { it.asItem() }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) { selected ->
            val sidewalk = selected.value!!
            if (isRight) {
                puzzleView.replaceRightSideImageResource(sidewalk.puzzleResId)
                rightSide = sidewalk
            } else {
                puzzleView.replaceLeftSideImageResource(sidewalk.puzzleResId)
                leftSide = sidewalk
            }
            checkIsFormComplete()
        }.show()
    }

    private enum class Sidewalk(val iconResId: Int, val puzzleResId: Int, val nameResId: Int) {
        NO(R.drawable.ic_sidewalk_no, R.drawable.ic_sidewalk_puzzle_no, R.string.quest_sidewalk_value_no),
        YES(R.drawable.ic_sidewalk_yes, R.drawable.ic_sidewalk_puzzle_yes, R.string.quest_sidewalk_value_yes);

        fun asItem() = Item(this, iconResId, nameResId)
    }

    companion object {
        private const val SIDEWALK_LEFT = "sidewalk_left"
        private const val SIDEWALK_RIGHT = "sidewalk_right"
    }
}
