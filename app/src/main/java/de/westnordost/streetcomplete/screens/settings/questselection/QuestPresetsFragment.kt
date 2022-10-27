package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.databinding.FragmentQuestPresetsBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.dialogs.EditTextDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Shows a screen in which the user can select which preset of quest selections he wants to
 *  use. */
class QuestPresetsFragment : Fragment(R.layout.fragment_quest_presets), HasTitle {

    private val questPresetsController: QuestPresetsController by inject()
    private val urlConfigController: UrlConfigController by inject()
    private val prefs: SharedPreferences by inject()

    private val binding by viewBinding(FragmentQuestPresetsBinding::bind)

    override val title: String get() = getString(R.string.action_manage_presets)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = QuestPresetsAdapter(requireContext(), questPresetsController, urlConfigController, prefs)
        lifecycle.addObserver(adapter)
        binding.questPresetsList.adapter = adapter
        binding.addPresetButton.setOnClickListener { showPresetSelector() }
    }

    private fun showPresetSelector() {
        val c = context ?: return
        val presets = mutableListOf<QuestPreset>()
        presets.add(QuestPreset(0, c.getString(R.string.quest_presets_default_name)))
        presets.addAll(questPresetsController.getAll())
        var dialog: AlertDialog? = null
        val array = presets.map { it.name }.toTypedArray()
        val builder = AlertDialog.Builder(c)
            .setTitle(R.string.copy_preset_title)
            .setSingleChoiceItems(array, -1) { _, i ->
                dialog?.dismiss()
                onClickAddPreset(presets[i].id)
            }
            .setNegativeButton(R.string.copy_preset_new) { _, _ ->
                dialog?.dismiss()
                onClickAddPreset()
            }
        dialog = builder.create()
        dialog.show()
    }

    private fun onClickAddPreset(copyFrom: Long? = null) {
        val ctx = context ?: return
        val dialog = EditTextDialog(ctx, // todo: do i need to modify this?
            title = ctx.getString(R.string.quest_presets_preset_add),
            callback = { name -> addQuestPreset(name) }
        )
        dialog.editText.hint = ctx.getString(R.string.quest_presets_preset_name)
        dialog.editText.filters = arrayOf(InputFilter.LengthFilter(60))
        dialog.show()
    }

    private fun addQuestPreset(name: String, copyFrom: Long? = null) { // todo: call it properly?
        viewLifecycleScope.launch(Dispatchers.IO) {
                    if (copyFrom == null) questPresetsController.add(name)
                    else questPresetsController.add(name, copyFrom)
        }
    }
}
