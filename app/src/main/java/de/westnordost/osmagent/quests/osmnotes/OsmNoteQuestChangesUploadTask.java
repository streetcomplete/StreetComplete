package de.westnordost.osmagent.quests.osmnotes;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.statistics.QuestStatisticsDao;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.notes.NotesDao;

public class OsmNoteQuestChangesUploadTask implements Runnable
{
	@Inject NotesDao osmDao;
	@Inject OsmNoteQuestDao questDB;
	@Inject QuestStatisticsDao statisticsDB;

	public Long questId;
	public Long osmUserId;

	@Override public void run()
	{
		if(questId == null) throw new IllegalStateException("Quest must be set");

		OsmNoteQuest quest = questDB.get(questId);
		if(quest == null || quest.getStatus() != QuestStatus.ANSWERED)
		{
			return;
		}

		boolean success = uploadNoteChanges(quest);

		if(success)
		{
			statisticsDB.increase(quest.getType());
		}

		questDB.delete(quest.getId());
	}

	private boolean uploadNoteChanges(OsmNoteQuest quest)
	{
		String text = quest.getChanges().text;

		try
		{
			switch (quest.getChanges().action)
			{
				case OPEN:
					osmDao.create(quest.getNote().position, text);
					break;
				case COMMENT:
					osmDao.comment(quest.getNote().id, text);
					break;
				case CLOSE:
					osmDao.close(quest.getNote().id, text);
					break;
			}
		}
		// someone else already closed the note -> our contribution is probably worthless
		catch(OsmConflictException e)
		{
			return false;
		}
		return true;
	}
}
