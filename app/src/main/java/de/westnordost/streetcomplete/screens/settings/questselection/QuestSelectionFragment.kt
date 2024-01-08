package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.databinding.FragmentQuestSelectionBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneDetailFragment
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.concurrent.FutureTask

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
class QuestSelectionFragment : TwoPaneDetailFragment(R.layout.fragment_quest_selection), HasTitle {

    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val questPresetsSource: QuestPresetsSource by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val questTypeOrderController: QuestTypeOrderController by inject()
    private val countryBoundaries: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val prefs: Preferences by inject()

    private val binding by viewBinding(FragmentQuestSelectionBinding::bind)

    private lateinit var questSelectionAdapter: QuestSelectionAdapter

    override val title: String get() = getString(R.string.pref_title_quests2)

    override val subtitle: String get() {
        val presetName = questPresetsSource.selectedQuestPresetName ?: getString(R.string.quest_presets_default_name)
        return getString(R.string.pref_subtitle_quests_preset_name, presetName)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        questSelectionAdapter = QuestSelectionAdapter(
            requireContext(), visibleQuestTypeController, questTypeOrderController,
            questTypeRegistry, ::onDisplayedListSizeChanged, countryBoundaries, prefs
        )
        questSelectionAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createOptionsMenu(binding.toolbar.root)

        viewLifecycleOwner.lifecycle.addObserver(questSelectionAdapter)
        binding.questSelectionList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.questSelectionList.layoutManager = LinearLayoutManager(context)
        binding.questSelectionList.adapter = questSelectionAdapter
    }

    private fun createOptionsMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_quest_selection)

        val searchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                questSelectionAdapter.filter = newText.orEmpty()
                return false
            }
        })

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reset -> {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.pref_quests_reset)
                        .setPositiveButton(android.R.string.ok) { _, _ -> resetQuestVisibilitiesAndOrder() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                R.id.action_deselect_all -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.pref_quests_deselect_all)
                        .setPositiveButton(android.R.string.ok) { _, _ -> deselectAllQuests() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun resetQuestVisibilitiesAndOrder() {
        viewLifecycleScope.launch(Dispatchers.IO) {
            visibleQuestTypeController.clearVisibilities()
            questTypeOrderController.clear()
        }
    }

    private fun deselectAllQuests() {
        viewLifecycleScope.launch(Dispatchers.IO) {
            visibleQuestTypeController.setVisibilities(questTypeRegistry.associateWith { false })
        }
    }

    private fun onDisplayedListSizeChanged(size: Int) {
        val isEmpty = size == 0
        binding.tableHeader.isInvisible = isEmpty
        binding.emptyText.isInvisible = !isEmpty
    }
}
