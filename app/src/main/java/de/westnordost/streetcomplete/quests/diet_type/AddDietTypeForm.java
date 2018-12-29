package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddDietTypeForm extends AbstractQuestAnswerFragment
{
	private static final String
			YES = "yes",
			NO = "no",
			ONLY = "only";

	public static final String OSM_VALUE = "answer";

	private static final String ARG_DIET = "diet_explanation";
	public static AddDietTypeForm create(int dietExplanationResId)
	{
		AddDietTypeForm form = new AddDietTypeForm();
		Bundle args = new Bundle();
		args.putInt(ARG_DIET, dietExplanationResId);
		form.setArguments(args);
		return form;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View contentView = setContentView(R.layout.quest_diet_type_explanation);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_yes_no_only);

		buttonPanel.findViewById(R.id.buttonYes).setOnClickListener(v -> onClickAnswer(YES));
		buttonPanel.findViewById(R.id.buttonNo).setOnClickListener(v -> onClickAnswer(NO));
		buttonPanel.findViewById(R.id.buttonOnly).setOnClickListener(v -> onClickAnswer(ONLY));

		TextView description = contentView.findViewById(R.id.dietType_description);
		int resId = getArguments().getInt(ARG_DIET);
		if(resId > 0)
		{
			description.setText(resId);
		}
		else
		{
			description.setVisibility(View.GONE);
		}

		return view;
	}

	protected void onClickAnswer(String answer)
	{
		Bundle bundle = new Bundle();
		bundle.putString(OSM_VALUE, answer);
		applyAnswer(bundle);
	}
}
