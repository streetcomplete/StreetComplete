package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddRoadSurfaceForm extends AbstractQuestAnswerFragment
{
	public static final String SURFACE = "surface";

	private final Surface[] SURFACES = new Surface[] {
		new Surface("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, new Surface[]{
				new Surface("asphalt", R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt),
				new Surface("concrete", R.drawable.surface_concrete, R.string.quest_surface_value_concrete),
				new Surface("sett", R.drawable.surface_sett, R.string.quest_surface_value_sett),
				new Surface("paving_stones", R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones),
				new Surface("cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_cobblestone),
				new Surface("wood", R.drawable.surface_wood, R.string.quest_surface_value_wood),
		}),
		new Surface("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, new Surface[]{
				new Surface("compacted", R.drawable.surface_compacted, R.string.quest_surface_value_compacted),
				new Surface("gravel", R.drawable.surface_gravel, R.string.quest_surface_value_gravel),
				new Surface("fine_gravel", R.drawable.surface_fine_gravel, R.string.quest_surface_value_fine_gravel),
				new Surface("pebblestone", R.drawable.surface_pebblestone, R.string.quest_surface_value_pebblestone),
				new Surface("grass_paver", R.drawable.surface_grass_paver, R.string.quest_surface_value_grass_paver),
		}),
		new Surface("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, new Surface[]{
				new Surface("dirt", R.drawable.surface_dirt, R.string.quest_surface_value_dirt),
				new Surface("grass", R.drawable.surface_grass, R.string.quest_surface_value_grass),
				new Surface("sand", R.drawable.surface_sand, R.string.quest_surface_value_sand),
		}),
	};

	private GroupedImageSelectAdapter imageSelector;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_streetSurface_title);

		View contentView = setContentView(R.layout.quest_street_surface);

		RecyclerView surfaceSelect = (RecyclerView) contentView.findViewById(R.id.surfaceSelect);
		imageSelector = new GroupedImageSelectAdapter(getActivity(),
				Arrays.<GroupedImageSelectAdapter.Item>asList(SURFACES));
		surfaceSelect.setAdapter(imageSelector);
		surfaceSelect.setNestedScrollingEnabled(false);

		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		if(getSelectedSurface() != null)
		{
			answer.putString(SURFACE, getSelectedSurface().value);
		}
		applyAnswer(answer);
	}

	@Override public boolean hasChanges()
	{
		return getSelectedSurface() != null;
	}

	private Surface getSelectedSurface()
	{
		return (Surface) imageSelector.getSelectedItem();
	}

	private static class Surface extends GroupedImageSelectAdapter.Item
	{
		public final String value;

		public Surface(String value, int drawableId, int titleId)
		{
			super(drawableId, titleId);
			this.value = value;
		}

		public Surface(String value, int drawableId, int titleId, GroupedImageSelectAdapter.Item[] items)
		{
			super(drawableId, titleId, items);
			this.value = value;
		}
	}
}
