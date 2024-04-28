package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
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
    private val viewModel: QuestPresetsViewModel
) : RecyclerView.Adapter<QuestPresetsAdapter.QuestPresetViewHolder>(), DefaultLifecycleObserver {

    var presets: List<QuestPresetSelection> = listOf()
        set(value) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition].id == value[newItemPosition].id
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    field[oldItemPosition] == value[newItemPosition]
            })
            field = value.toList()
            diff.dispatchUpdatesTo(this)
        }

    private val viewLifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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

        fun onBind(with: QuestPresetSelection) {
            binding.presetTitleText.text = with.nonEmptyName
            binding.selectionRadioButton.setOnCheckedChangeListener(null)
            binding.selectionRadioButton.isChecked = with.selected
            binding.selectionRadioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.select(with.id)
            }
            binding.menuButton.isEnabled = true
            binding.menuButton.setOnClickListener { onClickMenuButton(with) }
        }

        private fun onClickMenuButton(preset: QuestPresetSelection) {
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

        private fun onClickRenamePreset(preset: QuestPresetSelection) {
            val ctx = itemView.context
            val dialog = EditTextDialog(ctx,
                title = ctx.getString(R.string.quest_presets_rename),
                text = preset.nonEmptyName,
                hint = ctx.getString(R.string.quest_presets_preset_name),
                callback = { name -> viewModel.rename(preset.id, name) }
            )
            dialog.editText.filters = arrayOf(InputFilter.LengthFilter(60))
            dialog.show()
        }

        private fun onClickDuplicatePreset(preset: QuestPresetSelection) {
            val ctx = itemView.context
            val dialog = EditTextDialog(ctx,
                title = ctx.getString(R.string.quest_presets_duplicate),
                text = preset.nonEmptyName,
                hint = ctx.getString(R.string.quest_presets_preset_name),
                callback = { name -> viewModel.duplicate(preset.id, name) }
            )
            dialog.editText.filters = arrayOf(InputFilter.LengthFilter(60))
            dialog.show()
        }

        private fun onClickSharePreset(preset: QuestPresetSelection) {
            viewLifecycleScope.launch {
                val url = viewModel.createUrlConfig(preset.id)
                UrlConfigQRCodeDialog(context, url).show()
            }
        }

        private fun onClickDeleteQuestPreset(preset: QuestPresetSelection) {
            AlertDialog.Builder(itemView.context)
                .setMessage(itemView.context.getString(R.string.quest_presets_delete_message, preset.nonEmptyName))
                .setPositiveButton(R.string.delete_confirmation) { _, _ -> viewModel.delete(preset.id) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private val QuestPresetSelection.nonEmptyName: String get() =
        if (name.isNotEmpty()) name else context.getString(R.string.quest_presets_default_name)
}
