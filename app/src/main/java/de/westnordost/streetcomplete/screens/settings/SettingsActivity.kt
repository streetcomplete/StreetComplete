package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import com.russhwolf.settings.SettingsListener
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
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.databinding.ActivitySettingsBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.util.math.translate
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.android.ext.android.inject

class SettingsActivity : BaseActivity(), AbstractOsmQuestForm.Listener {

    private val binding by viewBinding(ActivitySettingsBinding::inflate)

    private val prefs by inject<Preferences>()

    private val listeners = mutableListOf<SettingsListener>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        binding.questFormContainer.setOnClickListener { popQuestForm() }

        updateContainerVisibility()
        supportFragmentManager.addOnBackStackChangedListener {
            updateContainerVisibility()
        }

        val launchQuestSelection = intent.getBooleanExtra(EXTRA_LAUNCH_QUEST_SETTINGS, false)
        binding.navHost.setContent {
            AppTheme {
                Surface {
                    SettingsNavHost(
                        onClickBack = { finish() },
                        onClickShowQuestTypeForDebug = ::onClickQuestType,
                        startDestination = if (launchQuestSelection) SettingsDestination.QuestSelection else null
                    )
                }
            }
        }

        listeners += prefs.onLanguageChanged { ActivityCompat.recreate(this) }
        listeners += prefs.onThemeChanged { ActivityCompat.recreate(this) }
    }

    override fun onDestroy() {
        super.onDestroy()
        listeners.forEach { it.deactivate() }
        listeners.clear()
    }

    //region as host for showing quest forms

    private val position get() = prefs.mapPosition

    override val displayedMapLocation: Location
        get() = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = position.latitude
            longitude = position.longitude
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

    private fun popQuestForm() {
        binding.questFormContainer.visibility = View.GONE
        supportFragmentManager.popBackStack()
    }

    private fun message(msg: String) {
        runOnUiThread {
            AlertDialog.Builder(this).setMessage(msg).show()
        }
    }

    private fun onClickQuestType(questType: QuestType) {
        if (questType !is OsmElementQuestType<*>) return

        val (element, geometry) = createMockElementWithGeometry(questType)
        val quest = OsmQuest(questType, element.type, element.id, geometry)

        val f = questType.createForm()
        if (f.arguments == null) f.arguments = bundleOf()
        f.requireArguments().putAll(
            AbstractQuestForm.createArguments(quest.key, quest.type, geometry, 30.0, 0.0)
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

        binding.questFormContainer.visibility = View.VISIBLE
        supportFragmentManager.commit {
            replace(R.id.questForm, f)
            addToBackStack(null)
        }
    }

    private fun updateContainerVisibility() {
        binding.questFormContainer.isGone = supportFragmentManager.findFragmentById(R.id.questForm) == null
    }

    private fun createMockElementWithGeometry(questType: OsmElementQuestType<*>): Pair<Element, ElementGeometry> {
        val firstPos = position.translate(20.0, 45.0)
        val secondPos = position.translate(20.0, 135.0)
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
        val geometry = ElementPolylinesGeometry(listOf(listOf(firstPos, secondPos)), position)
        // for testing quests requiring nodes code above can be commented out and this uncommented
        // val element = Node(1, centerPos, tags, 1)
        // val geometry = ElementPointGeometry(centerPos)
        return element to geometry
    }

    //endregion

    companion object {
        fun createLaunchQuestSettingsIntent(context: Context) =
            Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_LAUNCH_QUEST_SETTINGS, true)
            }

        private const val EXTRA_LAUNCH_QUEST_SETTINGS = "launch_quest_settings"
    }
}
