package de.westnordost.streetcomplete.settings.questselection

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.FutureTask
import javax.inject.Inject
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.data.visiblequests.*
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.databinding.RowQuestSelectionBinding
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.settings.genericQuestTitle
import kotlinx.coroutines.*
import java.util.*

/** Adapter for the list that in which the user can enable and disable quests as well as re-order
 *  them */
class QuestSelectionAdapter @Inject constructor(
    private val context: Context,
    private val visibleQuestTypeController: VisibleQuestTypeController,
    private val questTypeOrderController: QuestTypeOrderController,
    private val questTypeRegistry: QuestTypeRegistry,
    countryBoundaries: FutureTask<CountryBoundaries>,
    prefs: SharedPreferences
) : RecyclerView.Adapter<QuestSelectionAdapter.QuestVisibilityViewHolder>(), LifecycleObserver {

    private val currentCountryCodes: List<String>
    private val itemTouchHelper by lazy { ItemTouchHelper(TouchHelperCallback()) }

    private val viewLifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** all quest types */
    private var questTypes: MutableList<QuestVisibility> = mutableListOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    var filter: String = ""
    set(value) {
        val n = value.trim()
        if (n != field) {
            field = n
            notifyDataSetChanged()
        }
    }

    /** if a filter is active, the filtered quest types, otherwise null */
    private val filteredQuestTypes: List<QuestVisibility>? get() {
        val f = filter
        return if (f.isEmpty()) {
            null
        } else {
            val words = f.lowercase().split(' ')
            questTypes.filter { questVisibility ->
                val question = genericQuestTitle(context.resources, questVisibility.questType).lowercase()
                words.all { filterWord -> question.contains(filterWord) }
            }
        }
    }

    /** during dragging, a mutable copy of the quest types. This is necessary to show the
     *  dragging animation (where the dragged item pushes aside other items to make room while
     *  being dragged). */
    private var questTypesDuringDrag: MutableList<QuestVisibility>? = null

    private val shownQuestTypes: List<QuestVisibility> get() =
        questTypesDuringDrag ?: filteredQuestTypes ?: questTypes

    private val visibleQuestsListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType<*>, visible: Boolean) {
            /* not doing anything here - we assume that this happened due to a tap on the checkbox
             * for a quest, so the display and data was already updated :-/ */
        }

        override fun onQuestTypeVisibilitiesChanged() {
            // all/many visibilities have changed - update the data and notify UI of changes
            viewLifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    questTypes.forEach { it.visible = visibleQuestTypeController.isVisible(it.questType) }
                }
                notifyDataSetChanged()
            }
        }
    }

    private val questTypeOrderListener = object : QuestTypeOrderSource.Listener {
        override fun onQuestTypeOrderAdded(item: QuestType<*>, toAfter: QuestType<*>) {
            val itemIndex = questTypes.indexOfFirst { it.questType == item }
            val toAfterIndex = questTypes.indexOfFirst { it.questType == toAfter }

            val questType = questTypes.removeAt(itemIndex)
            questTypes.add(toAfterIndex + if (itemIndex > toAfterIndex) 1 else 0, questType)
            /* not calling notifyItemMoved here because the view change has already been performed
               on drag&drop, we just need to update the data now */
        }

        override fun onQuestTypeOrdersChanged() {
            // all/many quest orders have been changed - reinit list
            viewLifecycleScope.launch { questTypes = createQuestTypeVisibilityList() }
        }
    }

    init {
        val lat = Double.fromBits(prefs.getLong(Prefs.MAP_LATITUDE, 0.0.toBits()))
        val lng = Double.fromBits(prefs.getLong(Prefs.MAP_LONGITUDE, 0.0.toBits()))
        currentCountryCodes = countryBoundaries.get().getIds(lng, lat)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        viewLifecycleScope.launch { questTypes = createQuestTypeVisibilityList() }

        visibleQuestTypeController.addListener(visibleQuestsListener)
        questTypeOrderController.addListener(questTypeOrderListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        visibleQuestTypeController.removeListener(visibleQuestsListener)
        questTypeOrderController.removeListener(questTypeOrderListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewLifecycleScope.cancel()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestVisibilityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuestVisibilityViewHolder(RowQuestSelectionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: QuestVisibilityViewHolder, position: Int) {
        holder.onBind(shownQuestTypes[position])
    }

    override fun getItemCount() = shownQuestTypes.size

    private suspend fun createQuestTypeVisibilityList() = withContext(Dispatchers.IO) {
        val sortedQuestTypes = questTypeRegistry.toMutableList()
        questTypeOrderController.sort(sortedQuestTypes)
        sortedQuestTypes.map { QuestVisibility(it, visibleQuestTypeController.isVisible(it)) }.toMutableList()
    }

    /** Contains the logic for drag and drop (for reordering) */
    private inner class TouchHelperCallback : ItemTouchHelper.Callback() {
        private var draggedFrom = -1
        private var draggedTo = -1

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val qv = (viewHolder as QuestVisibilityViewHolder).item
            if (!qv.isInteractionEnabled) return 0

            return makeFlag(ACTION_STATE_IDLE, UP or DOWN) or
                   makeFlag(ACTION_STATE_DRAG, UP or DOWN)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            if (questTypesDuringDrag == null) questTypesDuringDrag = shownQuestTypes.toMutableList()
            Collections.swap(questTypesDuringDrag!!, from, to)
            notifyItemMoved(from, to)
            return true
        }

        override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val qv = (target as QuestVisibilityViewHolder).item
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
            val qt = questTypesDuringDrag
            /* since we modify the quest list during move (in onMove) for the animation, the quest
            *  type we dragged is now already at the position we want it to be. */
            if (draggedTo != draggedFrom && draggedTo > 0 && qt != null) {
                val item = qt[draggedTo].questType
                val toAfter = qt[draggedTo - 1].questType

                viewLifecycleScope.launch(Dispatchers.IO) {
                    questTypeOrderController.addOrderItem(item, toAfter)
                }
            }

            questTypesDuringDrag = null
            draggedFrom = -1
            draggedTo = -1
        }

        override fun isItemViewSwipeEnabled() = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }

    /** View Holder for a single quest type */
    inner class QuestVisibilityViewHolder(private val binding: RowQuestSelectionBinding) :
        RecyclerView.ViewHolder(binding.root), CompoundButton.OnCheckedChangeListener {

        lateinit var item: QuestVisibility

        private val isEnabledOnlyAtNight: Boolean
            get() {
                return item.questType.dayNightVisibility == DayNightCycle.ONLY_NIGHT
            }

        private val isEnabledInCurrentCountry: Boolean
            get() {
                (item.questType as? OsmElementQuestType<*>)?.let { questType ->
                    return when(val countries = questType.enabledInCountries) {
                        is AllCountries -> true
                        is AllCountriesExcept -> !countries.exceptions.containsAny(currentCountryCodes)
                        is NoCountriesExcept -> countries.exceptions.containsAny(currentCountryCodes)
                    }
                }
                return true
            }

        fun onBind(with: QuestVisibility) {
            this.item = with
            val colorResId = if (item.isInteractionEnabled) R.color.background else R.color.greyed_out
            itemView.setBackgroundResource(colorResId)
            binding.questIcon.setImageResource(item.questType.icon)
            binding.questTitle.text = genericQuestTitle(binding.questTitle.resources, item.questType)
            binding.visibilityCheckBox.setOnCheckedChangeListener(null)
            binding.visibilityCheckBox.isChecked = item.visible
            binding.visibilityCheckBox.isEnabled = item.isInteractionEnabled
            binding.visibilityCheckBox.setOnCheckedChangeListener(this)

            binding.dragHandle.isInvisible = !item.isInteractionEnabled
            binding.dragHandle.setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> itemTouchHelper.startDrag(this)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                true
            }

            binding.disabledText.isGone = isEnabledInCurrentCountry && !isEnabledOnlyAtNight
            if (!isEnabledInCurrentCountry) {
                val cc = if (currentCountryCodes.isEmpty()) "Atlantis" else currentCountryCodes[0]
                binding.disabledText.text =  binding.disabledText.resources.getString(
                    R.string.questList_disabled_in_country, Locale("", cc).displayCountry
                )
            } else if (isEnabledOnlyAtNight) {
                binding.disabledText.text = binding.disabledText.resources.getString(R.string.questList_disabled_at_day)
            }

            updateSelectionStatus()
        }

        private fun updateSelectionStatus() {
            if (!item.visible) {
                binding.questIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.greyed_out))
            } else {
                binding.questIcon.clearColorFilter()
            }
            binding.questTitle.isEnabled = item.visible
        }

        override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
            item.visible = b
            updateSelectionStatus()
            viewLifecycleScope.launch(Dispatchers.IO) {
                visibleQuestTypeController.setVisible(item.questType, item.visible)
            }
            if (b && item.questType.defaultDisabledMessage > 0) {
                AlertDialog.Builder(compoundButton.context)
                    .setTitle(R.string.enable_quest_confirmation_title)
                    .setMessage(item.questType.defaultDisabledMessage)
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no) { _, _ ->
                        compoundButton.isChecked = false
                    }
                    .show()
            }
        }
    }
}
