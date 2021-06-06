package de.westnordost.streetcomplete.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import javax.inject.Inject
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.fragment_show_quest_forms.*
import kotlinx.android.synthetic.main.row_quest_display.view.*
import kotlinx.android.synthetic.main.toolbar.*

/** activity only used in debug, to show all the different forms for the different quests. */
class ShowQuestFormsActivity : AppCompatActivity(), AbstractQuestAnswerFragment.Listener {

    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var prefs: SharedPreferences

    private val showQuestFormAdapter: ShowQuestFormAdapter = ShowQuestFormAdapter()

    private var currentQuestType: QuestType<*>? = null

    init {
        Injector.applicationComponent.inject(this)
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
            popQuestForm()
        } else {
            super.onBackPressed()
        }
    }

    private fun popQuestForm(message: String? = null) {
        message?.let { AlertDialog.Builder(this).setMessage(it).show() }
        questFormContainer.visibility = View.GONE
        supportFragmentManager.popBackStack()
        currentQuestType = null
    }

    inner class ShowQuestFormAdapter: ListAdapter<QuestType<*>>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder<QuestType<*>> =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_quest_display, parent, false))

        private inner class ViewHolder(itemView: View) : ListAdapter.ViewHolder<QuestType<*>>(itemView) {
            override fun onBind(with: QuestType<*>) {
                itemView.questIcon.setImageResource(with.icon)
                itemView.questTitle.text = genericQuestTitle(itemView, with)
                itemView.setOnClickListener { onClickQuestType(with) }
            }
        }
    }

    private fun onClickQuestType(questType: QuestType<*>) {
        val latitudeDelta = 0
        val longitudeDelta = 0
        val firstLat = Double.fromBits(prefs.getLong(Prefs.MAP_LATITUDE, 0.0.toBits()))
        val firstLng = Double.fromBits(prefs.getLong(Prefs.MAP_LONGITUDE, 0.0.toBits()))
        val firstPos = LatLon(firstLat, firstLng)
        val secondLat = Double.fromBits(prefs.getLong(Prefs.MAP_LATITUDE, (0.0 + latitudeDelta).toBits()))
        val secondLng = Double.fromBits(prefs.getLong(Prefs.MAP_LONGITUDE, (0.0 + longitudeDelta).toBits()))
        val secondPos = LatLon(secondLat, secondLng)
        val centerLat = Double.fromBits(prefs.getLong(Prefs.MAP_LATITUDE, (0.0 + latitudeDelta/2).toBits()))
        val centerLng = Double.fromBits(prefs.getLong(Prefs.MAP_LONGITUDE, (0.0 + longitudeDelta/2).toBits()))
        val centerPos = LatLon(centerLat, centerLng)
        val tags =  mapOf("highway" to "cycleway", "building" to "residential", "name" to "<object name>", "opening_hours" to "Mo-Fr 08:00-12:00,13:00-17:30; Sa 08:00-12:00", "addr:housenumber" to "176")
        val element = Way(1, listOf(1, 2), tags, 1)
        val elementGeometry = ElementPolylinesGeometry(listOf(listOf(firstPos, secondPos)), centerPos)

        val quest = object : Quest {
            override val key = OsmQuestKey(element.type, element.id, questType::class.simpleName!!)
            override val position = firstPos
            override val markerLocations = listOf<LatLon>(firstPos)
            override val geometry = elementGeometry
            override val type = questType
        }

        val f = questType.createForm()
        val args = AbstractQuestAnswerFragment.createArguments(quest, element, 0f, 0f)
        if(f.arguments != null) {
            f.arguments!!.putAll(args)
        } else {
            f.arguments = args
        }



        currentQuestType = questType

        questFormContainer.visibility = View.VISIBLE
        supportFragmentManager.commit {
            replace(R.id.questForm, f)
            addToBackStack(null)
        }
    }

    override fun onAnsweredQuest(questKey: QuestKey, answer: Any) {
        val builder = StringMapChangesBuilder(mapOf())
        (currentQuestType as? OsmElementQuestType<Any>)?.applyAnswerTo(answer, builder)
        val tagging = builder.create().changes.joinToString("\n")
        AlertDialog.Builder(this)
            .setMessage("Tagging\n$tagging")
            .show()
        popQuestForm()
    }
    override fun onComposeNote(questKey: QuestKey, questTitle: String) {
        popQuestForm("Composing note")
    }
    override fun onSplitWay(osmQuestKey: OsmQuestKey) {
        popQuestForm("Splitting way")
    }
    override fun onSkippedQuest(questKey: QuestKey) {
        popQuestForm("Skipping quest")
    }
    override fun onDeletePoiNode(osmQuestKey: OsmQuestKey) {
        popQuestForm("Deleting element")
    }
    override fun onReplaceShopElement(osmQuestKey: OsmQuestKey, tags: Map<String, String>) {
        popQuestForm("Replacing shop element")
    }
}
