package de.westnordost.osmagent.quests;

import android.app.DialogFragment;

public interface QuestType
{
	/** @return some kind of sort order for quest types, see QuestImportance */
	int importance();

	/** @return the dialog in which the user can add the data */
	DialogFragment getDialog();

	/** @return the name of the icon used to display this quest type on the map */
	String getIconName();
}
