package de.westnordost.streetcomplete.screens.about

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.databinding.RowLogMessageBinding
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class LogsAdapter : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: RowLogMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(with: LogMessage) {
            binding.messageTextView.text = with.toString()

            TextViewCompat.setTextAppearance(binding.messageTextView, with.level.styleResId)

            binding.timestampTextView.text = Instant
                .fromEpochMilliseconds(with.timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .time
                .toString()
        }
    }

    var messages: List<LogMessage>
        get() = _messages
        set(value) {
            _messages = value.toMutableList()
            notifyDataSetChanged()
        }

    private var _messages: MutableList<LogMessage> = mutableListOf()

    fun add(message: LogMessage) {
        _messages.add(message)
        notifyItemInserted(_messages.lastIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowLogMessageBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(_messages[position])
    }

    override fun getItemCount() = _messages.size
}
