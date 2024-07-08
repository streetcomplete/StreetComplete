package de.westnordost.streetcomplete.testutils

import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.data.preferences.Preferences
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.mockito.stubbing.Stubber

fun <T> eq(obj: T): T = Mockito.eq<T>(obj)
fun <T> argThat(matcher: (T) -> Boolean): T = Mockito.argThat<T>(ArgumentMatcher(matcher))
fun <T> any(): T = Mockito.any<T>()
fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
inline fun <reified T : Any> argumentCaptor(): ArgumentCaptor<T> =
    ArgumentCaptor.forClass(T::class.java)

fun <T> on(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)
fun <T> Stubber.on(mock: T): T = this.`when`(mock)

inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

// mock SharedPreferences that always return default value
fun mockPrefs(): SharedPreferences {
    val prefs: SharedPreferences = mock()
    on(prefs.getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenAnswer { inv -> inv.getArgument(1, String::class.java) }
    on(prefs.getInt(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer { inv -> inv.getArgument(1, Integer::class.java) }
    on(prefs.getLong(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())).thenAnswer { inv -> inv.getArgument(1, Long::class.java) }
    // style above doesn't work for boolean ("Cannot cast java.lang.Boolean to boolean")
    on(prefs.getBoolean(ArgumentMatchers.anyString(), eq(true))).thenAnswer { true }
    on(prefs.getBoolean(ArgumentMatchers.anyString(), eq(false))).thenAnswer { false }
    return prefs
}

fun mockPrefs2(): ObservableSettings {
    val prefs: ObservableSettings = mock()
    on(prefs.getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenAnswer { inv -> inv.getArgument(1, String::class.java) }
    on(prefs.getInt(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer { inv -> inv.getArgument(1, Integer::class.java) }
    on(prefs.getLong(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())).thenAnswer { inv -> inv.getArgument(1, Long::class.java) }
    // style above doesn't work for boolean ("Cannot cast java.lang.Boolean to boolean")
    on(prefs.getBoolean(ArgumentMatchers.anyString(), eq(true))).thenAnswer { true }
    on(prefs.getBoolean(ArgumentMatchers.anyString(), eq(false))).thenAnswer { false }
    return prefs
}

fun mockPrefs3(): Preferences {
    val prefs: Preferences = mock()
    val obs = mockPrefs2()
    on(prefs.prefs).thenReturn(obs)
    return prefs
}
