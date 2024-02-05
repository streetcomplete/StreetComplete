package de.westnordost.streetcomplete.screens.settings.debug

import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.AddElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.HideOsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.databinding.FragmentShowQuestFormsBinding
import de.westnordost.streetcomplete.databinding.RowQuestDisplayBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.math.translate
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.ListAdapter
import org.koin.android.ext.android.inject
import java.util.Locale

/** activity only used in debug, to show all the different forms for the different quests. */
class ShowQuestFormsActivity : BaseActivity(), AbstractOsmQuestForm.Listener {

    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val prefs: Preferences by inject()

    private val binding by viewBinding(FragmentShowQuestFormsBinding::inflate)

    private val showQuestFormAdapter: ShowQuestFormAdapter = ShowQuestFormAdapter()

    private var currentQuestType: QuestType? = null

    private var pos: LatLon = LatLon(0.0, 0.0)

    init {
        showQuestFormAdapter.list = questTypeRegistry.toMutableList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_show_quest_forms)

        val toolbar = binding.toolbarLayout.toolbar
        toolbar.navigationIcon = getDrawable(R.drawable.ic_close_24dp)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "Show Quest Forms"
        toolbar.inflateMenu(R.menu.menu_debug_quest_forms)

        val searchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                showQuestFormAdapter.filter = newText.orEmpty()
                return false
            }
        })

        binding.questFormContainer.setOnClickListener { popQuestForm() }

        binding.showQuestFormsList.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = showQuestFormAdapter
        }

        updateContainerVisibility()
        supportFragmentManager.addOnBackStackChangedListener {
            updateContainerVisibility()
        }
    }

    override fun onStart() {
        super.onStart()
        pos = LatLon(
            prefs.getDouble(Prefs.MAP_LATITUDE, 0.0),
            prefs.getDouble(Prefs.MAP_LONGITUDE, 0.0)
        )
    }

    private fun popQuestForm() {
        binding.questFormContainer.visibility = View.GONE
        supportFragmentManager.popBackStack()
        currentQuestType = null
    }

    private fun updateContainerVisibility() {
        binding.questFormContainer.isGone = supportFragmentManager.findFragmentById(R.id.questForm) == null
    }

    inner class ShowQuestFormAdapter : ListAdapter<QuestType>() {
        private val englishResources by lazy {
            val conf = Configuration(resources.configuration)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = createConfigurationContext(conf)
            localizedContext.resources
        }

        var filter: String = ""
            set(value) {
                val n = value.trim()
                if (n != field) {
                    field = n
                    filterQuestTypes(field)
                }
            }

        private fun questTypeMatchesSearchWords(questType: QuestType, words: List<String>) =
            genericQuestTitle(resources, questType).lowercase().containsAll(words)
                || genericQuestTitle(englishResources, questType).lowercase().containsAll(words)

        private fun filterQuestTypes(f: String) {
            if (f.isEmpty()) {
                list = questTypeRegistry.toMutableList()
            } else {
                val words = f.lowercase().split(' ')
                list = questTypeRegistry.filter { questTypeMatchesSearchWords(it, words) }.toMutableList()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder<QuestType> =
            ViewHolder(RowQuestDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        private inner class ViewHolder(val binding: RowQuestDisplayBinding) : ListAdapter.ViewHolder<QuestType>(binding) {
            override fun onBind(with: QuestType) {
                binding.questIcon.setImageResource(with.icon)
                binding.questTitle.text = genericQuestTitle(itemView.resources, with)
                binding.root.setOnClickListener { onClickQuestType(with) }
            }
        }
    }

    private fun onClickQuestType(questType: QuestType) {
        if (questType !is OsmElementQuestType<*>) return

        val firstPos = pos.translate(20.0, 45.0)
        val secondPos = pos.translate(20.0, 135.0)
        /* tags are values that results in more that quests working on showing/solving debug quest
           form, i.e. some quests expect specific tags to be set and crash without them - what is
           OK, but here some tag combination needs to be setup to reduce number of crashes when
           using test forms */
        val tags = mapOf(
            "highway" to "cycleway",
            "building" to "residential",
            "name" to "<object name>",
            "opening_hours" to "Mo-Fr 08:00-12:00,13:00-17:30; Sa 08:00-12:00",
            "addr:housenumber" to "176"
        )
        // way geometry is needed by quests using clickable way display (steps direction, sidewalk quest, lane quest, cycleway quest...)
        val element = Way(1, listOf(1, 2), tags, 1)
        val geometry = ElementPolylinesGeometry(listOf(listOf(firstPos, secondPos)), pos)
        // for testing quests requiring nodes code above can be commented out and this uncommented
        // val element = Node(1, centerPos, tags, 1)
        // val geometry = ElementPointGeometry(centerPos)

        val quest = OsmQuest(questType, element.type, element.id, geometry)

        val f = questType.createForm()
        if (f.arguments == null) f.arguments = bundleOf()
        f.requireArguments().putAll(
            AbstractQuestForm.createArguments(quest.key, quest.type, geometry, 30.0f, 0.0f)
        )
        f.requireArguments().putAll(AbstractOsmQuestForm.createArguments(element))
        f.hideOsmQuestController = object : HideOsmQuestController {
            override fun hide(key: OsmQuestKey) {}
        }
        f.addElementEditsController = object : AddElementEditsController {
            override fun add(
                type: ElementEditType,
                geometry: ElementGeometry,
                source: String,
                action: ElementEditAction,
                isNearUserLocation: Boolean
            ) {
                when (action) {
                    is DeletePoiNodeAction -> {
                        message("Deleted node")
                    }
                    is UpdateElementTagsAction -> {
                        val tagging = action.changes.changes.joinToString("\n")
                        message("Tagging\n$tagging")
                    }
                }
            }
        }

        currentQuestType = questType

        binding.questFormContainer.visibility = View.VISIBLE
        supportFragmentManager.commit {
            replace(R.id.questForm, f)
            addToBackStack(null)
        }
    }

    override val displayedMapLocation: Location
        get() = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = pos.latitude
            longitude = pos.longitude
        }

    override fun onEdited(editType: ElementEditType, geometry: ElementGeometry) {
        popQuestForm()
    }

    override fun onComposeNote(
        editType: ElementEditType,
        element: Element,
        geometry: ElementGeometry,
        leaveNoteContext: String,
    ) {
        message("Composing note")
        popQuestForm()
    }

    override fun onSplitWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry) {
        message("Splitting way")
        popQuestForm()
    }

    override fun onMoveNode(editType: ElementEditType, node: Node) {
        message("Moving node")
        popQuestForm()
    }

    override fun onQuestHidden(osmQuestKey: OsmQuestKey) {
        popQuestForm()
    }

    private fun message(msg: String) {
        runOnUiThread {
            AlertDialog.Builder(this).setMessage(msg).show()
        }
    }
}
