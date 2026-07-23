package de.westnordost.streetcomplete.overlays.way_lit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.LitStatus.*
import de.westnordost.streetcomplete.osm.lit.applyTo
import de.westnordost.streetcomplete.osm.lit.icon
import de.westnordost.streetcomplete.osm.lit.parseLitStatus
import de.westnordost.streetcomplete.osm.lit.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun WayLitOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element,
    preferences: Preferences = koinInject()
) {
    val originalLit = remember(element) { parseLitStatus(element.tags) }

    ItemSelectOverlayForm(
        on = on,
        itemsPerRow = 2,
        items = LitStatus.entries,
        selectableItems = remember { listOf(YES, NO, AUTOMATIC, NIGHT_AND_DAY) },
        initialSelectedItem = originalLit,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        lastPickedItemContent = {
            Image(painterResource(it.icon), stringResource(it.title), Modifier.height(24.dp))
        },
        onClickOk = { selectedItem ->
            val tagChanges = StringMapChangesBuilder(element.tags)
            selectedItem.applyTo(tagChanges)
            on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
        },
        prefs = preferences,
        favoriteKey = "WayLitOverlayForm",
        otherAnswers = { listOfNotNull(
            if (element.couldBeSteps()) {
                AnswerItem(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    tagChanges.changeToSteps()
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                }
            } else null
        ) }
    )
}
