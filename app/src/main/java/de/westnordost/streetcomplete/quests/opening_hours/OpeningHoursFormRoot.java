package de.westnordost.streetcomplete.quests.opening_hours;

import android.os.Parcelable;

/** A view that can be used as a root element to specify opening hours in the opening hours form */
public interface OpeningHoursFormRoot
{
	String getOpeningHoursString();
	Parcelable onSaveInstanceState();
	void onRestoreInstanceState(Parcelable state);
}
