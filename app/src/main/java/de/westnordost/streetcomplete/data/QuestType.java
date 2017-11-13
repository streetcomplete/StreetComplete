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

	/** @return the string resource id that explains why this quest is disabled by default or zero
	 *          if it is not disabled by default */
	int getDefaultDisabledMessage();
}
