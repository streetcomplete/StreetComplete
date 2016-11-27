package de.westnordost.streetcomplete.util;


import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** Taken from http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java */
public class SlippyMapMath
{
	public static Rect enclosingTiles(BoundingBox bbox, int zoom)
	{
		double notTheNextTile = 0.0000001;
		Point min = enclosingTile(bbox.getMin(), zoom);
		Point max = enclosingTile(
				new OsmLatLon(
						bbox.getMaxLatitude() - notTheNextTile,
						bbox.getMaxLongitude() - notTheNextTile),
				zoom);
		return new Rect(min.x, max.y, max.x, min.y);
	}

	public static Point enclosingTile(LatLon pos, int zoom)
	{
		double radianLat = Math.toRadians(pos.getLatitude());

		int tiles = 1<<zoom;
		int xtile = (int) Math.floor( (pos.getLongitude() + 180) / 360 * tiles ) ;
		int ytile = (int) Math.floor( (1 - Math.log(Math.tan(radianLat) + 1 / Math.cos(radianLat)) / Math.PI) / 2 * tiles ) ;

		xtile = Math.max(0, Math.min(xtile , tiles - 1));
		ytile = Math.max(0, Math.min(ytile , tiles - 1));

		return new Point(xtile, ytile);
	}

	public static BoundingBox asBoundingBoxOfEnclosingTiles(BoundingBox bbox, int zoom)
	{
		return asBoundingBox(enclosingTiles(bbox, zoom), zoom);
	}

	public static BoundingBox asBoundingBox(Point tile, int zoom)
	{
		return new BoundingBox(
				tile2lat(tile.y + 1, zoom),
				tile2lon(tile.x, zoom),
				tile2lat(tile.y, zoom),
				tile2lon(tile.x + 1, zoom)
		);
	}

	public static BoundingBox asBoundingBox(Rect tiles, int zoom)
	{
		return new BoundingBox(
				tile2lat(tiles.bottom + 1, zoom),
				tile2lon(tiles.left, zoom),
				tile2lat(tiles.top, zoom),
				tile2lon(tiles.right + 1, zoom)
		);
	}

	public static List<Point> asTileList(Rect tiles)
	{
		int size = (1 + tiles.height()) * (1 + tiles.width());
		List<Point> tileList = new ArrayList<>(size);
		for(int y = tiles.top; y <= tiles.bottom; ++y)
			for(int x = tiles.left; x <= tiles.right; ++x)
				tileList.add(new Point(x,y));

		return tileList;
	}

	/** Minimum rect that encloses all the given tiles */
	public static Rect minRect(List<Point> tiles)
	{
		if(tiles.isEmpty()) return null;
		Integer bottom = null, top = null, left = null, right = null;
		for(Point p : tiles)
		{
			if(bottom == null || bottom < p.y) bottom = p.y;
			if(top == null || top > p.y) top = p.y;
			if(left == null || left > p.x) left = p.x;
			if(right == null || right < p.x) right = p.x;
		}
		return new Rect(left, top, right, bottom);
	}

	private static double tile2lon(int x, int z) {
		return x / Math.pow(2.0, z) * 360.0 - 180;
	}

	private static double tile2lat(int y, int z) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}
}