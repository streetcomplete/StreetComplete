package de.westnordost.streetcomplete.data

import android.database.sqlite.SQLiteOpenHelper
import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.streetcomplete.util.KryoSerializer
import de.westnordost.streetcomplete.util.Serializer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

open class ApplicationDbTestCase {
    protected lateinit var dbHelper: SQLiteOpenHelper
    protected lateinit var serializer: Serializer

    @Before fun setUpHelper() {
        serializer = KryoSerializer()
        dbHelper = DbModule.sqLiteOpenHelper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DATABASE_NAME
        )
    }

    @Test fun databaseAvailable() {
        Assert.assertNotNull(dbHelper.readableDatabase)
    }

    @After fun tearDownHelper() {
        dbHelper.close()
        InstrumentationRegistry.getInstrumentation().targetContext
            .deleteDatabase(DATABASE_NAME)
    }

    companion object {
        private const val DATABASE_NAME = "streetcomplete_test.db"
    }
}
