package de.westnordost.streetcomplete.quests.oneway_suspects

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.view.ResImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class AddSuspectedOnewayForm : AbstractQuestAnswerFragment<SuspectedOnewayAnswer>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    private val binding by contentViewBinding(QuestStreetSidePuzzleBinding::bind)

    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private val db: WayTrafficFlowDao by inject()

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.puzzleView.showOnlyRightSide()

        viewLifecycleScope.launch {
            val isForward = withContext(Dispatchers.IO) { db.isForward(osmElement!!.id)!! }

            binding.puzzleView.setRightSideImage(ResImage(
                if (isForward) R.drawable.ic_oneway_lane
                else R.drawable.ic_oneway_lane_reverse
            ))
        }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )
    }

    private fun applyAnswer(answer: Boolean) {
        // the quest needs the way ID of the element to find out the direction of the oneway
        applyAnswer(SuspectedOnewayAnswer(answer, osmElement!!.id))
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }
}
