package de.westnordost.streetcomplete.screens.user.statistics

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentStatisticsBallPitBinding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows the user's solved quests of each type in some kind of ball pit.  */
class StatisticsByCountryFragment : Fragment(R.layout.fragment_statistics_ball_pit) {

    interface Listener {
        fun onClickedCountryFlag(countryCode: String, solvedCount: Int, rank: Int?, countryBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val binding by viewBinding(FragmentStatisticsBallPitBinding::bind)
    private val viewModel by viewModel<EditStatisticsViewModel>(ownerProducer = { requireParentFragment() })
    private var hasCreatedBallPit: Boolean = false // only add it once, no update

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.queryCountryStatistics()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(binding.ballPitView)
        observe(viewModel.countryStatistics) { countryStatistics ->
            if (countryStatistics != null && !hasCreatedBallPit) {
                binding.ballPitView.setViews(countryStatistics.map {
                    createCountryBubbleView(it.countryCode, it.count, it.rank) to it.count
                })
                hasCreatedBallPit = true
            }
        }
    }

    private fun createCountryBubbleView(countryCode: String, solvedCount: Int, rank: Int?): View {
        val ctx = requireContext()
        val countryBubbleView = CircularFlagView(ctx)
        countryBubbleView.id = View.generateViewId()
        countryBubbleView.layoutParams = ViewGroup.LayoutParams(240, 240)
        countryBubbleView.countryCode = countryCode
        countryBubbleView.elevation = ctx.resources.dpToPx(6)
        countryBubbleView.setOnClickListener { v ->
            listener?.onClickedCountryFlag(countryCode, solvedCount, rank, v)
        }
        return countryBubbleView
    }
}
