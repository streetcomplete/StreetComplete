package de.westnordost.streetcomplete.screens.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.data.logs.styleResId
import de.westnordost.streetcomplete.databinding.FragmentLogsBinding
import de.westnordost.streetcomplete.databinding.RowLogMessageBinding
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.android.inject

/** Shows the app logs */
class LogsFragment : TwoPaneDetailFragment(R.layout.fragment_logs) {

    private val logsController: LogsController by inject()
    private val binding by viewBinding(FragmentLogsBinding::bind)

    private var filters = LogsFilters()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createOptionsMenu(binding.toolbar.root)

        binding.logsList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val logs = logsController.getLogs(filters)
        binding.toolbar.root.title = getString(R.string.about_title_logs, logs.size)
        binding.logsList.adapter = LogsAdapter(logs)
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

    private fun onClickShare() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            val logText = logsController.getLogs(filters).format()
            val logTimestamp = LocalDateTime.now().toString()
            val logTitle =
                "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}_$logTimestamp.log"

            putExtra(Intent.EXTRA_TEXT, logText)
            putExtra(Intent.EXTRA_TITLE, logTitle)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun onClickFilter() {
        LogsFiltersDialog(requireContext(), filters) { newFilters ->
            filters = newFilters

            val logs = logsController.getLogs(filters)
            binding.toolbar.root.title = getString(R.string.about_title_logs, logs.size)
            binding.logsList.adapter = LogsAdapter(logs)
        }.show()
    }
}

class LogsAdapter(logs: List<LogMessage>) : ListAdapter<LogMessage>(logs) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(RowLogMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    inner class ViewHolder(val binding: RowLogMessageBinding) : ListAdapter.ViewHolder<LogMessage>(binding) {
        override fun onBind(with: LogMessage) {
            binding.messageTextView.text = with.toString()

            TextViewCompat.setTextAppearance(binding.messageTextView, with.level.styleResId)

            binding.timestampTextView.text = Instant
                .fromEpochMilliseconds(with.timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .time
                .toString()
        }
    }
}
