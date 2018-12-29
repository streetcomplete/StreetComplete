package de.westnordost.streetcomplete.quests.max_speed;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.TextChangedWatcher;


public class AddMaxSpeedForm extends AbstractQuestFormAnswerFragment
{
	public static final String
		MAX_SPEED_IMPLICIT_COUNTRY = "maxspeed_country",
		MAX_SPEED_IMPLICIT_ROADTYPE = "maxspeed_roadtype",
		ADVISORY_SPEED = "advisory_speed",
		MAX_SPEED = "maxspeed",
		LIVING_STREET = "living_street";

	private static final Collection<String>
		POSSIBLY_SLOWZONE_ROADS = Arrays.asList("residential","unclassified","tertiary" /*#1133*/),
		MAYBE_LIVING_STREET = Arrays.asList("residential","unclassified"),
		ROADS_WITH_DEFINITE_SPEED_LIMIT = Arrays.asList("trunk","motorway","living_street");

	private RadioGroup speedTypeSelect;
	private ViewGroup rightSide;

	private EditText speedInput;
	private Spinner speedUnitSelect;

	private enum SpeedType { SIGN, ZONE, ADVISORY, NO_SIGN }
	private SpeedType speedType;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_maxspeed);

		boolean couldBeSlowZone = getCountryInfo().isSlowZoneKnown() &&
			POSSIBLY_SLOWZONE_ROADS.contains(getOsmElement().getTags().get("highway"));
		RadioButton zoneBtn = contentView.findViewById(R.id.zone);
		zoneBtn.setVisibility(couldBeSlowZone ? View.VISIBLE : View.GONE);

		rightSide = contentView.findViewById(R.id.right_side);
		speedTypeSelect = contentView.findViewById(R.id.speedTypeSelect);
		speedTypeSelect.setOnCheckedChangeListener((group, checkedId) ->
		{
			setSpeedType(getSpeedType(checkedId));
		});

		addOtherAnswers();

		return view;
	}

	private void addOtherAnswers()
	{
		final String highwayTag = getOsmElement().getTags().get("highway");
		if(getCountryInfo().isLivingStreetKnown() && MAYBE_LIVING_STREET.contains(highwayTag))
		{
			addOtherAnswer(R.string.quest_maxspeed_answer_living_street, () ->
			{
				confirmLivingStreet(() ->
				{
					Bundle answer = new Bundle();
					answer.putBoolean(LIVING_STREET, true);
					applyAnswer(answer);
				});
			});
		}

		if(getCountryInfo().isAdvisorySpeedLimitKnown())
		{
			addOtherAnswer(R.string.quest_maxspeed_answer_advisory_speed_limit, () ->
			{
				speedTypeSelect.clearCheck();
				for(int i = 0; i < speedTypeSelect.getChildCount(); i++){
					speedTypeSelect.getChildAt(i).setEnabled(false);
				}
				setSpeedType(SpeedType.ADVISORY);
			});
		}
	}

	@Override protected void onClickOk()
	{
		if(speedType == SpeedType.NO_SIGN)
		{
			boolean couldBeSlowZone = getCountryInfo().isSlowZoneKnown() &&
				POSSIBLY_SLOWZONE_ROADS.contains(getOsmElement().getTags().get("highway"));

			if(couldBeSlowZone) confirmNoSignSlowZone(this::determineImplicitMaxspeedType);
			else                confirmNoSign(this::determineImplicitMaxspeedType);
		}
		else
		{
			if (userSelectedUnusualSpeed()) confirmUnusualInput(this::applySpeedLimitFormAnswer);
			else applySpeedLimitFormAnswer();
		}
	}

	@Override public boolean isFormComplete()
	{
		return speedType != null && (speedInput == null || !getSpeed().isEmpty());
	}

	private String getSpeed() { return speedInput.getText().toString(); }

	/* ---------------------------------------- With sign --------------------------------------- */

	private void setSpeedType(SpeedType speedType)
	{
		this.speedType = speedType;

		rightSide.removeAllViews();
		int layoutResId = getLayoutResId(speedType);
		if(layoutResId != 0)
		{
			getLayoutInflater().inflate(layoutResId, rightSide, true);
		}

		speedInput = rightSide.findViewById(R.id.maxSpeedInput);
		if(speedInput != null)
		{
			speedInput.requestFocus();
			speedInput.addTextChangedListener(new TextChangedWatcher(this::checkIsFormComplete));
		}
		speedUnitSelect = rightSide.findViewById(R.id.speedUnitSelect);
		if(speedUnitSelect != null)
		{
			List<String> measurementUnits = getCountryInfo().getMeasurementSystem();
			speedUnitSelect.setVisibility(measurementUnits.size() == 1 ? View.GONE : View.VISIBLE);
			speedUnitSelect.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item_centered, getSpinnerItems(measurementUnits)));
			speedUnitSelect.setSelection(0);
		}
		checkIsFormComplete();
	}

	private List<String> getSpinnerItems(List<String> units)
	{
		List<String> items = new ArrayList<>();
		for (String unit : units)
		{
			if (unit.equals("metric"))        items.add("km/h");
			else if (unit.equals("imperial")) items.add("mph");
		}
		return items;
	}

	private SpeedType getSpeedType(@IdRes int checkedId)
	{
		switch (checkedId)
		{
			case R.id.sign:    return SpeedType.SIGN;
			case R.id.zone:    return SpeedType.ZONE;
			case R.id.no_sign: return SpeedType.NO_SIGN;
		}
		return null;
	}

	@LayoutRes private int getLayoutResId(SpeedType speedType)
	{
		if(speedType == null) return 0;
		switch (speedType)
		{
			case SIGN:     return R.layout.quest_maxspeed_sign;
			case ZONE:     return R.layout.quest_maxspeed_zone_sign;
			case ADVISORY: return R.layout.quest_maxspeed_advisory;
		}
		return 0;
	}

	private boolean userSelectedUnusualSpeed()
	{
		int speed = Integer.parseInt(getSpeed());
		String speedUnit = (String) speedUnitSelect.getSelectedItem();
		double speedInKmh = speedUnit.equals("mph") ? mphToKmh(speed) : speed;
		return speedInKmh > 140 || speed > 20 && speed % 5 != 0;
	}

	private static double mphToKmh(double mph)
	{
		return 1.60934 * mph;
	}

	private void confirmUnusualInput(final Runnable callback)
	{
		if(getActivity() == null) return;
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.quest_generic_confirmation_title)
			.setMessage(R.string.quest_maxspeed_unusualInput_confirmation_description)
			.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) -> callback.run())
			.setNegativeButton(R.string.quest_generic_confirmation_no, null)
			.show();
	}

	private void applySpeedLimitFormAnswer()
	{
		int speed = Integer.parseInt(getSpeed());
		String speedStr = String.valueOf(speed);

		// km/h is the OSM default, is not mentioned
		String speedUnit = (String) speedUnitSelect.getSelectedItem();
		if(!speedUnit.equals("km/h"))
		{
			speedStr += " " + speedUnit;
		}

		Bundle answer = new Bundle();
		if(speedType == SpeedType.ADVISORY)
		{
			answer.putString(ADVISORY_SPEED, speedStr);
		}
		else
		{
			answer.putString(MAX_SPEED, speedStr);
			if (speedType == SpeedType.ZONE)
			{
				answer.putString(MAX_SPEED_IMPLICIT_COUNTRY, getCountryInfo().getCountryCode());
				answer.putString(MAX_SPEED_IMPLICIT_ROADTYPE, "zone" + speed);
			}
		}
		applyAnswer(answer);
	}

	/* ----------------------------------------- No sign ---------------------------------------- */

	private void confirmLivingStreet(final Runnable callback)
	{
		if(getActivity() == null) return;
		View view = getLayoutInflater().inflate(R.layout.quest_maxspeed_living_street_confirmation, null, false);
		// this is necessary because the inflated image view uses the activity context rather than
		// the fragment / layout inflater context' resources to access it's drawable
		ImageView img = view.findViewById(R.id.imgLivingStreet);
		img.setImageDrawable(getResources().getDrawable(R.drawable.ic_living_street));
		new AlertDialog.Builder(getActivity())
			.setView(view)
			.setTitle(R.string.quest_maxspeed_answer_living_street_confirmation_title)
			.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) -> callback.run())
			.setNegativeButton(R.string.quest_generic_confirmation_no, null)
			.show();
	}

	private void confirmNoSign(Runnable confirm)
	{
		if(getActivity() == null) return;
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
			.setMessage(R.string.quest_maxspeed_answer_noSign_confirmation)
			.setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive, (dialog, which) -> confirm.run())
			.setNegativeButton(R.string.quest_generic_confirmation_no, null)
			.show();
	}

	private void confirmNoSignSlowZone(Runnable confirm)
	{
		if(getActivity() == null) return;
		View view = getLayoutInflater().inflate(R.layout.quest_maxspeed_no_sign_no_slow_zone_confirmation, null, false);
		EditText input = view.findViewById(R.id.maxSpeedInput);
		input.setText("××");
		input.setInputType(EditorInfo.TYPE_NULL);

		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
			.setView(view)
			.setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive, (dialog, which) -> confirm.run())
			.setNegativeButton(R.string.quest_generic_confirmation_no, null)
			.show();
	}

	private void determineImplicitMaxspeedType()
	{
		final String highwayTag = getOsmElement().getTags().get("highway");
		if(ROADS_WITH_DEFINITE_SPEED_LIMIT.contains(highwayTag))
		{
			applyNoSignAnswer(highwayTag);
		}
		else
		{
			if(getCountryInfo().getCountryCode().equals("GB"))
			{
				determineLit(
					() -> applyNoSignAnswer("nsl_restricted"),
					() -> askIsDualCarriageway(
						() -> applyNoSignAnswer("nsl_dual"),
						() -> applyNoSignAnswer("nsl_single"))
				);
			}
			else
			{
				askUrbanOrRural(
					() -> applyNoSignAnswer("urban"),
					() -> applyNoSignAnswer("rural"));
			}
		}
	}

	private void askUrbanOrRural(Runnable onUrban, Runnable onRural)
	{
		if(getActivity() == null) return;
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.quest_maxspeed_answer_noSign_info_urbanOrRural)
			.setMessage(R.string.quest_maxspeed_answer_noSign_urbanOrRural_description)
			.setPositiveButton(R.string.quest_maxspeed_answer_noSign_urbanOk, (dialog, which) -> onUrban.run())
			.setNegativeButton(R.string.quest_maxspeed_answer_noSign_ruralOk, (dialog, which) -> onRural.run())
			.show();
	}

	private void determineLit(Runnable onYes, Runnable onNo)
	{
		String lit = getOsmElement().getTags().get("lit");
		if("yes".equals(lit)) onYes.run();
		else if("no".equals(lit)) onNo.run();
		else askLit(onYes, onNo);
	}

	private void askLit(Runnable onYes, Runnable onNo)
	{
		if(getActivity() == null) return;
		new AlertDialog.Builder(getActivity())
			.setMessage(R.string.quest_way_lit_road_title)
			.setPositiveButton(R.string.quest_generic_hasFeature_yes, (dialog, which) -> onYes.run())
			.setNegativeButton(R.string.quest_generic_hasFeature_no, (dialog, which) -> onNo.run())
			.show();
	}

	private void askIsDualCarriageway(Runnable onYes, Runnable onNo)
	{
		if(getActivity() == null) return;
		new AlertDialog.Builder(getActivity())
			.setMessage(R.string.quest_maxspeed_answer_noSign_singleOrDualCarriageway_description)
			.setPositiveButton(R.string.quest_generic_hasFeature_yes, (dialog, which) -> onYes.run())
			.setNegativeButton(R.string.quest_generic_hasFeature_no, (dialog, which) -> onNo.run())
			.show();
	}

	private void applyNoSignAnswer(String roadType)
	{
		Bundle answer = new Bundle();
		String countryCode = getCountryInfo().getCountryCode();
		answer.putString(MAX_SPEED_IMPLICIT_COUNTRY, countryCode);
		answer.putString(MAX_SPEED_IMPLICIT_ROADTYPE, roadType);
		applyAnswer(answer);
	}
}
