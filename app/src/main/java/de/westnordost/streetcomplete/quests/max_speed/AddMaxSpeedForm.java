package de.westnordost.streetcomplete.quests.max_speed;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddMaxSpeedForm extends AbstractQuestFormAnswerFragment
{
	public static final String
			MAX_SPEED = "maxspeed",
			MAX_SPEED_IMPLICIT_COUNTRY = "maxspeed_country",
			MAX_SPEED_IMPLICIT_ROADTYPE = "maxspeed_roadtype";

	private static final Collection<String>
			URBAN_OR_RURAL_ROADS = Arrays.asList("primary","secondary","tertiary","unclassified," +
					"primary_link","secondary_link","tertiary_link","road"),
			ROADS_WITH_DEFINITE_SPEED_LIMIT = Arrays.asList("trunk","motorway","living_street"),
			URBAN_OR_ZONE30_ROADS = Arrays.asList("residential");

	private EditText speedInput;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_maxspeed_title_short);

		String maxspeedLayoutName = getCountryInfo().getMaxspeedLayout();
		int maxspeedLayout;
		if(maxspeedLayoutName != null)
		{
			maxspeedLayout = getResources().getIdentifier(
					maxspeedLayoutName, "layout", getActivity().getPackageName());
		}
		else
		{
			maxspeedLayout = R.layout.quest_maxspeed;
		}
		View contentView = setContentView(maxspeedLayout);


		speedInput = (EditText) contentView.findViewById(R.id.maxSpeedInput);
		TextView unitText = (TextView) contentView.findViewById(R.id.unitText);
		unitText.setText(getCountryInfo().getSpeedUnit());

		return view;
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_maxspeed_answer_variable);
		answers.add(R.string.quest_maxspeed_answer_noSign);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_maxspeed_answer_variable)
		{
			// TODO confirm?

			Bundle answer = new Bundle();
			answer.putString(MAX_SPEED, "signals");
			applyImmediateAnswer(answer);
			return true;
		}
		if(itemResourceId == R.string.quest_maxspeed_answer_noSign)
		{
			String highwayTag = getOsmElement().getTags().get("highway");
			if(URBAN_OR_RURAL_ROADS.contains(highwayTag))
			{
				askUrbanOrRural();
			}
			else if(URBAN_OR_ZONE30_ROADS.contains(highwayTag))
			{
				confirmAskZone30();
			}
			else
			{
				confirmImplicitSpeedLimit(highwayTag);
			}
			return true;
		}

		return false;
	}

	private Resources getResources(Locale locale)
	{
		Configuration configuration = new Configuration(getActivity().getResources().getConfiguration());
		configuration.setLocale(locale);
		return getActivity().createConfigurationContext(configuration).getResources();
	}

	private void confirmAskZone30()
	{
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.quest_maxspeed_zone_confirmation, null, false);

		TextView maxSpeedText = (TextView) view.findViewById(R.id.maxSpeedText);
		final boolean isMph = getCountryInfo().getSpeedUnit().equals("mph");
		maxSpeedText.setText(isMph ? "20" : "30");

		TextView zoneText = (TextView) view.findViewById(R.id.zoneText);
		zoneText.setText(getResources(getCountryInfo().getLocale()).getString(R.string.quest_maxspeed_sign_zone));

		new AlertDialogBuilder(getActivity())
				.setView(view)
				.setPositiveButton(R.string.quest_generic_hasFeature_yes, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applyNoSignAnswer(isMph ? "zone20" : "zone30");
					}
				})
				.setNeutralButton(R.string.quest_generic_hasFeature_no, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applyNoSignAnswer("urban");
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void askUrbanOrRural()
	{
		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_maxspeed_answer_noSign_info_urbanOrRural)
				.setPositiveButton(R.string.quest_maxspeed_answer_noSign_urbanOk, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applyNoSignAnswer("urban");
					}
				})
				.setNeutralButton(R.string.quest_maxspeed_answer_noSign_ruralOk, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applyNoSignAnswer("rural");
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void confirmImplicitSpeedLimit(final String highwayTag)
	{
		new AlertDialogBuilder(getActivity())
				.setMessage(R.string.quest_maxspeed_answer_noSign_info)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applyNoSignAnswer(highwayTag);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void applyNoSignAnswer(String roadType)
	{
		Bundle answer = new Bundle();
		String countryCode = getCountryInfo().getCountryCode();
		answer.putString(MAX_SPEED_IMPLICIT_COUNTRY, countryCode);
		answer.putString(MAX_SPEED_IMPLICIT_ROADTYPE, roadType);
		applyImmediateAnswer(answer);
	}

	@Override protected void onClickOk()
	{
		if(!hasChanges())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		if(userSelectedUnrealisticSpeedLimit())
		{
			confirmUnusualInput();
		}
		else
		{
			applySpeedLimitFormAnswer();
		}
	}

	private boolean userSelectedUnrealisticSpeedLimit()
	{
		int speed = Integer.valueOf(speedInput.getText().toString());
		String speedUnit = getCountryInfo().getSpeedUnit();
		double speedInKmh = speedUnit.equals("mph") ? mphToKmh(speed) : speed;
		return speedInKmh > 140 || speed > 20 && speed % 5 != 0;
	}

	private static double mphToKmh(double mph)
	{
		return 1.60934 * mph;
	}

	private void applySpeedLimitFormAnswer()
	{
		Bundle answer = new Bundle();

		StringBuilder speedStr = new StringBuilder();
		int speed = Integer.valueOf(speedInput.getText().toString());
		speedStr.append(speed);

		// km/h is the OSM default, does not need to be mentioned
		String speedUnit = getCountryInfo().getSpeedUnit();
		if(!speedUnit.equals("km/h"))
		{
			speedStr.append(" " + speedUnit);
		}
		answer.putString(MAX_SPEED, speedStr.toString());
		applyFormAnswer(answer);
	}

	private void confirmUnusualInput()
	{
		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_generic_confirmation_title)
				.setMessage(R.string.quest_maxspeed_unusualInput_confirmation_description)
				.setPositiveButton(R.string.quest_generic_confirmation_yes, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applySpeedLimitFormAnswer();
					}
				})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
	}

	@Override public boolean hasChanges()
	{
		return !speedInput.getText().toString().isEmpty();
	}
}
