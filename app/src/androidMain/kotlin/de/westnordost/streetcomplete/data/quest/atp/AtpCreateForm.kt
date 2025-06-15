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
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.view.add
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.Locale
import kotlin.getValue
import kotlinx.coroutines.withContext

//see NoteDiscussionForm TODO
class AtpCreateForm : AbstractQuestForm() {
    private val hiddenQuestsController: QuestsHiddenController by inject()
    private val featureDictionaryLazy: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val elementEditsController: ElementEditsController by inject()

    private lateinit var entry: AtpEntry private set
    private val featureDictionary: FeatureDictionary get() = featureDictionaryLazy.value
    var hideQuestController: HideQuestController = hiddenQuestsController
    var selectedLocation: LatLon? = null
    var addElementEditsController: AddElementEditsController = elementEditsController

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        selectedLocation = position
        Log.e("ATP", "onClickMapAt activated")
        checkIsFormComplete()
        return true
    }

    override fun onClickOk() {
        Log.e("ATP", "onClickOk activated")
        if(selectedLocation == null) {
            return
        } else {
            viewLifecycleScope.launch { // viewLifecycleScope is here via cargo cult - what it is doing and is it needed TODO
                applyEdit(CreateNodeAction(selectedLocation!!, entry.tagsInATP))
            }
        }
        /*
        val streetOrPlaceName = streetOrPlaceCtrl.streetOrPlaceName!!
        lastWasPlaceName = streetOrPlaceName is PlaceName
        applyAnswer(streetOrPlaceName)
         */

        /*
        // TODO from ThingsOverlayForm
    override fun onClickOk() {
        if (element == null) {
            val feature = featureCtrl.feature!!
            val tags = HashMap<String, String>()
            val builder = StringMapChangesBuilder(tags)
            feature.applyTo(builder)
            builder.create().applyTo(tags)
            applyEdit(CreateNodeAction(geometry.center, tags))
        }
    }

         */

        // TODO from AbstractOsmQuestForm
        /*
        private fun createQuestChanges(answer: T): StringMapChanges {
            val changesBuilder = StringMapChangesBuilder(element.tags)
            osmElementQuestType.applyAnswerTo(answer, changesBuilder, geometry, element.timestampEdited)
            val changes = changesBuilder.create()
            require(!changes.isEmpty()) {
                "${osmElementQuestType.name} was answered by the user but there are no changes!"
            }
            return changes
        }*/
    }

    // from abstractOverlayForm - share code somehow?
    protected fun applyEdit(answer: ElementEditAction, geometry: ElementGeometry = this.geometry) {
        viewLifecycleScope.launch {
            solve(answer, geometry)
        }
    }

    // from abstractOverlayForm - share code somehow?
    private suspend fun solve(action: ElementEditAction, geometry: ElementGeometry) {
        /*
        TODO activate
        setLocked(true)
        val isSurvey = checkIsSurvey(geometry, recentLocationStore.get())
        if (!isSurvey && !confirmIsSurvey(requireContext())) {
            setLocked(false)
            return
        }
         */
        val isSurvey = true // TODO: see above

        withContext(Dispatchers.IO) {
            addElementEditsController.add(CreatePoiBasedOnAtp(), geometry, "survey", action, isSurvey)
        }
        listener?.onEdited(CreatePoiBasedOnAtp(), geometry)
    }
    override fun isFormComplete(): Boolean {
        Log.e("ATP", "isFormComplete activated")
        return selectedLocation != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        entry = Json.decodeFromString(args.getString(ATP_ENTRY)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setTitle(getString(osmElementQuestType.getTitle(element.tags)))
        setTitleHintLabel(getNameAndLocationSpanned(Node(
            1,
            position = entry.position,
            tags = entry.tagsInATP,
            version = 1,
            timestampEdited = 1,
        ), resources, featureDictionary))
        //setObjNote(element.tags["note"])
    }

    override fun onStart() {
        super.onStart()
        updateButtonPanel()
    }

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?
    protected fun updateButtonPanel() {
        //val otherAnswersItem = AnswerItem(R.string.quest_generic_otherAnswers2) { showOtherAnswers() }
        //setButtonPanelAnswers(listOf(otherAnswersItem) + buttonPanelAnswers)
        val otherAnswersItem = AnswerItem(R.string.quest_generic_otherAnswers2) { showOtherAnswers() }
        // TODO proper buttons
        // This place does not exist
        // This place is mapped already
        val a = AnswerItem(R.string.quest_atp_add_missing_poi_mapped_already) { /*applyAnswer(false)*/ hideQuest() }
        val b = AnswerItem(R.string.quest_atp_add_missing_poi_does_not_exist) { /*applyAnswer(true)*/ hideQuest() }

        setButtonPanelAnswers(listOf(a, b, otherAnswersItem))
    }

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?
    private fun showOtherAnswers() {
        val otherAnswersButton = view?.findViewById<ViewGroup>(R.id.buttonPanel)?.children?.firstOrNull() ?: return
        val answers = assembleOtherAnswers()
        val popup = PopupMenu(requireContext(), otherAnswersButton)
        for (i in answers.indices) {
            val otherAnswer = answers[i]
            val order = answers.size - i
            popup.menu.add(Menu.NONE, i, order, otherAnswer.title)
        }
        popup.show()

        popup.setOnMenuItemClickListener { item ->
            answers[item.itemId].action()
            true
        }
    }

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?
    // TODO: get "UH" button working, see above PRIORITY
    private fun assembleOtherAnswers(): List<IAnswerItem> {
        val answers = mutableListOf<IAnswerItem>()

        answers.add(AnswerItem(R.string.quest_generic_answer_notApplicable) { onClickCantSay() })

        //answers.addAll(otherAnswers)
        return answers
    }

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?

    protected fun onClickCantSay() {
        context?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_leave_new_note_title)
            .setMessage(R.string.quest_leave_new_note_description)
            .setNegativeButton(R.string.quest_leave_new_note_no) { _, _ -> hideQuest() }
            .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ -> composeNote() }
            .show()
        }
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

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?
    private val englishResources: Resources
        get() {
            val conf = Configuration(resources.configuration)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = super.requireContext().createConfigurationContext(conf)
            return localizedContext.resources
        }

    // taken from AbstractOsmQuestForm, TODO - should common part reside somewhere?
    protected fun composeNote() {

        val questTitle = "CreatePoiBasedOnAtp" //TODO: do it properly //englishResources.getString(osmElementQuestType.getTitle(element.tags))
        val hintLabel = "TODO" //well, TODO //getNameAndLocationSpanned(element, englishResources, featureDictionary)
        val leaveNoteContext = if (hintLabel.isNullOrBlank()) {
            "Unable to answer \"$questTitle\""
        } else {
            "Unable to answer \"$questTitle\" – $hintLabel"
        }
        // TODO get it working, I guess
        //listener?.onComposeNote(osmElementQuestType, element, geometry, leaveNoteContext)
    }

    // include equivalents of
    //private val noteSource: NotesWithEditsSource by inject()
    //private val noteEditsController: NoteEditsController by inject()
    // to get ATP data from my API

    /*
    interface Listener {
        /** Called when the user successfully answered the quest */
        fun onNoteQuestSolved(questType: QuestType, noteId: Long, position: LatLon)
        /** Called when the user did not answer the quest but also did not hide it */
        fun onNoteQuestClosed()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener
     */

    companion object {
        private const val ATP_ENTRY = "atp_entry"

        fun createArguments(entry: AtpEntry) = bundleOf(
            ATP_ENTRY to Json.encodeToString(entry)
        )
    }
}
