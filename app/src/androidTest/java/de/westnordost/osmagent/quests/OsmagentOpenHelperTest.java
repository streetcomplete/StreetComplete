package de.westnordost.osmagent.quests;

public class OsmagentOpenHelperTest extends AndroidDbTestCase
{
	private OsmagentOpenHelper helper;

	public OsmagentOpenHelperTest()
	{
		super(OsmagentOpenHelper.DB_NAME);
	}

	@Override public void setUp()
	{
		super.setUp();
		helper = new OsmagentOpenHelper(getContext());
	}

	@Override public void tearDown()
	{
		super.tearDown();
		helper.close();
	}

	public void testSetUp()
	{
		assertNotNull(helper.getReadableDatabase());
	}
}
