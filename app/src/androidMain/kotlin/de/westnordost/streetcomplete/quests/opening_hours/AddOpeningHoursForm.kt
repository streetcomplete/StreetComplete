package de.westnordost.streetcomplete.quests.opening_hours

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_add_times
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursCommentDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode
import de.westnordost.streetcomplete.ui.util.content
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.stringResource

class AddOpeningHoursForm : AbstractOsmQuestForm<OpeningHoursAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private var originalOpeningHours: HierarchicOpeningHours? = null

    private var isDisplayingPrevious: MutableState<Boolean> = mutableStateOf(false)

    private var openingHours: MutableState<HierarchicOpeningHours> =
        mutableStateOf(HierarchicOpeningHours())

    private val showCommentDialog: MutableState<Boolean> = mutableStateOf(false)

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious.value) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { isDisplayingPrevious.value = false },
                AnswerItem(R.string.quest_generic_hasFeature_yes) {
                    applyAnswer(RegularOpeningHours(originalOpeningHours!!))
                }
            )
        } else {
            emptyList()
        }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_openingHours_no_sign) { confirmNoSign() },
        AnswerItem(R.string.quest_openingHours_answer_no_regular_opening_hours) { showCommentDialog.value = true },
        AnswerItem(R.string.quest_openingHours_answer_247) { showConfirm24_7Dialog() },
        AnswerItem(R.string.quest_openingHours_answer_seasonal_opening_hours) {
            isDisplayingPrevious.value = false
            val allMonths = listOf(MonthRange(Month.January, Month.December))
            openingHours.value = HierarchicOpeningHours(
                openingHours.value.monthsList.map { months ->
                    if (months.selectors.isEmpty()) months.copy(selectors = allMonths) else months
                }
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalOpeningHours = element.tags["opening_hours"]
            ?.toOpeningHoursOrNull(lenient = true)
            ?.toHierarchicOpeningHours()
        isDisplayingPrevious.value = originalOpeningHours != null
        openingHours.value = originalOpeningHours ?: HierarchicOpeningHours()
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
                timeMode = TimeMode.Spans,
                countryInfo = countryInfo,
                addButtonContent = { Text(stringResource(Res.string.quest_openingHours_add_times)) },
                locale = countryInfo.userPreferredLocale,
                userLocale = Locale.current,
                enabled = !isDisplayingPrevious.value,
            )

            if (showCommentDialog.value) {
                OpeningHoursCommentDialog(
                    onDismissRequest = { showCommentDialog.value = false },
                    onConfirm = { applyAnswer(DescribeOpeningHours(it)) }
                )
            }
        } }
        checkIsFormComplete()
    }

    override fun onClickOk() {
        applyAnswer(RegularOpeningHours(openingHours.value))
    }

    private fun showConfirm24_7Dialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_openingHours_24_7_confirmation)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(AlwaysOpen) }
            .setNegativeButton(R.string.quest_generic_hasFeature_no, null)
            .show()
    }

    private fun confirmNoSign() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoOpeningHoursSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() =
        openingHours.value.isComplete() && !isDisplayingPrevious.value
}
