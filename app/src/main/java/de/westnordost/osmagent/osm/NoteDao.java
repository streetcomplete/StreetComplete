package de.westnordost.osmagent.osm;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

public class NoteDao
{
	private SQLiteOpenHelper dbHelper;
	private Serializer serializer;

	@Inject
	public NoteDao(SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		this.dbHelper = dbHelper;
		this.serializer = serializer;
	}

	/** adds or updates (overwrites) a note */
	public void put(Note note)
	{
		ContentValues values = new ContentValues();

		values.put(NoteTable.Columns.ID, note.id);
		LatLon pos = note.position;
		values.put(NoteTable.Columns.LATITUDE, pos.getLatitude());
		values.put(NoteTable.Columns.LONGITUDE, pos.getLongitude());
		values.put(NoteTable.Columns.STATUS, note.status.ordinal());
		values.put(NoteTable.Columns.CREATED, note.dateCreated.getTime());
		if(note.dateClosed != null)
		{
			values.put(NoteTable.Columns.CLOSED, note.dateClosed.getTime());
		}
		values.put(NoteTable.Columns.COMMENTS, serializer.toBytes(note.comments));

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.insertWithOnConflict(NoteTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

}
