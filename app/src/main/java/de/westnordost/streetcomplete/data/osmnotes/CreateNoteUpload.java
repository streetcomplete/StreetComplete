package de.westnordost.streetcomplete.data.osmnotes;

import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.osmapi.common.SingleElementHandler;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener;
import de.westnordost.streetcomplete.util.ImageUploader;

public class CreateNoteUpload
{
	private static final String TAG = "CreateNoteUpload";

	private final NotesDao osmDao;
	private final CreateNoteDao createNoteDB;
	private final NoteDao noteDB;
	private final OsmNoteQuestDao noteQuestDB;
	private final MapDataDao mapDataDao;
	private final OsmNoteQuestType questType;
	private final QuestStatisticsDao statisticsDB;
	private final ImageUploader imageUploader;
	private OnUploadedChangeListener uploadedChangeListener;

	@Inject public CreateNoteUpload(
			CreateNoteDao createNoteDB, NotesDao osmDao, NoteDao noteDB,
			OsmNoteQuestDao noteQuestDB, MapDataDao mapDataDao, OsmNoteQuestType questType,
			QuestStatisticsDao statisticsDB, ImageUploader imageUploader)
	{
		this.createNoteDB = createNoteDB;
		this.noteQuestDB = noteQuestDB;
		this.noteDB = noteDB;
		this.osmDao = osmDao;
		this.mapDataDao = mapDataDao;
		this.questType = questType;
		this.statisticsDB = statisticsDB;
		this.imageUploader = imageUploader;
	}

	public synchronized void setProgressListener(OnUploadedChangeListener uploadedChangeListener)
	{
		this.uploadedChangeListener = uploadedChangeListener;
	}

	public synchronized void upload(AtomicBoolean cancelState)
	{
		int created = 0, obsolete = 0;
		for(CreateNote createNote : createNoteDB.getAll(null))
		{
			if(cancelState.get()) break;

			if(uploadCreateNote(createNote) != null)
			{
				uploadedChangeListener.onUploaded();
				created++;
			}
			else
			{
				uploadedChangeListener.onDiscarded();
				obsolete++;
			}
		}
		String logMsg = "Created " + created + " notes";
		if(obsolete > 0)
		{
			logMsg += " but dropped " + obsolete + " because they were obsolete already";
		}
		Log.i(TAG, logMsg);
	}

	Note uploadCreateNote(CreateNote n)
	{
		if(isAssociatedElementDeleted(n))
		{
			Log.i(TAG, "Dropped to be created note " + getCreateNoteStringForLog(n) +
					" because the associated element has already been deleted");
			deleteNote(n);
			return null;
		}

		Note newNote = createOrCommentNote(n);

		if (newNote != null)
		{
			// add a closed quest as a blocker so that at this location no quests are created.
			// if the note was not added, don't do this (see below) -> probably based on old data
			OsmNoteQuest noteQuest = new OsmNoteQuest(newNote, questType);
			noteQuest.setStatus(QuestStatus.CLOSED);
			noteDB.put(newNote);
			noteQuestDB.add(noteQuest);
			statisticsDB.addOneNote();
		}
		else
		{
			Log.i(TAG, "Dropped a to be created note " + getCreateNoteStringForLog(n) +
					" because a note with the same associated element has already been closed");
			// so the problem has likely been solved by another mapper
		}

		deleteNote(n);

		return newNote;
	}

	private void deleteNote(CreateNote n)
	{
		createNoteDB.delete(n.id);
		AttachPhotoUtils.deleteImages(n.imagePaths);
	}

	private static String getCreateNoteStringForLog(CreateNote n)
	{
		return "\"" + n.text + "\" at " + n.position.getLatitude() + ", " + n.position.getLongitude();
	}

	private boolean isAssociatedElementDeleted(CreateNote n)
	{
		return n.hasAssociatedElement() && retrieveElement(n) == null;
	}

	private Element retrieveElement(CreateNote n)
	{
		switch(n.elementType)
		{
			case NODE: return mapDataDao.getNode(n.elementId);
			case WAY:  return mapDataDao.getWay(n.elementId);
			case RELATION: return mapDataDao.getRelation(n.elementId);
		}
		return null;
	}

	/** Create a note at the given position, or, if there is already a note at the exact same
	 *  position AND its associated element is the same, adds the user's message as another comment.
	 *
	 *  Returns null and does not add the note comment if that note has already been closed because
	 *  the contribution is very likely obsolete (based on old data)*/
	private Note createOrCommentNote(CreateNote n)
	{
		if(n.hasAssociatedElement())
		{
			Note oldNote = findAlreadyExistingNoteWithSameAssociatedElement(n);
			if(oldNote != null)
			{
				return commentNote(oldNote, n.text, n.imagePaths);
			}
		}
		return createNote(n);
	}

	private Note createNote(CreateNote n)
	{
		String text = getCreateNoteText(n);
		text += AttachPhotoUtils.uploadAndGetAttachedPhotosText(imageUploader, n.imagePaths);
		Note result = osmDao.create(n.position, text);
		imageUploader.activate(result.id);
		return result;
	}

	private Note commentNote(Note note, String text, List<String> attachedImagePaths)
	{
		if(note.isOpen())
		{
			try
			{
				text += AttachPhotoUtils.uploadAndGetAttachedPhotosText(imageUploader, attachedImagePaths);
				Note result = osmDao.comment(note.id, text);
				imageUploader.activate(result.id);
				return result;
			}
			catch (OsmConflictException e)
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	private static String getCreateNoteText(CreateNote note)
	{
		if(note.hasAssociatedElement())
		{
			if(note.questTitle != null)
			{
				return "Unable to answer \"" + note.questTitle + "\"" +
						" for " + getAssociatedElementString(note) +
						" via "+ ApplicationConstants.USER_AGENT+":\n\n" + note.text;
			}
			else
			{
				return "for " + getAssociatedElementString(note) +
						" via "+ ApplicationConstants.USER_AGENT+":\n\n" + note.text;
			}
		}
		return note.text + "\n\nvia "+ ApplicationConstants.USER_AGENT;
	}

	private Note findAlreadyExistingNoteWithSameAssociatedElement(final CreateNote newNote)
	{
		SingleElementHandler<Note> handler = new SingleElementHandler<Note>()
		{
			@Override public void handle(Note oldNote)
			{
				if(newNote.hasAssociatedElement())
				{
					String firstCommentText = oldNote.comments.get(0).text;
					String newNoteRegex = getAssociatedElementRegex(newNote);
					if(firstCommentText.matches(newNoteRegex))
					{
						super.handle(oldNote);
					}
				}
			}
		};
		final int hideClosedNoteAfter = 7;
		osmDao.getAll(new BoundingBox(
				newNote.position.getLatitude(), newNote.position.getLongitude(),
				newNote.position.getLatitude(), newNote.position.getLongitude()
		), handler, 10, hideClosedNoteAfter);
		return handler.get();
	}

	private static String getAssociatedElementRegex(CreateNote n)
	{
		String elementType = n.elementType.name();
		// before 0.11 - i.e. "way #123"
		String oldStyleRegex = elementType+"\\s*#"+n.elementId;
		// i.e. www.openstreetmap.org/way/123
		String newStyleRegex = "(osm|openstreetmap)\\.org\\/"+elementType+"\\/"+n.elementId;
		// i: turns on case insensitive regex, s: newlines are also captured with "."
		return "(?is).*(("+oldStyleRegex+")|("+newStyleRegex+")).*";
	}

	static String getAssociatedElementString(CreateNote n)
	{
		if(!n.hasAssociatedElement()) return null;

		String elementName = n.elementType.name().toLowerCase(Locale.UK);
		return "https://osm.org/" + elementName + "/" + n.elementId;
	}
}
