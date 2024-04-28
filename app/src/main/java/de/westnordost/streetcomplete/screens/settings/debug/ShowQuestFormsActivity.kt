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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.AddElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.HideOsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.databinding.FragmentShowQuestFormsBinding
import de.westnordost.streetcomplete.databinding.RowQuestDisplayBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.math.translate
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** activity only used in debug, to show all the different forms for the different quests. */
class ShowQuestFormsActivity : BaseActivity(), AbstractOsmQuestForm.Listener {

    private val binding by viewBinding(FragmentShowQuestFormsBinding::inflate)
    private val viewModel by viewModel<ShowQuestFormsViewModel>()

    private var currentQuestType: QuestType? = null

    private val filter: String get() =
        (binding.toolbarLayout.toolbar.menu.findItem(R.id.action_search).actionView as SearchView)
            .query.trim().toString()

    private val englishResources by lazy {
        val conf = Configuration(resources.configuration)
        conf.setLocale(Locale.ENGLISH)
        val localizedContext = createConfigurationContext(conf)
        localizedContext.resources
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_show_quest_forms)

        val questsAdapter = ShowQuestFormAdapter()

        val toolbar = binding.toolbarLayout.toolbar
        toolbar.navigationIcon = getDrawable(R.drawable.ic_close_24dp)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "Show Quest Forms"
        toolbar.inflateMenu(R.menu.menu_debug_quest_forms)

        val searchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                questsAdapter.quests = filterQuests(viewModel.quests, newText)
                return false
            }
        })

        val questsList = binding.showQuestFormsList
        questsAdapter.quests = filterQuests(viewModel.quests, filter)
        questsList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        questsList.layoutManager = LinearLayoutManager(this)
        questsList.adapter = questsAdapter

        binding.questFormContainer.setOnClickListener { popQuestForm() }

        updateContainerVisibility()
        supportFragmentManager.addOnBackStackChangedListener {
            updateContainerVisibility()
        }
    }

    private fun popQuestForm() {
        binding.questFormContainer.visibility = View.GONE
        supportFragmentManager.popBackStack()
        currentQuestType = null
    }

    private fun updateContainerVisibility() {
        binding.questFormContainer.isGone = supportFragmentManager.findFragmentById(R.id.questForm) == null
    }

    private fun filterQuests(quests: List<QuestType>, filter: String?): List<QuestType> {
        val words = filter.orEmpty().trim().lowercase()
        return if (words.isEmpty()) {
            quests
        } else {
            quests.filter { questTypeMatchesSearchWords(it, words.split(' ')) }
        }
    }

    private fun questTypeMatchesSearchWords(questType: QuestType, words: List<String>) =
        genericQuestTitle(resources, questType).lowercase().containsAll(words) ||
        genericQuestTitle(englishResources, questType).lowercase().containsAll(words)

    private inner class ShowQuestFormAdapter : RecyclerView.Adapter<ShowQuestFormAdapter.ViewHolder>() {
        var quests: List<QuestType> = listOf()
            set(value) {
                val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = field.size
                    override fun getNewListSize() = value.size
                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                        field[oldItemPosition] == value[newItemPosition]
                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                        areItemsTheSame(oldItemPosition, newItemPosition)
                })
                field = value.toList()
                diff.dispatchUpdatesTo(this)
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(RowQuestDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = quests.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(quests[position])
        }

        private inner class ViewHolder(val binding: RowQuestDisplayBinding) : RecyclerView.ViewHolder(binding.root) {
            fun onBind(with: QuestType) {
                binding.questIcon.setImageResource(with.icon)
                binding.questTitle.text = genericQuestTitle(itemView.resources, with)
                binding.root.setOnClickListener { onClickQuestType(with) }
            }
        }
    }

    private fun onClickQuestType(questType: QuestType) {
        if (questType !is OsmElementQuestType<*>) return

        val firstPos = viewModel.position.translate(20.0, 45.0)
        val secondPos = viewModel.position.translate(20.0, 135.0)
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
        val geometry = ElementPolylinesGeometry(listOf(listOf(firstPos, secondPos)), viewModel.position)
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
            latitude = viewModel.position.latitude
            longitude = viewModel.position.longitude
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
