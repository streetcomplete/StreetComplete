package de.westnordost.streetcomplete.data;

import android.database.sqlite.SQLiteOpenHelper;

public class StreetCompleteOpenHelperTest extends AndroidDbTestCase
{
	private SQLiteOpenHelper helper;

	public StreetCompleteOpenHelperTest()
	{
		super(StreetCompleteOpenHelper.DB_NAME);
	}

	@Override public void setUp() throws Exception
	{
		super.setUp();
		helper = DbModule.sqliteOpenHelper(getContext());
	}

	@Override public void tearDown() throws Exception
	{
		// first close, then call super (= delete database) to avoid warning
		helper.close();
		super.tearDown();
	}

	public void testSetUp()
	{
		assertNotNull(helper.getReadableDatabase());
	}
}
