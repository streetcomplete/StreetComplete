package de.westnordost.streetcomplete.quests.building_levels;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.TextChangedWatcher;


public class AddBuildingLevelsForm extends AbstractQuestFormAnswerFragment
{
	public static final String BUILDING_LEVELS = "building_levels";
	public static final String ROOF_LEVELS = "roof_levels";

	private EditText levelsInput, roofLevelsInput;

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

		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		answer.putInt(BUILDING_LEVELS, Integer.parseInt(getLevels()));
		if(!getRoofLevels().isEmpty())
		{
			answer.putInt(ROOF_LEVELS, Integer.parseInt(getRoofLevels()));
		}
		applyAnswer(answer);
	}

	private String getLevels() { return levelsInput.getText().toString(); }
	private String getRoofLevels() { return roofLevelsInput.getText().toString(); }

	@Override public boolean isFormComplete() { return !getLevels().isEmpty(); }
}
