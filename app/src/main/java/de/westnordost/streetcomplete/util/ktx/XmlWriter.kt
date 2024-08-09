package de.westnordost.streetcomplete.util.ktx

import nl.adaptivity.xmlutil.XmlWriter

fun XmlWriter.startTag(name: String) = startTag("", name, null)

fun XmlWriter.endTag(name: String) = endTag("", name, null)

fun XmlWriter.attribute(name: String, value: String) = attribute(null, name, null, value)
