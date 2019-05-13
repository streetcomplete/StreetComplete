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
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao

class QuestSelectionFragment : Fragment() {
    private val questSelectionAdapter: QuestSelectionAdapter
	private val questTypeRegistry: QuestTypeRegistry
	private val visibleQuestTypeDao: VisibleQuestTypeDao
	private val questTypeOrderList: QuestTypeOrderList

	init {
		val fields =
			InjectedFields()
		Injector.instance.applicationComponent.inject(fields)
	    questSelectionAdapter = fields.questSelectionAdapter
		questTypeRegistry = fields.questTypeRegistry
		visibleQuestTypeDao = fields.visibleQuestTypeDao
		questTypeOrderList = fields.questTypeOrderList
	}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.fragment_quest_selection, container, false)

        questSelectionAdapter.list = createQuestTypeVisibilityList()

        val questSelectionList = view.findViewById<RecyclerView>(R.id.questSelectionList)
        questSelectionList.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
        questSelectionList.layoutManager = LinearLayoutManager(context)
        questSelectionList.adapter = questSelectionAdapter

        return view
    }

    override fun onStart() {
        super.onStart()
        activity?.title = getString(R.string.pref_title_quests)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_quest_selection, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
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

    private fun onReset() {
        questTypeOrderList.clear()
        visibleQuestTypeDao.clear()
        questSelectionAdapter.list = createQuestTypeVisibilityList()
    }

    private fun onDeselectAll() {
        for (questType in questTypeRegistry.all) {
            if (questType !is OsmNoteQuestType) {
                visibleQuestTypeDao.setVisible(questType, false)
            }
        }
        questSelectionAdapter.list = createQuestTypeVisibilityList()
    }

    private fun createQuestTypeVisibilityList(): MutableList<QuestVisibility> {
        val questTypes = questTypeRegistry.all.toMutableList()
        questTypeOrderList.sort(questTypes)
	    return questTypes.map { QuestVisibility(it, visibleQuestTypeDao.isVisible(it)) }.toMutableList()
    }

	internal class InjectedFields {
		@Inject internal lateinit var questSelectionAdapter: QuestSelectionAdapter
		@Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
		@Inject internal lateinit var visibleQuestTypeDao: VisibleQuestTypeDao
		@Inject internal lateinit var questTypeOrderList: QuestTypeOrderList
	}
}
