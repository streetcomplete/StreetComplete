package de.westnordost.streetcomplete.quests.tactile_paving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class TactilePavingForm : YesNoQuestAnswerFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setContentView(R.layout.quest_tactile_paving)
        return view
    }
}
