package de.westnordost.osmagent.quests.osmnotes;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NotesDao;

// TODO test case
public class CreateNoteUpload
{
	private final NotesDao osmDao;
	private final CreateNoteDao createNoteDB;
	private final NoteDao noteDB;
	private final OsmNoteQuestDao noteQuestDB;

	@Inject public CreateNoteUpload(
			CreateNoteDao createNoteDB, NotesDao osmDao, NoteDao noteDB, OsmNoteQuestDao noteQuestDB)
	{
		this.createNoteDB = createNoteDB;
		this.noteQuestDB = noteQuestDB;
		this.noteDB = noteDB;
		this.osmDao = osmDao;
	}

	public void upload(AtomicBoolean cancelState)
	{
		for(CreateNote createNote : createNoteDB.getAll(null))
		{
			if(cancelState.get()) break;

			Note newNote = osmDao.create(createNote.position, createNote.text);

			createNoteDB.delete(createNote.id);
			// add a hidden quest as a blocker so that at this location no quests are created
			OsmNoteQuest noteQuest = new OsmNoteQuest(newNote);
			noteQuest.setStatus(QuestStatus.HIDDEN);
			noteDB.put(newNote);
			noteQuestDB.add(noteQuest);
		}

	}
}
