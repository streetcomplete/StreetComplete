package de.westnordost.streetcomplete.quests.camera_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddCameraTypeForm extends ImageListQuestAnswerFragment
{
	protected static final int MORE_THAN_95_PERCENT_COVERED = 8;

	private static final OsmItem[] CAMERA_TYPES = new OsmItem[]{
			new OsmItem("dome",    R.drawable.ic_camera_type_dome),
			new OsmItem("fixed",   R.drawable.ic_camera_type_fixed),
			new OsmItem("panning", R.drawable.ic_camera_type_panning)
			};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_cameraType_title);
		imageSelector.setCellLayout(R.layout.labeled_icon_select_cell);
		return view;
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return MORE_THAN_95_PERCENT_COVERED;
	}

	@Override protected OsmItem[] getItems()
	{
		return CAMERA_TYPES;
	}
}