package de.westnordost.streetcomplete.quests.postbox_collection_times

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope
import de.westnordost.osm_opening_hours.parser.toOpeningHours
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_collectionTimes_add_times
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode
import de.westnordost.streetcomplete.ui.util.content
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.stringResource

class AddPostboxCollectionTimesForm : AbstractOsmQuestForm<CollectionTimesAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private var originalOpeningHours: HierarchicOpeningHours? = null

    private var isDisplayingPrevious: MutableState<Boolean> = mutableStateOf(false)

    private var timeMode: MutableState<TimeMode> = mutableStateOf(TimeMode.Points)

    private var openingHours: MutableState<HierarchicOpeningHours> =
        mutableStateOf(HierarchicOpeningHours())

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious.value) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { isDisplayingPrevious.value = false },
                AnswerItem(R.string.quest_generic_hasFeature_yes) {
                    applyAnswer(CollectionTimes(originalOpeningHours!!))
                }
            )
        } else {
            emptyList()
        }

    override val otherAnswers get() = listOf(
        AnswerItem(R.string.quest_collectionTimes_answer_no_times_specified) { confirmNoSign() },
        when (timeMode.value) {
            TimeMode.Points -> AnswerItem(R.string.quest_collectionTimes_answer_time_spans) {
                timeMode.value = TimeMode.Spans
            }
            TimeMode.Spans -> AnswerItem(R.string.quest_collectionTimes_answer_time_points) {
                timeMode.value = TimeMode.Points
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val oh = element.tags["collection_times"]?.toOpeningHoursOrNull(lenient = true)
        originalOpeningHours = oh?.toHierarchicOpeningHours(allowTimePoints = true)
        isDisplayingPrevious.value = originalOpeningHours != null
        openingHours.value = originalOpeningHours ?: HierarchicOpeningHours()
        timeMode.value = if (oh?.containsTimeSpans() == true) TimeMode.Spans else TimeMode.Points
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapshotFlow { isDisplayingPrevious.value }
            .onEach {
                updateButtonPanel()
                checkIsFormComplete()
            }
            .launchIn(lifecycleScope)

        binding.composeViewBase.content { Surface {
            OpeningHoursTable(
                openingHours = openingHours.value,
                onChange = {
                    openingHours.value = it
                    checkIsFormComplete()
                },
                timeMode = timeMode.value,
                countryInfo = countryInfo,
                addButtonContent = { Text(stringResource(Res.string.quest_collectionTimes_add_times)) },
                locale = countryInfo.userPreferredLocale,
                userLocale = Locale.current,
                enabled = !isDisplayingPrevious.value,
            )
        } }

        checkIsFormComplete()
    }

    override fun onClickOk() {
        applyAnswer(CollectionTimes(openingHours.value))
    }

    private fun confirmNoSign() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoCollectionTimesSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() =
        openingHours.value.isComplete() && !isDisplayingPrevious.value
}
