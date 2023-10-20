package de.westnordost.streetcomplete.screens.settings.questselection

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.databinding.FragmentQuestPresetsBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.dialogs.EditTextDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Shows a screen in which the user can select which preset of quest selections he wants to
 *  use. */
class QuestPresetsFragment : TwoPaneDetailFragment(R.layout.fragment_quest_presets), HasTitle {

    private val questPresetsController: QuestPresetsController by inject()
    private val questTypeOrderController: QuestTypeOrderController by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val urlConfigController: UrlConfigController by inject()

    private val binding by viewBinding(FragmentQuestPresetsBinding::bind)

    override val title: String get() = getString(R.string.action_manage_presets)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = QuestPresetsAdapter(requireContext(), questPresetsController, questTypeOrderController, visibleQuestTypeController, urlConfigController)
        lifecycle.addObserver(adapter)
        binding.questPresetsList.adapter = adapter
        binding.addPresetButton.setOnClickListener { onClickAddPreset() }
    }

    private fun onClickAddPreset() {
        val ctx = context ?: return
        val dialog = EditTextDialog(ctx,
            title = ctx.getString(R.string.quest_presets_preset_add),
            hint = ctx.getString(R.string.quest_presets_preset_name),
            callback = { name -> addQuestPreset(name) }
        )
        dialog.editText.filters = arrayOf(InputFilter.LengthFilter(60))
        dialog.show()
    }

    private fun addQuestPreset(name: String) {
        viewLifecycleScope.launch(Dispatchers.IO) {
            val newPresetId = questPresetsController.add(name)
            questPresetsController.selectedId = newPresetId
        }
    }
}
