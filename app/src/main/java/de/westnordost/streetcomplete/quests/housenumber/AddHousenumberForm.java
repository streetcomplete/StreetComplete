package de.westnordost.streetcomplete.quests.housenumber;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddHousenumberForm extends AbstractQuestFormAnswerFragment
{
	public static final String HOUSENUMBER = "housenumber";

	// i.e. 9999/a, 9/a, 99/9, 99a, 99 a, 9 / a
	public static final String VALID_HOUSENUMBER_REGEX =
			"\\p{N}{1,4}((\\s?/\\s?\\p{N})|(\\s?/?\\s?\\p{L}))?";

	private EditText input;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_address_title);

		View contentView = setContentView(R.layout.quest_housenumber);

		input = (EditText) contentView.findViewById(R.id.input);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		final Button toggleKeyboardButton = (Button) contentView.findViewById(R.id.toggleKeyboard);
		toggleKeyboardButton.setText("abc");

		toggleKeyboardButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				if((input.getInputType() & InputType.TYPE_CLASS_NUMBER) != 0)
				{
					input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					toggleKeyboardButton.setText("123");
				}
				else
				{
					input.setInputType(InputType.TYPE_CLASS_NUMBER);
					toggleKeyboardButton.setText("abc");
				}

				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
			}
		});

		return view;
	}

	@Override protected void onClickOk()
	{
		if(!hasChanges())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		String regexNum = VALID_HOUSENUMBER_REGEX;
		String additionalRegex = getCountryInfo().getAdditionalValidHousenumberRegex();
		if(additionalRegex != null)
		{
			regexNum = "((" + regexNum + ")|("+additionalRegex+"))";
		}
		// i.e. 95-98 etc.
		String completeRegex = "^" + regexNum + "(-" + regexNum + ")?";

		if(!getHousenumber().matches(completeRegex))
		{
			confirmUnusualHousenumber();
		}
		else
		{
			applyHousenumberAnswer();
		}
	}

	@Override public boolean hasChanges()
	{
		return !getHousenumber().isEmpty();
	}

	private void applyHousenumberAnswer()
	{
		Bundle answer = new Bundle();
		answer.putString(HOUSENUMBER, getHousenumber());
		applyFormAnswer(answer);
	}

	private String getHousenumber()
	{
		return input.getText().toString().trim();
	}

	private void confirmUnusualHousenumber()
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				applyHousenumberAnswer();
			}
		};
		DialogInterface.OnClickListener onNo = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// nothing, just go back
			}
		};

		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_generic_confirmation_title)
				.setMessage(R.string.quest_address_unusualHousenumber_confirmation_description)
				.setPositiveButton(R.string.quest_generic_confirmation_yes, onYes)
				.setNegativeButton(R.string.quest_generic_confirmation_no, onNo)
				.show();
	}
}
