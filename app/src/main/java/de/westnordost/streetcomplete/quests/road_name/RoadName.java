package de.westnordost.streetcomplete.quests.road_name;

import android.support.annotation.NonNull;

/** Carries the data language code + name in that language */
public class RoadName
{
	@NonNull public String languageCode;
	@NonNull public String name;

	public RoadName(@NonNull String languageCode, @NonNull String name)
	{
		this.languageCode = languageCode;
		this.name = name;
	}
}
