package de.westnordost.osmagent.data.osmnotes;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import de.westnordost.osmagent.OsmagentConstants;
import de.westnordost.osmagent.data.QuestStatus;
import de.westnordost.osmagent.Prefs;
import de.westnordost.osmagent.data.tiles.DownloadedTilesDao;
import de.westnordost.osmagent.util.SlippyMapMath;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.notes.NotesDao;

public class OsmNotesDownload
{
	private static final String TAG = "QuestDownload";

	private final NotesDao noteServer;
	private final NoteDao noteDB;
	private final OsmNoteQuestDao noteQuestDB;
	private final CreateNoteDao createNoteDB;
	private final DownloadedTilesDao downloadedTilesDao;
	private final SharedPreferences preferences;

	private VisibleOsmNoteQuestListener listener;

	@Inject public OsmNotesDownload(
			NotesDao noteServer, NoteDao noteDB, OsmNoteQuestDao noteQuestDB,
			CreateNoteDao createNoteDB, DownloadedTilesDao downloadedTilesDao, SharedPreferences preferences)
	{
		this.noteServer = noteServer;
		this.noteDB = noteDB;
		this.noteQuestDB = noteQuestDB;
		this.createNoteDB = createNoteDB;
		this.downloadedTilesDao = downloadedTilesDao;
		this.preferences = preferences;
	}

	public void setQuestListener(VisibleOsmNoteQuestListener listener)
	{
		this.listener = listener;
	}

	public Set<LatLon> download(final Rect tiles, final Long userId, int max)
	{
		BoundingBox bbox = SlippyMapMath.asBoundingBox(tiles, OsmagentConstants.QUEST_TILE_ZOOM);

		final Set<LatLon> positions = new HashSet<>();
		final HashMap<Long, Long> previousQuestsByNoteId = getPreviousQuestsByNoteId(bbox);
		final Collection<Note> notes = new ArrayList<>();
		final Collection<OsmNoteQuest> quests = new ArrayList<>();
		final Collection<OsmNoteQuest> hiddenQuests = new ArrayList<>();

		noteServer.getAll(bbox, new Handler<Note>()
		{
			@Override public void handle(Note note)
			{

				OsmNoteQuest quest = new OsmNoteQuest(note);
				if(isNoteHidden(userId, note))
				{
					quest.setStatus(QuestStatus.HIDDEN);
					hiddenQuests.add(quest);
				}
				else
				{
					quests.add(quest);
					previousQuestsByNoteId.remove(note.id);
				}

				notes.add(note);
				positions.add(note.position);
			}
		}, max, 0);

		noteDB.putAll(notes);
		int hiddenAmount = noteQuestDB.replaceAll(hiddenQuests);
		int newAmount = noteQuestDB.addAll(quests);
		int visibleAmount = quests.size();

		if(listener != null)
		{
			for (OsmNoteQuest quest : quests)
			{
				// it is null if this quest is already in the DB, so don't call onQuestCreated
				if(quest.getId() == null) continue;

				listener.onQuestCreated(quest);
			}
			/* we do not call listener.onNoteQuestRemoved for hiddenQuests here, because on
			*  replacing hiddenQuests into DB, they get new quest IDs. As far as the DB is concerned,
			*  hidden note quests are always new quests which are hidden.
			*  If a note quest was visible before, it'll be removed below when the previous quests
			*  are cleared */
		}

		/* delete note quests created in a previous run in the given bounding box that are not
		   found again -> these notes have been closed/solved/removed */
		if(previousQuestsByNoteId.size() > 0)
		{
			if(listener != null)
			{
				for (Long questId : previousQuestsByNoteId.values())
				{
					listener.onNoteQuestRemoved(questId);
				}
			}

			noteQuestDB.deleteAll(previousQuestsByNoteId.values());
			noteDB.deleteUnreferenced();
		}

		for(CreateNote createNote : createNoteDB.getAll(bbox))
		{
			positions.add(createNote.position);
		}

		int closedAmount = previousQuestsByNoteId.size();

		downloadedTilesDao.putQuestType(tiles, OsmNoteQuest.type.getClass().getSimpleName());

		Log.i(TAG, "Successfully added " + newAmount + " new and removed " + closedAmount +
				" closed notes (" + hiddenAmount + " of " + (hiddenAmount + visibleAmount) +
				" notes are hidden)");

		return positions;
	}

	private HashMap<Long, Long> getPreviousQuestsByNoteId(BoundingBox bbox)
	{
		HashMap<Long, Long> result = new HashMap<>();
		for(OsmNoteQuest quest : noteQuestDB.getAll(bbox, null))
		{
			result.put(quest.getNote().id, quest.getId());
		}
		return result;
	}

	private boolean isNoteHidden(Long userId, Note note)
	{
		boolean result = false;
				/* many notes are created to report problems on the map that cannot be resolved
				 * through an on-site survey rather than questions from other (armchair) mappers
				 * that want something cleared up on-site.
				 * Likely, if something is posed as a question, the reporter expects someone to
				 * answer/comment on it, so let's only show these */
		boolean showNonQuestionNotes = preferences.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false);
		result |= !probablyContainsQuestion(note) && !showNonQuestionNotes;

				/* hide a note if he already contributed to it. This can also happen from outside
				   this application, which is why we need to overwrite its quest status. */
		result |= containsCommentFromUser(userId, note);
		return result;
	}

	private boolean containsCommentFromUser(Long userId, Note note)
	{
		if(userId == null) return false;

		for(NoteComment comment : note.comments)
		{
			if(comment.user != null && comment.user.id == userId)
				return true;
		}
		return false;
	}

	private boolean probablyContainsQuestion(Note note)
	{
		/* from left to right (if smartass IntelliJ wouldn't mess up left-to-right):
		   - latin question mark
		   - greek question mark (a different character than semikolon, though same appearance)
		   - semikolon (often used instead of proper greek question mark)
		   - mirrored question mark (used in script written from right to left, like Arabic)
		   - armenian question mark
		   - ethopian question mark
		   - full width question mark (often used in modern Chinese / Japanese)
		   (Source: https://en.wikipedia.org/wiki/Question_mark)

			NOTE: some languages, like Thai, do not use any question mark, so this would be more
			difficult to determine.
	   */
		String questionMarksAroundTheWorld = "[?;;؟՞፧？]";

		String text = note.comments.get(0).text;
		return text.matches(".*" + questionMarksAroundTheWorld + ".*");
	}
}