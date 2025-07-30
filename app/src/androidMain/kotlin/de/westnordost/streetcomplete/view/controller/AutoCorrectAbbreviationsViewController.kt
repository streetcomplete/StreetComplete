package de.westnordost.streetcomplete.view.controller

import android.text.Editable
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import de.westnordost.streetcomplete.data.meta.Abbreviations
import de.westnordost.streetcomplete.view.DefaultTextWatcher

/** Automatically expands abbreviations when finishing a word (via space, "-" or ".")  */
class AutoCorrectAbbreviationsViewController(private val editText: EditText) {

    var abbreviations: Abbreviations? = null

    init {
        if (editText.imeOptions and EditorInfo.IME_ACTION_DONE == 0
            || editText.imeOptions and EditorInfo.IME_ACTION_NEXT == 0) {
            editText.imeOptions = EditorInfo.IME_ACTION_DONE or editText.imeOptions
        }

        editText.addTextChangedListener(AbbreviationAutoCorrecter())

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                editText.text?.let { autoCorrectTextAt(it, editText.length()) }
            }
            false
        }
    }

    private fun autoCorrectTextAt(s: Editable, cursor: Int) {
        val abbrs = abbreviations ?: return

        val textToCursor = s.subSequence(0, cursor).trim().toString()
        val words = textToCursor.split("[ -]+".toRegex())
        // really no idea how the result of split can ever be empty, but it apparently happens sometimes #287
        if (words.isEmpty()) return
        val lastWordBeforeCursor = words[words.size - 1]

        val isFirstWord = words.size == 1
        val isLastWord = cursor == s.length

        val replacement = abbrs.getExpansion(lastWordBeforeCursor, isFirstWord, isLastWord)

        if (replacement != null) {
            val wordStart = textToCursor.indexOf(lastWordBeforeCursor)
            fixedReplace(s, wordStart, wordStart + lastWordBeforeCursor.length, replacement)
        }
    }

    private fun fixedReplace(s: Editable, replaceStart: Int, replaceEnd: Int, replaceWith: CharSequence) {
        // if I only call s.replace to replace the abbreviations with their respective expansions,
        // the caret seems to get confused. On my Android API level 19, if e.g. an abbreviation of
        // two letters is replaced with a word with ten letters, then I can not edit/delete the first
        // eight letters of the edit text anymore.
        // This method re-sets the text completely, so the caret and text length are also set anew

        val selEnd = editText.selectionEnd
        editText.text = s.replace(replaceStart, replaceEnd, replaceWith)
        val replaceLength = replaceEnd - replaceStart
        val addedCharacters = replaceWith.length - replaceLength
        editText.setSelection(selEnd + addedCharacters)
    }

    private inner class AbbreviationAutoCorrecter : DefaultTextWatcher() {
        private var cursorPos: Int = 0
        private var lastTextLength: Int = 0
        private var addedText: Boolean = false

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            cursorPos = start + count
            addedText = lastTextLength < s.length
            lastTextLength = s.length
        }

        override fun afterTextChanged(s: Editable) {
            if (addedText) {
                val endedWord = s.subSequence(0, cursorPos).toString().matches(".+[ -.]+$".toRegex())
                if (endedWord) {
                    autoCorrectTextAt(s, cursorPos)
                }
            }
        }
    }
}
