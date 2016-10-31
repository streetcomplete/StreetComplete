package de.westnordost.osmagent.data.osm;

import android.os.Bundle;

import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmapi.map.data.Element;

public interface OsmElementQuestType extends QuestType
{
	/** @return whether the given element matches with this quest type */
	boolean appliesTo(Element element);

	/** applies the data from answer to the given element */
	void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes);

	/** Android resource id to use for the commit message when this quest is solved */
	int getCommitMessageResourceId();
}
