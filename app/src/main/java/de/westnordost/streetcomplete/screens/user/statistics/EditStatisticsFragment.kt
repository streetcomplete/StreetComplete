package de.westnordost.streetcomplete.screens.user.statistics

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.databinding.FragmentEditStatisticsBinding
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows the user's edits of each type in some kind of ball pit. Clicking on each opens
 *  a EditTypeInfoFragment that shows the quest's details. */
class EditStatisticsFragment :
    Fragment(R.layout.fragment_edit_statistics),
    StatisticsByEditTypeFragment.Listener,
    StatisticsByCountryFragment.Listener {

    interface Listener {
        fun onClickedEditType(editType: EditType, editCount: Int, questBubbleView: View)
        fun onClickedCountryFlag(country: String, editCount: Int, rank: Int?, countryBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val viewModel by viewModel<EditStatisticsViewModel>()
    private val binding by viewBinding(FragmentEditStatisticsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.byEditTypeButton.setOnClickListener { v -> binding.selectorButton.check(v.id) }
        binding.byCountryButton.setOnClickListener { v -> binding.selectorButton.check(v.id) }

        binding.selectorButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.byEditTypeButton -> replaceFragment(StatisticsByEditTypeFragment())
                    R.id.byCountryButton -> replaceFragment(StatisticsByCountryFragment())
                }
            }
        }
        observe(viewModel.hasEdits) { hasEdits ->
            binding.emptyText.isGone = hasEdits
        }
        observe(viewModel.isSynchronizingStatistics) { isSynchronizing ->
            binding.emptyText.setText(
                if (isSynchronizing) {
                    R.string.stats_are_syncing
                } else {
                    R.string.quests_empty
                }
            )
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.commit {
            setTransition(TRANSIT_FRAGMENT_FADE)
            replace(R.id.questStatisticsFragmentContainer, fragment)
        }
    }

    override fun onClickedQuestType(editType: EditType, solvedCount: Int, questBubbleView: View) {
        listener?.onClickedEditType(editType, solvedCount, questBubbleView)
    }

    override fun onClickedCountryFlag(countryCode: String, solvedCount: Int, rank: Int?, countryBubbleView: View) {
        listener?.onClickedCountryFlag(countryCode, solvedCount, rank, countryBubbleView)
    }
}
