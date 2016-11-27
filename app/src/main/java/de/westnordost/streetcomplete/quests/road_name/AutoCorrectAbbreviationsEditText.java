package de.westnordost.streetcomplete.quests.road_name;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import javax.inject.Inject;

import dagger.Lazy;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.meta.Abbreviations;
import de.westnordost.streetcomplete.data.meta.CurrentCountry;

/** An edit text that expands abbreviations automatically when finishing a word (via space, "-" or
 *  ".") and capitalizes the first letter of each word that is longer than 3 letters. */
public class AutoCorrectAbbreviationsEditText extends EditText
{
	// Creation of Abbreviations takes some time, let's defer it until after the injection (so that
	// we can load it asynchronously
	@Inject Lazy<Abbreviations> abbreviations;
	@Inject CurrentCountry currentCountry;

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

	private void init()
	{
		Injector.instance.getApplicationComponent().inject(this);

		// asynchronously load the abbreviations already because we will need it latest after the
		// user wrote the first word (in debug mode, it takes whopping 3 seconds on my phone :-( )
		new Thread()
		{
			@Override public void run()
			{
				abbreviations.get();
			}
		}.start();

		setImeOptions(EditorInfo.IME_ACTION_DONE | getImeOptions());
		addTextChangedListener(new AbbreviationAutoCorrecter());

		setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
				EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

		setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_DONE)
				{
					autoCorrectTextAt(getText(), length());
				}
				return false;
			}
		});
	}

	private void autoCorrectTextAt(Editable s, int cursor)
	{
		String textToCursor = s.subSequence(0, cursor).toString();
		String[] words = textToCursor.split("[ -]+");
		String lastWordBeforeCursor = words[words.length-1];

		boolean isFirstWord = words.length == 1;
		boolean isLastWord = cursor == s.length();

		String replacement = abbreviations.get().getExpansion(lastWordBeforeCursor, isFirstWord, isLastWord);

		int wordStart = textToCursor.indexOf(lastWordBeforeCursor);
		if(replacement != null)
		{
			fixedReplace(s, wordStart, wordStart + lastWordBeforeCursor.length(), replacement);
		}
		else if (lastWordBeforeCursor.length() > 3)
		{
			Locale locale = currentCountry.getLocale();
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

	public boolean containsAbbreviations()
	{
		return abbreviations.get().containsAbbreviations(getText().toString());
	}

	private class AbbreviationAutoCorrecter implements TextWatcher
	{
		private int cursorPos;
		private int lastTextLength;
		private boolean addedText;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

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
