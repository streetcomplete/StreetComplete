package de.westnordost.streetcomplete.quests.bus_stop_shelter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractYesNoQuestAnswerFragment;

public class AddBusStopShelterForm extends AbstractYesNoQuestAnswerFragment
{
    public static final String BUS_STOP_SHELTER = "bus_stop_shelter";

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setTitle(R.string.quest_busStopShelter_title);
        return view;
	}

	@Override protected void onClickYesNo(boolean yes) {
        Bundle answer = new Bundle();
        answer.putString(BUS_STOP_SHELTER, yes ? "yes" : "no");
        applyImmediateAnswer(answer);
    }
}