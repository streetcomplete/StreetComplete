package de.westnordost.streetcomplete.data;

public class StreetCompleteOpenHelperTest extends AndroidDbTestCase
{
	private StreetCompleteOpenHelper helper;

	public StreetCompleteOpenHelperTest()
	{
		super(StreetCompleteOpenHelper.DB_NAME);
	}

	@Override public void setUp()
	{
		super.setUp();
		helper = new StreetCompleteOpenHelper(getContext());
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
