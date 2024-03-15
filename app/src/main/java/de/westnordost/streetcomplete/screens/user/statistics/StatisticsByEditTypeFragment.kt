package de.westnordost.streetcomplete.screens.user.statistics

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.databinding.FragmentStatisticsBallPitBinding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows the user's solved quests of each type in some kind of ball pit. Clicking on each opens
 *  a QuestTypeInfoFragment that shows the quest's details. */
class StatisticsByEditTypeFragment : Fragment(R.layout.fragment_statistics_ball_pit) {

    interface Listener {
        fun onClickedQuestType(editType: EditType, solvedCount: Int, questBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val binding by viewBinding(FragmentStatisticsBallPitBinding::bind)
    private val viewModel by viewModel<EditStatisticsViewModel>(ownerProducer = { requireParentFragment() })
    private var hasCreatedBallPit: Boolean = false // only add it once, no update

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.queryEditTypeStatistics()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(binding.ballPitView)
        observe(viewModel.editTypeStatistics) { editTypeStatistics ->
            if (editTypeStatistics != null && !hasCreatedBallPit) {
                binding.ballPitView.setViews(editTypeStatistics.map {
                    createEditTypeBubbleView(it.type, it.count) to it.count
                })
                hasCreatedBallPit = true
            }
        }
    }

    private fun createEditTypeBubbleView(editType: EditType, solvedCount: Int): View {
        val ctx = requireContext()
        val questView = ImageView(ctx)
        questView.id = View.generateViewId()
        questView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        questView.scaleType = ImageView.ScaleType.FIT_XY
        questView.setImageResource(editType.icon)

        val clickableContainer = FrameLayout(ctx)
        clickableContainer.layoutParams = ViewGroup.LayoutParams(256, 256)
        clickableContainer.foreground = requireContext().getDrawable(R.drawable.round_pressed)
        clickableContainer.elevation = ctx.resources.dpToPx(6)
        clickableContainer.outlineProvider = CircularOutlineProvider
        clickableContainer.addView(questView)
        clickableContainer.setOnClickListener { v ->
            listener?.onClickedQuestType(editType, solvedCount, v)
        }

        return clickableContainer
    }
}
