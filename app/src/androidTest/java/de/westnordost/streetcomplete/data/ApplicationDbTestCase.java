package de.westnordost.streetcomplete.data;

import android.database.sqlite.SQLiteOpenHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.westnordost.streetcomplete.util.KryoSerializer;
import de.westnordost.streetcomplete.util.Serializer;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertNotNull;

public class ApplicationDbTestCase
{
	private final static String DATABASE_NAME = "streetcomplete_test.db";

	protected SQLiteOpenHelper dbHelper;
	protected Serializer serializer;

	@Before public void setUpHelper()
	{
		serializer = new KryoSerializer();
		dbHelper = DbModule.sqliteOpenHelper(getInstrumentation().getTargetContext(), DATABASE_NAME);
	}

	@Test public void databaseAvailable()
	{
		assertNotNull(dbHelper.getReadableDatabase());
	}

	@After public void tearDownHelper()
	{
		dbHelper.close();
		getInstrumentation().getTargetContext().deleteDatabase(DATABASE_NAME);
	}
}
