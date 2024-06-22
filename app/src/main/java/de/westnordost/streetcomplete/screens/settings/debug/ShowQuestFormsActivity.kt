package de.westnordost.streetcomplete.screens.settings.debug

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
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
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.math.translate
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/** activity only used in debug, to show all the different forms for the different quests. */
class ShowQuestFormsActivity : BaseActivity(), AbstractOsmQuestForm.Listener {

    private val binding by viewBinding(FragmentShowQuestFormsBinding::inflate)
    private val viewModel by viewModel<ShowQuestFormsViewModel>()

    private var currentQuestType: QuestType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_show_quest_forms)

        binding.showQuestFormsScreen.setContent {
            AppTheme {
                Surface {
                    ShowQuestFormsScreen(
                        quests = viewModel.quests,
                        onClickQuestType = ::onClickQuestType,
                        onClickBack = { finish() }
                    )
                }
            }
        }

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

    private fun onClickQuestType(questType: QuestType) {
        if (questType !is OsmElementQuestType<*>) return

        val (element, geometry) = viewModel.createMockElementWithGeometry(questType)
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
