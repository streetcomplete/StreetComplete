package de.westnordost.streetcomplete.data.user.achievements

object UserLinksTable {
    const val NAME = "links"

    object Columns { const val LINK = "link" }

    const val CREATE = "CREATE TABLE $NAME (${Columns.LINK} varchar(255) PRIMARY KEY);"
}
