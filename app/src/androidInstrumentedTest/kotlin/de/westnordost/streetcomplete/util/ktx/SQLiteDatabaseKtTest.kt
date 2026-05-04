package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class SQLiteDatabaseKtTest : ApplicationDbTestCase() {

    @BeforeTest fun setUp() {
        database.exec("CREATE TABLE t (a int, b int)")
    }

    @AfterTest fun tearDown() {
        database.exec("DROP TABLE t")
    }
}
