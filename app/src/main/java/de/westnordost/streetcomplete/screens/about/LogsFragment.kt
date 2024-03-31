package de.westnordost.streetcomplete.screens.about

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.databinding.FragmentLogsBinding
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows the app logs */
class LogsFragment : TwoPaneDetailFragment(R.layout.fragment_logs) {

    private val binding by viewBinding(FragmentLogsBinding::bind)
    private val viewModel by viewModel<LogsViewModel>()

    private val adapter = LogsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createOptionsMenu(binding.toolbar.root)

        binding.logsList.adapter = adapter
        binding.logsList.itemAnimator = null // default animations are too slow when logging many messages quickly
        binding.logsList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        observe(viewModel.logs) { logs ->
            binding.toolbar.root.title = getString(R.string.about_title_logs, logs.size)
            val hasPreviouslyScrolledToBottom = binding.logsList.hasScrolledToBottom()
            adapter.messages = logs
            if (hasPreviouslyScrolledToBottom) {
                binding.logsList.scrollToPosition(logs.lastIndex)
            }
        }
    }

    private fun createOptionsMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_logs)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share -> {
                    onClickShare()
                    true
                }
                R.id.action_filter -> {
                    onClickFilter()
                    true
                }
                else -> false
            }
        }
    }

    private fun onClickShare() = viewLifecycleScope.launch {
        val logText = viewModel.logs.value.format()
        val logTimestamp = LocalDateTime.now().toString()
        val logTitle = "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}_$logTimestamp.log"

        val shareIntent = Intent(Intent.ACTION_SEND).also {
            it.putExtra(Intent.EXTRA_TEXT, logText)
            it.putExtra(Intent.EXTRA_TITLE, logTitle)
            it.type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun onClickFilter() {
        LogsFiltersDialog(requireContext(), viewModel.filters.value) { newFilters ->
            if (newFilters != null) {
                viewModel.setFilters(newFilters)
            }
        }.show()
    }
}

private fun View.hasScrolledToBottom(): Boolean = !canScrollVertically(1)
