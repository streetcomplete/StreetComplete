package de.westnordost.osmagent.quests.types;

import android.app.DialogFragment;
import android.os.Bundle;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

public interface QuestType
{
	/** @return some kind of sort order for quest types, see QuestImportance */
	int importance();

	/** @return the dialog in which the user can add the data */
	DialogFragment getDialog();
}
