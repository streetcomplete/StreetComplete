package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.westnordost.streetcomplete.App
import de.westnordost.streetcomplete.AppLocaleUpdater
import de.westnordost.streetcomplete.AppViewModel
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.scope.KoinActivityScope
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

/** Android entry point for the shared application. */
class MainActivity : ComponentActivity(), AndroidScopeComponent {
    override val scope: Scope by activityScope()
    private val viewModel: AppViewModel by viewModel()
    private val appLocaleUpdater: AppLocaleUpdater by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        appLocaleUpdater.update()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)
        setContent {
            KoinActivityScope {
                val uri by viewModel.pendingUri.collectAsState()
                App(
                    uri = uri,
                    onConsumedUri = viewModel::consumeUri,
                    viewModel = viewModel,
                )
            }
        }
    }

    override fun onResume() {
        // Android can reset the default locales without another Application configuration callback.
        appLocaleUpdater.update()
        super.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.toString()?.let(viewModel::openUri)
        }
    }
}
