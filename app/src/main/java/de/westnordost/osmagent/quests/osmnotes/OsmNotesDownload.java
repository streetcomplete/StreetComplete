package de.westnordost.osmagent.quests.osmnotes;

import android.util.LongSparseArray;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.QuestListener;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.notes.NotesDao;

public class OsmNotesDownload
{
	private final NotesDao noteServer;
	private final NoteDao noteDB;
	private final OsmNoteQuestDao noteQuestDB;
	private final CreateNoteDao createNoteDB;

	private QuestListener questListener;

	private int visibleAmount;

	@Inject public OsmNotesDownload(
			NotesDao noteServer, NoteDao noteDB, OsmNoteQuestDao noteQuestDB,
			CreateNoteDao createNoteDB)
	{
		this.noteServer = noteServer;
		this.noteDB = noteDB;
		this.noteQuestDB = noteQuestDB;
		this.createNoteDB = createNoteDB;
	}

	public void setQuestListener(QuestListener questListener)
	{
		this.questListener = questListener;
	}

	public int getVisibleQuestsRetrieved()
	{
		return visibleAmount;
	}

	public Set<LatLon> download(final BoundingBox bbox, final Long userId, int max)
	{
		visibleAmount = 0;
		final Set<LatLon> positions = new HashSet<>();

		final LongSparseArray<OsmNoteQuest> oldQuestsByNoteId = new LongSparseArray<>();
		for(OsmNoteQuest quest : noteQuestDB.getAll(bbox, null))
		{
			oldQuestsByNoteId.put(quest.getNote().id, quest);
		}

		noteServer.getAll(bbox, new Handler<Note>()
		{
			@Override public void handle(Note note)
			{
				OsmNoteQuest quest = new OsmNoteQuest(note);

				positions.add(note.position);

					/* hide a note if he already contributed to it. This can also happen from outside
					   this application, which is why we need to overwrite its quest status */
				if(containsCommentFromUser(userId, note))
				{
					quest.setStatus(QuestStatus.HIDDEN);
					noteDB.put(note);
					noteQuestDB.replace(quest);
				}
				else
				{
					noteDB.put(note);
					if(noteQuestDB.add(quest))
					{
						if(questListener != null)
						{
							questListener.onQuestCreated(quest);
						}
					}
					visibleAmount++;
				}

				oldQuestsByNoteId.remove(quest.getNote().id);
			}
		}, max, 0);

		/* delete note quests created in a previous run in the given bounding box that are not
		   found again -> these notes have been closed/solved/removed */
		removeObsoleteNoteQuests(oldQuestsByNoteId);

		for(CreateNote createNote : createNoteDB.getAll(bbox))
		{
			positions.add(createNote.position);
		}

		return positions;
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

	private void removeObsoleteNoteQuests(LongSparseArray<OsmNoteQuest> oldQuests)
	{
		if(oldQuests.size() > 0)
		{

			for (int i=0; i<oldQuests.size(); ++i)
			{
				OsmNoteQuest quest = oldQuests.valueAt(i);
				if(noteQuestDB.delete(quest.getId()))
				{
					if(questListener != null)
					{
						questListener.onQuestRemoved(quest);
					}
				}

			}
			noteDB.deleteUnreferenced();
		}
	}
}
