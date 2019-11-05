package de.westnordost.streetcomplete.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode

import javax.inject.Inject
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OsmQuestAnswerListener
import de.westnordost.streetcomplete.quests.QuestAnswerComponent
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.fragment_show_quest_forms.*
import kotlinx.android.synthetic.main.row_quest_display.view.*
import kotlinx.android.synthetic.main.toolbar.*

class ShowQuestFormsActivity : AppCompatActivity(), OsmQuestAnswerListener {
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var prefs: SharedPreferences
    private val showQuestFormAdapter: ShowQuestFormAdapter = ShowQuestFormAdapter()

    init {
        Injector.instance.applicationComponent.inject(this)
        showQuestFormAdapter.list = questTypeRegistry.all.toMutableList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_show_quest_forms)
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = "Show Quest Forms"

        questFormContainer.setOnClickListener { onBackPressed() }

        showQuestFormsList.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = showQuestFormAdapter
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            questFormContainer.visibility = View.GONE
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    inner class ShowQuestFormAdapter: ListAdapter<QuestType<*>>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder<QuestType<*>> =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_quest_display, parent, false))

        private inner class ViewHolder(itemView: View) : ListAdapter.ViewHolder<QuestType<*>>(itemView) {
            override fun onBind(with: QuestType<*>) {
                itemView.questIcon.setImageResource(with.icon)
                itemView.questTitle.text = itemView.resources.getString(with.title, "…")
                itemView.setOnClickListener { onClickQuestType(with) }
            }
        }
    }

    private fun onClickQuestType(questType: QuestType<*>) {
        val lat = Double.fromBits(prefs.getLong(Prefs.MAP_LATITUDE, 0.0.toBits()))
        val lng = Double.fromBits(prefs.getLong(Prefs.MAP_LONGITUDE, 0.0.toBits()))
        val pos = OsmLatLon(lat, lng)
        val elementGeometry = ElementPointGeometry(pos)
        val element = OsmNode(1, 1, pos, mapOf("highway" to "cycleway", "building" to "residential"))

        val f = questType.createForm()
        val args = QuestAnswerComponent.createArguments(1, QuestGroup.OSM)
        args.putSerializable(AbstractQuestAnswerFragment.ARG_ELEMENT, element)
        args.putSerializable(AbstractQuestAnswerFragment.ARG_GEOMETRY, elementGeometry)
        args.putString(AbstractQuestAnswerFragment.ARG_QUESTTYPE, questType.javaClass.simpleName)
        f.arguments = args

        questFormContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.questForm, f)
            .addToBackStack(null)
            .commit()
    }

    override fun onAnsweredQuest(questId: Long, group: QuestGroup, answer: Any) { onBackPressed() }
    override fun onComposeNote(questId: Long, group: QuestGroup, questTitle: String) { onBackPressed() }
    override fun onSplitWay(osmQuestId: Long) { onBackPressed() }
    override fun onSkippedQuest(questId: Long, group: QuestGroup) { onBackPressed() }
}
