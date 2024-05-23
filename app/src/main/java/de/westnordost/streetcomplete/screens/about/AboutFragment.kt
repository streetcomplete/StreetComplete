package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ui.util.composableContent

class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        composableContent { Surface { AboutScreen(
            onClickChangelog = { /* TODO */ },
            onClickCredits = { /* TODO */ },
            onClickPrivacyStatement = { /* TODO */ },
            onClickLogs = { /* TODO */ },
            onClickBack = { /* TODO */ }
        ) } }
}
