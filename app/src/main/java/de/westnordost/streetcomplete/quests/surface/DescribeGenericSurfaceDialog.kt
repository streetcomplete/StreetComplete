package de.westnordost.streetcomplete.quests.surface

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestSurfaceDetailedAnswerImpossibleBinding

class DescribeGenericSurfaceDialog(
    context: Context,
    onSurfaceDescribed: (txt: String) -> Unit
) : AlertDialog(context) {
    init {
        val binding = QuestSurfaceDetailedAnswerImpossibleBinding.inflate(LayoutInflater.from(context))

        setTitle(context.resources.getString(R.string.quest_surface_detailed_answer_impossible_title))

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            val txt = binding.explanationInput.text.toString().trim()

            if (txt.isEmpty()) {
                Builder(context)
                    .setMessage(R.string.quest_surface_detailed_answer_impossible_description)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            } else {
                onSurfaceDescribed(txt)
            }
        }

        setButton(
            DialogInterface.BUTTON_NEGATIVE,
            context.getString(android.R.string.cancel),
            null as DialogInterface.OnClickListener?
        )
        setView(binding.root)
    }
}
