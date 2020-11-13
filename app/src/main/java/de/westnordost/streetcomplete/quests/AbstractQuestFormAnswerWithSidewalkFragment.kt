package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment.Listener.SidewalkSide
import kotlinx.android.synthetic.main.fragment_quest_answer.*

/**
 * Abstract base class for dialogs that need to handle sidewalk quests. If the the OSM element of
 * the quest is annotated with the 'sidewalk' tag, it will ask the question for both the left and
 * the right side (if they are available).
 */
abstract class AbstractQuestFormAnswerWithSidewalkFragment<T> : AbstractQuestFormAnswerFragment<T>() {

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    protected var elementHasSidewalk = false
    protected var sidewalkOnBothSides = false
    protected var currentSidewalkSide: SidewalkSide? = null

    protected open fun shouldHandleSidewalks(): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (shouldHandleSidewalks()) {
            handlePossibleSidewalks()
        }
    }

    private fun handlePossibleSidewalks() {
        if (osmElement == null) {
            return
        }

        var sidewalkTag = osmElement!!.tags["sidewalk"]
        if (sidewalkTag == null) {
            //return TODO sst
            sidewalkTag = "both"
        }

        when {
            sidewalkTag.contentEquals("both") -> {
                sidewalkOnBothSides = true
                currentSidewalkSide = SidewalkSide.LEFT

                okButton.text = resources.getText(R.string.next)
            }
            sidewalkTag.contentEquals("left") -> {
                titleLabel.text = getLeftSidewalkTitle()
                currentSidewalkSide = SidewalkSide.LEFT
            }
            sidewalkTag.contentEquals("right") -> {
                titleLabel.text = getRightSidewalkTitle()
                currentSidewalkSide = SidewalkSide.RIGHT
            }
        }

        if (currentSidewalkSide != null) {
            elementHasSidewalk = true
            titleLabel.text = "test"
            listener?.onHighlightSidewalkSide(questId, questGroup, currentSidewalkSide!!)
        }
    }

    protected fun switchToOppositeSidewalkSide() {
        if (currentSidewalkSide == null) {
            return
        }

        bottomSheetContainer.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.enter_from_right)
        )

        currentSidewalkSide =
            if (currentSidewalkSide == SidewalkSide.LEFT) SidewalkSide.RIGHT
            else SidewalkSide.LEFT
        listener?.onHighlightSidewalkSide(questId, questGroup, currentSidewalkSide!!)

        titleLabel.text =
            if (currentSidewalkSide == SidewalkSide.LEFT) getLeftSidewalkTitle()
            else getRightSidewalkTitle()
        okButton.text = resources.getText(android.R.string.ok)
        resetInputs()
    }

    protected open fun resetInputs() {
        // NOP
    }

    protected open fun getLeftSidewalkTitle(): CharSequence {
        return titleLabel.text
    }

    protected open fun getRightSidewalkTitle(): CharSequence {
        return titleLabel.text
    }
}
