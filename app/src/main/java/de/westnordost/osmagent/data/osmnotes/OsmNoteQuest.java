package de.westnordost.osmagent.data.osmnotes;

import java.util.Date;

import de.westnordost.osmagent.data.Quest;
import de.westnordost.osmagent.data.QuestImportance;
import de.westnordost.osmagent.data.QuestStatus;
import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmagent.dialogs.AbstractQuestAnswerFragment;
import de.westnordost.osmagent.dialogs.note_discussion.NoteDiscussionForm;
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
		// NOTE: using the same method as in CreateNote, we could actually get the ElementGeometry
		// here. However, to make users answer notes that other users created, barely makes sense
		// (otherwise they could probably answer it themselves), so any notes created by this app
		// will/should likely not show up for other users of this app

		// no geometry other than the marker location
		return new ElementGeometry(getMarkerLocation());
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

		@Override public AbstractQuestAnswerFragment getForm()
		{
			return new NoteDiscussionForm();
		}

		@Override public String getIconName() {	return "note"; }
	}
}
