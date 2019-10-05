package de.westnordost.streetcomplete.data

import android.content.ContentValues
import android.database.Cursor

interface ObjectRelationalMapping<T> {
    fun toContentValues(obj: T): ContentValues
    fun toObject(cursor: Cursor): T
}
