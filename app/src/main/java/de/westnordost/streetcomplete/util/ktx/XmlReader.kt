package de.westnordost.streetcomplete.util.ktx

import nl.adaptivity.xmlutil.XmlReader

fun XmlReader.attribute(name: String): String = getAttributeValue(null, name)!!

fun XmlReader.attributeOrNull(name: String): String? = getAttributeValue(null, name)
