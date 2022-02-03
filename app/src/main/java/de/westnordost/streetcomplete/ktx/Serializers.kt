package de.westnordost.streetcomplete.ktx

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

fun <T> mapSerializer(valueSerializer: KSerializer<T>) =
    MapSerializer(String.serializer(), valueSerializer)

val stringMapSerializer = mapSerializer(String.serializer())

val stringListSerializer = ListSerializer(String.serializer())
