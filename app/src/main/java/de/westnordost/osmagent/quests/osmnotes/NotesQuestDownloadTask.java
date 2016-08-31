package de.westnordost.osmagent.quests.osmnotes;

import javax.inject.Inject;

import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.notes.NotesDao;

public class NotesQuestDownloadTask implements Runnable
{
	private static final int MAX_NOTES_RETRIEVE = 100;

	@Inject NotesDao osmDao;
	@Inject OsmNoteQuestDao questDao;
	@Inject NoteDao noteDB;

	public Long osmUserId;
	public BoundingBox bbox;

	@Override public void run()
	{
		if(bbox == null) throw new IllegalStateException("Bounding box must be set");

		osmDao.getAll(bbox, new NoteHandler(), MAX_NOTES_RETRIEVE, 0);
	}

	private class NoteHandler implements Handler<Note>
	{
		@Override public void handle(Note note)
		{
			// the last comment is from this user? Ignore this note, user already contributed to it
			NoteComment lastComment = note.comments.get(note.comments.size() - 1);
			if (osmUserId != null && lastComment.user != null && lastComment.user.id == osmUserId)
			{
				return;
			}

			noteDB.put(note);
			OsmNoteQuest quest = new OsmNoteQuest(note);
			questDao.add(quest);
		}
	}
}
