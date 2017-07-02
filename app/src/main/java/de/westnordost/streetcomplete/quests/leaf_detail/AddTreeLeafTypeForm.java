package de.westnordost.streetcomplete.quests.leaf_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddTreeLeafTypeForm extends ImageListQuestAnswerFragment
{
	protected static final int MORE_THAN_99_PERCENT_COVERED = 2;

	private static final OsmItem[] LEAF_TYPES = new OsmItem[]{
			new OsmItem("needleleaved", R.drawable.leaf_type_needleleaved, R.string.quest_treeLeaf_needleleaved_answer),
			new OsmItem("broadleaved", R.drawable.leaf_type_broadleaved, R.string.quest_treeLeaf_broadleaved_answer),
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_treeLeaf_title);
		imageSelector.setCellLayout(R.layout.icon_select_cell_with_label_below);
		return view;
	}


	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return MORE_THAN_99_PERCENT_COVERED;
	}

	@Override protected OsmItem[] getItems()
	{
		return LEAF_TYPES;
	}
}