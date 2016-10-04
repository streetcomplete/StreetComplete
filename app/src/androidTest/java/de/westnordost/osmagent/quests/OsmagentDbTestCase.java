package de.westnordost.osmagent.quests;

import android.database.sqlite.SQLiteOpenHelper;

import de.westnordost.osmagent.util.KryoSerializer;
import de.westnordost.osmagent.util.Serializer;

public class OsmagentDbTestCase extends AndroidDbTestCase
{
	protected SQLiteOpenHelper dbHelper;
	protected Serializer serializer;

	public OsmagentDbTestCase()
	{
		super(OsmagentOpenHelper.DB_NAME);
	}

	@Override public void setUp()
	{
		super.setUp();
		serializer = new KryoSerializer();
		dbHelper = new OsmagentOpenHelper(getContext());
	}

	@Override public void tearDown()
	{
		// first close, then call super (= delete database) to avoid warning
		dbHelper.close();
		super.tearDown();
	}
}
