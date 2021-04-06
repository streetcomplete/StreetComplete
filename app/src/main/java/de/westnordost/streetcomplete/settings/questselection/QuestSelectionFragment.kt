package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.HasTitle

import javax.inject.Inject
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
class QuestSelectionFragment
    : Fragment(R.layout.fragment_quest_selection), HasTitle, QuestSelectionAdapter.Listener {

    @Inject internal lateinit var questSelectionAdapter: QuestSelectionAdapter
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var visibleQuestTypeController: VisibleQuestTypeController
    @Inject internal lateinit var questTypeOrderList: QuestTypeOrderList

    override val title: String get() = getString(R.string.pref_title_quests)

    init {
        Injector.applicationComponent.inject(this)
        questSelectionAdapter.list = createQuestTypeVisibilityList()
        questSelectionAdapter.listener = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        view.findViewById<RecyclerView>(R.id.questSelectionList).apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = questSelectionAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_quest_selection, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                context?.let {
                    AlertDialog.Builder(it)
                        .setMessage(R.string.pref_quests_reset)
                        .setPositiveButton(android.R.string.ok) { _, _ -> onReset() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
                return true
            }
            R.id.action_deselect_all -> {
                onDeselectAll()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
            questSelectionAdapter.list = createQuestTypeVisibilityList()
        }
    }

    private fun onDeselectAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            visibleQuestTypeController.setAllVisible(questTypeRegistry.all.filter { it !is OsmNoteQuestType }, false)
            questSelectionAdapter.list = createQuestTypeVisibilityList()
        }
    }

    private fun createQuestTypeVisibilityList(): MutableList<QuestVisibility> {
        val questTypes = questTypeRegistry.all.toMutableList()
        questTypeOrderList.sort(questTypes)
        return questTypes.map { QuestVisibility(it, visibleQuestTypeController.isVisible(it)) }.toMutableList()
    }
}
