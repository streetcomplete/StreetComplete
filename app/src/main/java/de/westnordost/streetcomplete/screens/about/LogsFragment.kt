package de.westnordost.streetcomplete.screens.about

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.databinding.FragmentLogsBinding
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

/** Shows the app logs */
class LogsFragment : TwoPaneDetailFragment(R.layout.fragment_logs) {

    private val logsController: LogsController by inject()
    private val binding by viewBinding(FragmentLogsBinding::bind)
    private val adapter = LogsAdapter()

    private var filters: LogsFilters

    init {
        val startOfToday = LocalDateTime(systemTimeNow().toLocalDate(), LocalTime(0, 0, 0))
        filters = LogsFilters(timestampNewerThan = startOfToday)
    }

    private val logsControllerListener = object : LogsController.Listener {
        override fun onAdded(message: LogMessage) { viewLifecycleScope.launch { onMessageAdded(message) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createOptionsMenu(binding.toolbar.root)

        binding.logsList.adapter = adapter
        binding.logsList.itemAnimator = null // default animations are too slow when logging many messages quickly
        binding.logsList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }

        showLogs()

        logsController.addListener(logsControllerListener)
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        filters = Json.decodeFromString(savedInstanceState.getString(FILTERS_DATA)!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FILTERS_DATA, Json.encodeToString(filters))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logsController.removeListener(logsControllerListener)
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
        val logText = getLogs().format()
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
        LogsFiltersDialog(requireContext(), filters) { newFilters ->
            filters = newFilters
            showLogs()
        }.show()
    }

    private fun onMessageAdded(message: LogMessage) {
        if (filters.matches(message)) {
            adapter.add(message)
            binding.toolbar.root.title = getString(R.string.about_title_logs, adapter.messages.size)

            if (hasScrolledToBottom()) {
                binding.logsList.scrollToPosition(adapter.messages.lastIndex)
            }
        }
    }

    private fun showLogs() {
        viewLifecycleScope.launch {
            val logs = getLogs()
            adapter.messages = logs
            binding.toolbar.root.title = getString(R.string.about_title_logs, logs.size)
            binding.logsList.scrollToPosition(logs.lastIndex)
        }
    }

    private suspend fun getLogs(): List<LogMessage> = withContext(Dispatchers.IO) {
        logsController.getLogs(
            levels = filters.levels,
            messageContains = filters.messageContains,
            newerThan = filters.timestampNewerThan?.toEpochMilli(),
            olderThan = filters.timestampOlderThan?.toEpochMilli()
        )
    }

    private fun hasScrolledToBottom(): Boolean = !binding.logsList.canScrollVertically(1)

    companion object {
        private const val FILTERS_DATA = "filters_data"
    }
}
