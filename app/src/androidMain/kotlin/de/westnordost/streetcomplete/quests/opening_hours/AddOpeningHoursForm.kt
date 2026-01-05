package de.westnordost.streetcomplete.quests.opening_hours

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
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
import de.westnordost.streetcomplete.resources.quest_openingHours_comment_description
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

    private var isAlwaysDisplayingMonths: MutableState<Boolean> = mutableStateOf(false)

    private var openingHours: MutableState<HierarchicOpeningHours> =
        mutableStateOf(HierarchicOpeningHours())

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious.value) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { isDisplayingPrevious.value = false },
                AnswerItem(R.string.quest_generic_hasFeature_yes) {
                    applyAnswer(RegularOpeningHours(
                        element.tags["opening_hours"]!!.toOpeningHours(lenient = true)
                    ))
                }
            )
        } else {
            emptyList()
        }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_openingHours_no_sign) { confirmNoSign() },
        AnswerItem(R.string.quest_openingHours_answer_no_regular_opening_hours) { showInputCommentDialog() },
        AnswerItem(R.string.quest_openingHours_answer_247) { showConfirm24_7Dialog() },
        AnswerItem(R.string.quest_openingHours_answer_seasonal_opening_hours) {
            isDisplayingPrevious.value = false
            isAlwaysDisplayingMonths.value = true
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
                locale = countryInfo.userPreferredLocale,
                userLocale = Locale.current,
                enabled = !isDisplayingPrevious.value,
                displayMonths = isAlwaysDisplayingMonths.value,
            )
        } }
        checkIsFormComplete()
    }

    override fun onClickOk() {
        applyAnswer(RegularOpeningHours(openingHours.value.toOpeningHours()))
    }

    private fun showInputCommentDialog() {
        val comment = mutableStateOf("")
        val dialogBinding = ComposeViewBinding.inflate(layoutInflater)
        dialogBinding.composeViewBase.content { Surface(Modifier.padding(24.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_openingHours_comment_description))
                }
                TextField(
                    value = comment.value,
                    onValueChange = {
                        val noDoubleQuotes = it.replace("\"", "").trim()
                        if (noDoubleQuotes.length < 253) comment.value = noDoubleQuotes
                    }
                )
            }
        } }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_openingHours_comment_title)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (comment.value.isNotEmpty()) {
                    applyAnswer(DescribeOpeningHours(comment.value))
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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
