package de.westnordost.streetcomplete.quests.roof_shape;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;

public class AddRoofShapeForm extends AbstractQuestFormAnswerFragment
{
	public static final String ROOF_SHAPE = "roof_shape";

	private static final String SELECTED_INDEX = "selected_item";

	private static final int MORE_THAN_95_PERCENT_COVERED = 8;

	private ImageSelectAdapter imageSelector;

	private static final RoofShape[] ROOF_SHAPES = new RoofShape[]{
			new RoofShape("gabled",			R.drawable.ic_roof_gabled),
			new RoofShape("hipped",			R.drawable.ic_roof_hipped),
			new RoofShape("flat",			R.drawable.ic_roof_flat),
			new RoofShape("pyramidal",		R.drawable.ic_roof_pyramidal),

			new RoofShape("half-hipped",	R.drawable.ic_roof_half_hipped),
			new RoofShape("skillion",		R.drawable.ic_roof_skillion),
			new RoofShape("gambrel",		R.drawable.ic_roof_gambrel),
			new RoofShape("round",			R.drawable.ic_roof_round),

			new RoofShape("double_saltbox",	R.drawable.ic_roof_double_saltbox),
			new RoofShape("saltbox",		R.drawable.ic_roof_saltbox),
			new RoofShape("mansard",		R.drawable.ic_roof_mansard),
			new RoofShape("dome",			R.drawable.ic_roof_dome),

			new RoofShape("quadruple_saltbox", R.drawable.ic_roof_quadruple_saltbox),
			new RoofShape("round_gabled",	R.drawable.ic_roof_round_gabled),
			new RoofShape("onion",			R.drawable.ic_roof_onion),
			new RoofShape("cone",			R.drawable.ic_roof_cone),
			};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_roofShape_title);

		View contentView = setContentView(R.layout.quest_roof_shape);

		final RecyclerView roofList = (RecyclerView) contentView.findViewById(R.id.roofShapeSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), 4);
		roofList.setLayoutManager(lm);

		final List<ImageSelectAdapter.Item> roofShapesList = Arrays.<ImageSelectAdapter.Item>asList(ROOF_SHAPES);

		imageSelector = new ImageSelectAdapter();
		imageSelector.setItems(roofShapesList.subList(0,MORE_THAN_95_PERCENT_COVERED));
		if(savedInstanceState != null)
		{
			imageSelector.setSelectedIndex(savedInstanceState.getInt(SELECTED_INDEX, -1));
		}
		roofList.setAdapter(imageSelector);
		roofList.setNestedScrollingEnabled(false);

		final Button showMoreButton = (Button) contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				imageSelector.add(roofShapesList.subList(MORE_THAN_95_PERCENT_COVERED, roofShapesList.size()));
				showMoreButton.setVisibility(View.GONE);
			}
		});

		return view;
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_INDEX, imageSelector.getSelectedIndex());
	}

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

	@Override public boolean hasChanges()
	{
		return imageSelector.getSelectedIndex() != -1;
	}

	private static class RoofShape extends ImageSelectAdapter.Item
	{
		public final String osmValue;

		public RoofShape(String osmValue, int drawableId)
		{
			super(drawableId, -1);
			this.osmValue = osmValue;
		}
	}
}