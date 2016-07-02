package de.westnordost.osmagent.quests.dialogs;

import android.os.Bundle;

public interface QuestDialogListener
{
	/** Called when the user answered the quest with the given id. What is in the bundle, is up to
	 *  the dialog with which the quest was answered */
	void onAnsweredQuest(int questId, Bundle answer);

	/** Called when the user did not answer the quest with the given id but instead left a note */
	void onLeaveNote(int questId, String note);

	/** Called when the user chose to skip the quest */
	void onSkippedQuest(int questId);
}
