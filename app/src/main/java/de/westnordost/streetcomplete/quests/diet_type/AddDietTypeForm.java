package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddDietTypeForm extends AbstractQuestAnswerFragment
{

	public static final String YES = "YES";
	public static final String NO = "NO";
	public static final String ONLY = "ONLY";
	public static final String ANSWER = "answer";

	TextView description;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View contentView = setContentView(R.layout.quest_diet_type_explanation);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_diet_type);

		Button buttonYes = (Button) buttonPanel.findViewById(R.id.buttonYes);
		buttonYes.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(YES);
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

		Button buttonOnly = (Button) buttonPanel.findViewById(R.id.buttonOnly);
		buttonOnly.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(ONLY);
			}
		});

		description = (TextView) contentView.findViewById(R.id.description);
		setDescription();

		return view;
	}

	@Override public boolean hasChanges()
	{
		return false;
	}

	protected void onClickAnswer(String answer)
	{
		Bundle bundle = new Bundle();
		bundle.putString(ANSWER, answer);
		applyImmediateAnswer(bundle);
	}

	private void setDescription()
	{
		OsmElement element = getOsmElement();
		String name = element.getTags() != null ? element.getTags().get("amenity") : null;

		switch (name)
		{
			case "cafe":
				name = getResources().getString(R.string.quest_dietType_cafe);
				break;
			case "restaurant":
				name = getResources().getString(R.string.quest_dietType_restaurant);
				break;
			case "fast_food":
				name = getResources().getString(R.string.quest_dietType_fast_food);
				break;
		}

		description.setText(getResources().getString(R.string.quest_dietType_explanation, name));
	}
}
