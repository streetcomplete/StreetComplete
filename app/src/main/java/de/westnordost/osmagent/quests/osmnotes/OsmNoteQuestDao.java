package de.westnordost.osmagent.quests.osmnotes;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.AQuestDao;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuestDao extends AQuestDao<OsmNoteQuest>
{
	private final Serializer serializer;

	@Inject public OsmNoteQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper);
		this.serializer = serializer;
	}

	@Override protected String getTableName()
	{
		return OsmNoteQuestTable.NAME;
	}

	@Override protected String getMergedViewName()
	{
		return OsmNoteQuestTable.NAME_MERGED_VIEW;
	}

	@Override protected String getIdColumnName()
	{
		return OsmNoteQuestTable.Columns.QUEST_ID;
	}

	@Override protected String getLatitudeColumnName()
	{
		return NoteTable.Columns.LATITUDE;
	}

	@Override protected String getLongitudeColumnName()
	{
		return NoteTable.Columns.LONGITUDE;
	}

	@Override protected String getQuestStatusColumnName()
	{
		return OsmNoteQuestTable.Columns.QUEST_STATUS;
	}

	@Override protected ContentValues createNonFinalContentValuesFrom(OsmNoteQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(OsmNoteQuestTable.Columns.QUEST_STATUS, quest.getStatus().name());
		values.put(OsmNoteQuestTable.Columns.LAST_UPDATE, quest.getLastUpdate().getTime());

		if(quest.getChanges() != null)
		{
			values.put(OsmNoteQuestTable.Columns.CHANGES, serializer.toBytes(quest.getChanges()));
		}

		return values;
	}

	@Override protected ContentValues createContentValuesFrom(OsmNoteQuest quest)
	{
		ContentValues values = createNonFinalContentValuesFrom(quest);
		values.put(OsmNoteQuestTable.Columns.QUEST_ID, quest.getId());
		if(quest.getNote() != null)
		{
			values.put(OsmNoteQuestTable.Columns.NOTE_ID, quest.getNote().id);
		}
		return values;
	}

	@Override protected OsmNoteQuest createObjectFrom(Cursor cursor)
	{
		int colQuestId = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.QUEST_ID),
			colNoteId = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.NOTE_ID),
			colQuestStatus = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.QUEST_STATUS),
			colChanges = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.CHANGES),
			colLastUpdate = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.LAST_UPDATE);

		long questId = cursor.getLong(colQuestId);

		NoteChange changes = null;
		if(!cursor.isNull(colChanges))
		{
			changes = serializer.toObject(cursor.getBlob(colChanges), NoteChange.class);
		}
		QuestStatus status = QuestStatus.valueOf(cursor.getString(colQuestStatus));

		Date lastUpdate = new Date(cursor.getLong(colLastUpdate));

		Note note = null;
		if(!cursor.isNull(colNoteId))
		{
			note = NoteDao.createObjectFrom(serializer, cursor);
		}

		return new OsmNoteQuest(questId, note, status, changes, lastUpdate);
	}
}
