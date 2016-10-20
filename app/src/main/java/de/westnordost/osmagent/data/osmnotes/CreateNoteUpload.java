package de.westnordost.osmagent.data.osmnotes;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.data.QuestStatus;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.data.BoundingBox;
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

			uploadCreateNote(createNote);
		}
	}

	private void uploadCreateNote(CreateNote n)
	{
		Note newNote = createOrCommentNote(n);

		createNoteDB.delete(n.id);
		if (newNote != null)
		{
			// add a hidden quest as a blocker so that at this location no quests are created.
			// if the note was not added, don't do this (see below) -> probably based on old data
			OsmNoteQuest noteQuest = new OsmNoteQuest(newNote);
			noteQuest.setStatus(QuestStatus.HIDDEN);
			noteDB.put(newNote);
			noteQuestDB.add(noteQuest);
		}
	}

	/** Create a note at the given position, or, if there is already a note at the exact same
	 *  position AND its associated element is the same, adds the user's message as another comment.
	 *
	 *  Returns null and does not add the note comment if that note has already been closed because
	 *  the contribution is very likely obsolete (based on old data)*/
	private Note createOrCommentNote(CreateNote n)
	{
		if(hasAssociatedElement(n))
		{
			Note oldNote = findAlreadyExistingNoteWithSameAssociatedElement(n);

			if(oldNote != null)
			{
				if(oldNote.isOpen())
				{
					try
					{
						return osmDao.comment(oldNote.id, n.text);
					}
					catch (OsmConflictException e)
					{
						// has been closed in the meantime
						return null;
					}
				}
				else
				{
					return null;
				}
			}
		}
		return osmDao.create(n.position, n.text + getAssociatedElementString(n));
	}

	private Note findAlreadyExistingNoteWithSameAssociatedElement(final CreateNote newNote)
	{
		SingleElementHandler<Note> handler = new SingleElementHandler<Note>()
		{
			@Override public void handle(Note oldNote)
			{
				if(hasAssociatedElement(newNote))
				{
					String firstCommentText = oldNote.comments.get(0).text;

					if(firstCommentText.matches(getAssociatedElementRegex(newNote)))
					{
						super.handle(oldNote);
					}
				}
			}
		};
		osmDao.getAll(new BoundingBox(
				newNote.position.getLatitude(), newNote.position.getLongitude(),
				newNote.position.getLatitude(), newNote.position.getLongitude()
		), handler, 10, 0);
		return handler.get();
	}

	private static boolean hasAssociatedElement(CreateNote n)
	{
		return n.elementType != null && n.elementId != null;
	}

	private static String getAssociatedElementRegex(CreateNote n)
	{
		String elementType = n.elementType.name();
		return "(?i:.*"+elementType+"\\s*#"+n.elementId+".*)";
	}

	private static String getAssociatedElementString(CreateNote n)
	{
		return " (" + n.elementType.name().toLowerCase(Locale.UK) + " #" + n.elementId + " )";
	}
}
