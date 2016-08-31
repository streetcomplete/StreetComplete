package de.westnordost.osmagent.quests.osmnotes;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;

import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.LatLon;
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

	public void deleteUnreferenced()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String where = NoteTable.Columns.ID + " NOT IN ( " +
				"SELECT " + OsmNoteQuestTable.Columns.NOTE_ID + " FROM " + OsmNoteQuestTable.NAME +
				")";

		db.delete(NoteTable.NAME, where, null);
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
}
