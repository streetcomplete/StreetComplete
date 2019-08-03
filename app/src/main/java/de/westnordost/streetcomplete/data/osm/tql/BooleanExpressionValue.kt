package de.westnordost.streetcomplete.data.osm.tql

interface BooleanExpressionValue<in T> {
    fun matches(obj: T?): Boolean
}
