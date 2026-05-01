package de.westnordost.streetcomplete.quests.religion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.barrier_type.icon
import de.westnordost.streetcomplete.quests.barrier_type.title
import de.westnordost.streetcomplete.quests.religion.Religion.MULTIFAITH
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddReligionForm : AbstractOsmQuestForm<Religion>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val items = remember {
            (Religion.entries - MULTIFAITH).sortedBy { religionPosition(it.osmValue) }
        }

        ItemSelectQuestForm(
            items = items,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddReligionForm",
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_religion_for_place_of_worship_answer_multi)) { applyAnswer(MULTIFAITH) }
            )
        )
    }

    private fun religionPosition(osmValue: String): Int {
        val position = countryInfo.popularReligions.indexOf(osmValue)
        if (position < 0) {
            // not present at all in config, so should be put at the end
            return Integer.MAX_VALUE
        }
        return position
    }
}
