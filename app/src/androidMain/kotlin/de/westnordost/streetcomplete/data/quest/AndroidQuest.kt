package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.quests.AbstractQuestForm

interface AndroidQuest {
    /** returns the fragment in which the user can add the data */
    fun createForm(): AbstractQuestForm
}
