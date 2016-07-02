package de.westnordost.osmagent.quests.types;

import android.app.DialogFragment;
import android.os.Bundle;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

public interface QuestType
{
	/** @return a query string that is accepted by Overpass and does not exceed the given bbox */
	String getOverpassQuery(BoundingBox bbox);

	/** @return whether the given element matches with this quest type */
	boolean appliesTo(Element element);

	/** @return some kind of sort order for quest types, see QuestImportance */
	int importance();

	/** @return the dialog in which the user can add the data */
	DialogFragment getDialog();

	/** applies the data from answer to the given element */
	void applyAnswerTo(Bundle answer, Element element);
}
