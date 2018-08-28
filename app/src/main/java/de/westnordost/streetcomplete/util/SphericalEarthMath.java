package de.westnordost.streetcomplete.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static java.lang.Math.*;

/** Calculate stuff assuming a spherical Earth. The Earth is not spherical, but it is a good
 *  approximation and totally sufficient for our use here. */
public class SphericalEarthMath
{
	/**
	 * In meters. See https://en.wikipedia.org/wiki/Earth_radius#Mean_radius
	 */
	public static final double EARTH_RADIUS = 6371000;

	/**
	 * Calculate a bounding box that contains the given circle. In other words, it is a square
	 * centered at the given position and with a side length of radius*2.
	 *
	 * @param center of the circle
	 * @param radius in meters
	 * @return The bounding box that contains the area
	 */
	public static BoundingBox enclosingBoundingBox(LatLon center, double radius)
	{
		double distance = sqrt(2) * radius;
		LatLon min = translate(center, distance, 225);
		LatLon max = translate(center, distance, 45);

		return new BoundingBox(min, max);
	}

	/**
	 * Calculate a bounding box that contains the given positions.
	 * @throws IllegalArgumentException if the multipositions list is empty or all lists contained therein are empty
	 */
	public static BoundingBox enclosingBoundingBox(Iterable<LatLon> positions)
	{
		Iterator<LatLon> it = positions.iterator();
		if(!it.hasNext()) throw new IllegalArgumentException("positions is empty");

		LatLon origin = it.next();
		double minLatOffset = 0, minLonOffset = 0, maxLatOffset = 0, maxLonOffset = 0;

		while(it.hasNext())
		{
			LatLon pos = it.next();

			// calculate with offsets here to properly handle 180th meridian
			double lat = normalizeLongitude(pos.getLatitude() - origin.getLatitude());
			double lon = pos.getLongitude() - origin.getLongitude();

			if (lat < minLatOffset) minLatOffset = lat;
			if (lon < minLonOffset) minLonOffset = lon;
			if (lat > maxLatOffset) maxLatOffset = lat;
			if (lon > maxLonOffset) maxLonOffset = lon;
		}
		return new BoundingBox(
			origin.getLatitude() + minLatOffset,
			origin.getLongitude() + minLonOffset,
			origin.getLatitude() + maxLatOffset,
			origin.getLongitude() + maxLonOffset);
	}

	/** @return a new position in the given distance and angle from the original position */
	public static LatLon translate(LatLon pos, double distance, double angle)
	{
		double φ1 = Math.toRadians(pos.getLatitude());
		double λ1 = Math.toRadians(pos.getLongitude());
		double α1 = Math.toRadians(angle);
		double σ12 = distance / EARTH_RADIUS;

		double y = sin(φ1) * cos(σ12) + cos(φ1) * sin(σ12) * cos(α1);

		double a = cos(φ1) * cos(σ12) - sin(φ1) * sin(σ12) * cos(α1);
		double b = sin(σ12) * sin(α1);
		double x = sqrt(sqr(a) + sqr(b));

		double φ2 = atan2(y, x);
		double λ2 = λ1 + atan2(b, a);

		return createTranslated(Math.toDegrees(φ2), Math.toDegrees(λ2));
	}

	/** area enclosed in the given bbox in m²*/
	public static double enclosedArea(BoundingBox bbox)
	{
		LatLon min = bbox.getMin();
		LatLon max = bbox.getMax();
		LatLon minLatMaxLon = new OsmLatLon(min.getLatitude(), max.getLongitude());
		LatLon maxLatMinLon = new OsmLatLon(max.getLatitude(), min.getLongitude());

		return distance(min, minLatMaxLon) * distance(min, maxLatMinLon);
	}

	/**
	 * @return distance between two points in meters
	 */
	public static double distance(LatLon pos1, LatLon pos2)
	{
		return EARTH_RADIUS * distance(
				Math.toRadians(pos1.getLatitude()),
				Math.toRadians(pos1.getLongitude()),
				Math.toRadians(pos2.getLatitude()),
				Math.toRadians(pos2.getLongitude()
				));
	}

	/**
	 * @return distance covered by the given polyline
	 */
	public static double distance(List<LatLon> positions)
	{
		double length = 0;
		for(int i = 0; i < positions.size() -1; i++)
		{
			LatLon p0 = positions.get(i);
			LatLon p1 = positions.get(i+1);
			length += distance(p0, p1);
		}
		return length;
	}

	/** @return whether any point on line1 is at most the given distance away from any other point
	 *          on line2. */
	public static boolean isWithinDistance(double distance, List<LatLon> line1, List<LatLon> line2)
	{
		for (LatLon linePoint1 : line1)
		{
			for (LatLon linePoint2 : line2)
			{
				if (SphericalEarthMath.distance(linePoint1, linePoint2) <= distance )
				{
					return true;
				}
			}
		}
		return false;
	}

	/** @return initial bearing from one point to the other.<br/>
	 *          If you take a globe and draw a line straight up to the north pole from pos1 and a
	 *          second one that connects pos1 and pos2, this is the angle between those two
	 *          lines */
	public static double bearing(LatLon pos1, LatLon pos2)
	{
		double bearing =  Math.toDegrees(bearing(
				Math.toRadians(pos1.getLatitude()),
				Math.toRadians(pos1.getLongitude()),
				Math.toRadians(pos2.getLatitude()),
				Math.toRadians(pos2.getLongitude())
		));

		if(bearing < 0) bearing += 360;
		if(bearing >= 360) bearing -= 360;
		return bearing;
	}

	/** @return final initial bearing from one point to the other.<br/>
	 *          If you take a globe and draw a line straight up to the north pole from <em>pos2</em>
	 *          and a second one that connects pos1 and pos2 (and goes on straight after this), this
	 *          is the angle between those two lines */
	public static double finalBearing(LatLon pos1, LatLon pos2)
	{
		double bearing =  Math.toDegrees(finalBearing(
				Math.toRadians(pos1.getLatitude()),
				Math.toRadians(pos1.getLongitude()),
				Math.toRadians(pos2.getLatitude()),
				Math.toRadians(pos2.getLongitude())
		));

		if(bearing < 0) bearing += 360;
		return bearing;
	}

	/**
	 * @return the line around the center point of the given polyline
	 * @throws IllegalArgumentException if positions list has less than two elements
	 */
	public static List<LatLon> centerLineOfPolyline(List<LatLon> positions)
	{
		if(positions.size() < 2) throw new IllegalArgumentException("positions list must contain at least 2 elements");

		double halfDistance = distance(positions) / 2;
		for(int i = 0; i < positions.size() -1; i++)
		{
			LatLon pos0 = positions.get(i);
			LatLon pos1 = positions.get(i+1);
			halfDistance -= distance(pos0, pos1);
			if(halfDistance > 0) continue;

			List<LatLon> result = new ArrayList<>(2);
			result.add(pos0);
			result.add(pos1);
			return result;
		}
		throw new RuntimeException();
	}

	/**
	 * @return the center point of the given polyline
	 * @throws IllegalArgumentException if positions list is empty
	 */
	public static LatLon centerPointOfPolyline(List<LatLon> positions)
	{
		if(positions.isEmpty()) throw new IllegalArgumentException("positions list is empty");

		double halfDistance = distance(positions) / 2;
		double distance = 0;
		for(int i = 0; i < positions.size() -1; i++)
		{
			LatLon start = positions.get(i);
			LatLon end = positions.get(i+1);
			double segmentDistance = distance(start, end);
			distance += segmentDistance;

			if(distance > halfDistance)
			{
				double ratio = (distance - halfDistance) / segmentDistance;
				double lat = end.getLatitude() - ratio * (end.getLatitude() - start.getLatitude());
				double lon = normalizeLongitude(end.getLongitude() - ratio * normalizeLongitude(end.getLongitude() - start.getLongitude()));
				return new OsmLatLon(lat, lon);
			}
		}
		return positions.get(0);
	}

	/**
	 * @return the center point of the given polygon
	 * @throws IllegalArgumentException if positions list is empty
	 */
	public static LatLon centerPointOfPolygon(List<LatLon> polygon)
	{
		if(polygon.isEmpty()) throw new IllegalArgumentException("positions list is empty");

		double lon = 0, lat = 0, area = 0;
		LatLon origin = polygon.get(0);
		int len = polygon.size();

		for(int i = 0, j = len-1; i<len; j = i, ++i)
		{
			LatLon pos0 = polygon.get(j);
			LatLon pos1 = polygon.get(i);

			// calculating with offsets to avoid rounding imprecision and 180th meridian problem
			double dx1 = normalizeLongitude(pos0.getLongitude() - origin.getLongitude());
			double dy1 = pos0.getLatitude() - origin.getLatitude();
			double dx2 = normalizeLongitude(pos1.getLongitude() - origin.getLongitude());
			double dy2 = pos1.getLatitude() - origin.getLatitude();

			double f = dx1 * dy2 - dx2 * dy1;
			lon += (dx1 + dx2) * f;
			lat += (dy1 + dy2) * f;
			area += f;
		}
		area *= 3;

		if(area == 0) return origin;

		return new OsmLatLon(
			lat / area + origin.getLatitude(),
			normalizeLongitude(lon / area + origin.getLongitude()));
	}

	/**
	 * @return whether the given position is within the given polygon. Whether the polygon is
	 *         defined clockwise or counterclockwise does not matter.
	 * */
	public static boolean isInPolygon(LatLon position, List<LatLon> polygon)
	{
		// this is the counting number algorithm, see http://geomalgorithms.com/a03-_inclusion.html
		boolean cn = false;
		for (int i = 0, j = polygon.size()-1; i < polygon.size(); j = i++)
		{
			LatLon pos0 = polygon.get(j);
			LatLon pos1 = polygon.get(i);

			if ( (pos1.getLatitude() > position.getLatitude()) != (pos0.getLatitude() > position.getLatitude()) )
			{
				double vt = (position.getLatitude() - pos1.getLatitude()) / (pos0.getLatitude() - pos1.getLatitude());
				double lon = normalizeLongitude(pos1.getLongitude() + vt * normalizeLongitude(pos0.getLongitude() - pos1.getLongitude()));
				if (normalizeLongitude(position.getLongitude() - lon) < 0)
					cn = !cn;
			}
		}
		return cn;
	}

	/**
	 * @return whether the given position is within the given multipolygon. Polygons defined
	 *         counterclockwise count as outer shells, polygons defined clockwise count as holes.
	 *
	 * It is assumed that shells do not overlap with other shells and holes do not overlap with other
	 * holes. (Though, of course a shell can be within a hole within a shell, that's okay)
	 * */
	public static boolean isInMultipolygon(LatLon position, List<List<LatLon>> multipolygon)
	{
		int containment = 0;
		for (List<LatLon> polygon : multipolygon)
		{
			if(isInPolygon(position, polygon))
			{
				if(isRingDefinedClockwise(polygon)) containment--;
				else containment++;
			}
		}
		return containment > 0;
	}

	/** @return whether the given ring is defined clockwise
	 *  @throws IllegalArgumentException if positions list is empty */
	public static boolean isRingDefinedClockwise(List<LatLon> ring)
	{
		if(ring.isEmpty()) throw new IllegalArgumentException("positions list is empty");

		double sum = 0;
		int len = ring.size();
		LatLon origin = ring.get(0);
		for(int i = 0, j = len-1; i<len; j = i, ++i)
		{
			LatLon pos0 = ring.get(j);
			LatLon pos1 = ring.get(i);

			// calculating with offsets to handle 180th meridian
			double lon0 = normalizeLongitude(pos0.getLongitude() - origin.getLongitude());
			double lat0 = pos0.getLatitude() - origin.getLatitude();
			double lon1 = normalizeLongitude(pos1.getLongitude() - origin.getLongitude());
			double lat1 = pos1.getLatitude() - origin.getLatitude();

			sum += lon0 * lat1 - lon1 * lat0;
		}
		return sum > 0;
	}

	// https://en.wikipedia.org/wiki/Great-circle_navigation#cite_note-2
	private static double distance(double φ1, double λ1, double φ2, double λ2)
	{
		double Δλ = λ2 - λ1;

		double y = sqrt(sqr(cos(φ2)*sin(Δλ)) + sqr(cos(φ1)*sin(φ2) - sin(φ1)*cos(φ2)*cos(Δλ)));
		double x = sin(φ1)*sin(φ2) + cos(φ1)*cos(φ2)*cos(Δλ);
		return atan2(y, x);
	}

	//See https://en.wikipedia.org/wiki/Great-circle_navigation#Course_and_distance
	private static double bearing(double φ1, double λ1, double φ2, double λ2)
	{
		double Δλ = λ2 - λ1;
		return Math.atan2(sin(Δλ), cos(φ1) * tan(φ2) - sin(φ1) * cos(Δλ));
	}

	private static double finalBearing(double φ1, double λ1, double φ2, double λ2)
	{
		double Δλ = λ2 - λ1;
		return Math.atan2(sin(Δλ), -cos(φ2)*tan(φ1) + sin(φ1)*cos(Δλ));
	}

	private static double sqr(double x) { return Math.pow(x, 2); }

	private static LatLon createTranslated(double lat, double lon)
	{
		lon = normalizeLongitude(lon);

		boolean crossedPole = false;
		// north pole
		if(lat > 90)
		{
			lat = 180-lat;
			crossedPole = true;
		}
		// south pole
		else if(lat < -90)
		{
			lat = -180-lat;
			crossedPole = true;
		}

		if(crossedPole)
		{
			lon += 180;
			if(lon > 180) lon -= 360;
		}

		return new OsmLatLon(lat, lon);
	}

	private static double normalizeLongitude(double lon) {
		while(lon > 180) lon -= 360;
		while(lon < -180) lon += 360;
		return lon;
	}
}
