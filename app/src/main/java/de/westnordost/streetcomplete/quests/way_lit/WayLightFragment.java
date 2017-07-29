package de.westnordost.streetcomplete.quests.way_lit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class WayLightFragment extends YesNoQuestAnswerFragment
{
	public static final String OTHER_ANSWER = "OTHER_ANSWER";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle();
		return view;
	}

	private void setTitle()
	{
		final OsmElement element = getOsmElement();
		String type = element.getTags().get("highway");
		String name = getName();
		if (Arrays.asList(AddWayLit.ROADS_WITH_LIGHT).contains(type)) {
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

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_way_lit_24_7);
		answers.add(R.string.quest_way_lit_automatic);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if (super.onClickOtherAnswer(itemResourceId)) return true;

		if (itemResourceId == R.string.quest_way_lit_24_7)
		{
			addAnswer("24/7");
			return true;
		}
		if (itemResourceId == R.string.quest_way_lit_automatic)
		{
			addAnswer("automatic");
			return true;
		}

		return false;
	}

	private void addAnswer(String value)
	{
		Bundle answer = new Bundle();
		answer.putString(OTHER_ANSWER, value);
		applyImmediateAnswer(answer);
	}

}
