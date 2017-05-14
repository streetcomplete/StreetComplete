package de.westnordost.streetcomplete.quests.roof_shape;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.ImageMultiSelectAdapter;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;

public class AddRoofShapeForm extends ImageListQuestAnswerFragment
{
	public static final String ROOF_SHAPE = "roof_shape";

	private static final ListValue[] ROOF_SHAPES = new ListValue[]{
			new ListValue("gabled",			R.drawable.ic_roof_gabled),
			new ListValue("hipped",			R.drawable.ic_roof_hipped),
			new ListValue("flat",			R.drawable.ic_roof_flat),
			new ListValue("pyramidal",		R.drawable.ic_roof_pyramidal),

			new ListValue("half-hipped",	R.drawable.ic_roof_half_hipped),
			new ListValue("skillion",		R.drawable.ic_roof_skillion),
			new ListValue("gambrel",		R.drawable.ic_roof_gambrel),
			new ListValue("round",			R.drawable.ic_roof_round),

			new ListValue("double_saltbox",	R.drawable.ic_roof_double_saltbox),
			new ListValue("saltbox",		R.drawable.ic_roof_saltbox),
			new ListValue("mansard",		R.drawable.ic_roof_mansard),
			new ListValue("dome",			R.drawable.ic_roof_dome),

			new ListValue("quadruple_saltbox", R.drawable.ic_roof_quadruple_saltbox),
			new ListValue("round_gabled",	R.drawable.ic_roof_round_gabled),
			new ListValue("onion",			R.drawable.ic_roof_onion),
			new ListValue("cone",			R.drawable.ic_roof_cone),
			};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_roofShape_title);

		showMoreButton = (Button) contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				expand();
			}
		});

		List<ImageSelectAdapter.Item> roofShapesList = Arrays.<ImageSelectAdapter.Item>asList(ROOF_SHAPES);

		imageSelector.setItems(roofShapesList.subList(0,MORE_THAN_95_PERCENT_COVERED));
				expand();
			}
			int index = savedInstanceState.getInt(SELECTED_INDEX, -1);
			if(index > -1) imageSelector.selectIndex(index);

		final Button showMoreButton = (Button) view.findViewById(R.id.buttonShowMore);
	}

	private void expand()
	{
		List<ImageSelectAdapter.Item> roofShapesList = Arrays.<ImageSelectAdapter.Item>asList(ROOF_SHAPES);
		imageSelector.addItems(roofShapesList.subList(MORE_THAN_95_PERCENT_COVERED, roofShapesList.size()));
		showMoreButton.setVisibility(View.GONE);
	}

		outState.putBoolean(EXPANDED, showMoreButton.getVisibility() == View.GONE);
	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		Integer selectedIndex = imageSelector.getSelectedIndex();
		if(selectedIndex != -1)
		{
			String osmValue = ROOF_SHAPES[selectedIndex].osmValue;
			answer.putString(ROOF_SHAPE, osmValue);
		}
		applyFormAnswer(answer);
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_roofShape_answer_many);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_roofShape_answer_many)
		{
			Bundle answer = new Bundle();
			answer.putString(ROOF_SHAPE, "many");
			applyImmediateAnswer(answer);
			return true;
		}

		return false;
	}
}