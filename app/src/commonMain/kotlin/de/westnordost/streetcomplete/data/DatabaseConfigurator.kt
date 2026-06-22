package de.westnordost.streetcomplete.data

/** Creates the database tables and upgrades them to the current version */
interface DatabaseConfigurator {
    /** Current version of the database */
    val version: Int

    /** Create [db] at current [version] */
    fun onCreate(db: Database)

    /** Update [db] from [oldVersion] to [version] */
    fun onUpgrade(db: Database, oldVersion: Int)
}
