package de.westnordost.streetcomplete.quests.oneway_suspects

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestSuspectedOnewayBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import kotlin.math.PI

class AddSuspectedOnewayForm : AbstractOsmQuestForm<SuspectedOnewayAnswer>() {

    override val contentLayoutResId = R.layout.quest_suspected_oneway
    private val binding by contentViewBinding(QuestSuspectedOnewayBinding::bind)

    private val db: WayTrafficFlowDao by inject()

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )

    private var mapRotation: Float = 0f
    private var wayRotation: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleScope.launch {
            val isForward = withContext(Dispatchers.IO) { db.isForward(element.id) == true }
            binding.onewayIcon.setImageResource(
                if (isForward) R.drawable.ic_oneway_yes else R.drawable.ic_oneway_yes_reverse
            )
            binding.onewayIcon.rotation = wayRotation + mapRotation
        }
    }

    override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation = rotation.toFloat()
        binding.onewayIcon.rotation = wayRotation + mapRotation
    }

    private fun applyAnswer(answer: Boolean) {
        // the quest needs the way ID of the element to find out the direction of the oneway
        applyAnswer(SuspectedOnewayAnswer(answer, element.id))
    }
}
