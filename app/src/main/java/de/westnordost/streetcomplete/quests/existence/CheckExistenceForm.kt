package de.westnordost.streetcomplete.quests.existence

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class CheckExistenceForm : AbstractOsmQuestForm<Unit>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStateFromTags()
    }

    private fun initStateFromTags() {
        val objectNote = element.tags["note"]
        if (objectNote != null) {
            this.setHint(getString(R.string.note_for_object) + " " + objectNote)
        }
    }
    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { deletePoiNode() },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(Unit) }
    )
}
