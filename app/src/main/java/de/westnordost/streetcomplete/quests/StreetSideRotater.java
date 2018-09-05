package de.westnordost.streetcomplete.quests;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.view.View;

import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle;

public class StreetSideRotater
{
	private final StreetSideSelectPuzzle puzzle;
	private final View compassView;
	private final float wayOrientationAtCenter;

	private final Handler uiThread = new Handler(Looper.getMainLooper());

	public StreetSideRotater(StreetSideSelectPuzzle puzzle, View compassView, ElementGeometry geometry)
	{
		this.puzzle = puzzle;
		this.compassView = compassView;
		wayOrientationAtCenter = getWayOrientationAtCenterLineInDegrees(geometry);
	}

	@AnyThread public void onMapOrientation(final float rotation, final float tilt)
	{
		uiThread.post(() ->
		{
			puzzle.setStreetRotation(wayOrientationAtCenter + toDegrees(rotation));
			compassView.setRotation(toDegrees(rotation));
			compassView.setRotationX(toDegrees(tilt));
		});
	}

	private static float toDegrees(float radians)
	{
		return (float) (180 * radians / Math.PI);
	}

	private static float getWayOrientationAtCenterLineInDegrees(ElementGeometry e)
	{
		if(e.polylines == null) return 0;

		List<LatLon> points = e.polylines.get(0);
		if(points != null && points.size() > 1)
		{
			List<LatLon> centerLine = SphericalEarthMath.centerLineOfPolyline(points);
			if(centerLine != null)
			{
				return (float) SphericalEarthMath.bearing(centerLine.get(0), centerLine.get(1));
			}
		}
		return 0;
	}
}
