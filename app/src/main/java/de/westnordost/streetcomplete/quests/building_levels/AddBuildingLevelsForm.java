package de.westnordost.streetcomplete.quests.building_levels;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.quests.LastPickedValuesStore;
import de.westnordost.streetcomplete.util.TextChangedWatcher;

public class AddBuildingLevelsForm extends AbstractQuestFormAnswerFragment
{
	public static final String BUILDING_LEVELS = "building_levels";
	public static final String ROOF_LEVELS = "roof_levels";

	private static final int MAX_FAVS = 1;

	private EditText levelsInput, roofLevelsInput;

	@Inject LastPickedValuesStore favs;

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		Injector.instance.getApplicationComponent().inject(this);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_building_levels);

		TextWatcher onTextChangedListener = new TextChangedWatcher(this::checkIsFormComplete);

		levelsInput = contentView.findViewById(R.id.levelsInput);
		levelsInput.requestFocus();
		levelsInput.addTextChangedListener(onTextChangedListener);
		roofLevelsInput = contentView.findViewById(R.id.roofLevelsInput);
		roofLevelsInput.addTextChangedListener(onTextChangedListener);

		addOtherAnswer(R.string.quest_buildingLevels_answer_multipleLevels, () ->
		{
			new AlertDialog.Builder(getActivity())
					.setMessage(R.string.quest_buildingLevels_answer_description)
					.setPositiveButton(android.R.string.ok, null)
					.show();
		});

		View btnPickLast = contentView.findViewById(R.id.btn_pickLast);
		LinkedList<String> lastPicked = favs.getLastPicked(getClass().getSimpleName());
		if(lastPicked.isEmpty())
		{
			btnPickLast.setVisibility(View.GONE);
		}
		else
		{
			btnPickLast.setVisibility(View.VISIBLE);

			String[] favValues = lastPicked.getFirst().split("#");

			TextView lastLevelsText = contentView.findViewById(R.id.lastLevelsText);
			lastLevelsText.setText(favValues[0]);

			TextView lastRoofLevelsText = contentView.findViewById(R.id.lastRoofLevelsText);
			lastRoofLevelsText.setText(favValues.length > 1 ? favValues[1] : " ");

			btnPickLast.setOnClickListener(v -> {
				levelsInput.setText(lastLevelsText.getText());
				roofLevelsInput.setText(lastRoofLevelsText.getText());
				btnPickLast.setVisibility(View.GONE);
			});
		}

		return view;
	}

	@Override protected void onClickOk()
	{
		List<Integer> favValues = new LinkedList<>();
		Bundle answer = new Bundle();

		int buildingLevels = Integer.parseInt(getLevels());
		answer.putInt(BUILDING_LEVELS, buildingLevels);
		favValues.add(buildingLevels);

		if(!getRoofLevels().isEmpty())
		{
			int roofLevels = Integer.parseInt(getRoofLevels());
			answer.putInt(ROOF_LEVELS, roofLevels);
			favValues.add(roofLevels);
		}
		favs.addLastPicked(getClass().getSimpleName(), TextUtils.join("#", favValues), MAX_FAVS);
		applyAnswer(answer);
	}

	private String getLevels() { return levelsInput.getText().toString().trim(); }
	private String getRoofLevels() { return roofLevelsInput.getText().toString().trim(); }

	@Override public boolean isFormComplete() { return !getLevels().isEmpty(); }
}
