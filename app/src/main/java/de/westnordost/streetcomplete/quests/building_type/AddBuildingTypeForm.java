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
			HOUSE =			new Item("house", R.drawable.building_house, R.string.quest_buildingType_house),
			APARTMENTS =	new Item("apartments",	R.drawable.building_apartment, R.string.quest_buildingType_apartments),
			DETACHED =		new Item("detached", R.drawable.building_detached, R.string.quest_buildingType_detached),
			TERRACE =		new Item("terrace", R.drawable.building_terrace, R.string.quest_buildingType_terrace),
			HOTEL =			new Item("hotel", R.drawable.building_hotel, R.string.quest_buildingType_hotel),
			DORMITORY =		new Item("dormitory", R.drawable.building_dormitory, R.string.quest_buildingType_dormitory),
			FARM =			new Item("farm", R.drawable.building_farm, R.string.quest_buildingType_farm),
			HOUSEBOAT =		new Item("houseboat", R.drawable.building_houseboat, R.string.quest_buildingType_houseboat),
			BUNGALOW =		new Item("bungalow", R.drawable.building_bungalow, R.string.quest_buildingType_bungalow),
			STATIC_CARAVAN = new Item("static_caravan", R.drawable.building_static_caravan, R.string.quest_buildingType_static_caravan),

			INDUSTRIAL =	new Item("industrial",	R.drawable.building_industrial, R.string.quest_buildingType_industrial),
			RETAIL =		new Item("retail", R.drawable.building_retail, R.string.quest_buildingType_retail),
			WAREHOUSE =		new Item("warehouse", R.drawable.building_warehouse, R.string.quest_buildingType_warehouse),
			KIOSK =			new Item("kiosk", R.drawable.building_kiosk, R.string.quest_buildingType_kiosk),

			SCHOOL =		new Item("school", R.drawable.building_school, R.string.quest_buildingType_school),
			COLLEGE =		new Item("college", R.drawable.building_college, R.string.quest_buildingType_college),
			HOSPITAL =		new Item("hospital", R.drawable.building_hospital, R.string.quest_buildingType_hospital),
			PUBLIC =		new Item("public", R.drawable.building_public, R.string.quest_buildingType_public),
			STADIUM =		new Item("stadium", R.drawable.building_stadium, R.string.quest_buildingType_stadium),
			TRAIN_STATION =	new Item("train_station", R.drawable.building_train_station, R.string.quest_buildingType_train_station),
			TRANSPORTATION = new Item("transportation", R.drawable.building_transportation, R.string.quest_buildingType_transportation),
			UNIVERSITY = 	new Item("university", R.drawable.building_university, R.string.quest_buildingType_university),

			CHURCH =		new Item("church", R.drawable.building_church,	R.string.quest_buildingType_church),
			CHAPEL =		new Item("chapel", R.drawable.building_chapel,	R.string.quest_buildingType_chapel),
			CATHEDRAL =		new Item("cathedral", R.drawable.building_cathedral,	R.string.quest_buildingType_cathedral),
			MOSQUE =		new Item("mosque", R.drawable.building_mosque,	R.string.quest_buildingType_mosque),
			TEMPLE =		new Item("temple", R.drawable.building_temple,	R.string.quest_buildingType_temple),
			SYNAGOGUE =		new Item("synagogue", R.drawable.building_synagogue,	R.string.quest_buildingType_synagogue),
			SHRINE =		new Item("shrine", R.drawable.building_shrine,	R.string.quest_buildingType_shrine),

			CARPORT =		new Item("carport", R.drawable.building_carport, R.string.quest_buildingType_carport),
			GARAGE =		new Item("garage",	R.drawable.building_garage, R.string.quest_buildingType_garage),
			GARAGES =		new Item("garages", R.drawable.building_garages, R.string.quest_buildingType_garages),
			PARKING = 		new Item("parking", R.drawable.building_parking, R.string.quest_buildingType_parking),

			BARN =			new Item("barn", R.drawable.building_barn,	R.string.quest_buildingType_barn),
			COWSHED =		new Item("cowshed", R.drawable.building_cowshed,	R.string.quest_buildingType_cowshed),
			STABLE =		new Item("stable", R.drawable.building_stable,	R.string.quest_buildingType_stable),

			SHED =			new Item("shed", R.drawable.building_shed,	R.string.quest_buildingType_shed),
			HUT =			new Item("hut", R.drawable.building_hut, R.string.quest_buildingType_hut),
			ROOF =			new Item("roof", R.drawable.building_roof, R.string.quest_buildingType_roof),
			GREENHOUSE =	new Item("greenhouse",	R.drawable.building_greenhouse,	R.string.quest_buildingType_greenhouse),
			CABIN =			new Item("cabin", R.drawable.building_cabin, R.string.quest_buildingType_cabin),
			CONSTRUCTION =	new Item("construction", R.drawable.building_construction, R.string.quest_buildingType_construction),
			RUINS =			new Item("ruins", R.drawable.building_ruins, R.string.quest_buildingType_ruins),
			SERVICE =		new Item("service", R.drawable.building_service, R.string.quest_buildingType_service),
			TRANSFORMER_TOWER = new Item("transformer_tower", R.drawable.building_transformer_tower, R.string.quest_buildingType_transformer_tower);

	private final Item[] TOP_BUILDINGS = new Item[] {
			HOUSE, DETACHED, APARTMENTS,
			GARAGE, GARAGES, HUT,
			RETAIL, INDUSTRIAL
	};

	private final Item[] ALL_BUILDINGS = new Item[] {
			new Item("residential", R.drawable.panorama_building_residential, R.string.quest_buildingType_residential, new Item[]{
					DETACHED, HOUSE, TERRACE, APARTMENTS, BUNGALOW,
					FARM, HOUSEBOAT, STATIC_CARAVAN, DORMITORY, HOTEL,
			}),
			new Item("commercial", R.drawable.panorama_building_commercial, R.string.quest_buildingType_commercial, new Item[]{
					INDUSTRIAL, RETAIL, KIOSK, WAREHOUSE
			}),
			new Item("civic", R.drawable.panorama_building_civic, R.string.quest_buildingType_civic, new Item[]{
					COLLEGE, UNIVERSITY, SCHOOL, PUBLIC, HOSPITAL, STADIUM, TRAIN_STATION, TRANSPORTATION
			}),
			new Item(null, R.drawable.panorama_building_religious, R.string.quest_buildingType_religious, new Item[]{
					CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, SYNAGOGUE, SHRINE
			}),
			new Item(null, R.drawable.panorama_building_cars, R.string.quest_buildingType_cars, new Item[]{
					CARPORT, GARAGE, GARAGES, PARKING
			}),
			new Item("farm_auxiliary", R.drawable.panorama_building_farm_auxiliary, R.string.quest_buildingType_farm_auxiliary, new Item[]{
					BARN, COWSHED, STABLE
			}),
			new Item(null, R.drawable.panorama_building_other, R.string.quest_buildingType_other, new Item[]{
					HUT, SHED, CABIN, GREENHOUSE, CONSTRUCTION, RUINS, ROOF, SERVICE, TRANSFORMER_TOWER
			}),
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

			if(!building.hasValue())
			{
				new AlertDialogBuilder(getContext())
						.setMessage(R.string.quest_building_type_no_value_confirmation)
						.setPositiveButton(R.string.ok, null)
						.show();
				return;
			}

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
