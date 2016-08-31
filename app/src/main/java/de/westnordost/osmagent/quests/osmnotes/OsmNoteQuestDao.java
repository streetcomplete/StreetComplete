package de.westnordost.osmagent.quests.osmnotes;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.osm.persist.OsmQuestTable;
import de.westnordost.osmagent.quests.QuestDao;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuestDao extends QuestDao<OsmNoteQuest>
{

	@Inject public OsmNoteQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		super(dbHelper, serializer);
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
		return OsmNoteQuestTable.Columns.NOTE_ID;
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
		values.put(OsmQuestTable.Columns.LAST_UPDATE, quest.getLastUpdate().getTime());

		if(quest.getChanges() != null)
		{
			values.put(OsmNoteQuestTable.Columns.CHANGES, serializer.toBytes(quest.getChanges()));
		}

		return values;
	}

	@Override protected ContentValues createContentValuesFrom(OsmNoteQuest quest)
	{
		ContentValues values = createNonFinalContentValuesFrom(quest);
		values.put(OsmNoteQuestTable.Columns.NOTE_ID, quest.getNote().id);
		return values;
	}

	@Override protected OsmNoteQuest createObjectFrom(Cursor cursor)
	{
		int colNoteId = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.NOTE_ID),
				colQuestStatus = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.QUEST_STATUS),
				colChanges = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.CHANGES),
				colLastUpdate = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.LAST_UPDATE),
				colLat = cursor.getColumnIndexOrThrow(NoteTable.Columns.LATITUDE),
				colLon = cursor.getColumnIndexOrThrow(NoteTable.Columns.LONGITUDE),
				colStatus = cursor.getColumnIndexOrThrow(NoteTable.Columns.STATUS),
				colCreated = cursor.getColumnIndexOrThrow(NoteTable.Columns.CREATED),
				colClosed = cursor.getColumnIndexOrThrow(NoteTable.Columns.CLOSED),
				colComments = cursor.getColumnIndexOrThrow(NoteTable.Columns.COMMENTS);

		NoteChange changes = null;
		if(!cursor.isNull(colChanges))
		{
			changes = serializer.toObject(cursor.getBlob(colChanges), NoteChange.class);
		}
		QuestStatus status = QuestStatus.valueOf(cursor.getString(colQuestStatus));

		Note note = new Note();
		note.id = colNoteId;
		note.position = new OsmLatLon(cursor.getDouble(colLat), cursor.getDouble(colLon));
		note.dateCreated = new Date(cursor.getLong(colCreated));
		if(!cursor.isNull(colClosed))
		{
			note.dateClosed = new Date(cursor.getLong(colClosed));
		}
		note.status = Note.Status.valueOf(cursor.getString(colStatus));
		note.comments = serializer.toObject(cursor.getBlob(colComments), List.class);

		Date lastUpdate = new Date(cursor.getLong(colLastUpdate));

		return new OsmNoteQuest(note, status, changes, lastUpdate);
	}
}
