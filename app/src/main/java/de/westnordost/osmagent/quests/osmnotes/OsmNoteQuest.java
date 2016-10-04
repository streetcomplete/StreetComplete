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
	public OsmNoteQuest(Note note)
	{
		this(null, note, QuestStatus.NEW, null, new Date());
	}

	public OsmNoteQuest(Long id, Note note, QuestStatus status, String comment, Date lastUpdate)
	{
		this.id = id;
		this.note = note;
		this.status = status;
		this.comment = comment;
		this.lastUpdate = lastUpdate;
	}

	private Long id;
	private Date lastUpdate;
	private QuestStatus status;
	private Note note;

	private String comment;

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
		/* if it is hidden, clear notes comments because we do not need them anymore and they take
		 up (a lot of) space in the DB */
		if(!status.isVisible())
		{
			if (note != null) note.comments.clear();
		}
	}

	@Override public Long getId()
	{
		return id;
	}

	@Override public LatLon getMarkerLocation()
	{
		return note.position;
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

	public String getComment()
	{
		return comment;
	}

	public void setComment(String changes)
	{
		this.comment = comment;
	}

	public Date getLastUpdate()
	{
		return lastUpdate;
	}

	@Override public void setId(long id)
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
