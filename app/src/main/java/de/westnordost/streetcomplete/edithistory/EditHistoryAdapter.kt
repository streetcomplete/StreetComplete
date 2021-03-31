package de.westnordost.streetcomplete.edithistory

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.edithistory.overlayIcon
import de.westnordost.streetcomplete.ktx.toast
import java.lang.System.currentTimeMillis
import java.util.Collections
import kotlin.collections.ArrayList

class EditHistoryAdapter(
    val onSelected: (edit: Edit) -> Unit,
    val onUndo: (edit: Edit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rows: MutableList<EditHistoryItem> = ArrayList()
    private var selectedEdit: Edit? = null

    // TODO add/manage time rows...

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
        }

        editIndices.forEach {
            rows.removeAt(it)
            notifyItemRemoved(it)
        }
    }

    override fun getItemViewType(position: Int): Int = when(rows[position]) {
        is EditItem -> EDIT
        IsSyncedItem -> SYNCED
        is EditTimeItem -> EDIT_TIME
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            EDIT -> EditViewHolder(inflater.inflate(R.layout.row_edit_item, parent, false))
            SYNCED -> SyncedViewHolder(inflater.inflate(R.layout.row_edit_synced, parent, false))
            EDIT_TIME -> EditTimeViewHolder(inflater.inflate(R.layout.row_edit_time, parent, false))
            else       -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]
        when(holder) {
            is EditViewHolder -> holder.onBind((row as EditItem).edit)
            is EditTimeViewHolder -> holder.onBind((row as EditTimeItem).timestamp)
        }
    }

    override fun getItemCount(): Int = rows.size

    private fun select(edit: Edit) {
        val previousSelectedIndex = rows.indexOfFirst { it is EditItem && it.edit == selectedEdit }
        val newSelectedIndex = rows.indexOfFirst { it is EditItem && it.edit == edit }
        check(newSelectedIndex != -1)

        selectedEdit = edit

        if (previousSelectedIndex != -1) notifyItemChanged(previousSelectedIndex)
        notifyItemChanged(newSelectedIndex)

        onSelected(edit)
    }

    private fun undo(edit: Edit) {
        onUndo(edit)
    }

    private inner class EditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questIcon = itemView.findViewById<ImageView>(R.id.questIcon)
        private val overlayIcon = itemView.findViewById<ImageView>(R.id.overlayIcon)
        private val undoButtonIcon = itemView.findViewById<ImageView>(R.id.undoButtonIcon)

        fun onBind(edit: Edit) {
            undoButtonIcon.isEnabled = edit.isUndoable
            undoButtonIcon.isInvisible = selectedEdit != edit

            if (edit.icon != 0) questIcon.setImageResource(edit.icon)
            else questIcon.setImageDrawable(null)

            if (edit.overlayIcon != 0) overlayIcon.setImageResource(edit.overlayIcon)
            else overlayIcon.setImageDrawable(null)

            itemView.isEnabled = edit.isUndoable
            itemView.isSelected = edit == selectedEdit
            itemView.setOnClickListener {
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

    private inner class EditTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val abbrTimeText = itemView.findViewById<TextView>(R.id.abbrTimeText)

        fun onBind(timestamp: Long) {
            abbrTimeText.text = DateUtils.getRelativeTimeSpanString(
                timestamp, currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL
            )
        }
    }

    private class SyncedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private sealed class EditHistoryItem

private data class EditItem(val edit: Edit) : EditHistoryItem()
private object IsSyncedItem : EditHistoryItem()
private data class EditTimeItem(val timestamp: Long) : EditHistoryItem()

private const val EDIT = 0
private const val SYNCED = 1
private const val EDIT_TIME = 2
