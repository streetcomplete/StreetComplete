package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.screens.main.map.VectorTileProvider
import de.westnordost.streetcomplete.ui.util.composableContent
import org.koin.android.ext.android.inject

class PrivacyStatementFragment : Fragment() {

    private val vectorTileProvider: VectorTileProvider by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        composableContent { Surface {
            PrivacyStatementScreen(
                vectorTileProvider,
                onClickBack = { /* TODO */ }
            )
        } }
}
