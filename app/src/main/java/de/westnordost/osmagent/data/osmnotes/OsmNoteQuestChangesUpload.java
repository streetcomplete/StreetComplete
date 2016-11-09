package de.westnordost.osmagent.data.osmnotes;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.data.QuestStatus;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NotesDao;

// TODO test case
public class OsmNoteQuestChangesUpload
{
	private final NotesDao osmDao;
	private final OsmNoteQuestDao questDB;
	private final NoteDao noteDB;

	@Inject public OsmNoteQuestChangesUpload(NotesDao osmDao, OsmNoteQuestDao questDB, NoteDao noteDB)
	{
		this.osmDao = osmDao;
		this.questDB = questDB;
		this.noteDB = noteDB;
	}

	public void upload(AtomicBoolean cancelState)
	{
		for(OsmNoteQuest quest : questDB.getAll(null, QuestStatus.ANSWERED))
		{
			if(cancelState.get()) break;

			uploadNoteChanges(quest);
		}
	}

	Note uploadNoteChanges(OsmNoteQuest quest)
	{
		String text = quest.getComment();

		try
		{
			Note newNote = osmDao.comment(quest.getNote().id, text);

			if(newNote != null)
			{
				/* Unlike OSM quests, note quests are not deleted when the user contributed to it
				   but must remain in the database with the status HIDDEN as long as they are not
				   solved. The reason is because as long as a note is unsolved, the problem at that
				   position persists and thus it should still block other quests to be created.
				   (Reminder: Note quests block other quests)
				  */
				// so, not this: questDB.delete(quest.getId());
				quest.setStatus(QuestStatus.HIDDEN);
				questDB.update(quest);
				noteDB.put(quest.getNote());
			}
			return newNote;
		}
		catch(OsmConflictException e)
		{
			// someone else already closed the note -> our contribution is probably worthless. Delete
			questDB.delete(quest.getId());
			noteDB.delete(quest.getNote().id);

			return null;
		}
	}
}
