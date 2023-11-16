package de.westnordost.streetcomplete.quests.piste_ref

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewPisteRefBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddPisteRefForm : AbstractOsmQuestForm<PisteRefAnswer>() {

    override val contentLayoutResId = R.layout.view_piste_ref
    private val binding by contentViewBinding(ViewPisteRefBinding::bind)

    override val otherAnswers get() =
        listOfNotNull(
            AnswerItem(R.string.quest_piste_ref_connection) { confirmPisteConnection() }
        )

    private val ref get() = binding.pisteRefInput.nonBlankTextOrNull

    override fun onClickOk() {
        applyAnswer(PisteRef(ref!!))
    }

    private fun confirmPisteConnection() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(
                PisteConnection
            ) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pisteDifficulty = element.tags["piste:difficulty"]
        val color = getColorForPisteDifficulty(pisteDifficulty)
        binding.pisteRefInput.background = ShapeDrawable(OvalShape())
        binding.pisteRefInput.background.setTint(color)
        binding.pisteRefInput.doAfterTextChanged { checkIsFormComplete() }
    }

    private fun getColorForPisteDifficulty(difficulty: String?): Int {
        return when (difficulty) {
            "novice" -> Color.parseColor("#008351")
            "easy" -> Color.parseColor("#2255BB")
            "intermediate" -> Color.parseColor("#C1121C")
            "advanced" -> Color.parseColor("#000000")
            else -> Color.parseColor("#8e9291")
        }
    }

    override fun isFormComplete() = ref != null
}
