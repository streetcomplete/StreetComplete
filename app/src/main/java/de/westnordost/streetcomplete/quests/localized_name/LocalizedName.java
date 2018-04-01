package de.westnordost.streetcomplete.quests.localized_name;

import android.support.annotation.NonNull;

/** Carries the data language code + name in that language */
public class LocalizedName
{
	@NonNull public String languageCode;
	@NonNull public String name;

	public LocalizedName(@NonNull String languageCode, @NonNull String name)
	{
		this.languageCode = languageCode;
		this.name = name;
	}
}
