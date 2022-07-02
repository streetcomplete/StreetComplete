package de.westnordost.streetcomplete.screens.user.statistics

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.databinding.FragmentStatisticsBallPitBinding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** Shows the user's solved quests of each type in some kind of ball pit.  */
class StatisticsByCountryFragment : Fragment(R.layout.fragment_statistics_ball_pit) {
    private val statisticsSource: StatisticsSource by inject()

    interface Listener {
        fun onClickedCountryFlag(countryCode: String, solvedCount: Int, rank: Int?, countryBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val binding by viewBinding(FragmentStatisticsBallPitBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(binding.ballPitView)

        viewLifecycleScope.launch {
            val countriesStatistics = withContext(Dispatchers.IO) { statisticsSource.getCountryStatistics() }

            binding.ballPitView.setViews(countriesStatistics.map {
                createCountryBubbleView(it.countryCode, it.count, it.rank) to it.count
            })
        }
    }

    private fun createCountryBubbleView(countryCode: String, solvedCount: Int, rank: Int?): View {
        val ctx = requireContext()
        val countryBubbleView = CircularFlagView(ctx)
        countryBubbleView.id = View.generateViewId()
        countryBubbleView.layoutParams = ViewGroup.LayoutParams(240, 240)
        countryBubbleView.countryCode = countryCode
        countryBubbleView.elevation = ctx.dpToPx(6)
        countryBubbleView.setOnClickListener { v ->
            listener?.onClickedCountryFlag(countryCode, solvedCount, rank, v)
        }
        return countryBubbleView
    }
}
