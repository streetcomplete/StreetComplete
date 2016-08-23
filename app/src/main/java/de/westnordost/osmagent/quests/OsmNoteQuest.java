package de.westnordost.osmagent.quests;

import android.app.DialogFragment;

import de.westnordost.osmagent.quests.persist.NoteChange;
import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuest implements Quest
{
	public OsmNoteQuest(Note note, QuestStatus status, NoteChange changes)
	{
		this.note = note;
		this.status = status;
		this.changes = changes;
	}

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
