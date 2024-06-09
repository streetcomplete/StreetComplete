package de.westnordost.streetcomplete.quests

import android.app.DatePickerDialog
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
import de.westnordost.streetcomplete.data.location.RecentLocationStore
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
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.replacePlace
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.quests.custom.CustomQuestList
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import de.westnordost.streetcomplete.util.AccessManagerDialog
import de.westnordost.streetcomplete.util.accessKeys
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.containsAnyKey
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.isArea
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toInstant
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.view.add
import de.westnordost.streetcomplete.view.checkIsSurvey
import de.westnordost.streetcomplete.view.confirmIsSurvey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Abstract base class for any bottom sheet with which the user answers a specific quest(ion)  */
abstract class AbstractOsmQuestForm<T> : AbstractQuestForm(), IsShowingQuestDetails {

    // dependencies
    private val elementEditsController: ElementEditsController by inject()
    private val noteEditsController: NoteEditsController by inject()
    private val osmQuestsHiddenController: OsmQuestsHiddenController by inject()
    private val featureDictionaryLazy: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val recentLocationStore: RecentLocationStore by inject()
    private val customQuestList: CustomQuestList by inject()
    private val osmQuestController: OsmQuestController by inject()

    protected val featureDictionary: FeatureDictionary get() = featureDictionaryLazy.value

    // only used for testing / only used for ShowQuestFormsActivity! Found no better way to do this
    var addElementEditsController: AddElementEditsController = elementEditsController
    var hideOsmQuestController: HideOsmQuestController = osmQuestsHiddenController

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
        fun onEdited(editType: ElementEditType, geometry: ElementGeometry)

        /** Called when the user chose to leave a note instead */
        fun onComposeNote(editType: ElementEditType, element: Element, geometry: ElementGeometry, leaveNoteContext: String)

        /** Called when the user chose to split the way */
        fun onSplitWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry)

        /** Called when the user chose to move the node */
        fun onMoveNode(editType: ElementEditType, node: Node)

        /** Called when the user chose to hide the quest instead */
        fun onQuestHidden(questKey: QuestKey)

        /** Called when the user chose to edit tags */
        fun onEditTags(element: Element, geometry: ElementGeometry, questKey: QuestKey?, editTypeName: String? = null)
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
        val otherAnswersItem = AnswerItem(R.string.quest_generic_otherAnswers2) { showOtherAnswers() }
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
            createConstructionAnswer()?.let { answers.add(it) }
            createAccessManagerAnswer()?.let { answers.add(it) }
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
        val isReplacePlaceEnabled = osmElementQuestType.isReplacePlaceEnabled
        if (!isDeletePoiEnabled && !isReplacePlaceEnabled) return null

        return AnswerItem(R.string.quest_generic_answer_does_not_exist) {
            if (isReplacePlaceEnabled) {
                replacePlace() // allow both being enabled, but prefer replace over delete
            } else {
                deletePoiNode()
            }
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
                    AlertDialog.Builder(it)
                        .setTitle(R.string.create_custom_quest_title_message)
                        .setViewWithDefaultPadding(text)
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

    protected fun applyAnswer(answer: T, extra: Boolean = false) {
        viewLifecycleScope.launch {
            if (TagEditor.showingTagEditor) {
                val changesBuilder = StringMapChangesBuilder(element.tags)
                osmElementQuestType.applyAnswerTo(answer, changesBuilder, geometry, element.timestampEdited)
                TagEditor.changes = changesBuilder.create()
            } else
                solve(UpdateElementTagsAction(element, createQuestChanges(answer)), extra)
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
        val hintLabel = getNameAndLocationLabel(element, englishResources, featureDictionary)
        val leaveNoteContext = if (hintLabel.isNullOrBlank()) {
            "Unable to answer \"$questTitle\""
        } else {
            "Unable to answer \"$questTitle\" – $hintLabel"
        }
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

    protected fun replacePlace(extra: Boolean = true) {
        if (element.isPlaceOrDisusedPlace()) {
            ShopGoneDialog(
                requireContext(),
                element.geometryType,
                countryOrSubdivisionCode,
                featureDictionary,
                onSelectedFeature = { onShopReplacementSelected(it, extra) },
                onLeaveNote = this::composeNote,
                geometry.center
            ).show()
        } else {
            composeNote()
        }
    }

    private fun onShopReplacementSelected(tags: Map<String, String>, extra: Boolean = true) {
        viewLifecycleScope.launch {
            val builder = StringMapChangesBuilder(element.tags)
            builder.replacePlace(tags)
            solve(UpdateElementTagsAction(element, builder.create()), extra)
        }
    }

    protected fun deletePoiNode(extra: Boolean = true) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.osm_element_gone_description)
            .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ -> onDeletePoiNodeConfirmed(extra) }
            .setNeutralButton(R.string.leave_note) { _, _ -> composeNote() }
            .show()
    }

    private fun onDeletePoiNodeConfirmed(extra: Boolean = true) {
        viewLifecycleScope.launch {
            solve(DeletePoiNodeAction(element as Node), extra)
        }
    }

    private fun createItsPrivateAnswer(): AnswerItem? {
        return if (elementWithoutAccessTagsFilter.matches(element) && thingsWithMaybeAccessFilter.matches(element))
            AnswerItem(R.string.quest_private) {
                viewLifecycleScope.launch {
                    val builder = StringMapChangesBuilder(element.tags)
                    builder["access"] = "private"
                    solve(UpdateElementTagsAction(element, builder.create()), true)
                }
            }
        else null
    }

    private fun createAccessManagerAnswer(): AnswerItem? {
        if (!"ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}".toElementFilterExpression().matches(element)) return null
        val title = if (element.tags.containsAnyKey(*accessKeys))
                R.string.manage_access
            else R.string.add_access
        return AnswerItem(title) {
            AccessManagerDialog(requireContext(), element.tags) {
                viewLifecycleScope.launch { solve(UpdateElementTagsAction(element, it.create()), true) }
            }.show()
        }
    }

    private fun createConstructionAnswer(): AnswerItem? {
        if (!elementWithoutAccessTagsFilter.matches(element)
            || !element.tags.containsKey("highway")
            || element.tags["highway"] == "construction"
        ) return null
        return AnswerItem(R.string.quest_construction) {
            val tomorrow = systemTimeNow().toLocalDate().plus(1, DateTimeUnit.DAY)
            val p = DatePickerDialog(requireContext(), { _, y, m, d ->
                val finishDate = LocalDate(y, m + 1, d)
                val today = systemTimeNow().toLocalDate()
                val builder = StringMapChangesBuilder(element.tags)
                val diff = finishDate.toEpochDays() - today.toEpochDays()
                if (diff <= 0) return@DatePickerDialog // don't even bother to tell the user if they are trying to enter wrong data

                // for short construction up to a few months it's better to use conditional access
                // as per https://wiki.openstreetmap.org/wiki/Tag:highway%3Dconstruction
                if (diff < 200) { // we arbitrarily set the few months to 200 days
                    val f = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.US)
                    builder["access:conditional"] = "no @ (${f.format(today.toJavaLocalDate())}-${f.format(finishDate.toJavaLocalDate())})"
                    viewLifecycleScope.launch { solve(UpdateElementTagsAction(element, builder.create()), true) }
                } else {
                    // if we actually change the highway to construction, we let the user set a construction value
                    val t = EditText(requireContext()).apply {
                        setText(element.tags["highway"])
                    }
                    val f = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                    builder["opening_date"] = f.format(finishDate.toJavaLocalDate())
                    builder["highway"] = "construction"
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.quest_construction_value)
                        .setViewWithDefaultPadding(t)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            t.text.toString().takeIf { it.isNotBlank() }?.let { builder["construction"] = it }
                            viewLifecycleScope.launch { solve(UpdateElementTagsAction(element, builder.create()), true) }
                        }
                        .show()
                }
            }, tomorrow.year, tomorrow.monthNumber - 1, tomorrow.dayOfMonth)
            p.datePicker.minDate = tomorrow.toInstant().toEpochMilliseconds()
            p.show()
        }
    }

    private fun createItsDemolishedAnswer(): AnswerItem? {
        if (!element.isArea()) return null
        return if (demolishableBuildingsFilter.matches(element))
            AnswerItem(R.string.quest_generic_answer_does_not_exist) {
                AlertDialog.Builder(requireContext())
                    .setItems(arrayOf(requireContext().getString(R.string.quest_building_demolished), requireContext().getString(R.string.leave_note))) { di, i ->
                        di.dismiss()
                        if (i == 0) {
                            viewLifecycleScope.launch {
                                val builder = StringMapChangesBuilder(element.tags)
                                builder["demolished:building"] = builder["building"] ?: "yes"
                                builder.remove("building")
                                solve(UpdateElementTagsAction(element, builder.create()), true)
                            }
                        } else {
                            composeNote()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        else null
    }

    private suspend fun solve(action: ElementEditAction, extra: Boolean = false) {
        Log.i(TAG, "solve ${questType.name} for ${element.key}, extra: $extra, in TagEditor: ${TagEditor.showingTagEditor}")
        if (TagEditor.showingTagEditor) return

        // really bad hacky way of using separate changesets for some "other answers",
        // but doesn't require changing database stuff and commit can be reverted without breaking stuff
        val source = if (extra) "survey,extra" else "survey"

        setLocked(true)
        val isSurvey = checkIsSurvey(geometry, recentLocationStore.get())
        if (!isSurvey && !confirmIsSurvey(requireContext())) {
            setLocked(false)
            return
        }
        if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
            // necessary because otherwise pins may remain if quest is not in database
            OsmQuestController.lastAnsweredQuestKey = questKey as? OsmQuestKey

        val l = listener // form is closed after adding the edit, so the listener may already be null when called
        withContext(Dispatchers.IO) {
            if (action is UpdateElementTagsAction && !action.changes.isValid()) {
                val questTitle = englishResources.getQuestTitle(osmElementQuestType, element.tags)
                val text = createNoteTextForTooLongTags(questTitle, element.type, element.id, action.changes.changes)
                noteEditsController.add(0, NoteEditAction.CREATE, geometry.center, text)
            } else {
                addElementEditsController.add(osmElementQuestType, geometry, source, action, isSurvey)
            }
        }
        l?.onEdited(osmElementQuestType, geometry)
    }

    companion object {
        private const val ARG_ELEMENT = "element"

        fun createArguments(element: Element) = bundleOf(
            ARG_ELEMENT to Json.encodeToString(element)
        )

        // check the most common access tags
        val elementWithoutAccessTagsFilter = """
nodes, ways, relations with
 !access
 and !access:conditional
 and !bicycle
 and !bicycle:conditional
 and !foot
 and !foot:conditional
 and !vehicle
 and !vehicle:conditional
 and !motor_vehicle
 and !motor_vehicle:conditional
 and !motorcycle
 and !motorcycle:conditional
 and !horse
 and !bus
 and !hgv
 and !motorcar
 and !psv
 and !ski
    """.toElementFilterExpression()

        // in some cases changing building to demolished:building is not enough
        val demolishableBuildingsFilter = """
ways, relations with building
  and building !~ no|construction|ruins|collapsed|damaged|proposed|ruin|destroyed
  and !building:demolished
  and !building:razed
  and !shop and !amenity and !historic and !craft and !healthcare and !office and !attraction and !tourism
    """.toElementFilterExpression()

        private val thingsWithMaybeAccessFilter = """
nodes, ways with
  amenity ~ recycling|bicycle_parking|bench|picnic_table
  or leisure ~ track|pitch
  or highway ~ path|footway
    """.toElementFilterExpression()
    }
}

private const val TAG = "AbstractOsmQuestForm"
