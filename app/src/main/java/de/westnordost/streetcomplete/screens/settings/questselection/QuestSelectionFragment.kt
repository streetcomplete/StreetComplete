package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.databinding.FragmentQuestSelectionBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
class QuestSelectionFragment : TwoPaneDetailFragment(R.layout.fragment_quest_selection), HasTitle {

    private val binding by viewBinding(FragmentQuestSelectionBinding::bind)
    private val viewModel by viewModel<QuestSelectionViewModel>()

    private lateinit var questSelectionAdapter: QuestSelectionAdapter

    override val title: String get() = getString(R.string.pref_title_quests2)

    override val subtitle: String get() =
        getString(
            R.string.pref_subtitle_quests_preset_name,
            viewModel.selectedQuestPresetName ?: getString(R.string.quest_presets_default_name)
        )

    private val filter: String get() =
        (binding.toolbar.root.menu.findItem(R.id.action_search).actionView as SearchView)
            .query.trim().toString()

    private val englishResources by lazy {
        val conf = Configuration(resources.configuration)
        conf.setLocale(Locale.ENGLISH)
        val localizedContext = requireContext().createConfigurationContext(conf)
        localizedContext.resources
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        questSelectionAdapter = QuestSelectionAdapter(requireContext(), viewModel)
        questSelectionAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createOptionsMenu(binding.toolbar.root)

        binding.questSelectionList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.questSelectionList.layoutManager = LinearLayoutManager(context)
        binding.questSelectionList.adapter = questSelectionAdapter

        observe(viewModel.quests) { quests ->
            questSelectionAdapter.quests = filterQuests(quests, filter)
            updateDisplayedQuestCount()
        }
    }

    private fun createOptionsMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_quest_selection)

        val searchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                questSelectionAdapter.quests = filterQuests(viewModel.quests.value, newText)
                updateDisplayedQuestCount()
                return false
            }
        })

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reset -> {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.pref_quests_reset)
                        .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.resetQuestSelectionsAndOrder() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                R.id.action_deselect_all -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.pref_quests_deselect_all)
                        .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.unselectAllQuests() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateDisplayedQuestCount() {
        val isEmpty = questSelectionAdapter.itemCount == 0
        binding.tableHeader.isInvisible = isEmpty
        binding.emptyText.isInvisible = !isEmpty
    }

    private fun filterQuests(quests: List<QuestSelection>, filter: String?): List<QuestSelection> {
        val words = filter.orEmpty().trim().lowercase()
        return if (words.isEmpty()) {
            quests
        } else {
            quests.filter { questTypeMatchesSearchWords(it.questType, words.split(' ')) }
        }
    }

    private fun questTypeMatchesSearchWords(questType: QuestType, words: List<String>) =
        genericQuestTitle(resources, questType).lowercase().containsAll(words) ||
        genericQuestTitle(englishResources, questType).lowercase().containsAll(words)
}
