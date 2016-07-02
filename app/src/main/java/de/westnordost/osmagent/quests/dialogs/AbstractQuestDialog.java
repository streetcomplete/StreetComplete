package de.westnordost.osmagent.quests.dialogs;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

/** Abstract base class for any dialog that makes quest related callbacks to the activity via the
 *  QuestDialogListener */
public class AbstractQuestDialog extends DialogFragment
{
	protected QuestDialogListener callbackListener;

	public static final String QUEST_ID = "questId";
	protected int questId;

	@Override
	public void onCreate(Bundle inState)
	{
		super.onCreate(inState);

		Bundle bundle;
		if(inState != null)
		{
			bundle = inState;
		}
		else
		{
			bundle = getArguments();
		}

		if(bundle == null || bundle.getInt(QUEST_ID, -1) == -1)
		{
			throw new IllegalStateException("You need to pass a " +	QUEST_ID + " as an argument.");
		}

		questId = bundle.getInt(QUEST_ID);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState)
	{
		outState.putInt(QUEST_ID, questId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			callbackListener = (QuestDialogListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement QuestDialogListener");
		}
	}

}
