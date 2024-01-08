package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.databinding.RowQuestPresetBinding
import de.westnordost.streetcomplete.view.dialogs.EditTextDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Adapter for the list in which the user can select which preset of quest selections he wants to
 *  use. */
class QuestPresetsAdapter(
    private val context: Context,
    private val questPresetsController: QuestPresetsController,
    private val questTypeOrderController: QuestTypeOrderController,
    private val visibleQuestTypeController: VisibleQuestTypeController,
    private val urlConfigController: UrlConfigController
) : RecyclerView.Adapter<QuestPresetsAdapter.QuestPresetViewHolder>(), DefaultLifecycleObserver {

    private var presets: MutableList<QuestPreset> = mutableListOf()

    private val viewLifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() { viewLifecycleScope.launch {
            notifyDataSetChanged()
        } }

        override fun onAddedQuestPreset(preset: QuestPreset) { viewLifecycleScope.launch {
            presets.add(preset)
            notifyItemInserted(presets.size - 1)
        } }

        override fun onRenamedQuestPreset(preset: QuestPreset) { viewLifecycleScope.launch {
            val index = presets.indexOfFirst { it.id == preset.id }
            if (index != -1) {
                presets[index] = preset
                notifyItemChanged(index)
            }
        } }

        override fun onDeletedQuestPreset(presetId: Long) { viewLifecycleScope.launch {
            val deleteIndex = presets.indexOfFirst { it.id == presetId }
            presets.removeAt(deleteIndex)
            notifyItemRemoved(deleteIndex)
        } }
    }

    override fun onStart(owner: LifecycleOwner) {
        presets = mutableListOf()
        presets.add(QuestPreset(0, context.getString(R.string.quest_presets_default_name)))
        presets.addAll(questPresetsController.getAll())

        questPresetsController.addListener(questPresetsListener)
    }

    override fun onStop(owner: LifecycleOwner) {
        questPresetsController.removeListener(questPresetsListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
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
            binding.menuButton.isEnabled = true
            binding.menuButton.setOnClickListener { onClickMenuButton(with) }
        }

        private fun onSelectQuestPreset(presetId: Long) {
            viewLifecycleScope.launch(Dispatchers.IO) {
                questPresetsController.selectedId = presetId
            }
        }

        private fun onClickMenuButton(preset: QuestPreset) {
            val popup = PopupMenu(itemView.context, binding.menuButton)
            popup.setForceShowIcon(true)
            if (preset.id != 0L) {
                val renameItem = popup.menu.add(R.string.quest_presets_rename)
                renameItem.setIcon(R.drawable.ic_edit_24dp)
                renameItem.setOnMenuItemClickListener { onClickRenamePreset(preset); true }
            }

            val duplicateItem = popup.menu.add(R.string.quest_presets_duplicate)
            duplicateItem.setIcon(R.drawable.ic_content_copy_24dp)
            duplicateItem.setOnMenuItemClickListener { onClickDuplicatePreset(preset); true }

            val shareItem = popup.menu.add(R.string.quest_presets_share)
            shareItem.setIcon(R.drawable.ic_share_24dp)
            shareItem.setOnMenuItemClickListener { onClickSharePreset(preset); true }

            if (preset.id != 0L) {
                val deleteItem = popup.menu.add(R.string.quest_presets_delete)
                deleteItem.setIcon(R.drawable.ic_delete_24dp)
                deleteItem.setOnMenuItemClickListener { onClickDeleteQuestPreset(preset); true }
            }

            popup.show()
        }

        private fun onClickRenamePreset(preset: QuestPreset) {
            val ctx = itemView.context
            val dialog = EditTextDialog(ctx,
                title = ctx.getString(R.string.quest_presets_rename),
                text = preset.name,
                hint = ctx.getString(R.string.quest_presets_preset_name),
                callback = { name -> renameQuestPreset(preset.id, name) }
            )
            dialog.editText.filters = arrayOf(InputFilter.LengthFilter(60))
            dialog.show()
        }

        private fun renameQuestPreset(presetId: Long, name: String) {
            viewLifecycleScope.launch(Dispatchers.IO) {
                questPresetsController.rename(presetId, name)
            }
        }
        private fun onClickDuplicatePreset(preset: QuestPreset) {
            val ctx = itemView.context
            val dialog = EditTextDialog(ctx,
                title = ctx.getString(R.string.quest_presets_duplicate),
                text = preset.name,
                hint = ctx.getString(R.string.quest_presets_preset_name),
                callback = { name -> duplicateQuestPreset(preset.id, name) }
            )
            dialog.editText.filters = arrayOf(InputFilter.LengthFilter(60))
            dialog.show()
        }

        private fun duplicateQuestPreset(presetId: Long, name: String) {
            viewLifecycleScope.launch(Dispatchers.IO) {
                val newPresetId = questPresetsController.add(name)
                questTypeOrderController.copyOrders(presetId, newPresetId)
                visibleQuestTypeController.copyVisibilities(presetId, newPresetId)
                questPresetsController.selectedId = newPresetId
            }
        }

        private fun onClickSharePreset(preset: QuestPreset) {
            val url = urlConfigController.create(preset.id)
            UrlConfigQRCodeDialog(context, url).show()
        }

        private fun onClickDeleteQuestPreset(preset: QuestPreset) {
            AlertDialog.Builder(itemView.context)
                .setMessage(itemView.context.getString(R.string.quest_presets_delete_message, preset.name))
                .setPositiveButton(R.string.delete_confirmation) { _, _ -> deleteQuestPreset(preset.id) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        private fun deleteQuestPreset(presetId: Long) {
            viewLifecycleScope.launch(Dispatchers.IO) {
                questPresetsController.delete(presetId)
            }
        }
    }
}
