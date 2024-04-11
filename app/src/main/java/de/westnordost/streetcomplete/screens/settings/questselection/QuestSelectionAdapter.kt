package de.westnordost.streetcomplete.screens.settings.questselection

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.RowQuestSelectionBinding
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import java.util.Collections
import java.util.Locale

/** Adapter for the list in which the user can enable and disable quests as well as re-order them */
class QuestSelectionAdapter(
    private val context: Context,
    private val viewModel: QuestSelectionViewModel
) : RecyclerView.Adapter<QuestSelectionAdapter.QuestSelectionViewHolder>() {

    private val itemTouchHelper by lazy { ItemTouchHelper(TouchHelperCallback()) }

    var quests: List<QuestSelection> = listOf()
        set(value) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].questType == value[newItemPosition].questType
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].selected == value[newItemPosition].selected
            })
            field = value.toList()
            diff.dispatchUpdatesTo(this)
        }

    override fun getItemCount(): Int = quests.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestSelectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuestSelectionViewHolder(RowQuestSelectionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: QuestSelectionViewHolder, position: Int) {
        holder.onBind(quests[position])
    }

    /** Contains the logic for drag and drop (for reordering) */
    private inner class TouchHelperCallback : ItemTouchHelper.Callback() {
        private var draggedFrom = -1
        private var draggedTo = -1

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val qv = (viewHolder as QuestSelectionViewHolder).item ?: return 0
            if (!qv.isInteractionEnabled) return 0

            return makeFlag(ACTION_STATE_IDLE, UP or DOWN) or
                   makeFlag(ACTION_STATE_DRAG, UP or DOWN)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            Collections.swap(quests, from, to)
            notifyItemMoved(from, to)
            return true
        }

        override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val qv = (target as QuestSelectionViewHolder).item ?: return false
            return qv.isInteractionEnabled
        }

        override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            if (draggedFrom == -1) draggedFrom = fromPos
            draggedTo = toPos
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ACTION_STATE_IDLE) {
                onDropped()
            }
        }

        private fun onDropped() {
            /* since we modify the quest list during move (in onMove) for the animation, the quest
             * type we dragged is now already at the position we want it to be. */
            if (draggedTo != draggedFrom && draggedTo > 0) {
                val item = quests[draggedTo].questType
                val toAfter = quests[draggedTo - 1].questType

                viewModel.orderQuest(item, toAfter)
            }
            draggedFrom = -1
            draggedTo = -1
        }

        override fun isItemViewSwipeEnabled() = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }

    /** View Holder for a single quest type */
    inner class QuestSelectionViewHolder(private val binding: RowQuestSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var item: QuestSelection? = null

        @SuppressLint("ClickableViewAccessibility")
        fun onBind(item: QuestSelection) {
            this.item = item
            binding.questIcon.setImageResource(item.questType.icon)
            binding.questTitle.text = genericQuestTitle(binding.questTitle.resources, item.questType)

            binding.visibilityCheckBox.isEnabled = item.isInteractionEnabled
            binding.dragHandle.isInvisible = !item.isInteractionEnabled
            itemView.setBackgroundResource(if (item.isInteractionEnabled) R.color.background else R.color.greyed_out)

            if (!item.selected) {
                binding.questIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.greyed_out))
            } else {
                binding.questIcon.clearColorFilter()
            }
            binding.visibilityCheckBox.isChecked = item.selected
            binding.questTitle.isEnabled = item.selected

            val isEnabledInCurrentCountry = viewModel.isQuestEnabledInCurrentCountry(item.questType)
            binding.disabledText.isGone = isEnabledInCurrentCountry
            if (!isEnabledInCurrentCountry) {
                val country = viewModel.currentCountry?.let { Locale("", it).displayCountry } ?: "Atlantis"
                binding.disabledText.text = binding.disabledText.resources.getString(
                    R.string.questList_disabled_in_country, country
                )
            }

            binding.visibilityCheckBox.setOnClickListener {
                if (!item.selected && item.questType.defaultDisabledMessage != 0) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.enable_quest_confirmation_title)
                        .setMessage(item.questType.defaultDisabledMessage)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            viewModel.selectQuest(item.questType, true)
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> binding.visibilityCheckBox.isChecked = false }
                        .setOnCancelListener { binding.visibilityCheckBox.isChecked = false }
                        .show()
                } else {
                    viewModel.selectQuest(item.questType, !item.selected)
                }
            }

            binding.dragHandle.setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> itemTouchHelper.startDrag(this)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                true
            }
        }
    }
}
