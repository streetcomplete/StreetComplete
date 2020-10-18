package de.westnordost.streetcomplete.quests.surface

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R

class DescribeGenericSurfaceDialog(
    context: Context,
    onSurfaceDescribed: (txt: String) -> Unit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {
    val view = LayoutInflater.from(context).inflate(R.layout.quest_surface_detailed_answer_impossible, null)
    val explanationInput = view.findViewById<EditText>(R.id.explanationInput);
    init {
        // TODO enable/disable ok button based on whether commentInput is empty (with TextWatcher?)

        setTitle(context.resources.getString(R.string.quest_surface_detailed_answer_impossible_title))

        setButton(
            DialogInterface.BUTTON_POSITIVE,
            context.getString(android.R.string.yes)
        ) { _, _ ->
            val txt = explanationInput.text.toString().trim()
            if (txt.isNotEmpty()) {
                onSurfaceDescribed(txt)
            }
        }

        setButton(
            DialogInterface.BUTTON_NEGATIVE,
            context.getString(android.R.string.cancel),
            null as DialogInterface.OnClickListener?
        )
        setView(view)

        setOnShowListener {
            getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY)
            explanationInput.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                           count: Int) {
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                    val txt = explanationInput.text.toString().trim()
                    getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = txt.isNotEmpty()
                    if(txt.isNotEmpty()) {
                        getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.rgb(209, 64, 0)) // accent color is D14000
                    } else {
                        getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY)
                    }
                }
            })
        }
    }
}
