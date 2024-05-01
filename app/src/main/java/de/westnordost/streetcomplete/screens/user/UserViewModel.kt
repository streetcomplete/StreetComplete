package de.westnordost.streetcomplete.screens.user

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class UserViewModel : ViewModel() {
    abstract val isLoggedIn: StateFlow<Boolean>
}

class UserViewModelImpl(
    private val userLoginStatusSource: UserLoginStatusSource
): UserViewModel() {
    override val isLoggedIn = MutableStateFlow(userLoginStatusSource.isLoggedIn)

    private val loginStatusListener = object : UserLoginStatusSource.Listener {
        override fun onLoggedIn() { isLoggedIn.value = true }
        override fun onLoggedOut() { isLoggedIn.value = false }
    }

    init {
        userLoginStatusSource.addListener(loginStatusListener)
    }

    override fun onCleared() {
        userLoginStatusSource.removeListener(loginStatusListener)
    }
}
