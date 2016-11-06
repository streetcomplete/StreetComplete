package de.westnordost.osmagent.quests;

import android.os.Bundle;

import de.westnordost.osmagent.data.QuestGroup;

public class QuestAnswerComponent
{
	private static final String QUEST_ID = "questId";
	private static final String QUEST_GROUP = "questGroup";

	private OsmQuestAnswerListener callbackListener;

	private long questId;
	private QuestGroup questGroup;

	public static Bundle createArguments(long questId, QuestGroup group)
	{
		Bundle args = new Bundle();
		args.putLong(QUEST_ID, questId);
		args.putString(QUEST_GROUP, group.name());
		return args;
	}

	public Bundle getArguments()
	{
		return createArguments(questId, questGroup);
	}

	public void onCreate(Bundle arguments)
	{
		if(arguments == null || arguments.getLong(QUEST_ID, -1) == -1 ||
				arguments.getString(QUEST_GROUP, null) == null)
		{
			throw new IllegalStateException("Use QuestAnswerComponent.createArguments and pass the" +
					"created bundle as an argument.");
		}

		questId = arguments.getLong(QUEST_ID);
		questGroup = QuestGroup.valueOf(arguments.getString(QUEST_GROUP));
	}

	public void onAttach(OsmQuestAnswerListener listener)
	{
		callbackListener = listener;
	}

	public void onAnswerQuest(Bundle answer)
	{
		callbackListener.onAnsweredQuest(questId, questGroup, answer);
	}

	public void onLeaveNote(String text)
	{
		callbackListener.onLeaveNote(questId, questGroup, text);
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
