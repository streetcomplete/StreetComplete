package de.westnordost.streetcomplete.edithistory

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.edithistory.overlayIcon
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
            EDIT   -> EditViewHolder(inflater.inflate(R.layout.row_edit_item, parent, false))
            SYNCED -> SyncedViewHolder(inflater.inflate(R.layout.row_edit_synced, parent, false))
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

    private inner class EditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val clickArea = itemView.findViewById<ViewGroup>(R.id.clickArea)
        private val questIcon = itemView.findViewById<ImageView>(R.id.questIcon)
        private val selectionRing = itemView.findViewById<ImageView>(R.id.selectionRing)
        private val overlayIcon = itemView.findViewById<ImageView>(R.id.overlayIcon)
        private val undoButtonIcon = itemView.findViewById<ImageView>(R.id.undoButtonIcon)
        private val timeText = itemView.findViewById<TextView>(R.id.timeText)
        private val timeTextContainer = itemView.findViewById<ViewGroup>(R.id.timeTextContainer)

        fun onBind(edit: Edit, editAbove: Edit?) {
            undoButtonIcon.isEnabled = edit.isUndoable
            undoButtonIcon.isInvisible = selectedEdit != edit
            selectionRing.isInvisible = selectedEdit != edit

            if (edit.icon != 0) questIcon.setImageResource(edit.icon)
            else questIcon.setImageDrawable(null)

            if (edit.overlayIcon != 0) overlayIcon.setImageResource(edit.overlayIcon)
            else overlayIcon.setImageDrawable(null)

            val aboveTimeStr = editAbove?.let { formatSameDayTime(it.createdTimestamp) }
            val timeStr = formatSameDayTime(edit.createdTimestamp)
            timeTextContainer.isGone = aboveTimeStr == timeStr
            timeText.text = timeStr

            val res = itemView.context.resources
            val bgColor = res.getColor(if (edit.isSynced == true) R.color.slightly_greyed_out else R.color.background)
            itemView.setBackgroundColor(bgColor)
            clickArea.isSelected = edit == selectedEdit
            clickArea.setOnClickListener {
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

    private class SyncedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private fun formatSameDayTime(timestamp: Long) =
    DateUtils.formatSameDayTime(timestamp, currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT
)

private sealed class EditHistoryItem

private data class EditItem(val edit: Edit) : EditHistoryItem()
private object IsSyncedItem : EditHistoryItem()

private const val EDIT = 0
private const val SYNCED = 1
