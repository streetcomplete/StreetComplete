package de.westnordost.streetcomplete.quests;

import android.os.Bundle;

import java.util.ArrayList;

import de.westnordost.streetcomplete.data.QuestGroup;

public class QuestAnswerComponent
{
	private static final String ARG_QUEST_ID = "questId";
	private static final String ARG_QUEST_GROUP = "questGroup";

	private OsmQuestAnswerListener callbackListener;

	private long questId;
	private QuestGroup questGroup;

	public static Bundle createArguments(long questId, QuestGroup group)
	{
		Bundle args = new Bundle();
		args.putLong(ARG_QUEST_ID, questId);
		args.putString(ARG_QUEST_GROUP, group.name());
		return args;
	}

	public Bundle getArguments()
	{
		return createArguments(questId, questGroup);
	}

	public void onCreate(Bundle arguments)
	{
		if(arguments == null || arguments.getLong(ARG_QUEST_ID, -1) == -1 ||
				arguments.getString(ARG_QUEST_GROUP, null) == null)
		{
			throw new IllegalStateException("Use QuestAnswerComponent.createArguments and pass the " +
					"created bundle as an argument.");
		}

		questId = arguments.getLong(ARG_QUEST_ID);
		questGroup = QuestGroup.valueOf(arguments.getString(ARG_QUEST_GROUP));
	}

	public void onAttach(OsmQuestAnswerListener listener)
	{
		callbackListener = listener;
	}

	public void onAnswerQuest(Bundle answer)
	{
		callbackListener.onAnsweredQuest(questId, questGroup, answer);
	}

	public void onComposeNote(String questTitle)
	{
		callbackListener.onComposeNote(questId, questGroup, questTitle);
	}

	public void onLeaveNote(String questTitle, String text, ArrayList<String> imagePaths)
	{
		callbackListener.onLeaveNote(questId, questGroup, questTitle, text, imagePaths);
	}

	public void onSkippedQuest()
	{
		callbackListener.onSkippedQuest(questId, questGroup);
	}

	public long getQuestId()
	{
		return questId;
	}

	public QuestGroup getQuestGroup()
	{
		return questGroup;
	}
}
