package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import javax.inject.Inject
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.download.QuestDownloadService
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao

class QuestSelectionFragment : Fragment(), QuestSelectionAdapter.Listener {
    @Inject internal lateinit var questSelectionAdapter: QuestSelectionAdapter
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var visibleQuestTypeDao: VisibleQuestTypeDao
    @Inject internal lateinit var questTypeOrderList: QuestTypeOrderList

    private var hasChangedSomething: Boolean = false
    set(value) {
        if (field == value) return
        field = value
        /* because if the user changed the visibility or order of quests, he might
           be surprised if the quests currently in the download queue are continued to be
           downloaded.  */
        cancelCurrentDownload()
    }

    init {
        Injector.instance.applicationComponent.inject(this)
        questSelectionAdapter.list = createQuestTypeVisibilityList()
        questSelectionAdapter.listener = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_quest_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.questSelectionList).apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = questSelectionAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.title = getString(R.string.pref_title_quests)
        hasChangedSomething = false
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
        questTypeOrderList.apply(before, after)
        hasChangedSomething = true
    }

    override fun onChangedQuestVisibility(questType: QuestType<*>, visible: Boolean) {
        visibleQuestTypeDao.setVisible(questType, visible)
        hasChangedSomething = true
    }

    private fun onReset() {
        questTypeOrderList.clear()
        visibleQuestTypeDao.clear()
        questSelectionAdapter.list = createQuestTypeVisibilityList()
        hasChangedSomething = true
    }

    private fun onDeselectAll() {
        for (questType in questTypeRegistry.all) {
            if (questType !is OsmNoteQuestType) {
                visibleQuestTypeDao.setVisible(questType, false)
            }
        }
        questSelectionAdapter.list = createQuestTypeVisibilityList()
        hasChangedSomething = true
    }

    private fun createQuestTypeVisibilityList(): MutableList<QuestVisibility> {
        val questTypes = questTypeRegistry.all.toMutableList()
        questTypeOrderList.sort(questTypes)
        return questTypes.map { QuestVisibility(it, visibleQuestTypeDao.isVisible(it)) }.toMutableList()
    }

    private fun cancelCurrentDownload() {
        context?.let { it.startService(QuestDownloadService.createCancelIntent(it)) }
    }
}
