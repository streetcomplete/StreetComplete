package de.westnordost.streetcomplete.testutils

import io.mockative.ResultBuilder
import io.mockative.classOf
import io.mockative.coVerify
import io.mockative.every
import io.mockative.mock
import io.mockative.verify

fun <R> verifyInvokedExactlyOnce(block: () -> R): Unit = verify(block).wasInvoked(exactly = 1)
suspend fun <R> coVerifyInvokedExactlyOnce(block: suspend () -> R): Unit = coVerify(block).wasInvoked(exactly = 1)
fun <R> verifyInvokedExactly(times : Int, block: () -> R): Unit = verify(block).wasInvoked(exactly = times)

fun <T> on(methodCall: () -> T): ResultBuilder<T> = every (methodCall)

// doesn't work. why?
// inline fun <reified T : Any> mockA(): T = mock(classOf<T>())
