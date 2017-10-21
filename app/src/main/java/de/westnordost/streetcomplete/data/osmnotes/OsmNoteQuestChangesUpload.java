package de.westnordost.streetcomplete.data.osmnotes;

import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.util.ImageUploader;

public class OsmNoteQuestChangesUpload
{
	private static final String TAG = "NoteCommentUpload";

	private final NotesDao osmDao;
	private final OsmNoteQuestDao questDB;
	private final NoteDao noteDB;
	private final ImageUploader imageUploader;

	@Inject public OsmNoteQuestChangesUpload(NotesDao osmDao, OsmNoteQuestDao questDB,
											 NoteDao noteDB, ImageUploader imageUploader)
	{
		this.osmDao = osmDao;
		this.questDB = questDB;
		this.noteDB = noteDB;
		this.imageUploader = imageUploader;
	}

	public void upload(AtomicBoolean cancelState)
	{
		int created = 0, obsolete = 0;
		for(OsmNoteQuest quest : questDB.getAll(null, QuestStatus.ANSWERED))
		{
			if(cancelState.get()) break;

			if(uploadNoteChanges(quest) != null)
			{
				created++;
			}
			else
			{
				obsolete++;
			}
		}
		String logMsg = "Commented on " + created + " notes";
		if(obsolete > 0)
		{
			logMsg += " but dropped " + obsolete + " comments because the notes have already been closed";
		}
		Log.i(TAG, logMsg);
	}

	Note uploadNoteChanges(OsmNoteQuest quest)
	{
		String text = quest.getComment();

		try
		{
			if (quest.getImagePaths() != null)
			{
				List<String> urls = imageUploader.upload(quest.getImagePaths());
				if(urls != null)
				{
					text += "\n" + getAttachedImagesUrls(urls);
				}
			}
			Note newNote = osmDao.comment(quest.getNote().id, text);

			/* Unlike OSM quests, note quests are never deleted when the user contributed to it
			   but must remain in the database with the status CLOSED as long as they are not
			   solved. The reason is because as long as a note is unsolved, the problem at that
			   position persists and thus it should still block other quests to be created.
			   (Reminder: Note quests block other quests)
			  */
			// so, not this: questDB.delete(quest.getId());
			quest.setStatus(QuestStatus.CLOSED);
			quest.setNote(newNote);
			questDB.update(quest);
			noteDB.put(newNote);
			deleteNoteImages(quest.getImagePaths());

			return newNote;
		}
		catch(OsmConflictException e)
		{
			// someone else already closed the note -> our contribution is probably worthless. Delete
			questDB.delete(quest.getId());
			noteDB.delete(quest.getNote().id);
			deleteNoteImages(quest.getImagePaths());

			Log.i(TAG, "Dropped the comment " + getNoteQuestStringForLog(quest) +
					" because the note has already been closed");

			return null;
		}
	}

	private void deleteNoteImages(List<String> imagePaths)
	{
		for (String path : imagePaths)
		{
			File file = new File(path);
			if (file.exists())
			{
				file.delete();
			}
		}
	}

	private static String getNoteQuestStringForLog(OsmNoteQuest n)
	{
		LatLon pos = n.getMarkerLocation();
		return "\"" + n.getComment() + "\" at " + pos.getLatitude() + ", " + pos.getLongitude();
	}

	private static String getAttachedImagesUrls(List<String> imageLinks)
	{
		StringBuilder sb = new StringBuilder("Attached photo(s):\n");
		for(String link : imageLinks)
		{
			sb.append(link).append("\n");
		}
		return sb.toString();
	}
}
