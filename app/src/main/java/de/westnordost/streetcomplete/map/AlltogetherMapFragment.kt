package de.westnordost.streetcomplete.map

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R

class AlltogetherMapFragment : Fragment() {

    private var mapControls: MapControlsFragment? = null

    private var isMapInitialized = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        isMapInitialized = false
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().add(R.id.controls_fragment, MapControlsFragment()).commit()
        }
    }

    fun onMapControlsCreated(mapControls: MapControlsFragment?) {
        this.mapControls = mapControls
        tryInitializeMapControls()
    }

    private fun tryInitializeMapControls() {
        if (isMapInitialized && mapControls != null) {
            mapControls!!.onMapInitialized()
            onMapOrientation()
        }
    }

    fun showMapControls() { mapControls?.showControls() }
    fun hideMapControls() { mapControls?.hideControls() }



}
