package de.westnordost.streetcomplete.screens.settings.questselection

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.databinding.RowQuestSelectionBinding
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import de.westnordost.streetcomplete.util.ktx.toast
import java.util.Collections
import java.util.Locale

/** Adapter for the list in which the user can enable and disable quests as well as re-order them */
class QuestSelectionAdapter(
    private val context: Context,
    private val viewModel: QuestSelectionViewModel,
    private val questTypeRegistry: QuestTypeRegistry,
    private val prefs: ObservableSettings,
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

    var onlySceeQuests: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            viewModel.onlySceeQuests = value
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
            if (!qv.isInteractionEnabled(questTypeRegistry)) return 0

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
            return qv.isInteractionEnabled(questTypeRegistry)
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

        private val questSettingsInUseBackground by lazy { GradientDrawable().apply {
            gradientType = GradientDrawable.RADIAL_GRADIENT
            gradientRadius = 25f
            val bgColor = ContextCompat.getColor(context, R.color.background)
            val accentColor = ContextCompat.getColor(context, R.color.accent)
            colors = arrayOf(accentColor, bgColor).toIntArray()
        } }

        private val questPrefix by lazy { questPrefix(prefs) + "qs_" }
        private val questTypesWithUsedSettings by lazy {
            val set = hashSetOf<String>()
            prefs.keys.forEach {
                if (it.startsWith(questPrefix))
                    set.add(it.substringAfter(questPrefix).substringBefore("_"))
            }
            set
        }

        @SuppressLint("ClickableViewAccessibility")
        fun onBind(item: QuestSelection) {
            this.item = item
            binding.questIcon.setImageResource(item.questType.icon)
            binding.questTitle.text = genericQuestTitle(binding.questTitle.resources, item.questType)

            binding.visibilityCheckBox.isEnabled = item.isInteractionEnabled(questTypeRegistry)
            binding.dragHandle.isInvisible = !item.isInteractionEnabled(questTypeRegistry)
            itemView.setBackgroundResource(if (item.isInteractionEnabled(questTypeRegistry)) R.color.background else R.color.greyed_out)

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
                            setBackground(item)
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> binding.visibilityCheckBox.isChecked = false }
                        .setOnCancelListener { binding.visibilityCheckBox.isChecked = false }
                        .show()
                } else {
                    viewModel.selectQuest(item.questType, !item.selected)
                    setBackground(item)
                }
            }

            binding.dragHandle.setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> itemTouchHelper.startDrag(this)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                true
            }

            if (prefs.getBoolean(Prefs.EXPERT_MODE, false) && item.questType.hasQuestSettings) {
                binding.questSettings.isVisible = true
                binding.questSettings.setOnClickListener {
                    val settings = item.questType.getQuestSettingsDialog(it.context)
                    if (!prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
                        settings?.setOnDismissListener {
                            context.toast(R.string.quest_settings_per_preset_rescan, Toast.LENGTH_LONG)
                            synchronized(questSettingsInUseBackground) {
                                if (prefs.keys.any { it.startsWith(questPrefix + item.questType.name) })
                                    questTypesWithUsedSettings.add(item.questType.name)
                                else questTypesWithUsedSettings.remove(item.questType.name)
                                setBackground(item)
                            }
                        }
                    settings?.show()
                }
                setBackground(item)
            } else
                binding.questSettings.isGone = true
        }

        private fun setBackground(item: QuestSelection) {
            synchronized(questSettingsInUseBackground) {
                if (item.questType.name in questTypesWithUsedSettings)
                    binding.questSettings.background = questSettingsInUseBackground
                else binding.questSettings.setBackgroundColor(ContextCompat.getColor(context, R.color.background))
            }
        }
    }
}
