package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddPathSurfaceForm : AbstractOsmQuestForm<SurfaceOrIsStepsAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = Surface.selectableValuesForWays,
            itemsPerRow = 3,
            itemContent = { item ->
                ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
            },
            onClickOk = { applyAnswer(SurfaceAnswer(it)) },
            prefs = prefs,
            favoriteKey = "AddPathSurfaceForm",
            otherAnswers = listOfNotNull(
                if (element.couldBeSteps()) {
                    Answer(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                        applyAnswer(IsActuallyStepsAnswer)
                    }
                } else null,
                if (element.tags["indoor"] != "yes") {
                    Answer(stringResource(Res.string.quest_generic_answer_is_indoors)) {
                        applyAnswer(IsIndoorsAnswer)
                    }
                } else null,
            )
        )
    }
}
