package de.westnordost.streetcomplete.quests;

import android.os.Bundle;

import junit.framework.TestCase;

import java.util.ArrayList;

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
		final ArrayList<String> expectImagePaths = new ArrayList<>();
		expectImagePaths.add("dings");
		expectImagePaths.add("dongs");

		expectBundle.putString("A","B");

		c1.onAttach(new OsmQuestAnswerListener()
		{
			@Override public void onAnsweredQuest(long questId, QuestGroup group, Bundle answer)
			{
				assertEquals(expectQuestId, questId);
				assertEquals(expectGroup, group);
				assertEquals(expectBundle, answer);
			}

			@Override
			public void onComposeNote(long questId, QuestGroup group, String questTitle)
			{
				assertEquals(expectQuestId, questId);
				assertEquals(expectGroup, group);
				assertEquals(expectQuestTitle, questTitle);
			}

			@Override public void onLeaveNote(long questId, QuestGroup group, String questTitle,
											  String note, ArrayList<String> imagePaths)
			{
				assertEquals(expectQuestId, questId);
				assertEquals(expectGroup, group);
				assertEquals(expectNote, note);
				assertEquals(expectQuestTitle, questTitle);
				assertEquals(expectImagePaths, imagePaths);
			}

			@Override public void onSkippedQuest(long questId, QuestGroup group)
			{
				assertEquals(expectQuestId, questId);
				assertEquals(expectGroup, group);
			}
		});

		c1.onCreate(QuestAnswerComponent.createArguments(expectQuestId, expectGroup));
		c1.onComposeNote(expectQuestTitle);
		c1.onLeaveNote(expectQuestTitle, expectNote, expectImagePaths);
		c1.onAnswerQuest(expectBundle);
		c1.onSkippedQuest();
	}
}
