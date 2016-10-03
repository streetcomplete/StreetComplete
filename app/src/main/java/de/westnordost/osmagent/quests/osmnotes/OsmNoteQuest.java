package de.westnordost.osmagent.quests.osmnotes;

import android.app.DialogFragment;

import java.util.Date;

import de.westnordost.osmagent.quests.Quest;
import de.westnordost.osmagent.quests.QuestImportance;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.QuestType;
import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuest implements Quest
{

	/** A new note quest treated as answered to have a note created with the given text at the given
	 * position */
	public OsmNoteQuest(LatLon position, String text)
	{
		this(null, null, QuestStatus.ANSWERED, new NoteChange(position, text), new Date());
	}

	/** A new note quest for having the user contribute to the note discussion */
	public OsmNoteQuest(Note note)
	{
		this(null, note, QuestStatus.NEW, null, new Date());
	}

	/** Complete constructor */
	public OsmNoteQuest(Long id, Note note, QuestStatus status, NoteChange changes, Date lastUpdate)
	{
		this.id = id;
		this.note = note;
		this.status = status;
		this.changes = changes;
		this.lastUpdate = lastUpdate;
	}

	private Long id;
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
		return id;
	}

	@Override public LatLon getMarkerLocation()
	{
		return note != null ? note.position : changes.position;
		// notes with neither note.position nor changes.position set should not exist
	}

	@Override public ElementGeometry getGeometry()
	{
		// no geometry other than the marker location
		return null;
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

	public void setId(long id)
	{
		this.id = id;
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
