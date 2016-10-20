package de.westnordost.osmagent.data.osmnotes;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

public class NoteDao
{
	protected final SQLiteOpenHelper dbHelper;
	protected final Serializer serializer;

	@Inject public NoteDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	public void put(Note note)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(NoteTable.Columns.ID, note.id);
		LatLon pos = note.position;
		values.put(NoteTable.Columns.LATITUDE, pos.getLatitude());
		values.put(NoteTable.Columns.LONGITUDE, pos.getLongitude());
		values.put(NoteTable.Columns.STATUS, note.status.name());
		values.put(NoteTable.Columns.CREATED, note.dateCreated.getTime());
		if(note.dateClosed != null)
		{
			values.put(NoteTable.Columns.CLOSED, note.dateClosed.getTime());
		}
		values.put(NoteTable.Columns.COMMENTS, serializer.toBytes(note.comments));

		db.insertWithOnConflict(NoteTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public Note get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(NoteTable.NAME, null, NoteTable.Columns.ID + " = " + id,
				null, null, null, null, "1");

		try
		{
			if(!cursor.moveToFirst()) return null;
			return createObjectFrom(serializer, cursor);
		}
		finally
		{
			cursor.close();
		}
	}

	public boolean delete(long id)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(NoteTable.NAME, NoteTable.Columns.ID + " = " + id, null) == 1;
	}

	static Note createObjectFrom(Serializer serializer, Cursor cursor)
	{
		int colNoteId = cursor.getColumnIndexOrThrow(NoteTable.Columns.ID),
			colLat = cursor.getColumnIndexOrThrow(NoteTable.Columns.LATITUDE),
			colLon = cursor.getColumnIndexOrThrow(NoteTable.Columns.LONGITUDE),
			colStatus = cursor.getColumnIndexOrThrow(NoteTable.Columns.STATUS),
			colCreated = cursor.getColumnIndexOrThrow(NoteTable.Columns.CREATED),
			colClosed = cursor.getColumnIndexOrThrow(NoteTable.Columns.CLOSED),
			colComments = cursor.getColumnIndexOrThrow(NoteTable.Columns.COMMENTS);

		Note note = new Note();
		note.id = cursor.getLong(colNoteId);
		note.position = new OsmLatLon(cursor.getDouble(colLat), cursor.getDouble(colLon));
		note.dateCreated = new Date(cursor.getLong(colCreated));
		if(!cursor.isNull(colClosed))
		{
			note.dateClosed = new Date(cursor.getLong(colClosed));
		}
		note.status = Note.Status.valueOf(cursor.getString(colStatus));
		note.comments = serializer.toObject(cursor.getBlob(colComments), ArrayList.class);

		return note;
	}

	public int deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String where = NoteTable.Columns.ID + " NOT IN ( " +
				"SELECT " + OsmNoteQuestTable.Columns.NOTE_ID + " FROM " + OsmNoteQuestTable.NAME +
				")";

		return db.delete(NoteTable.NAME, where, null);
	}
}
