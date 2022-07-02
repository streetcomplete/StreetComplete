package de.westnordost.streetcomplete.screens.settings.questselection

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.databinding.DialogInputTextBinding
import de.westnordost.streetcomplete.databinding.FragmentQuestPresetsBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Shows a screen in which the user can select which preset of quest selections he wants to
 *  use. */
class QuestPresetsFragment : Fragment(R.layout.fragment_quest_presets), HasTitle {

    private val questPresetsController: QuestPresetsController by inject()

    private val binding by viewBinding(FragmentQuestPresetsBinding::bind)

    override val title: String get() = getString(R.string.action_manage_presets)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = QuestPresetsAdapter(requireContext(), questPresetsController)
        lifecycle.addObserver(adapter)
        binding.questPresetsList.adapter = adapter
        binding.addPresetButton.setOnClickListener { onClickAddPreset() }
    }

    private fun onClickAddPreset() {
        val ctx = context ?: return

        val dialogBinding = DialogInputTextBinding.inflate(layoutInflater)
        dialogBinding.editText.hint = ctx.getString(R.string.quest_presets_preset_name)

        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_presets_preset_add)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = dialogBinding.editText.text.toString().trim()
                viewLifecycleScope.launch(Dispatchers.IO) {
                    questPresetsController.add(name)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
