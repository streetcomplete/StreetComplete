package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Fragment that shows the "star" with the number of solved quests */
class AnswersCounterFragment : Fragment(R.layout.fragment_answers_counter) {

    private val viewModel by viewModel<AnswersCounterViewModel>()
    private val answersCounterView get() = view as AnswersCounterView

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        answersCounterView.setOnClickListener { viewModel.toggleShowingCurrentWeek() }

        observe(viewModel.isUploadingOrDownloading) { answersCounterView.showProgress = it }
        observe(viewModel.isShowingCurrentWeek) { answersCounterView.showLabel = it }
        observe(viewModel.answersCount) { count ->
            // only animate if count is positive, for positive feedback
            answersCounterView.setUploadedCount(count, count > 0)
        }
    }
}
