package de.westnordost.osmagent.data;

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
		// first close, then call super (= delete database) to avoid warning
		helper.close();
		super.tearDown();
	}

	public void testSetUp()
	{
		assertNotNull(helper.getReadableDatabase());
	}
}
