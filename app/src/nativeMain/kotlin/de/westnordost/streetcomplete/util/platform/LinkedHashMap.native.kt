package de.westnordost.streetcomplete.util.platform

// access order is sadly not supported by Kotlin's LinkedHashMap. See
// https://youtrack.jetbrains.com/issue/KT-52183/Add-LRU-mode-for-LinkedHashMap
actual fun <K, V> linkedHashMapWithAccessOrder(
    initialCapacity: Int,
    loadFactor: Float,
    accessOrder: Boolean,
): LinkedHashMap<K, V> = LinkedHashMap(initialCapacity, loadFactor)
