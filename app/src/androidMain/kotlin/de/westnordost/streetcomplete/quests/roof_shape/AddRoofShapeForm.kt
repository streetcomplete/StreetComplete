package de.westnordost.streetcomplete.quests.roof_shape

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.MANY
import org.jetbrains.compose.resources.painterResource

class AddRoofShapeForm : AImageListQuestForm<RoofShape, RoofShape>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_roofShape_answer_many) { applyAnswer(MANY) }
    )

    override val items = RoofShape.entries - MANY

    @Composable override fun BoxScope.ItemContent(item: RoofShape) {
        item.icon?.let { Image(painterResource(it), null)  }
    }

    override fun onClickOk(selectedItems: List<RoofShape>) {
        applyAnswer(selectedItems.single())
    }
}
