package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter;
import de.westnordost.streetcomplete.view.Item;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddRoadSurfaceForm extends AbstractQuestFormAnswerFragment
{
	public static final String SURFACE = "surface";

	private final Item
		ASPHALT =		new Item("asphalt", R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt),
		CONCRETE =		new Item("concrete", R.drawable.surface_concrete, R.string.quest_surface_value_concrete),
		FINE_GRAVEL =	new Item("fine_gravel", R.drawable.surface_fine_gravel, R.string.quest_surface_value_fine_gravel),
		PAVING_STONES =	new Item("paving_stones", R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones),
		COMPACTED =		new Item("compacted", R.drawable.surface_compacted, R.string.quest_surface_value_compacted),
		DIRT =			new Item("dirt", R.drawable.surface_dirt, R.string.quest_surface_value_dirt),
		SETT =			new Item("sett", R.drawable.surface_sett, R.string.quest_surface_value_sett),
		COBBLESTONE =	new Item("cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_cobblestone),
		GRASS_PAVER =	new Item("grass_paver", R.drawable.surface_grass_paver, R.string.quest_surface_value_grass_paver),
		WOOD =			new Item("wood", R.drawable.surface_wood, R.string.quest_surface_value_wood),
		METAL =			new Item("metal", R.drawable.surface_metal, R.string.quest_surface_value_metal),
		GRAVEL =		new Item("gravel", R.drawable.surface_gravel, R.string.quest_surface_value_gravel),
		PEBBLES =		new Item("pebblestone", R.drawable.surface_pebblestone, R.string.quest_surface_value_pebblestone),
		GRASS =			new Item("grass", R.drawable.surface_grass, R.string.quest_surface_value_grass),
		SAND =			new Item("sand", R.drawable.surface_sand, R.string.quest_surface_value_sand)
		;

	// covers very roughly 90% of (non-generic) choices
	private final Item[] TOP_SURFACES = new Item[] {
			ASPHALT, CONCRETE, FINE_GRAVEL,
			PAVING_STONES, COMPACTED, DIRT
	};

	private final Item[] ALL_SURFACES = new Item[] {
		new Item("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, new Item[]{
				ASPHALT, CONCRETE, PAVING_STONES,
				SETT, COBBLESTONE, GRASS_PAVER,
				WOOD, METAL,
		}),
		new Item("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, new Item[]{
				COMPACTED, FINE_GRAVEL, GRAVEL,
				PEBBLES,
		}),
		new Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, new Item[]{
				DIRT, GRASS, SAND
		}),
	};

	private GroupedImageSelectAdapter imageSelector;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_street_surface);

		RecyclerView surfaceSelect = contentView.findViewById(R.id.surfaceSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), 3);
		surfaceSelect.setLayoutManager(lm);
		surfaceSelect.setNestedScrollingEnabled(false);

		imageSelector = new GroupedImageSelectAdapter(lm);
		imageSelector.setItems(Arrays.asList(TOP_SURFACES));
		surfaceSelect.setAdapter(imageSelector);

		Button showMoreButton = contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(v -> {
			imageSelector.setItems(Arrays.asList(ALL_SURFACES));
			showMoreButton.setVisibility(View.GONE);
		});

		return view;
	}

	@Override protected void onClickOk()
	{
		final Bundle answer = new Bundle();

		Item surface = getSelectedItem();
		if(surface != null)
		{
			answer.putString(SURFACE, surface.value);

			if(surface.isGroup())
			{
				new AlertDialogBuilder(getContext())
						.setMessage(R.string.quest_surface_generic_surface_confirmation)
						.setNegativeButton(R.string.quest_generic_confirmation_no, null)
						.setPositiveButton(R.string.quest_generic_confirmation_yes,
								(dialog, which) -> applyFormAnswer(answer))
						.show();
				return;
			}
		}

		applyFormAnswer(answer);
	}

	@Override public boolean hasChanges()
	{
		return getSelectedItem() != null;
	}

	private Item getSelectedItem()
	{
		return imageSelector.getSelectedItem();
	}
}
