package de.westnordost.streetcomplete.quests

import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.bundleOf
import androidx.core.view.children
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.location.SurveyChecker
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
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.visiblequests.HideQuestController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.osm.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import de.westnordost.streetcomplete.view.add
import de.westnordost.streetcomplete.view.confirmIsSurvey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

/** Abstract base class for any bottom sheet with which the user answers a specific quest(ion)  */
abstract class AbstractOsmQuestForm<T> : AbstractQuestForm(), IsShowingQuestDetails {

    // dependencies
    private val elementEditsController: ElementEditsController by inject()
    private val noteEditsController: NoteEditsController by inject()
    private val hiddenQuestsController: QuestsHiddenController by inject()
    private val visibleEditTypeController: VisibleEditTypeController by inject()
    private val featureDictionaryLazy: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val surveyChecker: SurveyChecker by inject()

    protected val featureDictionary: FeatureDictionary get() = featureDictionaryLazy.value

    // only used for testing / only used for ShowQuestFormsScreen! Found no better way to do this
    var addElementEditsController: AddElementEditsController = elementEditsController
    var hideQuestController: HideQuestController = hiddenQuestsController

    // passed in parameters
    private val osmElementQuestType: OsmElementQuestType<T> get() = questType as OsmElementQuestType<T>
    protected lateinit var element: Element private set

    // overridable by child classes
    open val otherAnswers = listOf<IAnswerItem>()
    open val buttonPanelAnswers = listOf<IAnswerItem>()

    private val showReplacePlaceDialog: MutableState<Boolean> = mutableStateOf(false)

    interface Listener {
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
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        element = Json.decodeFromString(args.getString(ARG_ELEMENT)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObjNote(element.tags["note"])
    }

    @Composable
    override fun DialogContainer() {
        if (showReplacePlaceDialog.value) {
            ShopGoneDialog(
                onDismissRequest = { showReplacePlaceDialog.value = false },
                onSelectAnswer = { answer ->
                    when (answer) {
                        is ShopType -> onShopReplacementSelected(answer.feature)
                        ShopTypeAnswer.IsShopVacant -> onShopDisusedSelected()
                        ShopTypeAnswer.LeaveNote -> composeNote()
                    }
                },
                featureDictionary = featureDictionary,
                geometryType = element.geometryType,
                countryCode = countryOrSubdivisionCode,

            )
        }
    }

    override protected fun getTitle(): StringResource =
        osmElementQuestType.getTitle(element.tags) ?: questType.title

    @Composable
    override protected fun getSubtitle(): AnnotatedString? =
        nameAndLocationLabel(element, featureDictionary)

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

        answers.add(AnswerItem(R.string.quest_generic_answer_notApplicable) { onClickCantSay() })

        if (element.isSplittable()) {
            answers.add(AnswerItem(R.string.quest_generic_answer_differs_along_the_way) { onClickSplitWayAnswer() })
        }
        createDeleteOrReplaceElementAnswer()?.let { answers.add(it) }

        if (element is Node // add moveNodeAnswer only if it's a free floating node
            && mapDataWithEditsSource.getWaysForNode(element.id).isEmpty()
            && mapDataWithEditsSource.getRelationsForNode(element.id).isEmpty()) {
            answers.add(AnswerItem(R.string.move_node) { onClickMoveNodeAnswer() })
        }

        if (questType.visibilityEditable) {
            answers.add(AnswerItem(R.string.quest_generic_answer_disable_this_quest_type) { onClickDisableQuestType() })
        }

        answers.addAll(otherAnswers)
        return answers
    }

    private fun createDeleteOrReplaceElementAnswer(): AnswerItem? {
        val isDeletePoiEnabled = osmElementQuestType.isDeleteElementEnabled && element.type == ElementType.NODE
        val isReplacePlaceEnabled = osmElementQuestType.isReplacePlaceEnabled
        if (!isDeletePoiEnabled && !isReplacePlaceEnabled) return null
        check(!(isDeletePoiEnabled && isReplacePlaceEnabled)) {
            "Only isDeleteElementEnabled OR isReplaceShopEnabled may be true at the same time"
        }

        return AnswerItem(R.string.quest_generic_answer_does_not_exist) {
            if (isDeletePoiEnabled) {
                deletePoiNode()
            } else if (isReplacePlaceEnabled) {
                replacePlace()
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
        context?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_leave_new_note_title)
            .setMessage(R.string.quest_leave_new_note_description)
            .setNegativeButton(R.string.quest_leave_new_note_no) { _, _ -> hideQuest() }
            .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ -> composeNote() }
            .show()
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

    private fun onClickDisableQuestType() {
        context?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_disable_quest_type_title)
            .setMessage(R.string.quest_disable_quest_type_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_disable_quest_type_yes) { _, _ ->
                visibleEditTypeController.setVisibility(questType, false)
                hideQuest()
            }
            .show()
        }
    }

    protected fun applyAnswer(answer: T) {
        viewLifecycleScope.launch {
            solve(UpdateElementTagsAction(element, createQuestChanges(answer)))
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
        viewLifecycleScope.launch {
            val questTitleResource = osmElementQuestType.getTitle(element.tags) ?: questType.title
            val resourceEnvironment = getSystemResourceEnvironment()
            val questTitle = org.jetbrains.compose.resources.getString(resourceEnvironment, questTitleResource)
            val hintLabel = getNameAndLocationLabel(resourceEnvironment, LayoutDirection.Ltr, element, featureDictionary)
            val leaveNoteContext = if (hintLabel.isNullOrBlank()) {
                "Unable to answer \"$questTitle\""
            } else {
                "Unable to answer \"$questTitle\" – $hintLabel"
            }
            listener?.onComposeNote(osmElementQuestType, element, geometry, leaveNoteContext)
        }
    }

    protected fun hideQuest() {
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) { hideQuestController.hide(questKey) }
            listener?.onQuestHidden(questKey)
        }
    }

    protected fun replacePlace() {
        if (element.isPlaceOrDisusedPlace()) {
            showReplacePlaceDialog.value = true
        } else {
            composeNote()
        }
    }

    private fun onShopDisusedSelected() {
        val languages = getLanguagesForFeatureDictionary()
        val vacantShop = featureDictionary
            .getByTags(element.tags)
            .firstOrNull { it.toElement().isPlace() }
            ?.toPrefixedFeature("disused")
            ?: featureDictionary.getById("shop/vacant", languages)!!
        onShopReplacementSelected(vacantShop)
    }

    private fun onShopReplacementSelected(feature: Feature) {
        viewLifecycleScope.launch {
            val builder = StringMapChangesBuilder(element.tags)
            feature.applyReplacePlaceTo(builder)
            solve(UpdateElementTagsAction(element, builder.create()))
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
            solve(DeletePoiNodeAction(element as Node))
        }
    }

    private suspend fun solve(action: ElementEditAction) {
        setLocked(true)
        val isSurvey = surveyChecker.checkIsSurvey(geometry)
        if (!isSurvey && !confirmIsSurvey(requireContext())) {
            setLocked(false)
            return
        }
        withContext(Dispatchers.IO) {
            if (action is UpdateElementTagsAction && !action.changes.isValid()) {
                val questTitleResource = osmElementQuestType.getTitle(element.tags) ?: questType.title
                val questTitle = org.jetbrains.compose.resources.getString(getSystemResourceEnvironment(), questTitleResource)
                val text = createNoteTextForTooLongTags(questTitle, element.type, element.id, action.changes.changes)
                noteEditsController.add(0, NoteEditAction.CREATE, geometry.center, text)
            } else {
                addElementEditsController.add(osmElementQuestType, geometry, "survey", action, isSurvey)
            }
        }
        listener?.onEdited(osmElementQuestType, geometry)
    }


    /* Unfortunately, ResourceEnviornment's constructor is internal, so we cannot use this
       see https://youtrack.jetbrains.com/issue/CMP-9959/Access-resources-in-specific-language-outside-of-composition

    /** get English resource environment */
    @OptIn(InternalResourceApi::class)
    fun getEnglishResourceEnvironment() = ResourceEnvironment(
        language = LanguageQualifier("en"),
        region = RegionQualifier(""),
        theme = ThemeQualifier.LIGHT,
        density = DensityQualifier.MDPI,
    )
    */

    companion object {
        private const val ARG_ELEMENT = "element"

        fun createArguments(element: Element) = bundleOf(
            ARG_ELEMENT to Json.encodeToString(element)
        )
    }
}
