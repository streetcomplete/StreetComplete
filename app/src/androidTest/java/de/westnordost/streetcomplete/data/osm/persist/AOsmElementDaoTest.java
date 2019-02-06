package de.westnordost.streetcomplete.data.osm.persist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import de.westnordost.osmapi.map.data.Element;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AOsmElementDaoTest
{
	private static final String TABLE_NAME = "test";
	private static final String ID_COL = "id";
	private static final String VERSION_COL = "version";
	private static final String TESTDB = "testdb.db";

	private TestOsmElementDao dao;
	private SQLiteOpenHelper dbHelper;

	@Before public void setUpHelper()
	{
		dbHelper = new TestDbHelper(getInstrumentation().getTargetContext());
		dao = new TestOsmElementDao(dbHelper);
	}

	@After public void tearDownHelper()
	{
		dbHelper.close();
		getInstrumentation().getTargetContext().deleteDatabase(TESTDB);
	}

	@Test public void putGet()
	{
		dao.put(createElement(6,1));
		assertEquals(6,dao.get(6).getId());
		assertEquals(1,dao.get(6).getVersion());
	}

	@Test public void putAll()
	{
		ArrayList<Element> elements = new ArrayList<>();
		elements.add(createElement(1,2));
		elements.add(createElement(2,2));
		dao.putAll(elements);

		assertNotNull(dao.get(1));
		assertNotNull(dao.get(2));
	}

	@Test public void putOverwrite()
	{
		dao.put(createElement(6,0));
		dao.put(createElement(6,5));
		assertEquals(5,dao.get(6).getVersion());
	}

	@Test public void getNull()
	{
		assertNull(dao.get(6));
	}

	@Test public void delete()
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
