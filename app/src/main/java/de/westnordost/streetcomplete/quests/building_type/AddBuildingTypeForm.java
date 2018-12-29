package de.westnordost.streetcomplete.quests.building_type;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;


import static de.westnordost.streetcomplete.quests.building_type.BuildingType.*;

public class AddBuildingTypeForm extends GroupedImageListQuestAnswerFragment
{
	public static final String
		BUILDING = "building",
		MAN_MADE = "man_made";

	@Override protected Item[] getTopItems()
	{
		return new Item[] { DETACHED, APARTMENTS, HOUSE, GARAGE, SHED, HUT };
	}

	@Override protected Item[] getAllItems() {
		return new Item[]{
			new Item(RESIDENTIAL, new Item[]{
				DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, FARM, HOUSE,
				HUT, BUNGALOW, HOUSEBOAT, STATIC_CARAVAN, DORMITORY,
			}),
			new Item(COMMERCIAL, new Item[]{
				OFFICE, INDUSTRIAL, RETAIL, WAREHOUSE, KIOSK, HOTEL, STORAGE_TANK
			}),
			new Item(CIVIC, new Item[]{
				SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, TRAIN_STATION,
				TRANSPORTATION, COLLEGE, GOVERNMENT, STADIUM,
			}),
			new Item(RELIGIOUS, new Item[]{
				CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE
			}),
			new Item(null, R.drawable.ic_building_car, R.string.quest_buildingType_cars, new Item[]{
				GARAGE, GARAGES, CARPORT, PARKING
			}),
			new Item(null, R.drawable.ic_building_farm, R.string.quest_buildingType_farm, new Item[]{
				FARM, FARM_AUXILIARY, GREENHOUSE, STORAGE_TANK
			}),
			new Item(null, R.drawable.ic_building_other, R.string.quest_buildingType_other, new Item[]{
				SHED, ROOF, SERVICE, HUT, TOILETS, HANGAR, BUNKER
			})
		};
	}

	@Override protected int getItemsPerRow() { return 1; }

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		imageSelector.setGroupCellLayout(R.layout.cell_labeled_icon_select_with_description_group);
		imageSelector.setCellLayout(R.layout.cell_labeled_icon_select_with_description);
		addOtherAnswers();
		return view;
	}

	@Override protected void applyAnswer(String value)
	{
		Bundle answer = new Bundle();
		if(value.startsWith("man_made=")) {
			String man_made = value.split("=",2)[1];
			answer.putString(MAN_MADE, man_made);
		}
		else
		{
			answer.putString(BUILDING, value);
		}
		applyAnswer(answer);
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_buildingType_answer_multiple_types, this::showMultipleTypesHintDialog);
		addOtherAnswer(R.string.quest_buildingType_answer_construction_site, () -> {
			Bundle answer = new Bundle();
			answer.putString(BUILDING, "construction");
			applyAnswer(answer);
		});
	}

	private void showMultipleTypesHintDialog()
	{
		new AlertDialog.Builder(getActivity())
			.setMessage(R.string.quest_buildingType_answer_multiple_types_description)
			.setPositiveButton(android.R.string.ok, null)
			.show();
	}
}
