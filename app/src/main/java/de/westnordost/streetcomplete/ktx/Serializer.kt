package de.westnordost.streetcomplete.ktx

import de.westnordost.streetcomplete.util.Serializer

inline fun <reified T> Serializer.toObject(bytes: ByteArray):T = toObject(bytes, T::class.java)
