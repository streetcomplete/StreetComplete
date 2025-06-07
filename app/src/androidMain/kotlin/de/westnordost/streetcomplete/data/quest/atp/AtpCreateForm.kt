package de.westnordost.streetcomplete.data.quest.atp

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestForm

//see NoteDiscussionForm
class AtpCreateForm : AbstractQuestForm() {
    override val contentLayoutResId = R.layout.quest_note_discussion_content

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO val alreadyHidden = hiddenQuestsController.get(questKey) != null
        /*
        setButtonPanelAnswers(listOf(
            if (alreadyHidden) {
                AnswerItem(R.string.short_no_answer_on_button) { closeQuest() }
            } else {
                AnswerItem(R.string.quest_noteDiscussion_no) { hideQuest() }
            }
        ))
        */
        //binding.noteInput.doAfterTextChanged { checkIsFormComplete() }
    }
}
