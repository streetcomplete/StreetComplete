package de.westnordost.streetcomplete.quests.powerpoles_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_powerPolesMaterial_is_terminal
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddPowerPolesMaterialForm : AbstractOsmQuestForm<PowerPolesMaterialAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = PowerPolesMaterial.entries,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddPowerPolesMaterialForm",
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_powerPolesMaterial_is_terminal)) {
                    applyAnswer(PowerLineAnchoredToBuilding)
                }
            )
        )
    }
}
