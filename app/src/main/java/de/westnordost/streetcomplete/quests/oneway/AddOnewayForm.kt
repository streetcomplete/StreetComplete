package de.westnordost.streetcomplete.quests.oneway

import android.os.Bundle
import android.support.annotation.AnyThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*

class AddOnewayForm : YesNoQuestAnswerFragment() {

    @Inject internal lateinit var db: WayTrafficFlowDao

    private var streetSideRotater: StreetSideRotater? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, inState: Bundle?): View? {
        Injector.instance.applicationComponent.inject(this)

        val view = super.onCreateView(inflater, container, inState)
        setContentView(R.layout.quest_street_side_puzzle)
        setNoContentPadding()

        puzzleView.showOnlyRightSide()

        puzzleView.setRightSideImageResource(
            if (db.isForward(osmElement.id)!!) R.drawable.ic_oneway_lane
            else R.drawable.ic_oneway_lane_reverse
        )

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedle, elementGeometry)

        return view
    }

    override fun onClickYesNo(answer: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(YesNoQuestAnswerFragment.ANSWER, answer)
        // the quest needs the way ID of the element to find out the direction of the oneway
        bundle.putLong(WAY_ID, osmElement.id)
        applyAnswer(bundle)
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    companion object {
        val WAY_ID = "way_id"
    }
}
