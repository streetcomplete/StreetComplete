package de.westnordost.streetcomplete.quests.way_lit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class WayLitForm extends YesNoQuestAnswerFragment
{
	public static final String OTHER_ANSWER = "OTHER_ANSWER";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle();
		addOtherAnswers();
		return view;
	}

	private void setTitle()
	{
		final OsmElement element = getOsmElement();
		String type = element.getTags().get("highway");
		String name = getElementName();
		if (Arrays.asList(AddWayLit.LIT_NON_RESIDENTIAL_ROADS).contains(type) ||
				Arrays.asList(AddWayLit.LIT_RESIDENTIAL_ROADS).contains(type))
		{
			if (name != null)
			{
				setTitle(R.string.quest_way_lit_named_road_title, name);
			}
			else
			{
				setTitle(R.string.quest_way_lit_road_title);
			}
		}
		else
		{
			if (name != null && !name.trim().isEmpty())
			{
				setTitle(R.string.quest_way_lit_named_title, name);
			}
			else
			{
				setTitle(R.string.quest_way_lit_title);
			}
		}
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_way_lit_24_7, new Runnable()
		{
			@Override public void run()
			{
				applyAnswer("24/7");
			}
		});
		addOtherAnswer(R.string.quest_way_lit_automatic, new Runnable()
		{
			@Override public void run()
			{
				applyAnswer("automatic");
			}
		});
	}

	private void applyAnswer(String value)
	{
		Bundle answer = new Bundle();
		answer.putString(OTHER_ANSWER, value);
		applyImmediateAnswer(answer);
	}
}
