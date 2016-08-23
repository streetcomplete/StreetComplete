package de.westnordost.osmagent.quests.types;

import android.os.Bundle;

import de.westnordost.osmapi.map.data.Element;

public interface OsmElementQuestType extends QuestType
{
	/** @return whether the given element matches with this quest type */
	boolean appliesTo(Element element);

	/** applies the data from answer to the given element */
	void applyAnswerTo(Bundle answer, Element element);
}
