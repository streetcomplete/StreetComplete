package de.westnordost.streetcomplete.quests.localized_name;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import java.util.Locale;

import de.westnordost.streetcomplete.data.meta.Abbreviations;
import de.westnordost.streetcomplete.util.DefaultTextWatcher;

/** An edit text that expands abbreviations automatically when finishing a word (via space, "-" or
 *  ".") and capitalizes the first letter of each word that is longer than 3 letters. */
public class AutoCorrectAbbreviationsEditText extends android.support.v7.widget.AppCompatEditText
{
	private Abbreviations abbreviations;

	public AutoCorrectAbbreviationsEditText(Context context)
	{
		super(context);
		init();
	}

	public AutoCorrectAbbreviationsEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public AutoCorrectAbbreviationsEditText(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setAbbreviations(Abbreviations abbreviations)
	{
		this.abbreviations = abbreviations;
	}

	private void init()
	{
		setImeOptions(EditorInfo.IME_ACTION_DONE | getImeOptions());
		addTextChangedListener(new AbbreviationAutoCorrecter());

		setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
				EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

		setOnEditorActionListener((v, actionId, event) ->
		{
			if (actionId == EditorInfo.IME_ACTION_DONE)
			{
				autoCorrectTextAt(getText(), length());
			}
			return false;
		});
	}

	private void autoCorrectTextAt(Editable s, int cursor)
	{
		String textToCursor = s.subSequence(0, cursor).toString();
		String[] words = textToCursor.split("[ -]+");
		// really no idea how the result of split can ever be empty, but it apparently happens sometimes #287
		if(words.length == 0) return;
		String lastWordBeforeCursor = words[words.length-1];

		boolean isFirstWord = words.length == 1;
		boolean isLastWord = cursor == s.length();

		if(abbreviations == null) return;
		String replacement = abbreviations.getExpansion(lastWordBeforeCursor, isFirstWord, isLastWord);

		int wordStart = textToCursor.indexOf(lastWordBeforeCursor);
		if(replacement != null)
		{
			fixedReplace(s, wordStart, wordStart + lastWordBeforeCursor.length(), replacement);
		}
		else if (lastWordBeforeCursor.length() > 3)
		{
			Locale locale = abbreviations.getLocale();
			String capital = lastWordBeforeCursor.substring(0, 1).toUpperCase(locale);
			s.replace(wordStart, wordStart + 1, capital);
		}
	}

	private void fixedReplace(Editable s, int replaceStart, int replaceEnd, CharSequence replaceWith)
	{
		// if I only call s.replace to replace the abbreviations with their respective expansions,
		// the caret seems to get confused. On my Android API level 19, if i.e. an abbreviation of
		// two letters is replaced with a word with ten letters, then I can not edit/delete the first
		// eight letters of the edit text anymore.
		// This method re-sets the text completely, so the caret and text length are also set anew

		int selEnd = getSelectionEnd();
		setText(s.replace(replaceStart, replaceEnd, replaceWith));
		int replaceLength = replaceEnd - replaceStart;
		int addedCharacters = replaceWith.length() - replaceLength;
		setSelection(selEnd + addedCharacters);
	}

	private class AbbreviationAutoCorrecter extends DefaultTextWatcher
	{
		private int cursorPos;
		private int lastTextLength;
		private boolean addedText;

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			cursorPos = start + count;
			addedText = lastTextLength < s.length();
			lastTextLength = s.length();
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			if(addedText)
			{
				boolean endedWord = s.subSequence(0, cursorPos).toString().matches(".+[ -]+$");
				if (endedWord)
				{
					autoCorrectTextAt(s, cursorPos);
				}
			}
		}
	}
}
