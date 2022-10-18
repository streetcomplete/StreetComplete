package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.databinding.FragmentQuestSelectionBinding
import de.westnordost.streetcomplete.screens.DisplaysTitle
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.concurrent.FutureTask

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
class QuestSelectionFragment : Fragment(R.layout.fragment_quest_selection), HasTitle {

    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val questPresetsSource: QuestPresetsSource by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val questTypeOrderController: QuestTypeOrderController by inject()
    private val countryBoundaries: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val prefs: SharedPreferences by inject()

    private val binding by viewBinding(FragmentQuestSelectionBinding::bind)

    private lateinit var questSelectionAdapter: QuestSelectionAdapter

    private val parentTitleContainer: DisplaysTitle? get() =
        parentFragment as? DisplaysTitle ?: activity as? DisplaysTitle

    override val title: String get() = getString(R.string.pref_title_quests2)

    override val subtitle: String get() {
        val presetName = questPresetsSource.selectedQuestPresetName ?: getString(R.string.quest_presets_default_name)
        return getString(R.string.pref_subtitle_quests_preset_name, presetName)
    }

    private val visibleQuestTypeListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) {
            viewLifecycleScope.launch { updateTitle() }
        }
        override fun onQuestTypeVisibilitiesChanged() {
            viewLifecycleScope.launch { updateTitle() }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        questSelectionAdapter = QuestSelectionAdapter(
            requireContext(), visibleQuestTypeController, questTypeOrderController,
            questTypeRegistry, countryBoundaries, prefs
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        viewLifecycleOwner.lifecycle.addObserver(questSelectionAdapter)
        binding.questSelectionList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.questSelectionList.layoutManager = LinearLayoutManager(context)
        binding.questSelectionList.adapter = questSelectionAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_quest_selection, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val searchItem = menu.findItem(R.id.action_search)
        (searchItem.actionView as SearchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterQuestsByString(newText.orEmpty())
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.pref_quests_reset)
                    .setPositiveButton(android.R.string.ok) { _, _ -> resetQuestVisibilitiesAndOrder() }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                return true
            }
            R.id.action_deselect_all -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_quests_deselect_all)
                    .setPositiveButton(android.R.string.ok) { _, _ -> deselectAllQuests() }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        visibleQuestTypeController.addListener(visibleQuestTypeListener)
    }

    override fun onStop() {
        super.onStop()
        visibleQuestTypeController.removeListener(visibleQuestTypeListener)
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

    private fun filterQuestsByString(text: String) {
        questSelectionAdapter.filter = text
        val isEmpty = questSelectionAdapter.itemCount == 0
        binding.tableHeader.isInvisible = isEmpty
        binding.emptyText.isInvisible = !isEmpty
    }

    private fun updateTitle() {
        parentTitleContainer?.updateTitle(this)
    }
}
