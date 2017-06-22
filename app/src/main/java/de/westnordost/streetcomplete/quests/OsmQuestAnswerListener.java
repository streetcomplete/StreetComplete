package de.westnordost.streetcomplete.quests;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.QuestGroup;

public interface OsmQuestAnswerListener
{
	/** Called when the user answered the quest with the given id. What is in the bundle, is up to
	 *  the dialog with which the quest was answered */
	void onAnsweredQuest(long questId, QuestGroup group, Bundle answer);

	/** Called when the user did not answer the quest with the given id but instead left a note */
	void onLeaveNote(long questId, QuestGroup group, String questTitle, String note);

	/** Called when the user chose to skip the quest */
	void onSkippedQuest(long questId, QuestGroup group);
}
