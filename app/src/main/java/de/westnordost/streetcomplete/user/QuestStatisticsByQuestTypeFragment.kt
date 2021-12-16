package de.westnordost.streetcomplete.user


import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.databinding.FragmentQuestStatisticsBallPitBinding
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Shows the user's solved quests of each type in some kind of ball pit. Clicking on each opens
 *  a QuestTypeInfoFragment that shows the quest's details. */
class QuestStatisticsByQuestTypeFragment : Fragment(R.layout.fragment_quest_statistics_ball_pit)
{
    @Inject internal lateinit var statisticsSource: StatisticsSource
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry

    interface Listener {
        fun onClickedQuestType(questType: QuestType<*>, solvedCount: Int, questBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val binding by viewBinding(FragmentQuestStatisticsBallPitBinding::bind)

    init {
        Injector.applicationComponent.inject(this)
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(binding.ballPitView)

        viewLifecycleScope.launch {
            val statistics = withContext(Dispatchers.IO) { statisticsSource.getQuestStatistics() }
            binding.ballPitView.setViews(statistics.map { statistic ->
                createQuestTypeBubbleView(statistic.questType, statistic.solvedCount) to statistic.solvedCount
            })
        }
    }

    private fun createQuestTypeBubbleView(questType: QuestType<*>, solvedCount: Int): View {
        val ctx = requireContext()
        val questView = ImageView(ctx)
        questView.id = View.generateViewId()
        questView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        questView.scaleType = ImageView.ScaleType.FIT_XY
        questView.setImageResource(questType.icon)

        val clickableContainer = FrameLayout(ctx)
        clickableContainer.layoutParams = ViewGroup.LayoutParams(256,256)
        clickableContainer.foreground = requireContext().getDrawable(R.drawable.round_pressed)
        clickableContainer.elevation = 6f.toPx(ctx)
        clickableContainer.outlineProvider = CircularOutlineProvider
        clickableContainer.addView(questView)
        clickableContainer.setOnClickListener { v ->
            listener?.onClickedQuestType(questType, solvedCount, v)
        }

        return clickableContainer
    }
}

