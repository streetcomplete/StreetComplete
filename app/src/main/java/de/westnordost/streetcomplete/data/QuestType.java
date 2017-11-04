package de.westnordost.streetcomplete.data;

import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public interface QuestType
{
	/** @return the dialog in which the user can add the data */
	AbstractQuestAnswerFragment createForm();

	/** @return the icon resource id used to display this quest type on the map */
	int getIcon();

	/** @return the title resource id used to display the quest's question */
	int getTitle();

	/** @return whether the quest is downloaded and is visible by default. Quests that are not
	 *          enabled by default will not be shown to any user unless that user explicitly
	 *          enabled this quest in the settings */
	boolean isDefaultEnabled();
}
