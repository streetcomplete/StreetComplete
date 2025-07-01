package de.westnordost.streetcomplete.data.quest.atp

import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.children
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.osm.edits.AddElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.visiblequests.HideQuestController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.IAnswerItem
import de.westnordost.streetcomplete.quests.getTitle
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.confirmIsSurvey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.Locale
import kotlin.getValue
import kotlinx.coroutines.withContext

class AtpCreateForm : AbstractQuestForm() {
    private val hiddenQuestsController: QuestsHiddenController by inject()
    private val featureDictionaryLazy: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val elementEditsController: ElementEditsController by inject()
    private val surveyChecker: SurveyChecker by inject()

    private lateinit var entry: AtpEntry private set
    private val featureDictionary: FeatureDictionary get() = featureDictionaryLazy.value
    var hideQuestController: HideQuestController = hiddenQuestsController
    var selectedLocation: LatLon? = null
    var addElementEditsController: AddElementEditsController = elementEditsController

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        selectedLocation = position
        checkIsFormComplete()
        return true
    }

    override fun onClickOk() {
        if(selectedLocation == null) {
            return
        } else {
            viewLifecycleScope.launch { // viewLifecycleScope is here via cargo cult - what it is doing and is it needed TODO
                applyEdit(CreateNodeAction(selectedLocation!!, entry.tagsInATP))
            }
        }
    }

    protected fun applyEdit(answer: ElementEditAction, geometry: ElementGeometry = this.geometry) {
        viewLifecycleScope.launch {
            solve(answer, geometry)
        }
    }

    private suspend fun solve(action: ElementEditAction, geometry: ElementGeometry) {
        setLocked(true)
        val isSurvey = surveyChecker.checkIsSurvey(geometry)
        if (!isSurvey && !confirmIsSurvey(requireContext())) {
            setLocked(false)
            return
        }

        withContext(Dispatchers.IO) {
            addElementEditsController.add(CreatePoiBasedOnAtp(), geometry, "survey", action, isSurvey)
        }
        listener?.onEdited(CreatePoiBasedOnAtp(), geometry)
    }
    override fun isFormComplete(): Boolean {
        return selectedLocation != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        entry = Json.decodeFromString(args.getString(ATP_ENTRY)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: maybe should be more prominent?
        setTitleHintLabel(getNameAndLocationSpanned(Node(
            1,
            position = entry.position,
            tags = entry.tagsInATP,
            version = 1,
            timestampEdited = 1,
        ), resources, featureDictionary))
    }

    override fun onStart() {
        super.onStart()
        updateButtonPanel()
    }

    protected fun updateButtonPanel() {
        // TODO: create answers to send to API, not just hide quests
        val mappedAlready = AnswerItem(R.string.quest_atp_add_missing_poi_mapped_already) { /*applyAnswer(false)*/ hideQuest() }
        val missing = AnswerItem(R.string.quest_atp_add_missing_poi_does_not_exist) { /*applyAnswer(true)*/ hideQuest() }
        val cantSay = AnswerItem(R.string.quest_generic_answer_notApplicable) { hideQuest() /* no option to leave note */ }

        setButtonPanelAnswers(listOf(mappedAlready, missing, cantSay))
    }

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?
    // TODO should something listen using this listener?
    interface Listener {
        /** The GPS position at which the user is displayed at */
        val displayedMapLocation: Location?

        /** Called when the user successfully answered the quest */
        fun onEdited(editType: ElementEditType, geometry: ElementGeometry)

        /** Called when the user successfully answered the quest */
        fun onRejectedAtpEntry(editType: ElementEditType, geometry: ElementGeometry)

        /** Called when the user chose to move the node */
        fun onMoveNode(editType: ElementEditType, node: Node)

        /** Called when the user chose to hide the quest instead */
        fun onQuestHidden(questKey: QuestKey)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?
    protected fun hideQuest() {
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) { hideQuestController.hide(questKey) }
            listener?.onQuestHidden(questKey)
        }
    }

    companion object {
        private const val ATP_ENTRY = "atp_entry"

        fun createArguments(entry: AtpEntry) = bundleOf(
            ATP_ENTRY to Json.encodeToString(entry)
        )
    }
}
