package de.westnordost.streetcomplete.screens.main.edithistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.edithistory.overlayIcon
import de.westnordost.streetcomplete.databinding.RowEditItemBinding
import java.text.DateFormat

/** Adapter to show the edit history in a list */
class EditHistoryAdapter(
    private val onClick: (EditItem) -> Unit
) : RecyclerView.Adapter<EditHistoryAdapter.EditViewHolder>() {

    var edits: List<EditItem> = listOf()
        set(value) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].edit.key == value[newItemPosition].edit.key
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition] == value[newItemPosition]
            })
            field = value.toList()
            diff.dispatchUpdatesTo(this)

            val newSelectedIndex = value.indexOfFirst { it.isSelected }
            if (newSelectedIndex != -1) recyclerView?.scrollToPosition(newSelectedIndex)
        }

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EditViewHolder(RowEditItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
        holder.onBind(edits[position])
    }

    override fun getItemCount() = edits.size

    inner class EditViewHolder(
        private val binding: RowEditItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: EditItem) {
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

            binding.timeDateContainer.isGone = !item.showDate && !item.showTime

            binding.dateText.isGone = !item.showDate
            binding.dateText.text = DateFormat.getDateInstance(DateFormat.SHORT).format(item.edit.createdTimestamp)

            binding.timeText.isGone = !item.showTime
            binding.timeText.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(item.edit.createdTimestamp)

            itemView.setBackgroundColor(
                itemView.context.resources.getColor(
                    if (item.edit.isSynced == true) R.color.slightly_greyed_out else R.color.background
                )
            )

            binding.clickArea.isSelected = item.isSelected
            binding.clickArea.setOnClickListener { onClick(item) }
        }
    }
}
