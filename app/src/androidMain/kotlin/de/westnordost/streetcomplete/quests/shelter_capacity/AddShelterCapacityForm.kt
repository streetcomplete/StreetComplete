package de.westnordost.streetcomplete.quests.shelter_capacity

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddShelterCapacityForm : AbstractOsmQuestForm<Int>() {

    @Composable
    override fun Content() {
        CountInputQuestForm(
            icon = painterResource(Res.drawable.count_sleeping_bag),
            onClickOk = { applyAnswer(it) }
        )
    }
}
