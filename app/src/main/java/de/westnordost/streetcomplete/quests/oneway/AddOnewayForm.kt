package de.westnordost.streetcomplete.quests.oneway

import android.os.Bundle
import androidx.annotation.AnyThread
import android.view.View
import androidx.core.os.bundleOf

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*

class AddOnewayForm : YesNoQuestAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    @Inject internal lateinit var db: WayTrafficFlowDao

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        puzzleView.showOnlyRightSide()

        puzzleView.setRightSideImageResource(
            if (db.isForward(osmElement!!.id)!!) R.drawable.ic_oneway_lane
            else R.drawable.ic_oneway_lane_reverse
        )

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedle, elementGeometry)
    }

    override fun onClickYesNo(answer: Boolean) {
        applyAnswer(bundleOf(
            ANSWER to answer,
            // the quest needs the way ID of the element to find out the direction of the oneway
            WAY_ID to osmElement!!.id
        ))
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    companion object {
        val WAY_ID = "way_id"
    }
}
