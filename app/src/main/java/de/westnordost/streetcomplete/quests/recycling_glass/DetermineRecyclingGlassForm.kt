package de.westnordost.streetcomplete.quests.recycling_glass

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_buttonpanel_glass_glassbottles.*
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.*


class DetermineRecyclingGlassForm : AbstractQuestAnswerFragment<RecyclingGlass>() {
    override val contentLayoutResId = R.layout.quest_determine_recycling_glass_explanation

    override val buttonsResId = R.layout.quest_buttonpanel_glass_glassbottles

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        glassButton.setOnClickListener { applyAnswer(ANY) }
        glassBottlesButton.setOnClickListener { applyAnswer(BOTTLES) }
    }
}
