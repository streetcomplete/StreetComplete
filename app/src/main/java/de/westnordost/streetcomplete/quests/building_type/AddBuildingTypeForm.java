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

import static de.westnordost.streetcomplete.quests.building_type.BuildingType.*;

public class AddBuildingTypeForm extends AbstractQuestFormAnswerFragment
{
	public static final String
		BUILDING = "building",
		MAN_MADE = "man_made";

	private final Item[] TOP_BUILDINGS = new Item[] {
			DETACHED, APARTMENTS, HOUSE, GARAGE, SHED, HUT
	};

	private final Item[] ALL_BUILDINGS = new Item[] {
			new Item(RESIDENTIAL, new Item[]{
					DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, FARM, HOUSE,
					HUT, BUNGALOW, HOUSEBOAT, STATIC_CARAVAN, DORMITORY,
			}),
			new Item(null, R.drawable.ic_building_commercial, R.string.quest_buildingType_commercial, R.string.quest_buildingType_commercial_generic_description, new Item[]{
					COMMERCIAL, INDUSTRIAL, RETAIL, WAREHOUSE, KIOSK, STORAGE_TANK
			}),
			new Item(CIVIC, new Item[]{
					SCHOOL, UNIVERSITY, HOSPITAL, HOTEL, KINDERGARTEN, TRAIN_STATION, TRANSPORTATION,
					COLLEGE, STADIUM,
			}),
			new Item(RELIGIOUS, new Item[]{
					CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE
			}),
			new Item(null, R.drawable.ic_building_car, R.string.quest_buildingType_cars, new Item[]{
					GARAGE, GARAGES, CARPORT, PARKING
			}),
			new Item(null, R.drawable.ic_building_farm, R.string.quest_buildingType_farm, new Item[]{
					FARM, FARM_AUXILIARY, GREENHOUSE, STORAGE_TANK
			}),
			new Item(null, R.drawable.ic_building_other, R.string.quest_buildingType_other, new Item[]{
					SHED, ROOF, SERVICE, HUT
			}),
	};

	private GroupedImageSelectAdapter imageSelector;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_image_select);

		RecyclerView buildingSelect = contentView.findViewById(R.id.imageSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), 1);
		buildingSelect.setLayoutManager(lm);
		buildingSelect.setNestedScrollingEnabled(false);

		imageSelector = new GroupedImageSelectAdapter(lm);
		imageSelector.setGroupCellLayout(R.layout.cell_labeled_icon_select_with_description_group);
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
		addOtherAnswer(R.string.quest_buildingType_answer_multiple_types, this::showMultipleTypesHintDialog);
		addOtherAnswer(R.string.quest_buildingType_answer_construction_site, () -> {
			Bundle answer = new Bundle();
			answer.putString(BUILDING, "construction");
			applyImmediateAnswer(answer);
		});
	}

	@Override public boolean hasChanges() { return getSelectedItem() != null; }

	private Item getSelectedItem() { return imageSelector.getSelectedItem(); }

	private void showMultipleTypesHintDialog()
	{
		new AlertDialogBuilder(getContext())
			.setMessage(R.string.quest_buildingType_answer_multiple_types_description)
			.setPositiveButton(android.R.string.ok, null)
			.show();
	}
}
