package de.westnordost.streetcomplete.quests.oneway;

import android.os.Bundle;

import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddOnewayForm extends YesNoQuestAnswerFragment
{
	public static final String WAY_ID = "way_id";

	@Override protected void onClickYesNo(boolean answer)
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(ANSWER, answer);
		// the quest needs the way ID of the element to find out the direction of the oneway
		bundle.putLong(WAY_ID, getOsmElement().getId());
		applyImmediateAnswer(bundle);
	}
}
