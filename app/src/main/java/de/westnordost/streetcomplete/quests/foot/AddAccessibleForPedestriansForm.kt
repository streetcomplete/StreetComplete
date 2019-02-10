package de.westnordost.streetcomplete.quests.foot

import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.foot.AccessibleForPedestriansAnswer.*

class AddAccessibleForPedestriansForm : AYesNoQuestAnswerFragment<AccessibleForPedestriansAnswer>() {

    override val contentLayoutResId = R.layout.quest_accessible_for_pedestrians_explanation

    // the living street answer stuff is copied from AddMaxSpeedForm
    override val otherAnswers: List<OtherAnswer> get() {
        val result = mutableListOf<OtherAnswer>()

        val highwayTag = osmElement!!.tags["highway"]!!
        if (countryInfo.isLivingStreetKnown && MAYBE_LIVING_STREET.contains(highwayTag)) {
            result.add(OtherAnswer(R.string.quest_maxspeed_answer_living_street) { confirmLivingStreet() })
        }
        return result
    }

    override fun onClick(answer: Boolean) { applyAnswer(if(answer) YES else NO) }

    private fun confirmLivingStreet() {
        activity?.let {
            val view = layoutInflater.inflate(R.layout.quest_maxspeed_living_street_confirmation, null, false)
            // this is necessary because the inflated image view uses the activity context rather than
            // the fragment / layout inflater context' resources to access it's drawable
            val img = view.findViewById<ImageView>(R.id.livingStreetImage)
            img.setImageDrawable(resources.getDrawable(R.drawable.ic_living_street))
            AlertDialog.Builder(it)
                .setView(view)
                .setTitle(R.string.quest_maxspeed_answer_living_street_confirmation_title)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IS_LIVING_STREET) }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

    companion object {
        private val MAYBE_LIVING_STREET = listOf("residential", "unclassified")
    }
}
