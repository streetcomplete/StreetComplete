package de.westnordost.streetcomplete.data;

import android.database.sqlite.SQLiteOpenHelper;

import de.westnordost.streetcomplete.util.KryoSerializer;
import de.westnordost.streetcomplete.util.Serializer;

public class ApplicationDbTestCase extends AndroidDbTestCase
{
	protected SQLiteOpenHelper dbHelper;
	protected Serializer serializer;

	public ApplicationDbTestCase()
	{
		super(StreetCompleteOpenHelper.DB_NAME);
	}

	@Override public void setUp() throws Exception
	{
		super.setUp();
		serializer = new KryoSerializer();
		dbHelper = DbModule.sqliteOpenHelper(getContext());
	}

	@Override public void tearDown() throws Exception
	{
		// first close, then call super (= delete database) to avoid warning
		dbHelper.close();
		super.tearDown();
	}
}
