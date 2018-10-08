package de.westnordost.streetcomplete.quests.housenumber;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.quests.building_type.BuildingType;
import de.westnordost.streetcomplete.util.TextChangedWatcher;
import de.westnordost.streetcomplete.view.Item;
import de.westnordost.streetcomplete.view.ItemViewHolder;


public class AddHousenumberForm extends AbstractQuestFormAnswerFragment
{
	public static final String
			NO_ADDRESS = "noaddress",
			HOUSENUMBER = "housenumber",
			HOUSENAME = "housename",
			CONSCRIPTIONNUMBER = "conscriptionnumber",
			STREETNUMBER = "streetnumber";

	private static final String	IS_HOUSENAME = "is_housename";
	// i.e. 9999/a, 9/a, 99/9, 99a, 99 a, 9 / a
	public static final String VALID_HOUSENUMBER_REGEX =
			"\\p{N}{1,4}((\\s?/\\s?\\p{N})|(\\s?/?\\s?\\p{L}))?";

	public static final String VALID_CONSCRIPTIONNUMBER_REGEX =	"\\p{N}{1,6}";

	@Nullable private EditText inputHouseNumber, inputHouseName, inputConscriptionNumber, inputStreetNumber;

	private boolean isHousename;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		restoreInstanceState(savedInstanceState);

		if(isHousename)
		{
			setLayout(R.layout.quest_housename);
		}
		else
		{
			setLayout(R.layout.quest_housenumber);
		}

		addOtherAnswers();

		return view;
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(IS_HOUSENAME, isHousename);
	}

	private void restoreInstanceState(Bundle inState)
	{
		isHousename = false;
		if(inState != null)
		{
			isHousename = inState.getBoolean(IS_HOUSENAME);
		}
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_address_answer_no_housenumber, this::onNoHouseNumber);

		addOtherAnswer(R.string.quest_address_answer_house_name, () ->
		{
			isHousename = true;
			setLayout(R.layout.quest_housename);
		});

		addOtherAnswer(R.string.quest_housenumber_multiple_numbers, () ->
		{
			new AlertDialog.Builder(getActivity())
				.setMessage(R.string.quest_housenumber_multiple_numbers_description)
				.setPositiveButton(android.R.string.ok, null)
				.show();
		});
	}

	@Override protected void onClickOk()
	{
		if(inputHouseName != null)
		{
			applyHouseNameAnswer(getInputText(inputHouseName));
		}
		else if(inputConscriptionNumber != null && inputStreetNumber != null)
		{
			applyConscriptionNumberAnswer(getInputText(inputConscriptionNumber), getInputText(inputStreetNumber));
		}
		else if(inputHouseNumber != null)
		{
			applyHouseNumberAnswer(getInputText(inputHouseNumber));
		}
	}

	private void onNoHouseNumber()
	{
		String buildingValue = getOsmElement().getTags().get("building");
		Item item = BuildingType.getByTag("building", buildingValue);
		if(item != null)
		{
			View inner = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_quest_address_no_housenumber, null, false);
			new ItemViewHolder(inner.findViewById(R.id.item_view)).bind(item);

			new AlertDialog.Builder(getActivity())
				.setView(inner)
				.setPositiveButton(R.string.quest_generic_hasFeature_yes, (dialog, which) -> applyNoHouseNumberAnswer())
				.setNegativeButton(R.string.quest_generic_hasFeature_no_leave_note, (dialog, which) -> onClickCantSay())
				.show();
		} else {
			// fallback in case the type of building is known by Housenumber quest but not by
			// building type quest
			onClickCantSay();
		}
	}

	private void applyNoHouseNumberAnswer()
	{
		Bundle answer = new Bundle();
		answer.putBoolean(NO_ADDRESS, true);
		applyAnswer(answer);
	}

	private void applyHouseNameAnswer(final String houseName)
	{
		Bundle answer = new Bundle();
		answer.putString(HOUSENAME, houseName);
		applyAnswer(answer);
	}

	private void applyHouseNumberAnswer(final String houseNumber)
	{
		final Bundle answer = new Bundle();
		boolean looksInvalid = !houseNumber.matches(getValidHousenumberRegex());

		confirmHousenumber(looksInvalid, () ->
		{
			answer.putString(HOUSENUMBER, houseNumber);
			applyAnswer(answer);
		});
	}

	private void applyConscriptionNumberAnswer(final String conscriptionNumber, final String streetNumber)
	{
		final Bundle answer = new Bundle();
		boolean looksInvalid = !conscriptionNumber.matches(VALID_CONSCRIPTIONNUMBER_REGEX);
		if(!streetNumber.isEmpty())
		{
			looksInvalid |= !streetNumber.matches(getValidHousenumberRegex());
		}

		confirmHousenumber(looksInvalid, () ->
		{
			answer.putString(CONSCRIPTIONNUMBER, conscriptionNumber);
			// streetNumber is optional
			if(!streetNumber.isEmpty())
			{
				answer.putString(STREETNUMBER, streetNumber);
			}
			applyAnswer(answer);
		});
	}

	@Override public boolean isFormComplete()
	{
		// streetNumber is always optional
		EditText input = getFirstNonNull(inputHouseNumber, inputHouseName, inputConscriptionNumber);
		return input != null && !getInputText(input).isEmpty();
	}

	private void setLayout(int layoutResourceId)
	{
		View view = setContentView(layoutResourceId);

		inputHouseNumber = view.findViewById(R.id.inputHouseNumber);
		inputHouseName = view.findViewById(R.id.inputHouseName);
		inputConscriptionNumber = view.findViewById(R.id.inputConscriptionNumber);
		inputStreetNumber = view.findViewById(R.id.inputStreetNumber);

		TextWatcher onChanged = new TextChangedWatcher(this::checkIsFormComplete);
		if(inputHouseNumber != null) inputHouseNumber.addTextChangedListener(onChanged);
		if(inputHouseName != null) inputHouseName.addTextChangedListener(onChanged);
		if(inputConscriptionNumber != null) inputConscriptionNumber.addTextChangedListener(onChanged);
		if(inputStreetNumber != null) inputStreetNumber.addTextChangedListener(onChanged);

		// streetNumber is always optional
		EditText input = getFirstNonNull(inputHouseNumber, inputHouseName, inputConscriptionNumber);
		if(input != null) input.requestFocus();

		initKeyboardButton(view);
	}

	private static EditText getFirstNonNull(EditText ...view)
	{
		for (EditText editText : view)
		{
			if(editText != null) return editText;
		}
		return null;
	}

	private void initKeyboardButton(View view)
	{
		final Button toggleKeyboardButton = view.findViewById(R.id.toggleKeyboard);
		if(toggleKeyboardButton != null)
		{
			toggleKeyboardButton.setText("abc");
			toggleKeyboardButton.setOnClickListener(v ->
			{
				View focus = getActivity().getCurrentFocus();
				if(focus != null && focus instanceof EditText)
				{
					EditText input = (EditText) focus;
					int start = input.getSelectionStart();
					int end = input.getSelectionEnd();
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
					// for some reason, the cursor position gets lost first time the input type is set (#1093)
					input.setSelection(start, end);

					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
				}
			});
		}
	}

	private String getValidHousenumberRegex()
	{
		String regex = VALID_HOUSENUMBER_REGEX;
		String additionalRegex = getCountryInfo().getAdditionalValidHousenumberRegex();
		if(additionalRegex != null)
		{
			regex = "((" + regex + ")|(" + additionalRegex + "))";
		}
		// i.e. "95-98" or "5,5a,6" etc. (but not: "1, 3" or "3 - 5" or "5,6-7")
		return "^" + regex + "((-" + regex + ")|(," + regex + ")+)?";
	}

	private static String getInputText(EditText editText)
	{
		return editText.getText().toString().trim();
	}

	private void confirmHousenumber(boolean isUnusual, final Runnable onConfirmed)
	{
		if(isUnusual)
		{
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.quest_generic_confirmation_title)
					.setMessage(R.string.quest_address_unusualHousenumber_confirmation_description)
					.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) -> onConfirmed.run())
					.setNegativeButton(R.string.quest_generic_confirmation_no, null)
					.show();
		}
		else
		{
			onConfirmed.run();
		}
	}
}
