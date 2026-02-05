package de.westnordost.streetcomplete.quests.diet_type

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.databinding.ComposeViewBinding.bind
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_NO
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_ONLY
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.create_new_note_hint
import de.westnordost.streetcomplete.resources.quest_defibrillator_location_description
import de.westnordost.streetcomplete.resources.quest_dietType_explanation
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.stringResource

class AddDietTypeForm : AbstractOsmQuestForm<DietAvailabilityAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers: List<AnswerItem> get() {
        val result = mutableListOf<AnswerItem>()
        if (element.tags["amenity"] == "cafe") {
            result.add(AnswerItem(R.string.quest_diet_answer_no_food) { confirmNoFood() })
        }
        return result
    }

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(DIET_NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(DIET_YES) },
        AnswerItem(R.string.quest_hasFeature_only) { applyAnswer(DIET_ONLY) },
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_dietType_explanation))
            }
        } }
    }

    private fun confirmNoFood() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoFood) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
