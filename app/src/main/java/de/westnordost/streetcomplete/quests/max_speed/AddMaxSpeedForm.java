package de.westnordost.streetcomplete.quests.max_speed;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
			URBAN_OR_RURAL_ROADS = Arrays.asList("primary","secondary","tertiary","unclassified",
					"primary_link","secondary_link","tertiary_link","road"),
			ROADS_WITH_DEFINITE_SPEED_LIMIT = Arrays.asList("trunk","motorway","living_street"),
			URBAN_OR_SLOWZONE_ROADS = Arrays.asList("residential");

	private EditText speedInput;
	private CheckBox zoneCheckbox;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_maxspeed_title_short);

		View contentView = setContentView(getMaxSpeedLayoutResourceId());
		speedInput = (EditText) contentView.findViewById(R.id.maxSpeedInput);

		View zoneContainer = contentView.findViewById(R.id.zoneContainer);
		if(zoneContainer != null)
		{
			initZoneCheckbox(zoneContainer);
		}

		return view;
	}

	private void initZoneCheckbox(View zoneContainer)
	{
		boolean isResidential = URBAN_OR_SLOWZONE_ROADS.contains(getOsmElement().getTags().get("highway"));
		boolean isSlowZoneKnown = getCountryInfo().isSlowZoneKnown();
		zoneContainer.setVisibility(isSlowZoneKnown && isResidential ? View.VISIBLE : View.GONE);

		zoneCheckbox = (CheckBox) zoneContainer.findViewById(R.id.zoneCheckbox);
		zoneCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked && speedInput.getText().toString().isEmpty())
				{
					// prefill speed input with normal "slow zone" value
					boolean isMph = getCountryInfo().getSpeedUnit().equals("mph");
					speedInput.setText(isMph ? "20" : "30");
				}
			}
		});

		zoneContainer.findViewById(R.id.zoneImg).setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				zoneCheckbox.toggle();
			}
		});
	}

	private int getMaxSpeedLayoutResourceId()
	{
		String layout = getCountryInfo().getMaxspeedLayout();
		if(layout != null)
		{
			return getResources().getIdentifier(layout,"layout", getActivity().getPackageName());
		}
		return R.layout.quest_maxspeed;
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_maxspeed_answer_noSign);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_maxspeed_answer_noSign)
		{
			final String highwayTag = getOsmElement().getTags().get("highway");
			if(URBAN_OR_RURAL_ROADS.contains(highwayTag))
			{
				confirmNoSign(new Runnable()
				{
					@Override public void run()
					{
						askUrbanOrRural();
					}
				});
			}
			else if(URBAN_OR_SLOWZONE_ROADS.contains(highwayTag))
			{
				if(getCountryInfo().isSlowZoneKnown())
				{
					confirmNoSignSlowZone(new Runnable()
					{
						@Override public void run()
						{
							applyNoSignAnswer("urban");
						}
					});
				}
				else
				{
					confirmNoSign(new Runnable()
					{
						@Override public void run()
						{
							applyNoSignAnswer("urban");
						}
					});
				}
			}
			else if(ROADS_WITH_DEFINITE_SPEED_LIMIT.contains(highwayTag))
			{
				confirmNoSign(new Runnable()
				{
					@Override public void run()
					{
						applyNoSignAnswer(highwayTag);
					}
				});
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

	private void confirmNoSignSlowZone(final Runnable callback)
	{
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.quest_maxspeed_no_sign_no_slow_zone_confirmation, null, false);

		ImageView imgSlowZone = (ImageView) view.findViewById(R.id.imgSlowZone);
		Drawable slowZoneDrawable = ((ImageView) getView().findViewById(R.id.zoneImg)).getDrawable();
		imgSlowZone.setImageDrawable(slowZoneDrawable);

		new AlertDialogBuilder(getActivity())
				.setView(view)
				.setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
				.setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive,
						new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which)
							{
								callback.run();
							}
						})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
	}

	private void askUrbanOrRural()
	{
		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_maxspeed_answer_noSign_info_urbanOrRural)
				.setMessage(R.string.quest_maxspeed_answer_noSign_urbanOrRural_description)
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
				.show();
	}

	private void confirmNoSign(final Runnable callback)
	{
		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
				.setMessage(R.string.quest_maxspeed_answer_noSign_confirmation)
				.setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive,
						new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which)
							{
								callback.run();
							}
						})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
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
			confirmUnusualInput(new Runnable()
			{
				@Override public void run()
				{
					applySpeedLimitFormAnswer();
				}
			});
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
		if(zoneCheckbox != null && zoneCheckbox.isChecked())
		{
			String countryCode = getCountryInfo().getCountryCode();
			answer.putString(MAX_SPEED_IMPLICIT_COUNTRY, countryCode);
			answer.putString(MAX_SPEED_IMPLICIT_ROADTYPE, "zone" + speed);
		}

		applyFormAnswer(answer);
	}

	private void confirmUnusualInput(final Runnable callback)
	{
		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_generic_confirmation_title)
				.setMessage(R.string.quest_maxspeed_unusualInput_confirmation_description)
				.setPositiveButton(R.string.quest_generic_confirmation_yes, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						callback.run();
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
