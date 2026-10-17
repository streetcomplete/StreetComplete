package de.westnordost.streetcomplete.screens.user.login

import androidx.compose.runtime.Composable

/** Leads user through the OAuth 2 auth flow to login */
@Composable
expect fun LoginScreen(viewModel: LoginViewModel, onClickBack: () -> Unit)
