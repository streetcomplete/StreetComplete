package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestProfilesController
import kotlinx.android.synthetic.main.fragment_quest_profiles.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuestProfilesFragment : DialogFragment(R.layout.fragment_quest_profiles) {

    @Inject internal lateinit var questProfilesController: QuestProfilesController
    @Inject internal lateinit var questProfilesAdapter: QuestProfilesAdapter

    init {
        Injector.applicationComponent.inject(this)
        lifecycle.addObserver(questProfilesAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_Bubble_Dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.questProfilesList.adapter = questProfilesAdapter
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
                questProfilesController.addQuestProfile(name)
            }
            view.addButton?.isEnabled = true
            view.nameInput?.setText("")
        }}
    }

    companion object {
        const val TAG = "QuestProfilesFragment"
    }
}

