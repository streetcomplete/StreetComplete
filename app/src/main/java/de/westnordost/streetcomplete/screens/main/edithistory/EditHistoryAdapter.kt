package de.westnordost.streetcomplete.screens.main.edithistory

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.edithistory.overlayIcon
import de.westnordost.streetcomplete.databinding.RowEditItemBinding
import de.westnordost.streetcomplete.databinding.RowEditSyncedBinding
import de.westnordost.streetcomplete.util.ktx.toast
import kotlin.collections.ArrayList

/** Adapter to show the edit history in a list */
class EditHistoryAdapter(
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rows: MutableList<EditHistoryItem> = ArrayList()
    private var selectedEdit: Edit? = null
    private var recyclerView: RecyclerView? = null

    fun setEdits(edits: List<Edit>) {
        rows.clear()

        rows.addAll(edits.map { EditItem(it) })

        val firstSyncedItemIndex = edits.indexOfFirst { it.isSynced == true }
        if (firstSyncedItemIndex != -1) {
            rows.add(firstSyncedItemIndex, IsSyncedItem)
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (rows[position]) {
        is EditHistoryItem.EditItem -> EDIT
        EditHistoryItem.SyncedHeader -> SYNCED
        EditHistoryItem.DateHeader -> DATE
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            EDIT   -> EditViewHolder(RowEditItemBinding.inflate(inflater, parent, false))
            SYNCED -> SyncedViewHolder(RowEditSyncedBinding.inflate(inflater, parent, false))
            DATE   -> DateViewHolder(RowEditDateBinding.inflate(inflater, parent, false))
            else   -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]
        (holder as? EditViewHolder)?.onBind(row as EditHistoryItem.EditItem)
    }

    override fun getItemCount(): Int = rows.size

    fun select(edit: Edit) {
        val previousSelectedIndex = rows.indexOfFirst { it is EditItem && it.edit == selectedEdit }
        val newSelectedIndex = rows.indexOfFirst { it is EditItem && it.edit == edit }
        /* edit can in rare cases not be in adapter any more - when the edit is removed from the
           database while it is being tapped */
        if (newSelectedIndex == -1) return

        recyclerView?.scrollToPosition(newSelectedIndex)

        selectedEdit = edit

        if (previousSelectedIndex != -1) notifyItemChanged(previousSelectedIndex)
        notifyItemChanged(newSelectedIndex)

        onSelected(edit)
    }

    private inner class EditViewHolder(
        private val binding: RowEditItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: EditHistoryItem.EditItem) {
            binding.undoButtonIcon.isEnabled = item.edit.isUndoable
            binding.undoButtonIcon.isInvisible = !item.isSelected
            binding.selectionRing.isInvisible = !item.isSelected

            if (item.edit.icon != 0) {
                binding.questIcon.setImageResource(item.edit.icon)
            } else {
                binding.questIcon.setImageDrawable(null)
            }

            if (item.edit.overlayIcon != 0) {
                binding.overlayIcon.setImageResource(item.edit.overlayIcon)
            } else {
                binding.overlayIcon.setImageDrawable(null)
            }

            val aboveTimeStr = editAbove?.formatSameDayTime()
            val timeStr = item.formatSameDayTime()
            binding.timeTextContainer.isGone = aboveTimeStr == timeStr
            binding.timeText.text = timeStr

            // Only show today's date if there is an above from a different day
            binding.todayTextContainer.isGone = !(item.isToday && editAbove?.isToday == false)
            binding.todayText.text = item.formatDate()

            itemView.setBackgroundColor(
                itemView.context.resources.getColor(
                    if (item.edit.isSynced == true) R.color.slightly_greyed_out
                    else R.color.background
                )
            )

            binding.clickArea.isSelected = item.isSelected
            binding.clickArea.setOnClickListener {
                if (selectedEdit == item) {
                    if (item.isUndoable) {
                        viewModel.undo(item)
                    } else {
                        itemView.context.toast(R.string.toast_undo_unavailable, Toast.LENGTH_LONG)
                    }
                } else {
                    select(item)
                }
            }
        }
    }

    private class SyncedViewHolder(binding: RowEditSyncedBinding) : RecyclerView.ViewHolder(binding.root)
    private class DateViewHolder(binding: RowEditDateBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val EDIT = 0
        private const val SYNCED = 1
        private const val DATE = 2

    }
}
