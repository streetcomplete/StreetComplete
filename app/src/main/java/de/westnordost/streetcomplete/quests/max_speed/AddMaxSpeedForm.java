package de.westnordost.streetcomplete.quests.max_speed;

import android.content.DialogInterface;
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

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddMaxSpeedForm extends AbstractQuestFormAnswerFragment
{
	public static final String
			MAX_SPEED = "maxspeed",
			ADVISORY_SPEED = "advisory_speed",
			MAX_SPEED_IMPLICIT_COUNTRY = "maxspeed_country",
			MAX_SPEED_IMPLICIT_ROADTYPE = "maxspeed_roadtype",
			LIVING_STREET = "living_street";

	private static final String	IS_ADVISORY_SPEED_LIMIT = "is_advisory_speed_limit";

	private static final Collection<String>
			URBAN_OR_RURAL_ROADS = Arrays.asList("primary","secondary","tertiary","unclassified",
					"primary_link","secondary_link","tertiary_link","road"),
			ROADS_WITH_DEFINITE_SPEED_LIMIT = Arrays.asList("trunk","motorway","living_street"),
			POSSIBLY_SLOWZONE_ROADS = Arrays.asList("residential","unclassified"),
			MAYBE_LIVING_STREET = Arrays.asList("residential");

	private EditText speedInput;
	private CheckBox zoneCheckbox;

	private boolean isAdvisorySpeedLimit;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		isAdvisorySpeedLimit = false;
		if(savedInstanceState != null)
		{
			isAdvisorySpeedLimit = savedInstanceState.getBoolean(IS_ADVISORY_SPEED_LIMIT);
		}

		if(isAdvisorySpeedLimit)
		{
			setStreetSignLayout(getAdvisorySpeedLimitLayoutResourceId());
		}
		else
		{
			setStreetSignLayout(getMaxSpeedLayoutResourceId());
		}

		addOtherAnswers();

		return view;
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(IS_ADVISORY_SPEED_LIMIT, isAdvisorySpeedLimit);
	}

	private void setStreetSignLayout(int resourceId)
	{
		View contentView = setContentView(resourceId);

		speedInput = contentView.findViewById(R.id.maxSpeedInput);

		View zoneContainer = contentView.findViewById(R.id.zoneContainer);
		if(zoneContainer != null)
		{
			initZoneCheckbox(zoneContainer);
		}
	}

	private void initZoneCheckbox(View zoneContainer)
	{
		boolean isResidential = POSSIBLY_SLOWZONE_ROADS.contains(getOsmElement().getTags().get("highway"));
		boolean isSlowZoneKnown = getCountryInfo().isSlowZoneKnown();
		zoneContainer.setVisibility(isSlowZoneKnown && isResidential ? View.VISIBLE : View.GONE);

		zoneCheckbox = zoneContainer.findViewById(R.id.zoneCheckbox);
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

	private int getAdvisorySpeedLimitLayoutResourceId()
	{
		String layout = getCountryInfo().getAdvisorySpeedLimitLayout();
		return getResources().getIdentifier(layout,"layout", getActivity().getPackageName());
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_maxspeed_answer_noSign, new Runnable()
		{
			@Override public void run()
			{
				final String highwayTag = getOsmElement().getTags().get("highway");
				if(URBAN_OR_RURAL_ROADS.contains(highwayTag))
				{
					confirmNoSign(new Runnable()
					{
						@Override public void run()
						{
							determineImplicitMaxspeedType();
						}
					});
				}
				else if(POSSIBLY_SLOWZONE_ROADS.contains(highwayTag))
				{
					if(getCountryInfo().isSlowZoneKnown())
					{
						confirmNoSignSlowZone(new Runnable()
						{
							@Override public void run()
							{
								determineImplicitMaxspeedType();
							}
						});
					}
					else
					{
						confirmNoSign(new Runnable()
						{
							@Override public void run()
							{
								determineImplicitMaxspeedType();
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
			}
		});

		final String highwayTag = getOsmElement().getTags().get("highway");
		if(getCountryInfo().isLivingStreetKnown() && MAYBE_LIVING_STREET.contains(highwayTag))
		{
			addOtherAnswer(R.string.quest_maxspeed_answer_living_street, new Runnable()
			{
				@Override public void run()
				{
					confirmLivingStreet(new Runnable() { @Override public void run()
					{
						Bundle answer = new Bundle();
						answer.putBoolean(LIVING_STREET, true);
						applyImmediateAnswer(answer);
					}});
				}
			});
		}

		if(getCountryInfo().isAdvisorySpeedLimitKnown())
		{
			addOtherAnswer(R.string.quest_maxspeed_answer_advisory_speed_limit, new Runnable()
			{
				@Override public void run()
				{
					isAdvisorySpeedLimit = true;
					setStreetSignLayout(getAdvisorySpeedLimitLayoutResourceId());
				}
			});
		}
	}

	private void determineImplicitMaxspeedType()
	{
		if(getCountryInfo().getCountryCode().equals("GB"))
		{
			askSingleOrDualCarriageway();
		}
		else
		{
			askUrbanOrRural();
		}
	}

	private void confirmLivingStreet(final Runnable callback)
	{
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.quest_maxspeed_living_street_confirmation, null, false);

		ImageView img = view.findViewById(R.id.imgLivingStreet);
		int drawableId = getResources().getIdentifier(
				getCountryInfo().getLivingStreetSign(),
				"drawable",
				getActivity().getPackageName());
		img.setImageDrawable(getResources().getDrawable(drawableId));

		new AlertDialogBuilder(getActivity())
				.setView(view)
				.setTitle(R.string.quest_maxspeed_answer_living_street_confirmation_title)
				.setPositiveButton(R.string.quest_generic_confirmation_yes,
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

	private void confirmNoSignSlowZone(final Runnable callback)
	{
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.quest_maxspeed_no_sign_no_slow_zone_confirmation, null, false);

		ImageView imgSlowZone = view.findViewById(R.id.imgSlowZone);
		ImageView mainLayoutImgSlowZone = getView() != null ? (ImageView) getView().findViewById(R.id.zoneImg) : null;
		if(mainLayoutImgSlowZone != null)
		{
			Drawable slowZoneDrawable = mainLayoutImgSlowZone.getDrawable();
			imgSlowZone.setImageDrawable(slowZoneDrawable);
		}

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

	private void askSingleOrDualCarriageway()
	{
		new AlertDialogBuilder(getActivity())
				.setMessage(R.string.quest_maxspeed_answer_noSign_singleOrDualCarriageway_description)
				.setPositiveButton(R.string.quest_generic_hasFeature_yes, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applyNoSignAnswer("nsl_dual");
					}
				})
				.setNegativeButton(R.string.quest_generic_hasFeature_no, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						applyNoSignAnswer("nsl_single");
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

		if (isAdvisorySpeedLimit)
		{
			answer.putString(ADVISORY_SPEED, speedStr.toString());
		}
		else
		{
			answer.putString(MAX_SPEED, speedStr.toString());
			if (zoneCheckbox != null && zoneCheckbox.isChecked())
			{
				String countryCode = getCountryInfo().getCountryCode();
				answer.putString(MAX_SPEED_IMPLICIT_COUNTRY, countryCode);
				answer.putString(MAX_SPEED_IMPLICIT_ROADTYPE, "zone" + speed);
			}
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
