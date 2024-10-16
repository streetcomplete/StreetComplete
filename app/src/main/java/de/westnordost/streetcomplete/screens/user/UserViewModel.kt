package de.westnordost.streetcomplete.screens.user

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.user.UserLoginSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class UserViewModel : ViewModel() {
    abstract val isLoggedIn: StateFlow<Boolean>
}

class UserViewModelImpl(
    private val userLoginSource: UserLoginSource
) : UserViewModel() {
    override val isLoggedIn = MutableStateFlow(userLoginSource.isLoggedIn)

    private val loginStatusListener = object : UserLoginSource.Listener {
        override fun onLoggedIn() { isLoggedIn.value = true }
        override fun onLoggedOut() { isLoggedIn.value = false }
    }

    init {
        userLoginSource.addListener(loginStatusListener)
    }

    override fun onCleared() {
        userLoginSource.removeListener(loginStatusListener)
    }
}
