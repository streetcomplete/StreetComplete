package de.westnordost.streetcomplete.screens.settings.questselection

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentQuestPresetsBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.dialogs.EditTextDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows a screen in which the user can select which preset of quest selections he wants to
 *  use. */
class QuestPresetsFragment : TwoPaneDetailFragment(R.layout.fragment_quest_presets), HasTitle {

    private val binding by viewBinding(FragmentQuestPresetsBinding::bind)
    private val viewModel by viewModel<QuestPresetsViewModel>()

    override val title: String get() = getString(R.string.action_manage_presets)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = QuestPresetsAdapter(requireContext(), viewModel)
        lifecycle.addObserver(adapter)
        binding.questPresetsList.adapter = adapter
        binding.addPresetButton.setOnClickListener { onClickAddPreset() }

        observe(viewModel.presets) { presets ->
            adapter.presets = presets
        }
    }

    private fun onClickAddPreset() {
        val ctx = context ?: return
        val dialog = EditTextDialog(ctx,
            title = getString(R.string.quest_presets_preset_add),
            hint = getString(R.string.quest_presets_preset_name),
            callback = { name -> viewModel.add(name) }
        )
        dialog.editText.filters = arrayOf(InputFilter.LengthFilter(60))
        dialog.show()
    }
}
