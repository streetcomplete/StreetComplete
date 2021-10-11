package de.westnordost.streetcomplete.settings.questselection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.databinding.RowQuestPresetBinding
import kotlinx.coroutines.*
import javax.inject.Inject

class QuestPresetsAdapter @Inject constructor(
    private val context: Context,
    private val questPresetsController: QuestPresetsController
) : RecyclerView.Adapter<QuestPresetsAdapter.QuestPresetViewHolder>(), LifecycleObserver {

    private var presets: MutableList<QuestPreset> = mutableListOf()

    private val viewLifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() { viewLifecycleScope.launch {
            notifyDataSetChanged()
        }}

        override fun onAddedQuestPreset(preset: QuestPreset) { viewLifecycleScope.launch {
            presets.add(preset)
            notifyItemInserted(presets.size - 1)
        }}

        override fun onDeletedQuestPreset(presetId: Long) { viewLifecycleScope.launch {
            val deleteIndex = presets.indexOfFirst { it.id == presetId }
            presets.removeAt(deleteIndex)
            notifyItemRemoved(deleteIndex)
        }}
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        presets = mutableListOf()
        presets.add(QuestPreset(0, context.getString(R.string.quest_presets_default_name)))
        presets.addAll(questPresetsController.getAll())

        questPresetsController.addListener(questPresetsListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        questPresetsController.removeListener(questPresetsListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewLifecycleScope.cancel()
    }

    override fun getItemCount(): Int = presets.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestPresetViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QuestPresetViewHolder(RowQuestPresetBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: QuestPresetViewHolder, position: Int) {
        holder.onBind(presets[position])
    }

    inner class QuestPresetViewHolder(private val binding: RowQuestPresetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(with: QuestPreset) {
            binding.presetTitleText.text = with.name
            binding.selectionRadioButton.setOnCheckedChangeListener(null)
            binding.selectionRadioButton.isChecked = questPresetsController.selectedId == with.id
            binding.selectionRadioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onSelectQuestPreset(with.id)
            }
            binding.deleteButton.isEnabled = true
            binding.deleteButton.isInvisible = with.id == 0L
            binding.deleteButton.setOnClickListener { onClickDeleteQuestPreset(with) }
        }

        fun onSelectQuestPreset(presetId: Long) {
            viewLifecycleScope.launch(Dispatchers.IO) {
                questPresetsController.selectedId = presetId
            }
        }

        fun onClickDeleteQuestPreset(preset: QuestPreset) {
            AlertDialog.Builder(itemView.context, R.style.Theme_Bubble_Dialog_Alert)
                .setMessage(itemView.context.getString(R.string.quest_presets_delete_message, preset.name))
                .setPositiveButton(R.string.delete_confirmation) { _,_ -> deleteQuestPreset(preset.id) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        fun deleteQuestPreset(presetId: Long) {
            binding.deleteButton.isEnabled = false
            viewLifecycleScope.launch(Dispatchers.IO) {
                questPresetsController.delete(presetId)
            }
        }
    }
}
