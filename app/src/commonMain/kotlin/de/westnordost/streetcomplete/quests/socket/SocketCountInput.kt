package de.westnordost.streetcomplete.quests.socket

/** Soft max for newly entered counts via stepper (reviewer raised 50→99). */
const val MAX_NEW_SOCKET_COUNT = 99

/**
 * Parse user text for a socket count field.
 * - empty → unresolved (`Cleared`)
 * - valid non-negative integer → that value (never truncated/clamped)
 * - invalid → `Invalid` (caller keeps previous state)
 */
fun parseSocketCountInput(text: String): ParseSocketCountResult = when {
    text.isEmpty() -> ParseSocketCountResult.Cleared
    else -> {
        val value = text.toIntOrNull()
        when {
            value == null || value < 0 -> ParseSocketCountResult.Invalid
            else -> ParseSocketCountResult.Value(value)
        }
    }
}

sealed interface ParseSocketCountResult {
    data object Cleared : ParseSocketCountResult
    data object Invalid : ParseSocketCountResult
    data class Value(val count: Int) : ParseSocketCountResult
}

fun formatSocketCountInput(count: Int?): String = count?.toString().orEmpty()

/** Stepper ceiling: at least [MAX_NEW_SOCKET_COUNT], but never below an existing larger count. */
fun socketCountStepperMax(current: Int?): Int =
    maxOf(MAX_NEW_SOCKET_COUNT, current ?: 0)

fun increaseSocketCount(current: Int?): Int {
    val value = current ?: 0
    return (value + 1).coerceAtMost(socketCountStepperMax(current))
}

/** From unresolved (`null`), decrease sets explicit 0 / "no". */
fun decreaseSocketCount(current: Int?): Int =
    if (current == null) 0 else (current - 1).coerceAtLeast(0)

fun isSocketFormComplete(
    counts: Map<SocketType, Int?>,
    socketTypes: List<SocketType>,
): Boolean =
    socketTypes.isNotEmpty() && socketTypes.all { counts[it] != null }
