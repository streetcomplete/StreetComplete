package de.westnordost.streetcomplete.quests.building_type;

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

public class AddBuildingTypeForm extends AbstractQuestFormAnswerFragment
{
	public static final String BUILDING = "building";

	private final Item
			RESIDENTIAL = 	new Item("residential", R.drawable.building_residential, R.string.quest_buildingType_residential),
			HOUSE =			new Item("house", R.drawable.building_house, R.string.quest_buildingType_house),
			APARTMENTS =	new Item("apartments",	R.drawable.building_apartment, R.string.quest_buildingType_apartments),
			DETACHED =		new Item("detached", R.drawable.building_house, R.string.quest_buildingType_detached),

			COMMERCIAL =	new Item("commercial",	R.drawable.building_commercial,	R.string.quest_buildingType_commercial),
			INDUSTRIAL =	new Item("industrial",	R.drawable.building_industrial, R.string.quest_buildingType_industrial),
			RETAIL =		new Item("retail", R.drawable.building_retail, R.string.quest_buildingType_retail),

			ROOF =			new Item("roof", R.drawable.building_roof, R.string.quest_buildingType_roof),
			GARAGE =		new Item("garage",	R.drawable.building_garage, R.string.quest_buildingType_garage),
			GARAGES =		new Item("garages", R.drawable.building_garages, R.string.quest_buildingType_garages),

			//Necessary?
			GREENHOUSE =	new Item("greenhouse",	R.drawable.building_greenhouse,	R.string.quest_buildingType_greenhouse),
			BARN =			new Item("barn", R.drawable.building_barn,	R.string.quest_buildingType_barn),
			SHED =			new Item("shed", R.drawable.building_shed,	R.string.quest_buildingType_shed),
			HUT =			new Item("hut", R.drawable.building_hut, R.string.quest_buildingType_hut),

			SCHOOL =		new Item("school", R.drawable.building_school, R.string.quest_buildingType_school),
			CHURCH =		new Item("church", R.drawable.building_church,	R.string.quest_buildingType_church),
			CIVIC = 		new Item("civic", R.drawable.building_civic, R.string.quest_buildingType_civic),

			HOTEL =			new Item("hotel", R.drawable.building_hotel, R.string.quest_buildingType_hotel);

	private final Item[] TOP_BUILDINGS = new Item[] {
			HOUSE, APARTMENTS, GARAGE,
			RETAIL, INDUSTRIAL, COMMERCIAL
	};

	private final Item[] ALL_BUILDINGS = new Item[] {
			//TODO: Maybe put all buildings into different groups
			/*new Item("residential", R.drawable.building_residential, R.string.quest_buildingType_residential, new Item[]{
					HOUSE, APARTMENTS, DETACHED
			}),
			new Item("civic", R.drawable.building_civic, R.string.quest_buildingType_civic, new Item[]{
					SCHOOL, CHURCH
			}),*/
			RESIDENTIAL, HOUSE, APARTMENTS, DETACHED,
			CIVIC, SCHOOL, CHURCH, HOTEL,
			ROOF, GARAGE, GARAGES,
			BARN, SHED, HUT, GREENHOUSE
	};

	private GroupedImageSelectAdapter imageSelector;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_street_surface);

		RecyclerView buildingSelect = contentView.findViewById(R.id.surfaceSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), 3);
		buildingSelect.setLayoutManager(lm);
		buildingSelect.setNestedScrollingEnabled(false);

		imageSelector = new GroupedImageSelectAdapter(lm);
		imageSelector.setItems(Arrays.asList(TOP_BUILDINGS));
		buildingSelect.setAdapter(imageSelector);

		Button showMoreButton = contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(v -> {
			imageSelector.setItems(Arrays.asList(ALL_BUILDINGS));
			showMoreButton.setVisibility(View.GONE);
		});

		return view;
	}

	@Override protected void onClickOk()
	{
		final Bundle answer = new Bundle();

		Item building = getSelectedItem();
		if(building != null)
		{
			answer.putString(BUILDING, building.value);

			if(building.isGroup())
			{
				new AlertDialogBuilder(getContext())
						.setMessage(R.string.quest_building_type_generic_building_confirmation)
						.setNegativeButton(R.string.quest_generic_confirmation_no, null)
						.setPositiveButton(R.string.quest_generic_confirmation_yes,
								(dialog, which) -> applyFormAnswer(answer))
						.show();
				return;
			}
		}

		applyFormAnswer(answer);
	}

	@Override public boolean hasChanges() { return getSelectedItem() != null; }

	private Item getSelectedItem() { return imageSelector.getSelectedItem(); }
}
