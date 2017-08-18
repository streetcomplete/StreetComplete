package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddDietTypeForm extends AbstractQuestAnswerFragment
{
	public static final String OTHER_ANSWER = "OTHER_ANSWER";
	public static final String ONLY_VEGETARIAN = "ONLY_VEGETARIAN";
	public static final String ONLY_VEGAN = "ONLY_VEGAN";
	public static final String NO = "NO";
	public static final String VEGETARIAN = "VEGETARIAN";
	public static final String VEGAN = "VEGAN";
	public static final String ANSWER = "answer";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View buttonPanel = setButtonsView(R.layout.quest_diet_type);

		Button buttonVegan = (Button) buttonPanel.findViewById(R.id.buttonVegan);
		buttonVegan.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(VEGAN);
			}
		});
		Button buttonVegetarian = (Button) buttonPanel.findViewById(R.id.buttonVegetarian);
		buttonVegetarian.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(VEGETARIAN);
			}
		});
		Button buttonNo = (Button) buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(NO);
			}
		});

		setTitle();
		addOtherAnswers();
		return view;
	}

	private void setTitle()
	{
		String name = getElementName();
		if(name != null)
		{
			setTitle(R.string.quest_dietType_name_title, name);
		}
		else
		{
			setTitle(R.string.quest_dietType_title);
		}
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_dietType_only_vegetarian, new Runnable()
		{
			@Override public void run()
			{
				applyAnswer(ONLY_VEGETARIAN);
			}
		});
		addOtherAnswer(R.string.quest_dietType_only_vegan, new Runnable()
		{
			@Override public void run()
			{
				applyAnswer(ONLY_VEGAN);
			}
		});
	}

	protected void onClickAnswer(String answer)
	{
		Bundle bundle = new Bundle();
		bundle.putString(ANSWER, answer);
		applyImmediateAnswer(bundle);
	}

	@Override public boolean hasChanges()
	{
		return false;
	}

	private void applyAnswer(String value)
	{
		Bundle answer = new Bundle();
		answer.putString(OTHER_ANSWER, value);
		applyImmediateAnswer(answer);
	}
}
