package de.westnordost.streetcomplete.quests.building_type;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.GroupedImageSelectDescriptionAdapter;
import de.westnordost.streetcomplete.view.Item;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddBuildingTypeForm extends AbstractQuestFormAnswerFragment
{
	public static final String BUILDING = "building";
	public static final String BUILDING_DESCRIPTION = "building_description";

	private final Item
		HOUSE = new Item("house", R.drawable.building_house, R.string.quest_buildingType_house, R.string.quest_buildingType_house_description),
		APARTMENTS = new Item("apartments", R.drawable.building_apartment, R.string.quest_buildingType_apartments, R.string.quest_buildingType_apartments_description),
		DETACHED = new Item("detached", R.drawable.building_detached, R.string.quest_buildingType_detached, R.string.quest_buildingType_detached_description),
		TERRACE = new Item("terrace", R.drawable.building_terrace, R.string.quest_buildingType_terrace, R.string.quest_buildingType_terrace_description),
		HOTEL =	new Item("hotel", R.drawable.building_hotel, R.string.quest_buildingType_hotel, R.string.quest_buildingType_hotel_description),
		DORMITORY = new Item("dormitory", R.drawable.building_dormitory, R.string.quest_buildingType_dormitory, R.string.quest_buildingType_dormitory_description),
		HOUSEBOAT =	new Item("houseboat", R.drawable.building_houseboat, R.string.quest_buildingType_houseboat, R.string.quest_buildingType_houseboat_description),
		BUNGALOW = new Item("bungalow", R.drawable.building_bungalow, R.string.quest_buildingType_bungalow, R.string.quest_buildingType_bungalow_description),
		STATIC_CARAVAN = new Item("static_caravan", R.drawable.building_static_caravan, R.string.quest_buildingType_static_caravan, R.string.quest_buildingType_static_caravan_description),

		COMMERCIAL = new Item("commercial", R.drawable.building_commercial, R.string.quest_buildingType_commercial, R.string.quest_buildingType_commercial_description),
		INDUSTRIAL = new Item("industrial",	R.drawable.building_industrial, R.string.quest_buildingType_industrial, R.string.quest_buildingType_industrial_description),
		RETAIL = new Item("retail", R.drawable.building_retail, R.string.quest_buildingType_retail, R.string.quest_buildingType_retail_description),
		WAREHOUSE = new Item("warehouse", R.drawable.building_warehouse, R.string.quest_buildingType_warehouse, R.string.quest_buildingType_warehouse_description),
		KIOSK =	new Item("kiosk", R.drawable.building_kiosk, R.string.quest_buildingType_kiosk, R.string.quest_buildingType_kiosk_description),

		SCHOOL = new Item("school", R.drawable.building_school, R.string.quest_buildingType_school, R.string.quest_buildingType_school_description),
		COLLEGE = new Item("college", R.drawable.building_college, R.string.quest_buildingType_college, R.string.quest_buildingType_college_description),
		HOSPITAL = new Item("hospital", R.drawable.building_hospital, R.string.quest_buildingType_hospital, R.string.quest_buildingType_hospital_description),
		PUBLIC = new Item("public", R.drawable.building_public, R.string.quest_buildingType_public, R.string.quest_buildingType_public_description),
		STADIUM = new Item("stadium", R.drawable.building_stadium, R.string.quest_buildingType_stadium, R.string.quest_buildingType_stadium_description),
		TRAIN_STATION =	new Item("train_station", R.drawable.building_train_station, R.string.quest_buildingType_train_station, R.string.quest_buildingType_train_station_description),
		TRANSPORTATION = new Item("transportation", R.drawable.building_transportation, R.string.quest_buildingType_transportation, R.string.quest_buildingType_transportation_description),
		UNIVERSITY = new Item("university", R.drawable.building_university, R.string.quest_buildingType_university, R.string.quest_buildingType_university_description),

		CHURCH = new Item("church", R.drawable.building_church, R.string.quest_buildingType_church, R.string.quest_buildingType_church_description),
		CHAPEL = new Item("chapel", R.drawable.building_chapel, R.string.quest_buildingType_chapel, R.string.quest_buildingType_chapel_description),
		CATHEDRAL = new Item("cathedral", R.drawable.building_cathedral, R.string.quest_buildingType_cathedral, R.string.quest_buildingType_cathedral_description),
		MOSQUE = new Item("mosque", R.drawable.building_mosque, R.string.quest_buildingType_mosque, R.string.quest_buildingType_mosque_description),
		TEMPLE = new Item("temple", R.drawable.building_temple, R.string.quest_buildingType_temple, R.string.quest_buildingType_temple_description),
		SYNAGOGUE =	new Item("synagogue", R.drawable.building_synagogue, R.string.quest_buildingType_synagogue, R.string.quest_buildingType_synagogue_description),

		CARPORT = new Item("carport", R.drawable.building_carport, R.string.quest_buildingType_carport, R.string.quest_buildingType_carport_description),
		GARAGE = new Item("garage",	R.drawable.building_garage, R.string.quest_buildingType_garage, R.string.quest_buildingType_garage_description),
		GARAGES = new Item("garages", R.drawable.building_garages, R.string.quest_buildingType_garages, R.string.quest_buildingType_garages_description),
		PARKING = new Item("parking", R.drawable.building_parking, R.string.quest_buildingType_parking, R.string.quest_buildingType_parking_description),

		FARM = new Item("farm", R.drawable.building_farm, R.string.quest_buildingType_farmhouse, R.string.quest_buildingType_farmhouse_description),
		FARM_AUXILIARY = new Item("farm_auxiliary", R.drawable.building_farm_auxiliary, R.string.quest_buildingType_farm_auxiliary, R.string.quest_buildingType_farm_auxiliary_description),
		BARN = new Item("barn", R.drawable.building_barn,	R.string.quest_buildingType_barn, R.string.quest_buildingType_barn_description),
		GREENHOUSE = new Item("greenhouse",	R.drawable.building_greenhouse,	R.string.quest_buildingType_greenhouse, R.string.quest_buildingType_greenhouse_description),

		SHED = new Item("shed", R.drawable.building_shed,	R.string.quest_buildingType_shed, R.string.quest_buildingType_shed_description),
		HUT = new Item("hut", R.drawable.building_hut, R.string.quest_buildingType_hut, R.string.quest_buildingType_hut_description),
		ROOF = new Item("roof", R.drawable.building_roof, R.string.quest_buildingType_roof, R.string.quest_buildingType_roof_description),
		CABIN = new Item("cabin", R.drawable.building_cabin, R.string.quest_buildingType_cabin, R.string.quest_buildingType_cabin_description),
		CONSTRUCTION = new Item("construction", R.drawable.building_construction, R.string.quest_buildingType_construction, R.string.quest_buildingType_construction_description),
		RUINS =	new Item("ruins", R.drawable.building_ruins, R.string.quest_buildingType_ruins, R.string.quest_buildingType_ruins_description),
		SERVICE = new Item("service", R.drawable.building_service, R.string.quest_buildingType_service, R.string.quest_buildingType_service_description);

	private final Item[] TOP_BUILDINGS = new Item[] {
			HOUSE, DETACHED, APARTMENTS,
			GARAGE, GARAGES, HUT,
			RETAIL, COMMERCIAL, INDUSTRIAL
	};

	private final Item[] ALL_BUILDINGS = new Item[] {
			new Item("residential", R.drawable.panorama_building_residential, R.string.quest_buildingType_residential, new Item[]{
					DETACHED, HOUSE, TERRACE, APARTMENTS, BUNGALOW,
					FARM, HOUSEBOAT, STATIC_CARAVAN, DORMITORY, HOTEL,
			}),
			new Item(null, R.drawable.panorama_building_commercial, R.string.quest_buildingType_commercial, new Item[]{
					COMMERCIAL, INDUSTRIAL, RETAIL, KIOSK, WAREHOUSE
			}),
			new Item("civic", R.drawable.panorama_building_civic, R.string.quest_buildingType_civic, new Item[]{
					COLLEGE, UNIVERSITY, SCHOOL, PUBLIC, HOSPITAL, STADIUM, TRAIN_STATION, TRANSPORTATION
			}),
			new Item("religious", R.drawable.panorama_building_religious, R.string.quest_buildingType_religious, new Item[]{
					CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, SYNAGOGUE
			}),
			new Item(null, R.drawable.panorama_building_cars, R.string.quest_buildingType_cars, new Item[]{
					CARPORT, GARAGE, GARAGES, PARKING
			}),
			new Item(null, R.drawable.panorama_building_farm_auxiliary, R.string.quest_buildingType_farm, new Item[]{
					FARM, FARM_AUXILIARY, BARN, GREENHOUSE
			}),
			new Item(null, R.drawable.panorama_building_other, R.string.quest_buildingType_other, new Item[]{
					HUT, SHED, CABIN, CONSTRUCTION, RUINS, ROOF, SERVICE
			}),
	};

	private GroupedImageSelectDescriptionAdapter imageSelector;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_image_select);

		RecyclerView buildingSelect = contentView.findViewById(R.id.imageSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), 2);
		buildingSelect.setLayoutManager(lm);
		buildingSelect.setNestedScrollingEnabled(false);

		imageSelector = new GroupedImageSelectDescriptionAdapter(lm);
		imageSelector.setItems(Arrays.asList(TOP_BUILDINGS));
		buildingSelect.setAdapter(imageSelector);

		Button showMoreButton = contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(v -> {
			imageSelector.setItems(Arrays.asList(ALL_BUILDINGS));
			showMoreButton.setVisibility(View.GONE);
		});

		addOtherAnswer(R.string.quest_buildingType_answer_input_description, this::showInputCommentDialog);

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
						.setMessage(R.string.quest_building_type_invalid_value)
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

	private void showInputCommentDialog()
	{
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.quest_building_type_comment, null);
		final EditText editText = view.findViewById(R.id.commentInput);

		new AlertDialogBuilder(getContext())
			.setTitle(R.string.quest_buildingType_comment_title)
			.setView(view)
			.setPositiveButton(android.R.string.ok, (dialog, which) ->
			{
				String txt = editText.getText().toString().replaceAll("\"","").trim();
				if(txt.isEmpty())
				{
					new AlertDialogBuilder(getContext())
						.setMessage(R.string.quest_buildingType_comment_emptyAnswer)
						.setPositiveButton(R.string.ok, null)
						.show();
					return;
				}

				Bundle answer = new Bundle();
				answer.putString(BUILDING_DESCRIPTION, "\""+txt+"\"");
				applyImmediateAnswer(answer);
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}
}
