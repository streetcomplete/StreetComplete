package de.westnordost.streetcomplete.view.controller

import android.app.Activity
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.view.isInvisible
import de.westnordost.streetcomplete.util.ktx.showKeyboard

/** Manages a [button] that switches the keyboard of the current focused edit text to and from
 *  either text input or number input. The button is only shown and functional if one of the edit
 *  texts in the [targets] set is focused */
class SwitchKeyboardButtonViewController(
    private val activity: Activity,
    private val button: Button,
    private val targets: Set<EditText>
) {
    init {
        button.text = "abc"
        button.setOnClickListener {
            val focus = activity.currentFocus
            if (focus != null && focus is EditText && focus in targets) {
                val start = focus.selectionStart
                val end = focus.selectionEnd
                if (focus.inputType and InputType.TYPE_CLASS_NUMBER != 0) {
                    focus.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    button.text = "123"
                } else {
                    focus.inputType = InputType.TYPE_CLASS_NUMBER
                    focus.keyListener = DigitsKeyListener.getInstance("0123456789.,- /")
                    button.text = "abc"
                }
                // for some reason, the cursor position gets lost first time the input type is set (#1093)
                focus.setSelection(start, end)
                focus.showKeyboard()
            }
        }
        updateVisibility()

        val onFocusChange = View.OnFocusChangeListener { v, hasFocus ->
            updateVisibility()
            if (hasFocus) v.showKeyboard()
        }
        for (target in targets) {
            target.onFocusChangeListener = onFocusChange
        }
    }

    private fun updateVisibility() {
        button.isInvisible = targets.none { it.hasFocus() }
    }
}
