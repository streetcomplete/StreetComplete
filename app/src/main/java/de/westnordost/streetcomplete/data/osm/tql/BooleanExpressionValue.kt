package de.westnordost.streetcomplete.data.osm.tql

interface BooleanExpressionValue {
    fun matches(ele: Any?): Boolean
}
