package de.westnordost.streetcomplete.quests.recycling;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;

public class AddRecyclingTypeForm extends ImageListQuestAnswerFragment
{
	private final OsmItem[] TYPES = new OsmItem[] {
			new OsmItem("overground", R.drawable.recycling_container, R.string.overground_recycling_container),
			new OsmItem("underground", R.drawable.recycling_container_underground, R.string.underground_recycling_container),
			new OsmItem("centre", R.drawable.recycling_centre, R.string.recycling_centre)
	};

	@Override protected ImageListQuestAnswerFragment.OsmItem[] getItems()
	{
		return TYPES;
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return TYPES.length;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_recycling_type_title);

		View contentView = setContentView(R.layout.quest_generic_list);
		imageSelector = new ImageSelectAdapter(1);
		RecyclerView valueList = (RecyclerView) contentView.findViewById(R.id.listSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), TYPES.length);
		valueList.setLayoutManager(lm);

		return view;
	}
}
