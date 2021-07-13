package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import kotlinx.android.synthetic.main.fragment_quest_presets.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuestPresetsFragment : DialogFragment(R.layout.fragment_quest_presets) {

    @Inject internal lateinit var questPresetsController: QuestPresetsController
    @Inject internal lateinit var questPresetsAdapter: QuestPresetsAdapter

    init {
        Injector.applicationComponent.inject(this)
        lifecycle.addObserver(questPresetsAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_Bubble_Dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.questProfilesList.adapter = questPresetsAdapter
        view.nameInput.addTextChangedListener { updateAddButtonEnablement() }
        view.addButton.setOnClickListener { onClickAddProfile() }
        updateAddButtonEnablement()
    }

    private fun updateAddButtonEnablement() {
        val view = view ?: return
        view.addButton.isEnabled = view.nameInput.text.toString().trim().isNotBlank()
    }

    private fun onClickAddProfile() {
        lifecycleScope.launch { view?.let { view ->
            view.addButton.isEnabled = false
            val name = view.nameInput.text.toString().trim()
            withContext(Dispatchers.IO) {
                questPresetsController.addQuestProfile(name)
            }
            view.addButton?.isEnabled = true
            view.nameInput?.setText("")
        }}
    }

    companion object {
        const val TAG = "QuestProfilesFragment"
    }
}

