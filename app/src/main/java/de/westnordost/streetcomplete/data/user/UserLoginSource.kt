package de.westnordost.streetcomplete.data.user

interface UserLoginSource {
    interface Listener {
        fun onLoggedIn()
        fun onLoggedOut()
    }

    val isLoggedIn: Boolean
    val accessToken: String?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
