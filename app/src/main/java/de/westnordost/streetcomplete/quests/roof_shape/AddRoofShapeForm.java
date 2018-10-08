package de.westnordost.streetcomplete.quests.roof_shape;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddRoofShapeForm extends ImageListQuestAnswerFragment
{
	protected static final int MORE_THAN_95_PERCENT_COVERED = 8;

	private static final Item[] ROOF_SHAPES = new Item[]{
			new Item("gabled",			R.drawable.ic_roof_gabled),
			new Item("hipped",			R.drawable.ic_roof_hipped),
			new Item("flat",			R.drawable.ic_roof_flat),
			new Item("pyramidal",		R.drawable.ic_roof_pyramidal),

			new Item("half-hipped",		R.drawable.ic_roof_half_hipped),
			new Item("skillion",		R.drawable.ic_roof_skillion),
			new Item("gambrel",			R.drawable.ic_roof_gambrel),
			new Item("round",			R.drawable.ic_roof_round),

			new Item("double_saltbox",	R.drawable.ic_roof_double_saltbox),
			new Item("saltbox",			R.drawable.ic_roof_saltbox),
			new Item("mansard",			R.drawable.ic_roof_mansard),
			new Item("dome",			R.drawable.ic_roof_dome),

			new Item("quadruple_saltbox", R.drawable.ic_roof_quadruple_saltbox),
			new Item("round_gabled",	R.drawable.ic_roof_round_gabled),
			new Item("onion",			R.drawable.ic_roof_onion),
			new Item("cone",			R.drawable.ic_roof_cone),
			};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		imageSelector.setCellLayout(R.layout.cell_labeled_icon_select);

		addOtherAnswer(R.string.quest_roofShape_answer_many, this::applyManyRoofsAnswer);

		return view;
	}

	private void applyManyRoofsAnswer()
	{
		Bundle answer = new Bundle();
		ArrayList<String> strings = new ArrayList<>(1);
		strings.add("many");
		answer.putStringArrayList(OSM_VALUES, strings);
		applyAnswer(answer);
	}

	@Override protected int getMaxNumberOfInitiallyShownItems() { return MORE_THAN_95_PERCENT_COVERED; }
	@Override protected Item[] getItems() { return ROOF_SHAPES; }
}
