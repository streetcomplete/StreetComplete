package de.westnordost.streetcomplete.quests

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.icu.text.DateFormat.getDateTimeInstance
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.AddElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
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
import de.westnordost.streetcomplete.screens.main.checkIsSurvey
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.Date
import java.util.Locale
import java.util.concurrent.FutureTask

/** Abstract base class for any bottom sheet with which the user answers a specific quest(ion)  */
abstract class AbstractOsmQuestForm<T> : AbstractQuestForm(), IsShowingQuestDetails {

    // dependencies
    private val elementEditsController: ElementEditsController by inject()
    private val noteEditsController: NoteEditsController by inject()
    private val osmQuestController: OsmQuestController by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))

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
    open val otherAnswers = listOf<AnswerItem>()
    open val buttonPanelAnswers = listOf<AnswerItem>()

    interface Listener {
        /** The GPS position at which the user is displayed at */
        val displayedMapLocation: Location?

        /** Called when the user successfully answered the quest */
        fun onEdited(editType: ElementEditType, element: Element, geometry: ElementGeometry)

        /** Called when the user chose to leave a note instead */
        fun onComposeNote(editType: ElementEditType, element: Element, geometry: ElementGeometry, leaveNoteContext: String)

        /** Called when the user chose to split the way */
        fun onSplitWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry)

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
        setTitleHintLabel(getNameAndLocationLabel(element.tags, resources, featureDictionary))

        if (prefs.getBoolean(Prefs.SHOW_HIDE_BUTTON, false)) {
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
        val otherAnswersItem = if (answers.size == 1) {
            answers.single()
        } else {
            AnswerItem(R.string.quest_generic_otherAnswers) { showOtherAnswers() }
        }
        setButtonPanelAnswers(listOf(otherAnswersItem) + buttonPanelAnswers)
    }

    private fun assembleOtherAnswers(): List<AnswerItem> {
        val answers = mutableListOf<AnswerItem>()

        answers.add(AnswerItem(R.string.quest_generic_answer_notApplicable) { onClickCantSay() })

        answers.add(AnswerItem(R.string.quest_generic_answer_show_edit_tags) { onClickEditTags(element, context) { viewLifecycleScope.launch { solve(it) } } })

        if (element.isSplittable()) {
            answers.add(AnswerItem(R.string.quest_generic_answer_differs_along_the_way) { onClickSplitWayAnswer() })
        }
        createDeleteOrReplaceElementAnswer()?.let { answers.add(it) }
        createItsPrivateAnswer()?.let { answers.add(it) }

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
            popup.menu.add(Menu.NONE, i, order, otherAnswer.titleResourceId)
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

    protected fun applyAnswer(answer: T) {
        viewLifecycleScope.launch {
            solve(UpdateElementTagsAction(createQuestChanges(answer)))
        }
    }

    private fun createQuestChanges(answer: T): StringMapChanges {
        val changesBuilder = StringMapChangesBuilder(element.tags)
        osmElementQuestType.applyAnswerTo(answer, changesBuilder, element.timestampEdited)
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
        if (element !is Way) return null
        return if (wayWithoutAccessTagsFilter.matches(element))
            AnswerItem(R.string.quest_way_private) {
                viewLifecycleScope.launch {
                    val builder = StringMapChangesBuilder(element.tags)
                    builder["access"] = "private"
                    solve(UpdateElementTagsAction(builder.create()))
                }
            }
        else null
    }

    // check the most common access tags
    private val wayWithoutAccessTagsFilter by lazy { """
        ways with highway
         and !access
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
    """.toElementFilterExpression() }

    private suspend fun solve(action: ElementEditAction) {
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
    }
}

fun onClickEditTags(element: Element, context: Context?, onSolved: (ElementEditAction) -> Unit) {
    val tags = element.tags
    context?.let { c ->

        var dialog: AlertDialog? = null
        val editField = EditText(c)
        editField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS// or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        editField.setText(tags.map { "${it.key}=${it.value}" }.sorted().joinToString("\n"))
        editField.addTextChangedListener { text ->
            var enabled = true
            if (!tagsOk(text.toString())) {
                dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                return@addTextChangedListener
            }
            val tagsNew = text.toString().toTags()
            if (tags.entries.containsAll(tagsNew.entries) && tagsNew.entries.containsAll(tags.entries))
                enabled = false // tags not changed
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = enabled
        }

        val date = Date(element.timestampEdited)
        val timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            getDateTimeInstance().format(date)
        else
            date.toString()
        dialog = AlertDialog.Builder(c)
            .setTitle(c.getString(R.string.quest_edit_tags_title, timestamp))
            .setView(editField)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_edit_tags_save) { _,_ ->
                // validity of tags already checked, and tags have changed
                val updatedTags = mutableMapOf<String, String>()
                editField.text.toString().split("\n").forEach {
                    if (it.isBlank()) return@forEach
                    updatedTags[it.substringBefore("=").trim()] = it.substringAfter("=").trim()
                }
                val builder = StringMapChangesBuilder(element.tags)
                for (key in element.tags.keys) {
                    if (!updatedTags.containsKey(key))
                        builder.remove(key)
                }
                for ((key, value) in updatedTags) {
                    if (tags[key] == value) continue
                    builder[key] = value
                }
                onSolved(UpdateElementTagsAction(builder.create()))
            }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
    }
}

fun tagsOk(text: String): Boolean {
    val keys = mutableSetOf<String>()
    text.split("\n").forEach {
        if (it.isBlank()) return@forEach // allow empty lines
        if (!it.contains("=") // no key-value separator
            || it.count { it == '=' } > 1 // more than one equals sign
            || it.substringBefore("=").isBlank() // no key
            || it.substringAfter("=").isBlank() // no value
            || !keys.add(it.substringBefore("="))) { // key already exists
            return false
        }
    }
    return true
}

fun String.toTags(): Map<String, String> {
    val tags = mutableMapOf<String, String>()
    split("\n").forEach {
        if (it.isBlank()) return@forEach // allow empty lines
        tags[it.substringBefore("=").trim()] = it.substringAfter("=").trim()
    }
    return tags
}
