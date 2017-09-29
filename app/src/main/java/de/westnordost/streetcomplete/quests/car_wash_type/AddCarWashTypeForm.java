package de.westnordost.streetcomplete.quests.car_wash_type;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddCarWashTypeForm extends ImageListQuestAnswerFragment
{
	public static final String AUTOMATED = "AUTOMATED";
	public static final String SELF_SERVICE = "SELF_SERVICE";
	public static final String SERVICE = "SERVICE";

	private final ImageListQuestAnswerFragment.OsmItem[] TYPES = new ImageListQuestAnswerFragment.OsmItem[] {
			new ImageListQuestAnswerFragment.OsmItem(AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
			new ImageListQuestAnswerFragment.OsmItem(SERVICE, R.drawable.car_wash_service, R.string.quest_carWashType_service),
			new ImageListQuestAnswerFragment.OsmItem(SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService)
	};

	@Override protected ImageListQuestAnswerFragment.OsmItem[] getItems()
	{
		return TYPES;
	}

	@Override protected int getItemsPerRow()
	{
		return 3;
	}

	@Override protected int getMaxSelectableItems()
	{
		return 2;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();

		ArrayList<String> osmValues = new ArrayList<>();
		for(Integer selectedIndex : imageSelector.getSelectedIndices())
		{
			osmValues.add(getItems()[selectedIndex].osmValue);
		}
		if(!osmValues.isEmpty() && osmValues.size() == 1)
		{
			answer.putStringArrayList(OSM_VALUES, osmValues);
			applyFormAnswer(answer);
		} else if (!osmValues.isEmpty() && osmValues.size() == 2 && osmValues.contains(SERVICE))
		{
			new AlertDialogBuilder(getActivity())
					.setTitle(R.string.quest_carWash_wrongInput)
					.setMessage(R.string.quest_carWash_wrongInput_description)
					.setNegativeButton(R.string.cancel, null)
					.show();
		}
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_carWashType_title);
		return view;
	}
}
