#!/usr/bin/env python

import sys
import xml.etree.ElementTree as ET

class Way:
	def __init__(self):
		self.tags = []
		self.coords = []

class Relation:
	def __init__(self):
		self.tags = []
		self.outer = []
		self.inner = []

class Polygon:
	def __init__(self):
		self.tags = []
		self.shell = None
		self.holes = []
	
	def to_string(self):
		return feature_string("Polygon", self.tags, polygon_string(self.shell, self.holes))
		
class MultiPolygon:
	def __init__(self):
		self.tags = []
		self.polys = []
	
	def to_string(self):
		return feature_string("MultiPolygon", self.tags, multipolygon_string(self.polys))

def point_in_poly(x,y,poly):
	n = len(poly[:-1])
	inside = False

	p1x = poly[0][0]
	p1y = poly[0][1]
	for i in range(n+1):
		p2x = poly[i % n][0]
		p2y = poly[i % n][1]
		if y > min(p1y,p2y):
			if y <= max(p1y,p2y):
				if x <= max(p1x,p2x):
					if p1y != p2y:
						xints = (y-p1y)*(p2x-p1x)/(p2y-p1y)+p1x
					if p1x == p2x or x <= xints:
						inside = not inside
		p1x = p2x
		p1y = p2y

	return inside

def feature_string(geometryType, tags, coords_str):
	return """\
{{
	"type":"Feature",
	"properties":{{
{0}
	}},
	"geometry":{{
		"type":"{1}",
		"coordinates":{2}
	}}
}}""".format(tags_string(interesting_tags(tags)), geometryType, coords_str)

def tags_string(tags):
	return ',\n'.join(map(lambda tag: '		"'+tag[0]+'":"'+tag[1]+'"', tags))

def linear_ring_string(coords):
	return '[' + ','.join(map(lambda coord: '['+str(coord[0])+','+str(coord[1])+']', coords)) + ']'

def polygon_string(shell, holes):
	return '[' + ','.join(map(lambda linear_ring: linear_ring_string(linear_ring), [shell] + holes)) + ']'

def multipolygon_string(polygons):
	return '[\n			' + ',\n			'.join(map(lambda p: polygon_string(p.shell, p.holes), polygons)) + '\n		]'

def interesting_tags(tags):
	return filter(lambda tag: tag[0] == "ISO3166-1:alpha2" or tag[0] == "ISO3166-2" or tag[0] == "name:en", tags)
	
def has_interesting_tags(tags):
	if not tags:
		return False
	return next(interesting_tags(tags), None) != None

def read(source_file_name):
	nodes = {}
	ways = {}
	relations = {}
	way = None
	relation = None
	element = None
	parser = ET.XMLPullParser(['start'])
	source_file = open(source_file_name, "r", encoding='utf8')
	parser.feed(source_file.read())
	source_file.close()
	for event, elem in parser.read_events():
		if elem.tag == 'node':
			nodes[elem.get('id')] = [float(elem.get('lon')), float(elem.get('lat'))]
		elif elem.tag == 'way':
			element = way = ways[elem.get('id')] = Way()
		elif elem.tag == 'relation':
			element = relation = relations[elem.get('id')] = Relation()
		elif elem.tag == 'tag':
			element.tags.append([elem.get('k'),elem.get('v')])
		elif elem.tag == 'nd':
			way.coords.append(nodes[elem.get('ref')])
		elif elem.tag == 'member':
			if elem.get('type') == 'way':
				coords = ways[elem.get('ref')].coords
				if elem.get('role') == 'outer':
					relation.outer.append(coords)
				elif elem.get('role') == 'inner':
					relation.inner.append(coords)
	return ways.values(), relations.values()

def convert_ways(ways):
	polygons = []
	for way in ways:
		polygon = Polygon()
		polygon.tags = way.tags
		polygon.shell = way.coords
		polygons.append(polygon)
	return polygons

def convert_relations(relations):
	geometries = []
	for r in relations:
		# convert to simple Polygon (with optional hole)
		if len(r.outer) == 1:
			polygon = Polygon()
			polygon.tags = r.tags
			polygon.shell = r.outer[0]
			polygon.holes = r.inner
			geometries.append(polygon)
		else:
			multipoly = MultiPolygon()
			multipoly.tags = r.tags
			# find which holes (if any) belong to which polygons and convert to MultiPolygon
			for shell in r.outer:
				polygon = Polygon()
				polygon.shell = shell
				for hole in r.inner:
					if point_in_poly(hole[0][0], hole[0][1], shell):
						polygon.holes.append(hole)
				multipoly.polys.append(polygon)
			geometries.append(multipoly)
	return geometries

source_file_name = "../res/countryBoundaries.osm"
target_file_name = "src/main/assets/countryBoundaries.json"

ways, relations = read(source_file_name)
geometries = convert_ways(ways) + convert_relations(relations)

features = []
for geometry in geometries:
	if not has_interesting_tags(geometry.tags):
		continue
	features.append(geometry.to_string())
target_file = open(target_file_name, "w", encoding='utf8')
target_file.write("""\
{
"type":"FeatureCollection",
"features":[
""")
target_file.write(",\n".join(features))
target_file.write("""
]}
""")
target_file.close()
