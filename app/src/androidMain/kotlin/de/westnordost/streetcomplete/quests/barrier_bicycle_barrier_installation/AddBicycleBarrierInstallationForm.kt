package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddBicycleBarrierInstallationForm : AbstractOsmQuestForm<BicycleBarrierInstallationAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = BicycleBarrierInstallation.entries,
            itemsPerRow = 3,
            itemContent = { item ->
                ImageWithLabel(painterResource(item.icon), stringResource(item.title))
            },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddBicycleBarrierInstallationForm",
            moveFavoritesToFront = false,
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_barrier_bicycle_type_not_cycle_barrier)) {
                    applyAnswer(BarrierTypeIsNotBicycleBarrier)
                }
            )
        )
    }
}
