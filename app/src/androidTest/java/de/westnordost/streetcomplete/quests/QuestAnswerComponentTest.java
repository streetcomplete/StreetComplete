package de.westnordost.streetcomplete.quests;

import android.os.Bundle;

import junit.framework.TestCase;

import de.westnordost.streetcomplete.data.QuestGroup;

public class QuestAnswerComponentTest extends TestCase
{
	public void testGetSet()
	{
		QuestAnswerComponent c1 = new QuestAnswerComponent();
		c1.onCreate(QuestAnswerComponent.createArguments(11, QuestGroup.OSM));

		assertEquals(QuestGroup.OSM, c1.getQuestGroup());
		assertEquals(11, c1.getQuestId());

		QuestAnswerComponent c2 = new QuestAnswerComponent();
		c2.onCreate(c1.getArguments());

		assertEquals(c2.getQuestGroup(), c1.getQuestGroup());
		assertEquals(c2.getQuestId(), c1.getQuestId());
	}

	public void testListener()
	{
		QuestAnswerComponent c1 = new QuestAnswerComponent();

		final int expectQuestId = 3;
		final QuestGroup expectGroup = QuestGroup.OSM_NOTE;
		final String expectNote = "test";
		final String expectQuestTitle = "What?";
		final Bundle expectBundle = new Bundle();
		expectBundle.putString("A","B");

		c1.onAttach(new OsmQuestAnswerListener()
		{
			@Override public void onAnsweredQuest(long questId, QuestGroup group, Bundle answer)
			{
				assertEquals(expectQuestId, questId);
				assertEquals(expectGroup, group);
				assertEquals(expectBundle, answer);
			}

			@Override public void onLeaveNote(long questId, QuestGroup group, String questTitle,
											  String note)
			{
				assertEquals(expectQuestId, questId);
				assertEquals(expectGroup, group);
				assertEquals(expectNote, note);
				assertEquals(expectQuestTitle, questTitle);
			}

			@Override public void onSkippedQuest(long questId, QuestGroup group)
			{
				assertEquals(expectQuestId, questId);
				assertEquals(expectGroup, group);
			}
		});

		c1.onCreate(QuestAnswerComponent.createArguments(expectQuestId, expectGroup));
		c1.onLeaveNote(expectQuestTitle, expectNote);
		c1.onAnswerQuest(expectBundle);
		c1.onSkippedQuest();
	}
}
