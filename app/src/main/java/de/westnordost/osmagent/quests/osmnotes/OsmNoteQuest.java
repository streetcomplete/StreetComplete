package de.westnordost.osmagent.quests.osmnotes;

import android.app.DialogFragment;

import java.util.Date;

import de.westnordost.osmagent.quests.Quest;
import de.westnordost.osmagent.quests.QuestImportance;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.QuestType;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuest implements Quest
{

	public OsmNoteQuest(Note note)
	{
		this(note, QuestStatus.NEW, null, new Date());
	}

	public OsmNoteQuest(Note note, QuestStatus status, NoteChange changes, Date lastUpdate)
	{
		this.note = note;
		this.status = status;
		this.changes = changes;
		this.lastUpdate = lastUpdate;
	}

	private Date lastUpdate;
	private QuestStatus status;
	private Note note;
	private NoteChange changes;
	private static QuestType type = new NoteQuestType();

	@Override public QuestType getType()
	{
		return type;
	}

	@Override public QuestStatus getStatus()
	{
		return status;
	}

	@Override public void setStatus(QuestStatus status)
	{
		this.status = status;
	}

	@Override public Long getId()
	{
		return note.id;
	}

	@Override public LatLon getMarkerLocation()
	{
		return note.position;
	}

	public Note getNote()
	{
		return note;
	}

	public void setNote(Note note)
	{
		this.note = note;
	}

	public NoteChange getChanges()
	{
		return changes;
	}

	public void setChange(NoteChange changes)
	{
		this.changes = changes;
	}

	public Date getLastUpdate()
	{
		return lastUpdate;
	}

	private static class NoteQuestType implements QuestType
	{
		@Override public int importance()
		{
			return QuestImportance.NOTE;
		}

		@Override public DialogFragment getDialog()
		{
			// TODO create dialog for viewing a note and leaving a comment (+ closing it?)
			return null;
		}
	}
}
