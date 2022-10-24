package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestController
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType
import de.westnordost.streetcomplete.data.quest.OtherSourceQuestKey
import de.westnordost.streetcomplete.screens.main.checkIsSurvey
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

abstract class AbstractOtherQuestForm : AbstractQuestForm(), IsShowingQuestDetails {
    // overridable by child classes
    open val otherAnswers = listOf<AnswerItem>()
    open val buttonPanelAnswers = listOf<AnswerItem>()
    private val elementEditsController: ElementEditsController by inject()
    private val otherQuestController: OtherSourceQuestController by inject()
    protected val mapDataSource: MapDataWithEditsSource by inject()

    protected var element: Element? = null
    private val dummyElement by lazy { Node(0, LatLon(0.0, 0.0)) }

    private val listener: AbstractOsmQuestForm.Listener? get() = parentFragment as? AbstractOsmQuestForm.Listener ?: activity as? AbstractOsmQuestForm.Listener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        // set element if available
        otherQuestController.getVisible(questKey as OtherSourceQuestKey)?.elementKey?.let { key ->
            mapDataSource.get(key.type, key.id)?.let { element = it }
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
        val e = element
        if (e != null) {
            if (otherAnswers.none { it.titleResourceId == R.string.quest_generic_answer_show_edit_tags })
                answers.add(AnswerItem(R.string.quest_generic_answer_show_edit_tags) { editTags(e) })
            if (e.type == ElementType.NODE)
                answers.add(AnswerItem(R.string.quest_generic_answer_does_not_exist) { deletePoiNode(e) })
        }
        answers.addAll(otherAnswers)
        return answers
    }

    protected fun deletePoiNode(element: Element) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.osm_element_gone_description)
            .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ -> onDeletePoiNodeConfirmed(element) }
            .setNeutralButton(R.string.leave_note) { _, _ -> composeNote() }
            .show()
    }

    private fun onDeletePoiNodeConfirmed(element: Element) {
        viewLifecycleScope.launch { editElement(element, DeletePoiNodeAction) }
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

    private fun onClickCantSay() {
        context?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_leave_new_note_title)
            .setMessage(R.string.quest_leave_new_note_description)
            .setNegativeButton(R.string.quest_leave_new_note_no) { _, _ -> hideQuest() }
            .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ -> composeNote() }
            .show()
        }
    }

    protected fun composeNote() {
        val questTitle = resources.getQuestTitle(questType, emptyMap())
        val leaveNoteContext = "Unable to answer \"$questTitle\""
        listener?.onComposeNote(questType as OtherSourceQuestType, element ?: dummyElement, geometry, leaveNoteContext)
    }

    protected fun tempHideQuest() {
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) { otherQuestController.tempHide(questKey as OtherSourceQuestKey) }
            listener?.onQuestHidden(questKey)
        }
    }

    protected fun hideQuest() {
        viewLifecycleScope.launch {
            withContext(Dispatchers.IO) { otherQuestController.hide(questKey as OtherSourceQuestKey) }
            listener?.onQuestHidden(questKey)
        }
    }

    protected fun editTags(e: Element) {
        val geo = if (e is Node) ElementPointGeometry(e.position) else mapDataSource.getGeometry(e.type, e.id) ?: return
        listener?.onEditTags(e, geo)
    }

    protected suspend fun editElement(element: Element, action: ElementEditAction) {
        setLocked(true)
        if (!checkIsSurvey(requireContext(), geometry, listOfNotNull(listener?.displayedMapLocation))) {
            setLocked(false)
            return
        }
        val qt = questType as OtherSourceQuestType
        tempHideQuest() // make it disappear. the questType should take care the quest does not appear again
        if (prefs.getBoolean(Prefs.CLOSE_FORM_IMMEDIATELY_AFTER_SOLVING, false) && !prefs.getBoolean(
                Prefs.SHOW_NEXT_QUEST_IMMEDIATELY, false)) {
            viewLifecycleScope.launch {
                // Only listener is mainFragment for closing bottom sheet and showing the quest
                // solved animation, so it's ok to call even though the edit was not done yet.
                listener?.onEdited(qt, element, geometry)
            }
            // don't hide quest here, this could be different for each type
            withContext(Dispatchers.IO) {
                elementEditsController.add(qt, element, geometry, "survey", action, questKey)
            }
        } else {
            withContext(Dispatchers.IO) {
                elementEditsController.add(qt, element, geometry, "survey", action, questKey)
            }
            listener?.onEdited(qt, element, geometry)
        }
    }
}
