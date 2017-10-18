package de.westnordost.streetcomplete.quests.housenumber;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
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
	public static final String
			HOUSENUMBER = "housenumber",
			HOUSENAME = "housename";

	private static final String	IS_HOUSENAME = "is_housename";
	// i.e. 9999/a, 9/a, 99/9, 99a, 99 a, 9 / a
	public static final String VALID_HOUSENUMBER_REGEX =
			"\\p{N}{1,4}((\\s?/\\s?\\p{N})|(\\s?/?\\s?\\p{L}))?";

	private EditText input;

	private boolean isHousename;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		isHousename = false;
		if(savedInstanceState != null)
		{
			isHousename = savedInstanceState.getBoolean(IS_HOUSENAME);
		}

		if(isHousename)
		{
			setLayout(R.layout.quest_housename);
		}
		else
		{
			setLayout(R.layout.quest_housenumber);
		}

		addOtherAnswer(R.string.quest_address_answer_house_name, new Runnable()
		{
			@Override public void run()
			{
				isHousename = true;
				setLayout(R.layout.quest_housename);
			}
		});

		return view;
	}

	private void setLayout(int layoutResourceId)
	{
		View contentView = setContentView(layoutResourceId);

		input = contentView.findViewById(R.id.input);
		final Button toggleKeyboardButton = contentView.findViewById(R.id.toggleKeyboard);

		if(toggleKeyboardButton != null)
		{
			toggleKeyboardButton.setText("abc");

			toggleKeyboardButton.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					if ((input.getInputType() & InputType.TYPE_CLASS_NUMBER) != 0)
					{
						input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
						toggleKeyboardButton.setText("123");
					} else
					{
						input.setInputType(InputType.TYPE_CLASS_NUMBER);
						input.setKeyListener(DigitsKeyListener.getInstance("0123456789.,- /"));
						toggleKeyboardButton.setText("abc");
					}

					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
				}
			});
		}
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(IS_HOUSENAME, isHousename);
	}

	@Override protected void onClickOk()
	{
		if(!hasChanges())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		final Bundle answer = new Bundle();
		final String input = getInputText();

		if(isHousename)
		{
			answer.putString(HOUSENAME, input);
			applyFormAnswer(answer);
		}
		else
		{
			if (!input.matches(getValidHousenumberRegex()))
			{
				confirmUnusualHousenumber(new Runnable()
				{
					@Override public void run()
					{
						answer.putString(HOUSENUMBER, input);
						applyFormAnswer(answer);
					}
				});
			}
			else
			{
				answer.putString(HOUSENUMBER, input);
				applyFormAnswer(answer);
			}
		}
	}

	private String getValidHousenumberRegex()
	{
		String regexNum = VALID_HOUSENUMBER_REGEX;
		String additionalRegex = getCountryInfo().getAdditionalValidHousenumberRegex();
		if(additionalRegex != null)
		{
			regexNum = "((" + regexNum + ")|("+additionalRegex+"))";
		}
		// i.e. 95-98 etc.
		return "^" + regexNum + "(-" + regexNum + ")?";
	}

	@Override public boolean hasChanges()
	{
		return !getInputText().isEmpty();
	}

	private String getInputText()
	{
		return input.getText().toString().trim();
	}

	private void confirmUnusualHousenumber(final Runnable onConfirmed)
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				onConfirmed.run();
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
