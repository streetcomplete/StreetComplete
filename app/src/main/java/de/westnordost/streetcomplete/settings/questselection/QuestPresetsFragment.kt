package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.databinding.DialogInputTextBinding
import de.westnordost.streetcomplete.databinding.FragmentQuestPresetsBinding
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuestPresetsFragment : Fragment(R.layout.fragment_quest_presets), HasTitle {

    @Inject internal lateinit var questPresetsController: QuestPresetsController
    @Inject internal lateinit var questPresetsAdapter: QuestPresetsAdapter

    private val binding by viewBinding(FragmentQuestPresetsBinding::bind)

    override val title: String get() = getString(R.string.action_manage_presets)

    init {
        Injector.applicationComponent.inject(this)
        lifecycle.addObserver(questPresetsAdapter)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.questPresetsList.adapter = questPresetsAdapter
        binding.addPresetButton.setOnClickListener { onClickAddPreset() }
    }

    private fun onClickAddPreset() {
        val ctx = context ?: return

        val dialogBinding = DialogInputTextBinding.inflate(layoutInflater)
        dialogBinding.editText.hint = ctx.getString(R.string.quest_presets_preset_name)

        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_presets_preset_add)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _,_ ->
                val name = dialogBinding.editText.text.toString().trim()
                viewLifecycleScope.launch(Dispatchers.IO) {
                    questPresetsController.add(name)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

    }
}

