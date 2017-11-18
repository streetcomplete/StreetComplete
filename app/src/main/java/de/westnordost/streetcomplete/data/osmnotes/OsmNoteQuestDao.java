package de.westnordost.streetcomplete.data.osmnotes;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.AQuestDao;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.WhereSelectionBuilder;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

import static de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestTable.Columns;

public class OsmNoteQuestDao extends AQuestDao<OsmNoteQuest>
{
	private final Serializer serializer;
	private final OsmNoteQuestType questType;
	private final SQLiteStatement add, replace;

	@Inject public OsmNoteQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer,
								   OsmNoteQuestType questType)
	{
		super(dbHelper);
		this.serializer = serializer;
		this.questType = questType;

		String sql = OsmNoteQuestTable.NAME + " ("+
				Columns.QUEST_ID+","+
				Columns.NOTE_ID+","+
				Columns.QUEST_STATUS+","+
				Columns.COMMENT+","+
				Columns.LAST_UPDATE+","+
				Columns.IMAGE_PATHS+
				") values (?,?,?,?,?,?);";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		add = db.compileStatement("INSERT OR IGNORE INTO " + sql);
		replace = db.compileStatement("INSERT OR REPLACE INTO " +sql);
	}

	public List<LatLon> getAllPositions(BoundingBox bbox)
	{
		String[] cols = { NoteTable.Columns.LATITUDE, NoteTable.Columns.LONGITUDE };
		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addBBox(bbox, qb);
		return getAllThings(getMergedViewName(), cols, qb,
				cursor -> new OsmLatLon(cursor.getDouble(0), cursor.getDouble(1)));
	}

	@Override protected String getTableName() { return OsmNoteQuestTable.NAME; }
	@Override protected String getMergedViewName() { return OsmNoteQuestTable.NAME_MERGED_VIEW; }
	@Override protected String getIdColumnName() { return Columns.QUEST_ID; }
	@Override protected String getLatitudeColumnName() { return NoteTable.Columns.LATITUDE; }
	@Override protected String getLongitudeColumnName()	{ return NoteTable.Columns.LONGITUDE; }
	@Override protected String getQuestStatusColumnName() { return Columns.QUEST_STATUS; }
	@Override protected String getLastChangedColumnName() {	return Columns.LAST_UPDATE; }

	@Override protected synchronized long executeInsert(OsmNoteQuest quest, boolean replace)
	{
		SQLiteStatement stmt = replace ? this.replace : this.add;

		if(quest.getId() != null)
		{
			stmt.bindLong(1, quest.getId());
		}
		else
		{
			stmt.bindNull(1);
		}
		stmt.bindLong(2, quest.getNote().id);
		stmt.bindString(3, quest.getStatus().name());
		if(quest.getComment() != null)
		{
			stmt.bindString(4, quest.getComment());
		}
		else
		{
			stmt.bindNull(4);
		}

		stmt.bindLong(5, quest.getLastUpdate().getTime());

		long result = stmt.executeInsert();
		stmt.clearBindings();
		return result;
	}

	@Override protected ContentValues createNonFinalContentValuesFrom(OsmNoteQuest quest)
	{
		ContentValues values = new ContentValues();
		values.put(Columns.QUEST_STATUS, quest.getStatus().name());
		values.put(Columns.LAST_UPDATE, quest.getLastUpdate().getTime());

		if(quest.getComment() != null)
		{
			values.put(Columns.COMMENT, quest.getComment());
		}

		if (quest.getImagePaths() != null)
		{
			values.put(Columns.IMAGE_PATHS, serializer.toBytes(quest.getImagePaths()));
		}

		return values;
	}

	@Override protected ContentValues createFinalContentValuesFrom(OsmNoteQuest quest)
	{
		ContentValues values = new ContentValues();
		if(quest.getNote() != null)
		{
			values.put(Columns.NOTE_ID, quest.getNote().id);
		}
		return values;
	}

	@Override protected OsmNoteQuest createObjectFrom(Cursor cursor)
	{
		int colQuestId = cursor.getColumnIndexOrThrow(Columns.QUEST_ID),
			colNoteId = cursor.getColumnIndexOrThrow(Columns.NOTE_ID),
			colQuestStatus = cursor.getColumnIndexOrThrow(Columns.QUEST_STATUS),
			colComment = cursor.getColumnIndexOrThrow(Columns.COMMENT),
			colLastUpdate = cursor.getColumnIndexOrThrow(Columns.LAST_UPDATE),
			colImagePaths = cursor.getColumnIndexOrThrow(Columns.IMAGE_PATHS);

		long questId = cursor.getLong(colQuestId);

		String comment = null;
		if(!cursor.isNull(colComment))
		{
			comment = cursor.getString(colComment);
		}
		QuestStatus status = QuestStatus.valueOf(cursor.getString(colQuestStatus));

		ArrayList<String> imagePaths = new ArrayList<>();
		if(!cursor.isNull(colImagePaths))
		{
			imagePaths = serializer.toObject(cursor.getBlob(colImagePaths), ArrayList.class);
		}

		Date lastUpdate = new Date(cursor.getLong(colLastUpdate));

		Note note = null;
		if(!cursor.isNull(colNoteId))
		{
			note = NoteDao.createObjectFrom(serializer, cursor);
		}

		return new OsmNoteQuest(questId, note, status, comment, lastUpdate, questType, imagePaths);
	}
}
