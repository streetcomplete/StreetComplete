package de.westnordost.streetcomplete.edithistory

import android.text.format.DateUtils
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
import de.westnordost.streetcomplete.ktx.findPrevious
import de.westnordost.streetcomplete.ktx.toast
import java.lang.System.currentTimeMillis
import java.text.DateFormat
import java.util.Collections
import kotlin.collections.ArrayList

/** Adapter to show the edit history in a list */
class EditHistoryAdapter(
    val onSelected: (edit: Edit) -> Unit,
    val onSelectionDeleted: () -> Unit,
    val onUndo: (edit: Edit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rows: MutableList<EditHistoryItem> = ArrayList()
    private var selectedEdit: Edit? = null
    private var recyclerView: RecyclerView? = null

    fun setEdits(edits: List<Edit>) {
        rows.clear()

        val sortedEdits = edits.sortedByDescending { it.createdTimestamp }
        rows.addAll(sortedEdits.map { EditItem(it) })

        val firstSyncedItemIndex = sortedEdits.indexOfFirst { it.isSynced == true }
        if (firstSyncedItemIndex != -1) {
            rows.add(firstSyncedItemIndex, IsSyncedItem)
        }

        notifyDataSetChanged()
    }

    fun onAdded(edit: Edit) {
        var insertIndex = rows.indexOfFirst { it is EditItem && it.edit.createdTimestamp < edit.createdTimestamp }
        if (insertIndex == -1) insertIndex = rows.size

        rows.add(insertIndex, EditItem(edit))
        if (insertIndex < rows.size) notifyItemChanged(insertIndex)
        notifyItemInserted(insertIndex)
    }

    fun onSynced(edit: Edit) {
        val editIndex = rows.indexOfFirst { it is EditItem && it.edit == edit }
        check(editIndex != -1)

        val syncedItemIndex = rows.indexOfFirst { it is IsSyncedItem }
        if (syncedItemIndex != -1) {
            Collections.swap(rows, syncedItemIndex, editIndex)
            notifyItemMoved(syncedItemIndex, editIndex)
        }
        // there is no "synced" item yet
        else {
            rows.add(editIndex, IsSyncedItem)
            notifyItemInserted(editIndex)
        }
    }

    fun onDeleted(edits: List<Edit>) {
        val editIndices = edits
            .map { edit -> rows.indexOfFirst { it is EditItem && it.edit == edit } }
            .filter { it != -1 }
            .sortedDescending()

        if (selectedEdit != null && edits.contains(selectedEdit)) {
            selectedEdit = null
            onSelectionDeleted()
        }

        for (index in editIndices) {
            rows.removeAt(index)
            notifyItemRemoved(index)
            if (index < rows.size) notifyItemChanged(index)
        }
    }

    override fun getItemViewType(position: Int): Int = when(rows[position]) {
        is EditItem -> EDIT
        IsSyncedItem -> SYNCED
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
            EDIT   -> EditViewHolder(RowEditItemBinding.inflate(inflater))
            SYNCED -> SyncedViewHolder(RowEditSyncedBinding.inflate(inflater))
            else   -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]
        val rowAbove = rows.findPrevious(position) { it is EditItem } as EditItem?
        when(holder) {
            is EditViewHolder -> holder.onBind((row as EditItem).edit, rowAbove?.edit)
        }
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

    private fun undo(edit: Edit) {
        onUndo(edit)
    }

    private inner class EditViewHolder(
        private val binding: RowEditItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(edit: Edit, editAbove: Edit?) {
            binding.undoButtonIcon.isEnabled = edit.isUndoable
            binding.undoButtonIcon.isInvisible = selectedEdit != edit
            binding.selectionRing.isInvisible = selectedEdit != edit

            if (edit.icon != 0) binding.questIcon.setImageResource(edit.icon)
            else binding.questIcon.setImageDrawable(null)

            if (edit.overlayIcon != 0) binding.overlayIcon.setImageResource(edit.overlayIcon)
            else binding.overlayIcon.setImageDrawable(null)

            val aboveTimeStr = editAbove?.let { formatSameDayTime(it.createdTimestamp) }
            val timeStr = formatSameDayTime(edit.createdTimestamp)
            binding.timeTextContainer.isGone = aboveTimeStr == timeStr
            binding.timeText.text = timeStr

            val res = itemView.context.resources
            val bgColor = res.getColor(if (edit.isSynced == true) R.color.slightly_greyed_out else R.color.background)
            itemView.setBackgroundColor(bgColor)
            binding.clickArea.isSelected = edit == selectedEdit
            binding.clickArea.setOnClickListener {
                if (selectedEdit == edit) {
                    if (edit.isUndoable) {
                        undo(edit)
                    } else {
                        itemView.context.toast(R.string.toast_undo_unavailable, Toast.LENGTH_LONG)
                    }
                } else {
                    select(edit)
                }
            }
        }
    }

    private class SyncedViewHolder(binding: RowEditSyncedBinding) : RecyclerView.ViewHolder(binding.root)
}

private fun formatSameDayTime(timestamp: Long) =
    DateUtils.formatSameDayTime(timestamp, currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT
)

private sealed class EditHistoryItem

private data class EditItem(val edit: Edit) : EditHistoryItem()
private object IsSyncedItem : EditHistoryItem()

private const val EDIT = 0
private const val SYNCED = 1
