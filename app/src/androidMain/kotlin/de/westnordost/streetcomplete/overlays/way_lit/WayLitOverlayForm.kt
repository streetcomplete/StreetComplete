package de.westnordost.streetcomplete.overlays.way_lit

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.LitStatus.AUTOMATIC
import de.westnordost.streetcomplete.osm.lit.LitStatus.NIGHT_AND_DAY
import de.westnordost.streetcomplete.osm.lit.LitStatus.NO
import de.westnordost.streetcomplete.osm.lit.LitStatus.YES
import de.westnordost.streetcomplete.osm.lit.applyTo
import de.westnordost.streetcomplete.osm.lit.icon
import de.westnordost.streetcomplete.osm.lit.parseLitStatus
import de.westnordost.streetcomplete.osm.lit.title
import de.westnordost.streetcomplete.overlays.AItemSelectOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class WayLitOverlayForm : AItemSelectOverlayForm<LitStatus>() {

    override val items: List<LitStatus> = LitStatus.entries

    override val selectableItems: List<LitStatus> =
        listOf(YES, NO, AUTOMATIC, NIGHT_AND_DAY)

    private val prefs: Preferences by inject()

    override val lastPickedItem: LitStatus? get() =
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<LitStatus>(it) }
            .firstOrNull()

    private var originalLitStatus: LitStatus? = null

    override val otherAnswers get() = listOfNotNull(
        createConvertToStepsAnswer()
    )

    @Composable override fun ItemContent(item: LitStatus) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    @Composable override fun LastPickedItemContent(item: LitStatus) {
        Image(painterResource(item.icon), stringResource(item.title))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        originalLitStatus = parseLitStatus(element!!.tags)
        selectedItem.value = originalLitStatus
    }

    override fun hasChanges(): Boolean =
        selectedItem.value != originalLitStatus

    override fun onClickOk() {
        prefs.addLastPicked(this::class.simpleName!!, selectedItem.value!!.name)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }

    private fun createConvertToStepsAnswer(): AnswerItem? =
        if (element!!.couldBeSteps()) {
            AnswerItem(R.string.quest_generic_answer_is_actually_steps) { changeToSteps() }
        } else {
            null
        }

    private fun changeToSteps() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        tagChanges.changeToSteps()
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
