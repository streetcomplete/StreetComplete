package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.DisplaysTitle
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.getVisible
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import kotlinx.android.synthetic.main.fragment_quest_selection.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


/** Shows a screen in which the user can enable and disable quests as well as re-order them */
class QuestSelectionFragment : Fragment(R.layout.fragment_quest_selection),
    HasTitle, QuestSelectionAdapter.Listener {

    @Inject internal lateinit var questSelectionAdapter: QuestSelectionAdapter
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var visibleQuestTypeController: VisibleQuestTypeController
    @Inject internal lateinit var questTypeOrderList: QuestTypeOrderList

    private val parentTitleContainer: DisplaysTitle? get() =
        parentFragment as? DisplaysTitle ?: activity as? DisplaysTitle

    override val title: String get() = getString(R.string.pref_title_quests2)
    override val subtitle: String get() {
        val enabledCount = questTypeRegistry.getVisible(visibleQuestTypeController).count()
        val totalCount = questTypeRegistry.all.size
        return getString(R.string.pref_subtitle_quests, enabledCount, totalCount)
    }

    private val visibleQuestTypeSourceListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilitiesChanged() {
            lifecycleScope.launch(Dispatchers.Main) {
                parentTitleContainer?.updateTitle(this@QuestSelectionFragment)
            }
        }
    }

    init {
        Injector.applicationComponent.inject(this)
        initQuestSelectionAdapter()
        questSelectionAdapter.listener = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val questSelectionList = view.findViewById<RecyclerView>(R.id.questSelectionList)
        questSelectionList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        questSelectionList.layoutManager = LinearLayoutManager(context)
        questSelectionList.adapter = questSelectionAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_quest_selection, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val searchItem = menu.findItem(R.id.action_search)
        (searchItem.actionView as SearchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                onFilter(newText.orEmpty())
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.pref_quests_reset)
                    .setPositiveButton(android.R.string.ok) { _, _ -> onReset() }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                return true
            }
            R.id.action_deselect_all -> {
                onDeselectAll()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        visibleQuestTypeController.addListener(visibleQuestTypeSourceListener)
    }

    override fun onStop() {
        super.onStop()
        visibleQuestTypeController.removeListener(visibleQuestTypeSourceListener)
    }

    override fun onReorderedQuests(before: QuestType<*>, after: QuestType<*>) {
        lifecycleScope.launch(Dispatchers.IO) {
            questTypeOrderList.apply(before, after)
        }
    }

    override fun onChangedQuestVisibility(questType: QuestType<*>, visible: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            visibleQuestTypeController.setVisible(questType, visible)
        }
    }

    private fun onReset() {
        lifecycleScope.launch(Dispatchers.IO) {
            questTypeOrderList.clear()
            visibleQuestTypeController.clear()
            withContext(Dispatchers.Main) { initQuestSelectionAdapter() }
        }
    }

    private fun onDeselectAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            visibleQuestTypeController.setAllVisible(questTypeRegistry.all.filter { it !is OsmNoteQuestType }, false)
            withContext(Dispatchers.Main) { initQuestSelectionAdapter() }
        }
    }

    private fun onFilter(text: String) {
        questSelectionAdapter.filter = text
        val isEmpty = questSelectionAdapter.itemCount == 0
        tableHeader.isInvisible = isEmpty
        emptyText.isInvisible = !isEmpty
    }

    private fun initQuestSelectionAdapter() {
        questSelectionAdapter.questTypes = createQuestTypeVisibilityList()
    }

    private fun createQuestTypeVisibilityList(): MutableList<QuestVisibility> {
        val questTypes = questTypeRegistry.all.toMutableList()
        questTypeOrderList.sort(questTypes)
        return questTypes.map { QuestVisibility(it, visibleQuestTypeController.isVisible(it)) }.toMutableList()
    }
}
