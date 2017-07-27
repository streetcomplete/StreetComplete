package de.westnordost.streetcomplete.quests.way_light;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddWayLightFragment extends YesNoQuestAnswerFragment
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
		String name = getName(getOsmElement());
		if (name != null && !name.trim().isEmpty())
		{
			setTitle(R.string.quest_way_light_named_title, name);
		} else
		{
			setTitle(R.string.quest_way_light_title);
		}
	}

	private String getName(OsmElement element)
	{
		return element != null && element.getTags() != null ? element.getTags().get("name") : null;
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_way_light_24_7);
		answers.add(R.string.quest_way_light_automatic);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if (super.onClickOtherAnswer(itemResourceId)) return true;

		if (itemResourceId == R.string.quest_way_light_24_7)
		{
			addAnswer("24/7");
			return true;
		}
		if (itemResourceId == R.string.quest_way_light_automatic)
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
