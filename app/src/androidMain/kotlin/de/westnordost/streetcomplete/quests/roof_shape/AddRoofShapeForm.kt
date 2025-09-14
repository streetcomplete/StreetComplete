package de.westnordost.streetcomplete.quests.roof_shape

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.MANY
import org.jetbrains.compose.resources.painterResource

class AddRoofShapeForm : AItemSelectQuestForm<RoofShape, RoofShape>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_roofShape_answer_many) { applyAnswer(MANY) }
    )

    override val items = RoofShape.entries - MANY

    @Composable override fun ItemContent(item: RoofShape) {
        Image(painterResource(item.icon), null)
    }

    override fun onClickOk(selectedItem: RoofShape) {
        applyAnswer(selectedItem)
    }
}
