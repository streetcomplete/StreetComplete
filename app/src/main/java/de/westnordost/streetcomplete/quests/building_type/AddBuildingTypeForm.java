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
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter;
import de.westnordost.streetcomplete.view.Item;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddBuildingTypeForm extends AbstractQuestFormAnswerFragment
{
	public static final String
		BUILDING = "building",
		MAN_MADE = "man_made",
		BUILDING_DESCRIPTION = "building_description";

	private static final Item
		HOUSE =        new Item("house",      R.drawable.ic_building_house,      R.string.quest_buildingType_house, R.string.quest_buildingType_house_description),
		APARTMENTS =   new Item("apartments", R.drawable.ic_building_apartments, R.string.quest_buildingType_apartments, R.string.quest_buildingType_apartments_description),
		DETACHED =     new Item("detached",   R.drawable.ic_building_detached,   R.string.quest_buildingType_detached, R.string.quest_buildingType_detached_description),
		SEMI_DETACHED =new Item("semidetached_house", R.drawable.ic_building_semi_detached, R.string.quest_buildingType_semi_detached, R.string.quest_buildingType_semi_detached_description),
		TERRACE =      new Item("terrace",    R.drawable.ic_building_terrace,    R.string.quest_buildingType_terrace, R.string.quest_buildingType_terrace_description),
		HOTEL =	       new Item("hotel",      R.drawable.ic_building_hotel,      R.string.quest_buildingType_hotel),
		DORMITORY =    new Item("dormitory",  R.drawable.ic_building_dormitory,  R.string.quest_buildingType_dormitory),
		HOUSEBOAT =    new Item("houseboat",  R.drawable.ic_building_houseboat,  R.string.quest_buildingType_houseboat),
		BUNGALOW =     new Item("bungalow",   R.drawable.ic_building_bungalow,   R.string.quest_buildingType_bungalow, R.string.quest_buildingType_bungalow_description),
		STATIC_CARAVAN = new Item("static_caravan", R.drawable.ic_building_static_caravan, R.string.quest_buildingType_static_caravan),
		HUT =          new Item("hut",        R.drawable.ic_building_hut,        R.string.quest_buildingType_hut, R.string.quest_buildingType_hut_description),

		COMMERCIAL =   new Item("commercial", R.drawable.ic_building_commercial, R.string.quest_buildingType_commercial, R.string.quest_buildingType_commercial_description),
		INDUSTRIAL =   new Item("industrial", R.drawable.ic_building_industrial, R.string.quest_buildingType_industrial, R.string.quest_buildingType_industrial_description),
		RETAIL =       new Item("retail",     R.drawable.ic_building_retail,     R.string.quest_buildingType_retail),
		WAREHOUSE =    new Item("warehouse",  R.drawable.ic_building_warehouse,  R.string.quest_buildingType_warehouse),
		KIOSK =        new Item("kiosk",      R.drawable.ic_building_kiosk,      R.string.quest_buildingType_kiosk),
		STORAGE_TANK = new Item("man_made=storage_tank", R.drawable.ic_building_storage_tank, R.string.quest_buildingType_storage_tank),

		KINDERGARTEN = new Item("kindergarten", R.drawable.ic_building_kindergarten, R.string.quest_buildingType_kindergarten),
		SCHOOL =       new Item("school",     R.drawable.ic_building_school,     R.string.quest_buildingType_school),
		COLLEGE =      new Item("college",    R.drawable.ic_building_college,    R.string.quest_buildingType_college),
		HOSPITAL =     new Item("hospital",   R.drawable.ic_building_hospital,   R.string.quest_buildingType_hospital),
		STADIUM =      new Item("stadium",    R.drawable.ic_sport_volleyball,    R.string.quest_buildingType_stadium),
		TRAIN_STATION =	new Item("train_station", R.drawable.ic_building_train_station, R.string.quest_buildingType_train_station),
		TRANSPORTATION = new Item("transportation", R.drawable.ic_building_transportation, R.string.quest_buildingType_transportation),
		UNIVERSITY =   new Item("university", R.drawable.ic_building_university, R.string.quest_buildingType_university),

		CHURCH =       new Item("church",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_church),
		CHAPEL =       new Item("chapel",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_chapel),
		CATHEDRAL =    new Item("cathedral",  R.drawable.ic_religion_christian,  R.string.quest_buildingType_cathedral),
		MOSQUE =       new Item("mosque",     R.drawable.ic_religion_muslim,     R.string.quest_buildingType_mosque),
		TEMPLE =       new Item("temple",     R.drawable.ic_building_temple,     R.string.quest_buildingType_temple),
		PAGODA =       new Item("pagoda",     R.drawable.ic_building_temple,     R.string.quest_buildingType_pagoda),
		SYNAGOGUE =	   new Item("synagogue",  R.drawable.ic_religion_jewish,     R.string.quest_buildingType_synagogue),

		CARPORT =      new Item("carport",    R.drawable.ic_building_carport,    R.string.quest_buildingType_carport, R.string.quest_buildingType_carport_description),
		GARAGE =       new Item("garage",	  R.drawable.ic_building_garage,     R.string.quest_buildingType_garage),
		GARAGES =      new Item("garages",    R.drawable.ic_building_garages,    R.string.quest_buildingType_garages),
		PARKING =      new Item("parking",    R.drawable.ic_building_parking,    R.string.quest_buildingType_parking),

		FARM =         new Item("farm",       R.drawable.ic_building_farm_house, R.string.quest_buildingType_farmhouse, R.string.quest_buildingType_farmhouse_description),
		FARM_AUXILIARY = new Item("farm_auxiliary", R.drawable.ic_building_barn, R.string.quest_buildingType_farm_auxiliary, R.string.quest_buildingType_farm_auxiliary_description),
		GREENHOUSE =   new Item("greenhouse", R.drawable.ic_building_greenhouse, R.string.quest_buildingType_greenhouse),

		SHED =         new Item("shed",       R.drawable.ic_building_shed,       R.string.quest_buildingType_shed),
		ROOF =         new Item("roof",       R.drawable.ic_building_roof,       R.string.quest_buildingType_roof, R.string.quest_buildingType_roof_description),
		SERVICE =      new Item("service",    R.drawable.ic_building_service, R.string.quest_buildingType_service, R.string.quest_buildingType_service_description);

	private final Item[] TOP_BUILDINGS = new Item[] {
			DETACHED, GARAGE,
			APARTMENTS, SEMI_DETACHED,
			COMMERCIAL, INDUSTRIAL,
	};

	private final Item[] ALL_BUILDINGS = new Item[] {
			new Item("residential", R.drawable.ic_building_apartments, R.string.quest_buildingType_residential, R.string.quest_buildingType_residential_description, new Item[]{
					DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, FARM, HOUSE,
					HUT, BUNGALOW, HOUSEBOAT, STATIC_CARAVAN, DORMITORY,
			}),
			new Item(null, R.drawable.ic_building_commercial, R.string.quest_buildingType_commercial, R.string.quest_buildingType_commercial_generic_description, new Item[]{
					COMMERCIAL, INDUSTRIAL, RETAIL, WAREHOUSE, KIOSK, STORAGE_TANK
			}),
			new Item("civic", R.drawable.ic_building_civic, R.string.quest_buildingType_civic, R.string.quest_buildingType_civic_description, new Item[]{
					SCHOOL, UNIVERSITY, HOSPITAL, HOTEL, KINDERGARTEN, TRAIN_STATION, TRANSPORTATION,
					COLLEGE, STADIUM,
			}),
			new Item("religious", R.drawable.ic_building_temple, R.string.quest_buildingType_religious, new Item[]{
					CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE
			}),
			new Item(null, R.drawable.ic_building_car, R.string.quest_buildingType_cars, new Item[]{
					GARAGE, GARAGES, CARPORT, PARKING
			}),
			new Item(null, R.drawable.ic_building_farm, R.string.quest_buildingType_farm, new Item[]{
					FARM, FARM_AUXILIARY, GREENHOUSE
			}),
			new Item(null, R.drawable.ic_building_other, R.string.quest_buildingType_other, new Item[]{
					SHED, ROOF, SERVICE
			}),
	};

	private GroupedImageSelectAdapter imageSelector;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_image_select);

		RecyclerView buildingSelect = contentView.findViewById(R.id.imageSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), 2);
		buildingSelect.setLayoutManager(lm);
		buildingSelect.setNestedScrollingEnabled(false);

		imageSelector = new GroupedImageSelectAdapter(lm);
		imageSelector.setGroupCellLayout(R.layout.cell_labeled_icon_select_with_description);
		imageSelector.setCellLayout(R.layout.cell_labeled_icon_select_with_description);
		imageSelector.setItems(Arrays.asList(TOP_BUILDINGS));
		buildingSelect.setAdapter(imageSelector);

		Button showMoreButton = contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(v -> {
			imageSelector.setItems(Arrays.asList(ALL_BUILDINGS));
			showMoreButton.setVisibility(View.GONE);
		});

		addOtherAnswers();

		return view;
	}

	@Override protected void onClickOk()
	{
		final Bundle answer = new Bundle();

		Item building = getSelectedItem();
		if(building != null)
		{
			if(!building.hasValue())
			{
				new AlertDialogBuilder(getContext())
					.setMessage(R.string.quest_building_type_invalid_value)
					.setPositiveButton(R.string.ok, null)
					.show();
				return;
			}

			if(building.value.startsWith("man_made=")) {
				String man_made = building.value.split("=",2)[1];
				answer.putString(MAN_MADE, man_made);
			}
			else
			{
				answer.putString(BUILDING, building.value);
			}

			if(building.isGroup())
			{
				new AlertDialogBuilder(getContext())
					.setMessage(R.string.quest_building_type_generic_building_confirmation)
					.setNegativeButton(R.string.quest_generic_confirmation_no, null)
					.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) -> applyFormAnswer(answer))
					.show();
				return;
			}
		}

		applyFormAnswer(answer);
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_buildingType_answer_input_description, this::showInputCommentDialog);
		addOtherAnswer(R.string.quest_buildingType_multiple_types, this::showMultipleTypesHintDialog);
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
				String txt = editText.getText().toString().trim();
				if(txt.isEmpty())
				{
					new AlertDialogBuilder(getContext())
						.setMessage(R.string.quest_generic_error_a_field_empty)
						.setPositiveButton(R.string.ok, null)
						.show();
					return;
				}

				Bundle answer = new Bundle();
				answer.putString(BUILDING_DESCRIPTION, txt);
				applyImmediateAnswer(answer);
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}

	private void showMultipleTypesHintDialog()
	{
		new AlertDialogBuilder(getContext())
			.setMessage(R.string.quest_buildingType_multiple_types_description)
			.setPositiveButton(android.R.string.ok, null)
			.show();
	}
}
