package de.westnordost.streetcomplete.quests

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
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.replaceShop
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import de.westnordost.streetcomplete.screens.main.RecentLocationStore
import de.westnordost.streetcomplete.screens.main.checkIsSurvey
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.isSplittable
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
    private val recentLocationStore: RecentLocationStore by inject()

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
        fun onQuestHidden(osmQuestKey: OsmQuestKey)
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
    }

    override fun onStart() {
        super.onStart()
        updateButtonPanel()
    }

    protected fun updateButtonPanel() {
        val answers = assembleOtherAnswers()
        val otherAnswersItem = if (answers.size == 1) {
            answers.single()
        } else {
            AnswerItem(R.string.quest_generic_otherAnswers) { showOtherAnswers() }
        }
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

        answers.addAll(otherAnswers)
        return answers
    }

    private fun createDeleteOrReplaceElementAnswer(): AnswerItem? {
        val isDeletePoiEnabled = osmElementQuestType.isDeleteElementEnabled && element.type == ElementType.NODE
        val isReplaceShopEnabled = osmElementQuestType.isReplaceShopEnabled
        if (!isDeletePoiEnabled && !isReplaceShopEnabled) return null
        check(!(isDeletePoiEnabled && isReplaceShopEnabled)) {
            "Only isDeleteElementEnabled OR isReplaceShopEnabled may be true at the same time"
        }

        return AnswerItem(R.string.quest_generic_answer_does_not_exist) {
            if (isDeletePoiEnabled) deletePoiNode()
            else if (isReplaceShopEnabled) replaceShop()
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
        val questTitle = englishResources.getQuestTitle(osmElementQuestType, element.tags)
        val leaveNoteContext = "Unable to answer \"$questTitle\""
        listener?.onComposeNote(osmElementQuestType, element, geometry, leaveNoteContext)
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
                onLeaveNote = this::composeNote
            ).show()
        } else {
            composeNote()
        }
    }

    private fun onShopReplacementSelected(tags: Map<String, String>) {
        viewLifecycleScope.launch {
            val builder = StringMapChangesBuilder(element.tags)
            builder.replaceShop(tags)
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
        if (!checkIsSurvey(requireContext(), geometry, recentLocationStore.get())) {
            setLocked(false)
            return
        }
        withContext(Dispatchers.IO) {
            if (action is UpdateElementTagsAction && !action.changes.isValid()) {
                val questTitle = englishResources.getQuestTitle(osmElementQuestType, element.tags)
                val text = createNoteTextForTooLongTags(questTitle, element.type, element.id, action.changes.changes)
                noteEditsController.add(0, NoteEditAction.CREATE, geometry.center, text)
            } else {
                addElementEditsController.add(osmElementQuestType, geometry, "survey", action)
            }
        }
        listener?.onEdited(osmElementQuestType, geometry)
    }

    companion object {
        private const val ARG_ELEMENT = "element"

        fun createArguments(element: Element) = bundleOf(
            ARG_ELEMENT to Json.encodeToString(element)
        )
    }
}
