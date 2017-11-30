package de.westnordost.streetcomplete.data.osm.persist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

import de.westnordost.streetcomplete.data.AndroidDbTestCase;
import de.westnordost.osmapi.map.data.Element;

import static org.mockito.Mockito.*;

public class AOsmElementDaoTest extends AndroidDbTestCase
{
	private static final String TABLE_NAME = "test";
	private static final String ID_COL = "id";
	private static final String VERSION_COL = "version";
	private static final String TESTDB = "testdb.db";

	private TestOsmElementDao dao;
	private SQLiteOpenHelper dbHelper;

	public AOsmElementDaoTest()
	{
		super(TESTDB);
	}

	@Override public void setUp() throws Exception
	{
		super.setUp();
		dbHelper = new TestDbHelper(getContext());
		dao = new TestOsmElementDao(dbHelper);
	}

	@Override public void tearDown() throws Exception
	{
		// first close, then call super (= delete database) to avoid warning
		dbHelper.close();
		super.tearDown();
	}

	public void testPutGet()
	{
		dao.put(createElement(6,1));
		assertEquals(6,dao.get(6).getId());
		assertEquals(1,dao.get(6).getVersion());
	}

	public void testPutAll()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createElement(1,2));
		elements.add(createElement(2,2));
		dao.putAll(elements);

		assertNotNull(dao.get(1));
		assertNotNull(dao.get(2));
	}

	public void testPutOverwrite()
	{
		dao.put(createElement(6,0));
		dao.put(createElement(6,5));
		assertEquals(5,dao.get(6).getVersion());
	}

	public void testGetNull()
	{
		assertNull(dao.get(6));
	}

	public void testDelete()
	{
		dao.put(createElement(6,0));
		dao.delete(6);
		assertNull(dao.get(6));
	}

	private class TestDbHelper extends SQLiteOpenHelper
	{
		public TestDbHelper(Context context)
		{
			super(context, TESTDB, null, 1);
		}

		@Override public void onCreate(SQLiteDatabase db)
		{
			// the AOsmElementDao is tied to the quest table... but we only need the id and type
			db.execSQL("CREATE TABLE " + OsmQuestTable.NAME + " (" +
					OsmQuestTable.Columns.ELEMENT_ID +		" int			NOT NULL, " +
					OsmQuestTable.Columns.ELEMENT_TYPE +	" varchar(255)	NOT NULL " +
					");");
			db.execSQL("INSERT INTO "+OsmQuestTable.NAME + " (" +
					OsmQuestTable.Columns.ELEMENT_ID + ", " +
					OsmQuestTable.Columns.ELEMENT_TYPE +	") VALUES " +
					"(1, \""+Element.Type.NODE.name()+"\");");

			db.execSQL("CREATE TABLE "+TABLE_NAME+" ( " +
					ID_COL+" int PRIMARY KEY, " +
					VERSION_COL+" int);");
		}

		@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{

		}
	}

	private class TestOsmElementDao extends AOsmElementDao<Element>
	{
		public TestOsmElementDao(SQLiteOpenHelper dbHelper)
		{
			super(dbHelper);
		}

		@Override protected String getElementTypeName()
		{
			return Element.Type.NODE.name();
		}

		@Override protected String getTableName()
		{
			return TABLE_NAME;
		}

		@Override protected String getIdColumnName()
		{
			return ID_COL;
		}

		@Override protected void executeInsert(Element e)
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			SQLiteStatement insert = db.compileStatement(
					"INSERT OR REPLACE INTO " + TABLE_NAME +
							"("+ID_COL+","+VERSION_COL+") VALUES (?,?)");

			insert.bindLong(1, e.getId());
			insert.bindLong(2, e.getVersion());

			insert.executeInsert();
		}

		@Override protected Element createObjectFrom(Cursor cursor)
		{
			return createElement(cursor.getLong(0), cursor.getInt(1));
		}
	}

	private Element createElement(long id, int version)
	{
		Element element = mock(Element.class);
		when(element.getId()).thenReturn(id);
		when(element.getVersion()).thenReturn(version);
		return element;
	}
}
