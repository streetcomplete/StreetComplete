package de.westnordost.osmagent.quests.persist;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.osm.NoteTable;
import de.westnordost.osmagent.quests.OsmNoteQuest;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuestDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public OsmNoteQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	// TODO this could go into a super- or helper class (duplicate code with OsmQuestDao)
	public void update(OsmNoteQuest quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rows = db.update(OsmNoteQuestTable.NAME, getQuestNonFinalContentValues(quest),
				OsmNoteQuestTable.Columns.NOTE_ID + " = " + quest.getNote().id, null);

		if(rows == 0)
		{
			throw new NullPointerException("Note with the id " +  quest.getNote().id + " does not exist.");
		}
	}

	public void delete(long id)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(OsmNoteQuestTable.NAME, OsmNoteQuestTable.Columns.NOTE_ID + " = " + id, null);
	}

	public long add(OsmNoteQuest quest)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		return db.insertOrThrow(OsmNoteQuestTable.NAME, null, getQuestContentValues(quest));
	}

	public List<OsmNoteQuest> getAll()
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(OsmNoteQuestTable.NAME_MERGED_VIEW,
				null, null, null, null, null, null, null);

		List<OsmNoteQuest> result = new ArrayList<>();

		try
		{
			if(cursor.moveToFirst())
			{
				while(!cursor.isAfterLast())
				{
					result.add(getCurrent(cursor));
					cursor.moveToNext();
				}
			}
		}
		finally
		{
			cursor.close();
		}


		return result;
	}

	public OsmNoteQuest get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(OsmNoteQuestTable.NAME_MERGED_VIEW, null,
				NoteTable.Columns.ID + " = " + id, null, null, null, null, "1");

		if(!cursor.moveToFirst()) return null;

		OsmNoteQuest quest = getCurrent(cursor);

		cursor.close();

		return quest;
	}

	private ContentValues getQuestContentValues(OsmNoteQuest quest)
	{
		ContentValues values = getQuestNonFinalContentValues(quest);
		values.put(OsmNoteQuestTable.Columns.NOTE_ID, quest.getNote().id);
		return values;
	}

	private ContentValues getQuestNonFinalContentValues(OsmNoteQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(OsmNoteQuestTable.Columns.QUEST_STATUS, quest.getStatus().ordinal());

		if(quest.getChanges() != null)
		{
			values.put(OsmNoteQuestTable.Columns.CHANGES, serializer.toBytes(quest.getChanges()));
		}

		return values;
	}

	private OsmNoteQuest getCurrent(Cursor cursor)
	{
		int colNoteId = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.NOTE_ID),
			colQuestStatus = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.QUEST_STATUS),
			colChanges = cursor.getColumnIndexOrThrow(OsmNoteQuestTable.Columns.CHANGES),
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
		QuestStatus status = QuestStatus.values()[cursor.getInt(colQuestStatus)];

		Note note = new Note();
		note.id = colNoteId;
		note.position = new OsmLatLon(cursor.getDouble(colLat), cursor.getDouble(colLon));
		note.dateCreated = new Date(cursor.getLong(colCreated));
		if(!cursor.isNull(colClosed))
		{
			note.dateClosed = new Date(cursor.getLong(colClosed));;
		}
		note.status = Note.Status.values()[cursor.getInt(colStatus)];
		note.comments = serializer.toObject(cursor.getBlob(colComments), List.class);

		return new OsmNoteQuest(note, status, changes);
	}
}
