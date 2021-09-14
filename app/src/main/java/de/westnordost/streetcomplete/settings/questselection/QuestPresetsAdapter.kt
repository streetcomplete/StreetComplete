package de.westnordost.streetcomplete.settings.questselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
import kotlinx.android.synthetic.main.row_quest_preset.view.*
import kotlinx.coroutines.*
import javax.inject.Inject

class QuestPresetsAdapter @Inject constructor(
    private val context: Context,
    private val questPresetsController: QuestPresetsController
) : RecyclerView.Adapter<QuestPresetsAdapter.QuestPresetViewHolder>(), LifecycleObserver {

    private var presets: MutableList<QuestPreset> = mutableListOf()

    private val lifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() { lifecycleScope.launch {
            notifyDataSetChanged()
        }}

        override fun onAddedQuestPreset(preset: QuestPreset) { lifecycleScope.launch {
            presets.add(preset)
            notifyItemInserted(presets.size - 1)
        }}

        override fun onDeletedQuestPreset(presetId: Long) { lifecycleScope.launch {
            val deleteIndex = presets.indexOfFirst { it.id == presetId }
            presets.removeAt(deleteIndex)
            notifyItemRemoved(deleteIndex)
        }}
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        presets = mutableListOf()
        presets.add(QuestPreset(0, context.getString(R.string.quest_presets_default_name)))
        presets.addAll(questPresetsController.getAllQuestPresets())

        questPresetsController.addListener(questPresetsListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        questPresetsController.removeListener(questPresetsListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycleScope.cancel()
    }

    override fun getItemCount(): Int = presets.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestPresetViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.row_quest_preset, parent, false)
        return QuestPresetViewHolder(layout)
    }

    override fun onBindViewHolder(holder: QuestPresetViewHolder, position: Int) {
        holder.onBind(presets[position])
    }

    inner class QuestPresetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(with: QuestPreset) {
            itemView.presetTitleText.text = with.name
            itemView.selectionRadioButton.setOnCheckedChangeListener(null)
            itemView.selectionRadioButton.isChecked = questPresetsController.selectedQuestPresetId == with.id
            itemView.selectionRadioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onSelectQuestPreset(with.id)
            }
            itemView.deleteButton.isEnabled = true
            itemView.deleteButton.isInvisible = with.id == 0L
            itemView.deleteButton.setOnClickListener { onClickDeleteQuestPreset(with) }
        }

        fun onSelectQuestPreset(presetId: Long) {
            lifecycleScope.launch(Dispatchers.IO) {
                questPresetsController.selectedQuestPresetId = presetId
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
            itemView.deleteButton.isEnabled = false
            lifecycleScope.launch(Dispatchers.IO) {
                questPresetsController.deleteQuestPreset(presetId)
            }
        }
    }
}
