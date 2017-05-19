package de.westnordost.streetcomplete.quests.max_speed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;

public class AddMaxSpeedForm extends AbstractQuestFormAnswerFragment
{
	public static final String MAX_SPEED = "maxspeed";
	public static final String MAX_SPEED_SOURCE = "maxspeed_source";

	private EditText speedInput;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_maxspeed_title);

		String maxspeedLayoutName = getCountryInfo().getMaxspeedLayout();
		int maxspeedLayout;
		if(maxspeedLayoutName != null)
		{
			maxspeedLayout = getResources().getIdentifier(
					maxspeedLayoutName, "layout", getActivity().getPackageName());
		}
		else
		{
			maxspeedLayout = R.layout.quest_maxspeed;
		}
		View contentView = setContentView(maxspeedLayout);

		speedInput = (EditText) contentView.findViewById(R.id.maxSpeedInput);

		return view;
	}

	@Override protected void onClickOk()
	{
		// TODO
	}

	@Override public boolean hasChanges()
	{
		return !speedInput.getText().toString().isEmpty();
	}
}
