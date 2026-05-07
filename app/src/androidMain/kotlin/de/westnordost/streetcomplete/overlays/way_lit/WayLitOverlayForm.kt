package de.westnordost.streetcomplete.overlays.way_lit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.LitStatus.*
import de.westnordost.streetcomplete.osm.lit.applyTo
import de.westnordost.streetcomplete.osm.lit.icon
import de.westnordost.streetcomplete.osm.lit.parseLitStatus
import de.westnordost.streetcomplete.osm.lit.title
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class WayLitOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectOverlayForm(
            itemsPerRow = 2,
            items = LitStatus.entries,
            selectableItems = remember { listOf(YES, NO, AUTOMATIC, NIGHT_AND_DAY) },
            initialSelectedItem = remember { parseLitStatus(element!!.tags) },
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            lastPickedItemContent = { Image(painterResource(it.icon), stringResource(it.title), Modifier.height(24.dp)) },
            onClickOk = { selectedItem ->
                val tagChanges = StringMapChangesBuilder(element!!.tags)
                selectedItem.applyTo(tagChanges)
                applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
            },
            prefs = prefs,
            favoriteKey = "WayLitOverlayForm",
            otherAnswers = listOfNotNull(
                if (element!!.couldBeSteps()) {
                    Answer(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                        val tagChanges = StringMapChangesBuilder(element!!.tags)
                        tagChanges.changeToSteps()
                        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
                    }
                } else {
                    null
                }
            )
        )
    }
}
