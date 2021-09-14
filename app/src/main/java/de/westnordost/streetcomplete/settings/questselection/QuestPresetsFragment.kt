package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import kotlinx.android.synthetic.main.dialog_input_text.view.*
import kotlinx.android.synthetic.main.fragment_quest_presets.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuestPresetsFragment : Fragment(R.layout.fragment_quest_presets), HasTitle {

    @Inject internal lateinit var questPresetsController: QuestPresetsController
    @Inject internal lateinit var questPresetsAdapter: QuestPresetsAdapter

    override val title: String get() = getString(R.string.action_manage_presets)

    init {
        Injector.applicationComponent.inject(this)
        lifecycle.addObserver(questPresetsAdapter)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.questProfilesList.adapter = questPresetsAdapter
        view.addPresetButton.setOnClickListener { onClickAddProfile() }
    }

    private fun onClickAddProfile() {
        val ctx = context ?: return

        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_input_text, null)
        val input = view.editText
        input.hint = ctx.getString(R.string.quest_presets_preset_name)

        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_presets_preset_add)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _,_ ->
                val name = input.text.toString().trim()
                lifecycleScope.launch(Dispatchers.IO) {
                    questPresetsController.addQuestProfile(name)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

    }
}

