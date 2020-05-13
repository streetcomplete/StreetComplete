package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.data.user.UserStore
import kotlinx.android.synthetic.main.fragment_quest_statistics.*
import javax.inject.Inject

/** Shows the user's solved quests of each type in some kind of ball pit. Clicking on each opens
 *  a QuestTypeInfoFragment that shows the quest's details. */
class QuestStatisticsFragment : Fragment(R.layout.fragment_quest_statistics),
    QuestStatisticsByQuestTypeFragment.Listener,  QuestStatisticsByCountryFragment.Listener

{
    @Inject internal lateinit var questStatisticsDao: QuestStatisticsDao
    @Inject internal lateinit var userStore: UserStore

    interface Listener {
        fun onClickedQuestType(questType: QuestType<*>, solvedCount: Int, questBubbleView: View)
        fun onClickedCountryFlag(country: String, solvedCount: Int, rank: Int?, countryBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyText.visibility = if (questStatisticsDao.getTotalAmount() == 0) View.VISIBLE else View.GONE

        byQuestTypeButton.setOnClickListener { v -> selectorButton.check(v.id) }
        byCountryButton.setOnClickListener { v -> selectorButton.check(v.id) }

        selectorButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.byQuestTypeButton -> replaceFragment(QuestStatisticsByQuestTypeFragment())
                    R.id.byCountryButton -> replaceFragment(QuestStatisticsByCountryFragment())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (userStore.isSynchronizingStatistics) {
            emptyText.setText(R.string.stats_are_syncing)
        } else {
            emptyText.setText(R.string.quests_empty)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_FADE)
            .replace(R.id.questStatisticsFragmentContainer, fragment)
            .commit()
    }

    override fun onClickedQuestType(questType: QuestType<*>, solvedCount: Int, questBubbleView: View) {
        listener?.onClickedQuestType(questType, solvedCount, questBubbleView)
    }

    override fun onClickedCountryFlag(countryCode: String, solvedCount: Int, rank: Int?, countryBubbleView: View) {
        listener?.onClickedCountryFlag(countryCode, solvedCount, rank, countryBubbleView)
    }
}

