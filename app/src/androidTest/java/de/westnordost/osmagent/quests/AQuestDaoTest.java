package de.westnordost.osmagent.quests;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AQuestDaoTest extends AndroidDbTestCase
{
	private static final String
			TABLE_NAME = "test",
			MERGED_VIEW_NAME = "test_full",
			ID_COL = "id",
			QS_COL = "quest_status",
			LAT_COL = "lat",
			LON_COL = "lon";

	private static final String TESTDB = "testdb.db";

	private TestQuestDao dao;
	private SQLiteOpenHelper dbHelper;

	public AQuestDaoTest()
	{
		super(TESTDB);
	}

	@Override public void setUp()
	{
		super.setUp();
		dbHelper = new TestDbHelper(getContext());
		dao = new TestQuestDao(dbHelper);
	}

	@Override public void tearDown()
	{
		super.tearDown();
		dbHelper.close();
	}

	public void testAddGet()
	{
		long id = 3;
		Quest q = createQuest(id,0,0, QuestStatus.CANCELED);
		dao.add(q);
		Quest q2 = dao.get(id);

		assertEquals(q.getId(), q2.getId());
		assertEquals(q.getMarkerLocation(), q2.getMarkerLocation());
		assertEquals(q.getStatus(), q2.getStatus());
	}

	public void testAddNoOverwrite()
	{
		dao.add(createQuest(3,0,0, QuestStatus.CANCELED));
		dao.add(createQuest(3,0,0, QuestStatus.NEW));

		assertEquals(QuestStatus.CANCELED, dao.get(3).getStatus());
	}

	public void testDelete()
	{
		assertEquals(0,dao.delete(0));
		dao.add(createQuest(1,0,0, QuestStatus.NEW));
		assertEquals(1,dao.delete(1));
	}

	public void testUpdateException()
	{
		try
		{
			dao.update(createQuest(0,0,0,QuestStatus.NEW));
			fail();
		}
		catch(NullPointerException e) { }
	}

	public void testUpdate()
	{
		dao.add(createQuest(1,0,0, QuestStatus.NEW));
		dao.update(createQuest(1,0,0, QuestStatus.ANSWERED));
		assertEquals(QuestStatus.ANSWERED, dao.get(1).getStatus());
	}

	public void testSeveralIdsGetByStatus()
	{
		dao.add(createQuest(1,0,0, QuestStatus.NEW));
		dao.add(createQuest(2,0,0, QuestStatus.NEW));
		dao.add(createQuest(2,0,0, QuestStatus.ANSWERED));
		assertEquals(2,dao.getIdsByStatus(QuestStatus.NEW).size());
	}

	public void testNoIdsGetByStatus()
	{
		dao.add(createQuest(1,0,0, QuestStatus.NEW));
		assertTrue(dao.getIdsByStatus(QuestStatus.ANSWERED).isEmpty());
	}

	public void testGetAllByBoundingBox()
	{
		BoundingBox bbox = new BoundingBox(50,1,51,2);

		// on border
		dao.add(createQuest(1,50,1, QuestStatus.NEW));
		// right lat but wrong lon
		dao.add(createQuest(2,50.5,50.5, QuestStatus.NEW));
		// wrong lat but right lon
		dao.add(createQuest(3,1.5,1.5, QuestStatus.NEW));
		// in
		dao.add(createQuest(4,50.5,1.5, QuestStatus.NEW));

		List<Quest> quests = dao.getAll(bbox, QuestStatus.NEW);
		Collections.sort(quests, new Comparator<Quest>()
		{
			@Override public int compare(Quest lhs, Quest rhs)
			{
				return (int) (lhs.getId() - rhs.getId());
			}
		});
		assertEquals(2,quests.size());
		assertEquals(1,(long) quests.get(0).getId());
		assertEquals(4,(long) quests.get(1).getId());
	}

	private class TestQuestDao extends AQuestDao<Quest>
	{
		public TestQuestDao(SQLiteOpenHelper dbHelper)
		{
			super(dbHelper);
		}

		@Override protected String getTableName()
		{
			return TABLE_NAME;
		}

		@Override protected String getMergedViewName()
		{
			return TABLE_NAME;
		}

		@Override protected String getIdColumnName()
		{
			return ID_COL;
		}

		@Override protected String getQuestStatusColumnName()
		{
			return QS_COL;
		}

		@Override protected String getLatitudeColumnName()
		{
			return LAT_COL;
		}

		@Override protected String getLongitudeColumnName()
		{
			return LON_COL;
		}

		@Override protected ContentValues createNonFinalContentValuesFrom(Quest quest)
		{
			ContentValues v = new ContentValues();
			v.put(QS_COL, quest.getStatus().name());
			return v;
		}

		@Override protected ContentValues createContentValuesFrom(Quest quest)
		{
			ContentValues v = createNonFinalContentValuesFrom(quest);
			v.put(ID_COL, quest.getId());
			v.put(LAT_COL, quest.getMarkerLocation().getLatitude());
			v.put(LON_COL, quest.getMarkerLocation().getLongitude());
			return v;
		}

		@Override protected Quest createObjectFrom(Cursor cursor)
		{
			return createQuest(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2),
					QuestStatus.valueOf(cursor.getString(3)));
		}
	}

	private Quest createQuest(long id, double lat, double lon, QuestStatus status)
	{
		Quest quest = mock(Quest.class);
		when(quest.getStatus()).thenReturn(status);
		when(quest.getId()).thenReturn(id);
		when(quest.getMarkerLocation()).thenReturn(new OsmLatLon(lat,lon));
		return quest;
	}

	private class TestDbHelper extends SQLiteOpenHelper
	{
		public TestDbHelper(Context context)
		{
			super(context, TESTDB, null, 1);
		}

		@Override public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE "+TABLE_NAME+" ( " +
					ID_COL+" int PRIMARY KEY, " +
					LAT_COL+" double, " +
					LON_COL+" double, " +
					QS_COL+" varchar(255));");
		}

		@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{

		}
	}
}
