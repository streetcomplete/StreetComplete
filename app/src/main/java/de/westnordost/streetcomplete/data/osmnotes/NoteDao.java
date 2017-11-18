package de.westnordost.streetcomplete.data.osmnotes;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

public class NoteDao
{
	private final SQLiteOpenHelper dbHelper;
	private final Serializer serializer;

	private final SQLiteStatement insert;

	@Inject public NoteDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;

		String sql = "INSERT OR REPLACE INTO " + NoteTable.NAME + " ("+
				NoteTable.Columns.ID+","+
				NoteTable.Columns.LATITUDE+","+
				NoteTable.Columns.LONGITUDE+","+
				NoteTable.Columns.STATUS+","+
				NoteTable.Columns.CREATED+","+
				NoteTable.Columns.CLOSED+","+
				NoteTable.Columns.COMMENTS+
				") values (?,?,?,?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		insert = db.compileStatement(sql);
	}

	public void putAll(Collection<Note> notes)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.beginTransaction();

		for(Note note : notes)
		{
			executeInsert(note);
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void put(Note note)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		executeInsert(note);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private void executeInsert(Note note)
	{
		insert.bindLong(1, note.id);
		insert.bindDouble(2, note.position.getLatitude());
		insert.bindDouble(3, note.position.getLongitude());
		insert.bindString(4, note.status.name());
		insert.bindLong(5, note.dateCreated.getTime());
		if(note.dateClosed != null)
		{
			insert.bindLong(6, note.dateClosed.getTime());
		}
		else
		{
			insert.bindNull(6);
		}
		insert.bindBlob(7, serializer.toBytes(note.comments));

		insert.executeInsert();
		insert.clearBindings();
	}

	public Note get(long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String where = NoteTable.Columns.ID + " = " + id;

		try (Cursor cursor = db.query(NoteTable.NAME, null, where,
				null, null, null, null, "1"))
		{
			if (!cursor.moveToFirst()) return null;
			return createObjectFrom(serializer, cursor);
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
