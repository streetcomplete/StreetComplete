package de.westnordost.streetcomplete.data;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public abstract class AndroidDbTestCase extends AndroidTestCase
{
	private Context ctx;
	private final String dbName;

	public AndroidDbTestCase(String dbName)
	{
		this.dbName = dbName;
	}

	@Override public Context getContext()
	{
		if(ctx == null)
		{
			ctx = new RenamingDelegatingContext(super.getContext(), "test_");
		}
		return ctx;
	}

	@Override public void setUp() throws Exception
	{
		super.setUp();
		// make sure the DB is created new for each test case
		getContext().deleteDatabase(dbName);
	}

	@Override public void tearDown() throws Exception
	{
		super.tearDown();
		getContext().deleteDatabase(dbName);
	}
}
