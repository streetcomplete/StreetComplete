package de.westnordost.streetcomplete.quests

import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.children
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.AddElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.HideOsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.replaceShop
import de.westnordost.streetcomplete.quests.custom.CustomQuestList
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import de.westnordost.streetcomplete.screens.main.checkIsSurvey
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.add
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.Locale
import java.util.concurrent.FutureTask

/** Abstract base class for any bottom sheet with which the user answers a specific quest(ion)  */
abstract class AbstractOsmQuestForm<T> : AbstractQuestForm(), IsShowingQuestDetails {

    // dependencies
    private val elementEditsController: ElementEditsController by inject()
    private val noteEditsController: NoteEditsController by inject()
    private val osmQuestController: OsmQuestController by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val customQuestList: CustomQuestList by inject()

    protected val featureDictionary: FeatureDictionary get() = featureDictionaryFuture.get()

    // only used for testing / only used for ShowQuestFormsActivity! Found no better way to do this
    var addElementEditsController: AddElementEditsController = elementEditsController
    var hideOsmQuestController: HideOsmQuestController = osmQuestController

    // passed in parameters
    private val osmElementQuestType: OsmElementQuestType<T> get() = questType as OsmElementQuestType<T>
    protected lateinit var element: Element private set

    private val englishResources: Resources
        get() {
            val conf = Configuration(resources.configuration)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = super.requireContext().createConfigurationContext(conf)
            return localizedContext.resources
        }

    // overridable by child classes
    open val otherAnswers = listOf<IAnswerItem>()
    open val buttonPanelAnswers = listOf<IAnswerItem>()

    interface Listener { // this is also used in AbstractOtherQuestForm for convenience
        /** The GPS position at which the user is displayed at */
        val displayedMapLocation: Location?

        /** Called when the user successfully answered the quest */
        fun onEdited(editType: ElementEditType, element: Element, geometry: ElementGeometry)

        /** Called when the user chose to leave a note instead */
        fun onComposeNote(editType: ElementEditType, element: Element, geometry: ElementGeometry, leaveNoteContext: String)

        /** Called when the user chose to split the way */
        fun onSplitWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry)

        /** Called when the user chose to move the node */
        fun onMoveNode(editType: ElementEditType, node: Node)

        /** Called when the user chose to hide the quest instead */
        fun onQuestHidden(questKey: QuestKey)

        /** Called when the user chose to edit tags */
        fun onEditTags(element: Element, geometry: ElementGeometry, questKey: QuestKey?)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        element = Json.decodeFromString(args.getString(ARG_ELEMENT)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(resources.getHtmlQuestTitle(osmElementQuestType, element.tags))
        setTitleHintLabel(getNameAndLocationLabel(element, resources, featureDictionary))

        if (!TagEditor.showingTagEditor && prefs.getBoolean(Prefs.SHOW_HIDE_BUTTON, false)) {
            floatingBottomView2.popIn()
            floatingBottomView2.setOnClickListener {
                tempHideQuest()
            }
            floatingBottomView2.setOnLongClickListener {
                hideQuest()
                true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateButtonPanel()
    }

    protected fun updateButtonPanel() {
        val answers = assembleOtherAnswers()
        if (answers.isEmpty()) {
            setButtonPanelAnswers(buttonPanelAnswers)
            return
        }
        val otherAnswersItem = if (answers.size == 1) {
            answers.single()
        } else {
            AnswerItem(R.string.quest_generic_otherAnswers) { showOtherAnswers() }
        }
        setButtonPanelAnswers(listOf(otherAnswersItem) + buttonPanelAnswers)
    }

    private fun assembleOtherAnswers(): List<IAnswerItem> {
        val answers = mutableListOf<IAnswerItem>()

        if (TagEditor.showingTagEditor) {
            // only allow few of the quest specific other answers in tag edit mode
            createItsPrivateAnswer()?.let { answers.add(it) }
            answers.addAll(otherAnswers)
            return answers
        }
        answers.add(AnswerItem(R.string.quest_generic_answer_notApplicable) { onClickCantSay() })

        if (prefs.getBoolean(Prefs.EXPERT_MODE, false))
            answers.add(AnswerItem(R.string.quest_generic_answer_show_edit_tags) { listener?.onEditTags(element, geometry, questKey) })

        if (element.isSplittable()) {
            answers.add(AnswerItem(R.string.quest_generic_answer_differs_along_the_way) { onClickSplitWayAnswer() })
        }
        createDeleteOrReplaceElementAnswer()?.let { answers.add(it) }
        if (prefs.getBoolean(Prefs.EXPERT_MODE, false)) {
            createItsPrivateAnswer()?.let { answers.add(it) }
            createItsDemolishedAnswer()?.let { answers.add(it) }
        }

        if (element is Node // add moveNodeAnswer only if it's a free floating node
                && (prefs.getBoolean(Prefs.EXPERT_MODE, false) ||
                    (mapDataWithEditsSource.getWaysForNode(element.id).isEmpty()
                    && mapDataWithEditsSource.getRelationsForNode(element.id).isEmpty())
                )) {
            answers.add(AnswerItem(R.string.move_node) { onClickMoveNodeAnswer() })
        }

        answers.addAll(otherAnswers)
        return answers
    }

    private fun createDeleteOrReplaceElementAnswer(): AnswerItem? {
        val isDeletePoiEnabled = osmElementQuestType.isDeleteElementEnabled && element.type == ElementType.NODE
        val isReplaceShopEnabled = osmElementQuestType.isReplaceShopEnabled
        if (!isDeletePoiEnabled && !isReplaceShopEnabled) return null

        return AnswerItem(R.string.quest_generic_answer_does_not_exist) {
            if (isReplaceShopEnabled) replaceShop() // allow both being enabled, but prefer replace over delete
            else deletePoiNode()
        }
    }

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

    protected fun onClickCantSay() {
        context?.let {
            val b = AlertDialog.Builder(it)
                .setTitle(R.string.quest_leave_new_note_title)
                .setMessage(R.string.quest_leave_new_note_description)
                .setNegativeButton(R.string.quest_leave_new_note_no) { _, _ -> hideQuest() }
                .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ -> composeNote() }
            if (prefs.getBoolean(Prefs.CREATE_EXTERNAL_QUESTS, false))
                b.setNeutralButton(R.string.create_custom_quest_button) { _, _ ->
                    val text = EditText(it)
                    text.isSingleLine = true
                    text.setPadding(30, 10, 30, 10)
                    AlertDialog.Builder(it)
                        .setTitle(R.string.create_custom_quest_title_message)
                        .setView(text)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            customQuestList.addEntry(element, text.text.toString())
                            listener?.onQuestHidden(questKey) // abuse this to close the quest form
                        }
                        .show()
                }
            b.show()
        }
    }

    private fun onClickSplitWayAnswer() {
        context?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_split_way_description)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.onSplitWay(osmElementQuestType, element as Way, geometry as ElementPolylinesGeometry)
            }
            .show()
        }
    }

    private fun onClickMoveNodeAnswer() {
        context?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_move_node_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.onMoveNode(osmElementQuestType, element as Node)
            }
            .show()
        }
    }

    protected fun applyAnswer(answer: T) {
        viewLifecycleScope.launch {
            if (TagEditor.showingTagEditor) {
                val changesBuilder = StringMapChangesBuilder(element.tags)
                osmElementQuestType.applyAnswerTo(answer, changesBuilder, geometry, element.timestampEdited)
                TagEditor.changes = changesBuilder.create()
            } else
                solve(UpdateElementTagsAction(createQuestChanges(answer)))
        }
    }

    private fun createQuestChanges(answer: T): StringMapChanges {
        val changesBuilder = StringMapChangesBuilder(element.tags)
        osmElementQuestType.applyAnswerTo(answer, changesBuilder, geometry, element.timestampEdited)
        val changes = changesBuilder.create()
        require(!changes.isEmpty()) {
            "${osmElementQuestType.name} was answered by the user but there are no changes!"
        }
        return changes
    }

    protected fun composeNote() {
        val questTitle = englishResources.getQuestTitle(osmElementQuestType, element.tags)
        val leaveNoteContext = "Unable to answer \"$questTitle\""
        listener?.onComposeNote(osmElementQuestType, element, geometry, leaveNoteContext)
    }

    protected fun tempHideQuest() {
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) { hideOsmQuestController.tempHide(questKey as OsmQuestKey) }
            listener?.onQuestHidden(questKey as OsmQuestKey)
        }
    }

    protected fun hideQuest() {
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) { hideOsmQuestController.hide(questKey as OsmQuestKey) }
            listener?.onQuestHidden(questKey as OsmQuestKey)
        }
    }

    protected fun replaceShop() {
        if (IS_SHOP_OR_DISUSED_SHOP_EXPRESSION.matches(element)) {
            ShopGoneDialog(
                requireContext(),
                element.geometryType,
                countryOrSubdivisionCode,
                featureDictionary,
                onSelectedFeature = this::onShopReplacementSelected,
                onLeaveNote = this::composeNote,
                geometry.center
            ).show()
        } else {
            composeNote()
        }
    }

    private fun onShopReplacementSelected(tags: Map<String, String>) {
        viewLifecycleScope.launch {
            val builder = StringMapChangesBuilder(element.tags)
            builder.replaceShop(tags)
            solve(UpdateElementTagsAction(builder.create()))
        }
    }

    protected fun deletePoiNode() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.osm_element_gone_description)
            .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ -> onDeletePoiNodeConfirmed() }
            .setNeutralButton(R.string.leave_note) { _, _ -> composeNote() }
            .show()
    }

    private fun onDeletePoiNodeConfirmed() {
        viewLifecycleScope.launch {
            solve(DeletePoiNodeAction)
        }
    }

    private fun createItsPrivateAnswer(): AnswerItem? {
        return if (elementWithoutAccessTagsFilter.matches(element)
                && (highwaysFilter.matches(element) || otherFilter.matches(element))
            )
            AnswerItem(R.string.quest_private) {
                viewLifecycleScope.launch {
                    val builder = StringMapChangesBuilder(element.tags)
                    builder["access"] = "private"
                    solve(UpdateElementTagsAction(builder.create()))
                }
            }
        else null
    }

    private fun createItsDemolishedAnswer(): AnswerItem? {
        if (!element.isArea()) return null
        return if (buildingsFilter.matches(element))
            AnswerItem(R.string.quest_building_demolished) {
                viewLifecycleScope.launch {
                    val builder = StringMapChangesBuilder(element.tags)
                    builder["demolished:building"] = builder["building"] ?: "yes"
                    builder.remove("building")
                    solve(UpdateElementTagsAction(builder.create()))
                }
            }
        else null
    }

    private suspend fun solve(action: ElementEditAction) {
        if (TagEditor.showingTagEditor) return
        setLocked(true)
        if (!checkIsSurvey(requireContext(), geometry, listOfNotNull(listener?.displayedMapLocation))) {
            setLocked(false)
            return
        }
        fun doThatStuff() {
            if (action is UpdateElementTagsAction && !action.changes.isValid()) {
                val questTitle = englishResources.getQuestTitle(osmElementQuestType, element.tags)
                val text = createNoteTextForTooLongTags(questTitle, element.type, element.id, action.changes.changes)
                noteEditsController.add(0, NoteEditAction.CREATE, geometry.center, text)
            } else {
                addElementEditsController.add(osmElementQuestType, element, geometry, "survey", action)
            }
        }
        if (prefs.getBoolean(Prefs.CLOSE_FORM_IMMEDIATELY_AFTER_SOLVING, false) && !prefs.getBoolean(Prefs.SHOW_NEXT_QUEST_IMMEDIATELY, false)) {
            viewLifecycleScope.launch {
                // Only listener is mainFragment for closing bottom sheet and showing the quest
                // solved animation, so it's ok to call even though the edit was not done yet.
                listener?.onEdited(osmElementQuestType, element, geometry)
            }
            // hides the quest pin immediately (and would close bottom sheet without solved animation)
            hideOsmQuestController.tempHide(questKey as OsmQuestKey)
            withContext(Dispatchers.IO) { doThatStuff() }
        } else {
            withContext(Dispatchers.IO) { doThatStuff() }
            listener?.onEdited(osmElementQuestType, element, geometry)
        }
    }

    companion object {
        private const val ARG_ELEMENT = "element"

        fun createArguments(element: Element) = bundleOf(
            ARG_ELEMENT to Json.encodeToString(element)
        )

        // check the most common access tags
        private val elementWithoutAccessTagsFilter = """
        nodes, ways, relations with
         !access
         and !bicycle
         and !foot
         and !vehicle
         and !motor_vehicle
         and !horse
         and !bus
         and !hgv
         and !motorcar
         and !psv
         and !ski
    """.toElementFilterExpression()

        private val buildingsFilter = """
        ways, relations with building
          and building !~ no|construction|ruins|collapsed|damaged|proposed|ruin|destroyed
          and !building:demolished
          and !building:razed
    """.toElementFilterExpression()

        private val highwaysFilter = "ways with highway ~ unclassified|residential|service|track|footway|bridleway|steps|path or leisure ~ track|pitch"
            .toElementFilterExpression()

        private val otherFilter = "nodes, ways with amenity ~ recycling|bicycle_parking|bench|picnic_table".toElementFilterExpression()
    }
}
