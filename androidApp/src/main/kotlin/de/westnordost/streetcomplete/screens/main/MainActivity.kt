package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.westnordost.streetcomplete.App
import de.westnordost.streetcomplete.AppViewModel
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.scope.KoinActivityScope
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

/** Android entry point for the shared application. */
class MainActivity : ComponentActivity(), AndroidScopeComponent {
    override val scope: Scope by activityScope()
    private val viewModel: AppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)
        setContent { KoinActivityScope { App(viewModel) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()?.let(viewModel::openUri)
            Intent.ACTION_MANAGE_NETWORK_USAGE -> viewModel.showSettings()
        }
    }
}
