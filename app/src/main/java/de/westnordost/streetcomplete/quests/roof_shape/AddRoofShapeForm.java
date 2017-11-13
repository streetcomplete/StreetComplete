package de.westnordost.streetcomplete.quests.roof_shape;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddRoofShapeForm extends ImageListQuestAnswerFragment
{
	protected static final int MORE_THAN_95_PERCENT_COVERED = 8;

	private static final OsmItem[] ROOF_SHAPES = new OsmItem[]{
			new OsmItem("gabled",			R.drawable.ic_roof_gabled),
			new OsmItem("hipped",			R.drawable.ic_roof_hipped),
			new OsmItem("flat",				R.drawable.ic_roof_flat),
			new OsmItem("pyramidal",		R.drawable.ic_roof_pyramidal),

			new OsmItem("half-hipped",		R.drawable.ic_roof_half_hipped),
			new OsmItem("skillion",			R.drawable.ic_roof_skillion),
			new OsmItem("gambrel",			R.drawable.ic_roof_gambrel),
			new OsmItem("round",			R.drawable.ic_roof_round),

			new OsmItem("double_saltbox",	R.drawable.ic_roof_double_saltbox),
			new OsmItem("saltbox",			R.drawable.ic_roof_saltbox),
			new OsmItem("mansard",			R.drawable.ic_roof_mansard),
			new OsmItem("dome",				R.drawable.ic_roof_dome),

			new OsmItem("quadruple_saltbox", R.drawable.ic_roof_quadruple_saltbox),
			new OsmItem("round_gabled",		R.drawable.ic_roof_round_gabled),
			new OsmItem("onion",			R.drawable.ic_roof_onion),
			new OsmItem("cone",				R.drawable.ic_roof_cone),
			};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		imageSelector.setCellLayout(R.layout.cell_labeled_icon_select);

		addOtherAnswer(R.string.quest_roofShape_answer_many, new Runnable()
		{
			@Override public void run()
			{
				applyManyRoofsAnswer();
			}
		});

		return view;
	}

	private void applyManyRoofsAnswer()
	{
		Bundle answer = new Bundle();
		ArrayList<String> strings = new ArrayList<>(1);
		strings.add("many");
		answer.putStringArrayList(OSM_VALUES, strings);
		applyImmediateAnswer(answer);
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return MORE_THAN_95_PERCENT_COVERED;
	}

	@Override protected OsmItem[] getItems()
	{
		return ROOF_SHAPES;
	}

}