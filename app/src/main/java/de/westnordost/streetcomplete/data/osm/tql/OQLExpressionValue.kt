package de.westnordost.streetcomplete.data.osm.tql

interface OQLExpressionValue : BooleanExpressionValue {
    fun toOverpassQLString(): String
}
