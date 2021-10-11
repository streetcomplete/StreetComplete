package de.westnordost.streetcomplete.quests.foot

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestMaxspeedLivingStreetConfirmationBinding
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.*

class AddProhibitedForPedestriansForm : AbstractQuestAnswerFragment<ProhibitedForPedestriansAnswer>() {

    override val contentLayoutResId = R.layout.quest_prohibited_for_pedestrians_separate_sidewalk_explanation

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) },
        AnswerItem(R.string.quest_sidewalk_value_yes) { applyAnswer(HAS_SEPARATE_SIDEWALK) }
    )

    // the living street answer stuff is copied from AddMaxSpeedForm
    override val otherAnswers: List<AnswerItem> get() {
        val result = mutableListOf<AnswerItem>()

        val highwayTag = osmElement!!.tags["highway"]!!
        if (countryInfo.isLivingStreetKnown && MAYBE_LIVING_STREET.contains(highwayTag)) {
            result.add(AnswerItem(R.string.quest_maxspeed_answer_living_street) { confirmLivingStreet() })
        }
        return result
    }

    private fun confirmLivingStreet() {
        val ctx = context ?: return
        val dialogBinding = QuestMaxspeedLivingStreetConfirmationBinding.inflate(layoutInflater)
        // this is necessary because the inflated image view uses the activity context rather than
        // the fragment / layout inflater context' resources to access it's drawable
        dialogBinding.livingStreetImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_living_street))
        AlertDialog.Builder(ctx)
            .setView(dialogBinding.root)
            .setTitle(R.string.quest_maxspeed_answer_living_street_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IS_LIVING_STREET) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    companion object {
        private val MAYBE_LIVING_STREET = listOf("residential", "unclassified")
    }
}
