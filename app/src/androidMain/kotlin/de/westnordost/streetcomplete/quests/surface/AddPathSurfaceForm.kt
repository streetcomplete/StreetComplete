package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPathSurfaceForm : AItemSelectQuestForm<Surface, SurfaceOrIsStepsAnswer>() {
    override val items get() = Surface.selectableValuesForWays

    override val otherAnswers get() = listOfNotNull(
        createConvertToStepsAnswer(),
        createMarkAsIndoorsAnswer(),
    )

    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: Surface) {
        val icon = item.icon
        if (icon != null) {
            ImageWithLabel(painterResource(icon), stringResource(item.title))
        }
    }

    override fun onClickOk(selectedItem: Surface) {
        applyAnswer(SurfaceAnswer(selectedItem))
    }

    private fun createConvertToStepsAnswer(): AnswerItem? =
        if (element.couldBeSteps()) {
            AnswerItem(R.string.quest_generic_answer_is_actually_steps) {
                applyAnswer(IsActuallyStepsAnswer)
            }
        } else {
            null
        }

    private fun createMarkAsIndoorsAnswer(): AnswerItem? {
        val way = element as? Way ?: return null
        if (way.tags["indoor"] == "yes") return null

        return AnswerItem(R.string.quest_generic_answer_is_indoors) {
            applyAnswer(IsIndoorsAnswer)
        }
    }
}
