package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.CountryStatisticsDao
import kotlinx.android.synthetic.main.fragment_quest_statistics_ball_pit.*
import javax.inject.Inject

/** Shows the user's solved quests of each type in some kind of ball pit.  */
class QuestStatisticsByCountryFragment : Fragment(R.layout.fragment_quest_statistics_ball_pit)
{
    @Inject internal lateinit var countryStatisticsDao: CountryStatisticsDao

    interface Listener {
        fun onClickedCountryFlag(countryCode: String, solvedCount: Int, countryBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(ballPitView)

        val solvedQuestsByIsoCode = countryStatisticsDao.getAll()
        // don't use US-AK, IN-HP etc.
        val solvedQuestsByCountry = mutableMapOf<String, Int>()
        for ((code, amount) in solvedQuestsByIsoCode) {
            val countryCode = code.substringBefore('-')
            solvedQuestsByCountry[countryCode] = amount + (solvedQuestsByCountry[countryCode] ?: 0)
        }

        ballPitView.setViews(solvedQuestsByCountry.map { (countryCode, amount) ->
            createCountryBubbleView(countryCode, amount) to amount
        })
    }

    private fun createCountryBubbleView(countryCode: String, solvedCount: Int): View {
        val ctx = requireContext()
        val countryBubbleView = CircularFlagView(ctx)
        countryBubbleView.id = View.generateViewId()
        countryBubbleView.layoutParams = ViewGroup.LayoutParams(240,240)
        countryBubbleView.countryCode = countryCode
        countryBubbleView.setOnClickListener { v ->
            listener?.onClickedCountryFlag(countryCode, solvedCount, v)
        }
        return countryBubbleView
    }
}

